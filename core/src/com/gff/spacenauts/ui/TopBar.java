package com.gff.spacenauts.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.screens.GameScreen;

/**
 * An UI element that shows the two health bars, the current score and the pause button.
 * 
 * @author Alessio Cali'
 *
 */
public class TopBar extends Stack {

	private Label scoreLabel;
	private Button pauseButton;
	private ProgressBar healthBar;
	private ProgressBar bossBar;
	private Table topBarUI;

	public TopBar(AssetManager assets){
		super();

		BitmapFont k32 = assets.get(AssetsPaths.FONT_KARMATIC_32);
		Label.LabelStyle k14_white = new Label.LabelStyle(k32, Color.WHITE);
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI);
		
		ProgressBar.ProgressBarStyle pStyle = new ProgressBar.ProgressBarStyle();
		pStyle.knobBefore = new NinePatchDrawable(uiAtlas.createPatch("health_player_before"));
		pStyle.knob = new TextureRegionDrawable(uiAtlas.findRegion("health_bar"));
		pStyle.knobAfter = new NinePatchDrawable(uiAtlas.createPatch("health_after"));

		ProgressBar.ProgressBarStyle bStyle = new ProgressBar.ProgressBarStyle(pStyle);
		bStyle.knobBefore = new NinePatchDrawable(uiAtlas.createPatch("health_boss_before"));
		
		Image background = new Image(new TextureRegionDrawable(uiAtlas.findRegion("black")));
		background.setFillParent(true);
		add(background);
		
		topBarUI = new Table();
		
		//Player's health bar
		healthBar = new ProgressBar(0, 100, 1, false, pStyle);
		topBarUI.add(healthBar).expandX().center().width(300);
		
		//Label showing the current score
		scoreLabel = new Label("Score  0", k14_white);
		topBarUI.add(scoreLabel).center().top().pad(5);
		
		//Boss's health bar
		bossBar = new ProgressBar(0, 100, 1, false, bStyle);
		bossBar.setVisible(false);
		topBarUI.add(bossBar).expandX().center().width(300);
		
		//Pause button
		pauseButton = new Button(new TextureRegionDrawable(uiAtlas.findRegion("pause_button")),
				new TextureRegionDrawable(uiAtlas.findRegion("resume_button")),
				new TextureRegionDrawable(uiAtlas.findRegion("resume_button")));
		pauseButton.addAction(new Action() {
			@Override
			public boolean act(float delta) {
				Button button = (Button)getActor();

				if (!GameScreen.getEngine().isRunning())
					button.setChecked(true);

				else 
					button.setChecked(false);

				return false;
			}
		});
		pauseButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {				
				GameScreen.getEngine().togglePause();
			}
		});	
		topBarUI.add(pauseButton).center().fill().pad(5).size(64f, 64f);
		add(topBarUI);
	}

	public void setScore(int score){
		scoreLabel.setText("Score " + score);
	}

	public void setPlayerHealth(float health){
		healthBar.setValue(health);
	}

	public void setBossHealth(float health){
		if (!bossBar.isVisible())
			bossBar.setVisible(true);

		bossBar.setValue(health);
	}
}
