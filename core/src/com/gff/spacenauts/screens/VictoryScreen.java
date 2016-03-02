package com.gff.spacenauts.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;

/**
 * The victory screen that is shown when the player completes a level.
 * When this screen is shown the next level is unlocked, based on the 
 * value provided with {@link #setUnlock(int)}.
 * 
 * @author Alessio Cali'
 *
 */
public class VictoryScreen extends ScreenAdapter {
	
	private Stage ui;
	private Screen nextScreen;
	private Game game;
	private TextureAtlas uiAtlas;
	private BitmapFont k64;
	private Music bgm;
	private Texture bg;
	private int score;
	private int unlock;
	
	public VictoryScreen(final Screen nextScreen, final Game game){
		this.nextScreen = nextScreen;
		this.game = game;
		unlock = 0;
	}

	@Override
	public void show () {
		//Load assets
		k64 = new BitmapFont(Gdx.files.internal(AssetsPaths.FONT_KARMATIC_64));
		bgm = Gdx.audio.newMusic(Gdx.files.internal(AssetsPaths.BGM_VICTORY));
		bg = new Texture(Gdx.files.internal(AssetsPaths.TEXTURE_BACKGROUND));
		uiAtlas = new TextureAtlas(Gdx.files.internal(AssetsPaths.ATLAS_UI));
		
		//Init styles
		Label.LabelStyle lStyle = new Label.LabelStyle(k64, Color.WHITE);
		lStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		
		ui = new Stage(new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT));
	
		//Root stack
		Stack stack = new Stack();
		stack.setFillParent(true);
		ui.addActor(stack);
		
		//Background image
		Image bgImage = new Image(bg);
		stack.add(bgImage);
		
		//UI table
		Table uiTable = new Table();
		uiTable.setFillParent(true);
		uiTable.top();
		stack.add(uiTable);
		
		//Labels
		Label winLabel = new Label("YOU WIN!", lStyle);
		uiTable.add(winLabel).center().top().padTop(400).padBottom(300);
		uiTable.row();
		
		Label scoreLabel = new Label("Your Score\n\n" + score, lStyle);
		uiTable.add(scoreLabel).padBottom(300).row();
		
		Label nextLabel = new Label("GO NEXT", lStyle);
		nextLabel.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				game.setScreen(nextScreen);
			}
		});
		uiTable.add(nextLabel).center();		
		
		//Final commands
		Gdx.input.setInputProcessor(ui);
		unlockLevel(unlock);
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
		ui.getViewport().update(width, height);
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
	
	/**
	 * Sets the LevelSet number of the next level to unlock. Once this screen is shown
	 * the next level gets unlocked.
	 * @param unlock
	 */
	public void setUnlock (int unlock) {
		this.unlock = unlock;
	}
	
	/**
	 * Compares unlock with the current value of {@link Globals#levelUnlocked}
	 * and if it's any higher updates it. Also updates the value inside the 
	 * preferences file.
	 * 
	 * @param unlock
	 */
	private void unlockLevel(int unlock) {
		if (unlock > Globals.levelUnlocked) Globals.levelUnlocked = unlock;
		Gdx.app.getPreferences(Globals.PREF_FILE).putInteger("levelUnlocked", Globals.levelUnlocked);
	}
}
