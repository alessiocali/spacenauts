package com.gff.spacenauts.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;

/**
 * A screen triggered after a Game Over. Allows the player to go back to the main menu or replay the current level.
 * 
 * @author Alessio Cali'
 *
 */
public class GameOverScreen extends ScreenAdapter {
	
	private Stage ui;
	private Viewport viewport;
	private String currentLevel;
	private Game game;
	public GameOverScreen(String currentLevel, Game game){
		this.currentLevel = currentLevel;
		this.game = game;
	}
	
	@Override
	public void show () {
		BitmapFont k64 = new BitmapFont(Gdx.files.internal(AssetsPaths.FONT_KARMATIC_64));
		Label.LabelStyle lStyle = new Label.LabelStyle(k64, Color.WHITE);
		viewport = new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT);
		ui = new Stage(viewport);
		
		Table root = new Table();
		root.setFillParent(true);
		
		Label gameOverLabel = new Label("GAME OVER", lStyle);
		root.add(gameOverLabel).center();	
		root.row();
		
		//Add RETRY only if not in multiplayer mode (currentLevel == null)
		if (currentLevel != null) {
			Label retryLabel = new Label("RETRY", lStyle);
			retryLabel.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					GameScreen gameScreen = new GameScreen(currentLevel, game);
					game.setScreen(new LoadingScreen(gameScreen, game, gameScreen));
				}
			});
			root.add(retryLabel).center();
			root.row();
		}
		
		Label backToMenuLabel = new Label("BACK TO MENU", lStyle);
		backToMenuLabel.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				InitialScreen initial = new InitialScreen(game);
				game.setScreen(new LoadingScreen(initial, game, initial));
			}
		});
		root.add(backToMenuLabel);
		
		ui.addActor(root);
		
		Gdx.input.setInputProcessor(ui);
	}
	
	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}
	
	@Override
	public void hide () {
		Gdx.input.setInputProcessor(null);
		dispose();
	}
	
	@Override
	public void dispose () {
		if (ui != null) ui.dispose();
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

}
