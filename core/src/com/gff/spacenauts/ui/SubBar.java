package com.gff.spacenauts.ui;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.ai.PowerUpAI;

/**
 * An UI element that hosts the current PowerUp and the boss's name.
 * 
 * @author Alessio Cali'
 *
 */
public class SubBar extends Table {

	private Stack powerUpStack;
	private Image powerUpBackground;
	private Image powerUpImage;
	private Label powerUpField;
	private Label bossField;
	
	private HashMap<String, TextureRegionDrawable> powerUpMap;
	
	public SubBar(AssetManager assets) {
		TextureAtlas textures = assets.get(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		BitmapFont a28 = assets.get(AssetsPaths.FONT_ATARI_28, BitmapFont.class);
		cachePowerUpTextures(textures);
		powerUpBackground = new Image(uiAtlas.findRegion("black"));
		powerUpBackground.setFillParent(true);
		powerUpImage = new Image();
		powerUpImage.setFillParent(true);
		powerUpStack = new Stack();
		powerUpStack.add(powerUpBackground);
		powerUpStack.add(powerUpImage);
		powerUpStack.setVisible(false);
		
		Label.LabelStyle fieldStyle = new Label.LabelStyle(a28, Color.WHITE);
		fieldStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		
		powerUpField = new Label("", fieldStyle);
		powerUpField.setVisible(false);
		powerUpField.setTouchable(Touchable.disabled);
		bossField = new Label("", fieldStyle);
		bossField.setVisible(false);
		bossField.setTouchable(Touchable.disabled);
		
		add(powerUpStack).size(64, 64).expandY().fill();
		add(powerUpField).expandY().fill();
		add().expand();
		add(bossField).expandY().fill();
	}
	
	private void cachePowerUpTextures(TextureAtlas textures) {
		powerUpMap = new HashMap<String, TextureRegionDrawable>();
		powerUpMap.put("TRIGUN", new TextureRegionDrawable(textures.findRegion("trigun")));
		powerUpMap.put("AUTOGUN", new TextureRegionDrawable(textures.findRegion("autogun")));
		powerUpMap.put("HEAVYGUN", new TextureRegionDrawable(textures.findRegion("heavygun")));
	}
	
	public void setPowerUp(PowerUpAI.PowerUpState powerUp){
		if (powerUp == null) {
			powerUpStack.setVisible(false);
			powerUpField.setVisible(false);
		} else {
			powerUpStack.setVisible(true);
			powerUpField.setVisible(true);
			powerUpImage.setDrawable(powerUpMap.get(powerUp.getId()));
			powerUpField.setText(powerUp.getId());
		}
	}
	
	public void setBossName(String bossName){
		bossField.setVisible(true);
		bossField.setText(bossName);
	}
}
