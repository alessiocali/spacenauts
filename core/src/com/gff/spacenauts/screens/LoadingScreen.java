package com.gff.spacenauts.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;

/**
 * A screen that loads assets for another Screen. It will switch to the loaded screen once it's finished.
 * The list of {@link AssetManager#load(String, Class) AssetManager.load()} commands must be executed within
 * an instance of the {@link Loadable} interface. The loaded assets can be retrieved using the {@link #getAssets()} method. 
 * 
 * @author Alessio
 *
 */
public class LoadingScreen extends Stage implements Screen {
	
	/**
	 * Defines a class that needs its assets loaded.
	 * 
	 * @author Alessio
	 *
	 */
	public interface Loadable {
		
		/**
		 * Queues all needed assets in the loading list.
		 * 
		 * @param assets
		 */
		public void preload (AssetManager assets);
		
		/**
		 * Hands over the loaded assets to this Loadable instance.
		 * 
		 * @param assets
		 */
		public void handAssets (AssetManager assets);
		
	}

	private AssetManager assets;
	private Screen next;
	private Game game;
	private Loadable loader;

	private BitmapFont font;
	private Table root;
	private Label nowLoading;
	private Label dots;

	public LoadingScreen (AssetManager assets, Screen next, Game game, Loadable loader) {
		super(new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT));
		this.assets = assets;
		this.next = next;
		this.game = game;
		this.loader = loader;
		
		root = new Table();
		root.setFillParent(true);
		root.right().bottom();
		addActor(root);
		
		font = new BitmapFont(Gdx.files.internal(AssetsPaths.FONT_KARMATIC_64));
		Label.LabelStyle lStyle = new Label.LabelStyle(font, Color.WHITE);
		
		nowLoading = new Label("NOW LOADING", lStyle);
		dots = new Label(".", lStyle);

		root.add(nowLoading).expandX().right().padBottom(5);
		root.add(dots).left().padBottom(5);
		
		loader.preload(assets);
	}
	
	public LoadingScreen(Screen caller, Game game, Loadable loader) {
		this(new AssetManager(), caller, game, loader);
	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {

		if (assets.update()) {
			loader.handAssets(assets);
			game.setScreen(next);
		} 

		switch((int)(System.currentTimeMillis() / 1000) % 3) {
		case 0:
			dots.setText(".");
			break;
		case 1:
			dots.setText("..");
			break;
		case 2:
			dots.setText("...");
			break;
		}
		
		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		act(delta);
		draw();

	}
	
	public AssetManager getAssets() {
		return assets;
	}

	@Override
	public void resize(int width, int height) {
		getViewport().update(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {
		
	}

}
