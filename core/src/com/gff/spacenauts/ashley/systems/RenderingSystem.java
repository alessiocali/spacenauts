package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.listeners.AnimationListener;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.ui.GameUI;

/**
 * Renders sprites, the map, UI elements and bounding shapes (when in debug mode). Also updates {@link Sprite}s who are tied to an {@link Animation}.
 * 
 * @author Alessio Cali'
 *
 */
public class RenderingSystem extends IteratingSystem {

	private ShapeRenderer renderer;
	private SpriteBatch spriteBatch;
	private OrthoCachedTiledMapRenderer mapRenderer;
	private TiledMap map;
	private ShaderProgram immunityShader;
	private GameUI ui;

	public RenderingSystem(GameScreen game){
		super(Families.RENDERING_FAMILY);
		ui = game.getUI();
		renderer = new ShapeRenderer();
		renderer.setColor(0, 255, 0, 1);
		spriteBatch = new SpriteBatch();
		map = game.getMap();
		mapRenderer = new OrthoCachedTiledMapRenderer(map, Globals.UNITS_PER_PIXEL);
		mapRenderer.setBlending(true);
		setupImmunityShader();
	}

	@Override
	public void update(float delta){
		Entity cameraEntity = GameScreen.getEngine().getCamera();

		if (cameraEntity != null){

			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Viewport camera =  Mappers.wcm.get(cameraEntity).viewport;

			camera.apply();
			renderer.setProjectionMatrix(camera.getCamera().combined);
			spriteBatch.setProjectionMatrix(camera.getCamera().combined);
			mapRenderer.setView((OrthographicCamera)camera.getCamera());
			mapRenderer.render();
			renderer.begin(ShapeType.Line);
			spriteBatch.begin();
			super.update(delta);
			spriteBatch.end();
			renderer.end();
			ui.render(delta);
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (Families.SPRITE_FAMILY.matches(entity)){
			Render render = Mappers.rm.get(entity);
			Vector2 pos = Mappers.pm.get(entity).value;

			boolean dirty = false;
			if (Families.IMMUNE_FAMILY.matches(entity)) {
				render.sprite.setColor(1, 1, 1, 0.75f);
				spriteBatch.setShader(immunityShader);
				dirty = true;
			}

			if (render.animation != null && render.sprite != null){
				if (GameScreen.getEngine().isRunning()) {
					if (render.animationTimer == 0)
						for (AnimationListener listener : render.listeners) listener.onStart(entity, render.animation);						

					render.animationTimer += deltaTime;
				}

				TextureRegion region = render.animation.getKeyFrame(render.animationTimer);
				render.sprite.setTexture(region.getTexture());
				render.sprite.setRegion(region);
				render.sprite.setSize(region.getRegionWidth(), region.getRegionHeight());
				if (region instanceof Sprite) {
					render.sprite.setAlpha(((Sprite)region).getColor().a);
					dirty = true;
				}

				if (render.animation.isAnimationFinished(render.animationTimer) && (render.animation.getPlayMode() == PlayMode.NORMAL || render.animation.getPlayMode() == PlayMode.REVERSED)) {
					for (AnimationListener listener : render.listeners) listener.onEnd(entity, render.animation);
					render.animationTimer = 0;
					render.animation = null;
				}

			} if (render.sprite != null) {

				Sprite sprite = render.sprite;
				float ang = Mappers.am.get(entity).getAngleDegrees();

				sprite.setCenter(pos.x, pos.y);
				sprite.setOriginCenter();
				sprite.setRotation(ang);
				sprite.setScale(render.scaleX, render.scaleY);
				sprite.draw(spriteBatch);
				if (dirty) {
					//Removes the immunity shader if it has been used. Also resets the sprite's opacity.
					spriteBatch.setShader(null);
					render.sprite.setColor(Color.WHITE);
				}
			}
		}

		//Debug lines.
		if (Families.BODY_FAMILY.matches(entity) && Globals.debug){
			Polygon body = Mappers.bm.get(entity).polygon;

			renderer.setColor(Color.GREEN);
			float[] vertices = body.getTransformedVertices();
			int length = vertices.length;				

			for (int i = 0 ; i < length ; i+=2){
				if (i+3 < length)
					renderer.line(vertices[i], vertices[i+1], vertices[i+2], vertices[i+3]);
				else
					renderer.line(vertices[length-2], vertices[length-1], vertices[0], vertices[1]);
			}
			
			renderer.setColor(Color.RED);
			
			Rectangle r = body.getBoundingRectangle();
			renderer.rect(r.x, r.y, r.width, r.height);
		}
	}

	/**
	 * Prepares the shader used for drawing immune enemies. Kudos to 
	 * <a href="http://stackoverflow.com/questions/24099103/libgdx-changing-sprite-color-while-hurt">this guy</a> because I don't know
	 * anything about shader programming. 
	 */
	private void setupImmunityShader() {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "uniform mat4 u_projTrans;\n" //
				+ "varying vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" //
				+ "varying LOWP vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "uniform sampler2D u_texture;\n" //
				+ "void main()\n"//
				+ "{\n" //
				+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords).a;\n" //
				+ "}";

		immunityShader = new ShaderProgram(vertexShader, fragmentShader);
	}

}
