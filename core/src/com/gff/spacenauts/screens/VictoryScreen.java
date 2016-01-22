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
	private BitmapFont k64;
	
	public VictoryScreen(final String nextLevel, final Game game){
		this.nextLevel = nextLevel;
		this.game = game;
	}

	@Override
	public void show () {
		k64 = new BitmapFont(Gdx.files.internal(AssetsPaths.FONT_KARMATIC_64));
		Label.LabelStyle lStyle = new Label.LabelStyle(k64, Color.WHITE);
		viewport = new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT);
		ui = new Stage(viewport);
		
		Table root = new Table();
		root.setFillParent(true);
		
		Label winLabel = new Label("YOU WIN!", lStyle);
		root.add(winLabel).center();
		root.row();
		
		Label nextLabel = new Label("GO NEXT", lStyle);
		nextLabel.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				game.setScreen(new GameScreen(nextLevel, game));
			}
		});
		root.add(nextLabel).center();
		
		ui.addActor(root);
		
		Gdx.input.setInputProcessor(ui);
	}
	
	@Override
	public void hide () {
		Gdx.input.setInputProcessor(null);
		dispose();
	}
	
	@Override
	public void dispose () {
		if (k64 != null) k64.dispose();
		if (ui != null) ui.dispose();
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

}
