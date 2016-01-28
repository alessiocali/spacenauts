package com.gff.spacenauts.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;

/**
 * The victory screen that is shown when the player completes a level.
 * 
 * @author Alessio Cali'
 *
 */
public class VictoryScreen extends ScreenAdapter {
	
	private Stage ui;
	private Viewport viewport;
	private String nextLevel;
	private Game game;
	private TextureAtlas uiAtlas;
	private BitmapFont k64;
	private Music bgm;
	private Texture bg;
	private int score;
	
	public VictoryScreen(final String nextLevel, final Game game){
		this.nextLevel = nextLevel;
		this.game = game;
	}

	@Override
	public void show () {
		k64 = new BitmapFont(Gdx.files.internal(AssetsPaths.FONT_KARMATIC_64));
		bgm = Gdx.audio.newMusic(Gdx.files.internal(AssetsPaths.BGM_VICTORY));
		bg = new Texture(Gdx.files.internal(AssetsPaths.TEXTURE_BACKGROUND));
		Image bgImage = new Image(bg);
		uiAtlas = new TextureAtlas(Gdx.files.internal(AssetsPaths.ATLAS_UI));
		Label.LabelStyle lStyle = new Label.LabelStyle(k64, Color.WHITE);
		lStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		viewport = new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT);
		ui = new Stage(viewport);
		
		Stack stack = new Stack();
		stack.setFillParent(true);
		stack.add(bgImage);
		
		Table root = new Table();
		root.setFillParent(true);
		root.top();
		stack.add(root);
		
		Label winLabel = new Label("YOU WIN!", lStyle);
		root.add(winLabel).center().top().padTop(400).padBottom(300);
		root.row();
		
		Label scoreLabel = new Label("Your Score\n\n" + score, lStyle);
		root.add(scoreLabel).padBottom(300).row();
		
		Label nextLabel = new Label("GO NEXT", lStyle);
		nextLabel.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				GameScreen gameScreen = new GameScreen(nextLevel, game);
				game.setScreen(new LoadingScreen(gameScreen, game, gameScreen));
			}
		});
		root.add(nextLabel).center();
		
		ui.addActor(stack);
		
		Gdx.input.setInputProcessor(ui);
		bgm.play();
	}
	
	@Override
	public void hide () {
		Gdx.input.setInputProcessor(null);
		bgm.stop();
		dispose();
	}
	
	@Override
	public void dispose () {
		if (k64 != null) k64.dispose();
		if (ui != null) ui.dispose();
		if (bgm != null) bgm.dispose();
		if (uiAtlas != null) uiAtlas.dispose();
		if (bg != null) bg.dispose();
	}
	
	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}
	
	@Override
	public void render(float delta){
		Gdx.gl.glClearColor(0,0,0,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();
		
		if (Globals.debug)
			ui.setDebugAll(true);
	}

	public void setScore (int score) {
		this.score = score;
	}
}
