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
		
		//Styles
		Label.LabelStyle fieldStyle = new Label.LabelStyle(a28, Color.WHITE);
		fieldStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		
		//These three fields make up the PowerUp Icon.
		//The black background
		powerUpBackground = new Image(uiAtlas.findRegion("black"));
		powerUpBackground.setFillParent(true);
		
		//The powerup icon
		powerUpImage = new Image();
		powerUpImage.setFillParent(true);
		
		//The stack containing these two
		powerUpStack = new Stack();
		powerUpStack.add(powerUpBackground);
		powerUpStack.add(powerUpImage);
		powerUpStack.setVisible(false);
		add(powerUpStack).size(80, 80).expandY().fill();

		//The powerup label
		powerUpField = new Label("", fieldStyle);
		powerUpField.setVisible(false);
		powerUpField.setTouchable(Touchable.disabled);
		add(powerUpField).expandY().fill();
		add().expand();
		
		//The boss label
		bossField = new Label("", fieldStyle);
		bossField.setVisible(false);
		bossField.setTouchable(Touchable.disabled);
		add(bossField).expandY().fill();
	}
	
	/**
	 * Pairs every Powerup ID with its icon.
	 * 
	 * @param textures
	 */
	private void cachePowerUpTextures(TextureAtlas textures) {
		powerUpMap = new HashMap<String, TextureRegionDrawable>();
		powerUpMap.put("TRIGUN", new TextureRegionDrawable(textures.findRegion("TRIGUN")));
		powerUpMap.put("AUTOGUN", new TextureRegionDrawable(textures.findRegion("AUTOGUN")));
		powerUpMap.put("HEAVYGUN", new TextureRegionDrawable(textures.findRegion("HEAVYGUN")));
		powerUpMap.put("SHIELD", new TextureRegionDrawable(textures.findRegion("SHIELD")));
		powerUpMap.put("OCHITA_RED", new TextureRegionDrawable(textures.findRegion("OCHITA_RED")));
		powerUpMap.put("OCHITA_GREEN", new TextureRegionDrawable(textures.findRegion("OCHITA_GREEN")));
	}
	
	/**
	 * Set the current power up with the one provided.
	 * If null it will remove the powerup fields from
	 * the sub bar.
	 * 
	 * @param powerUp
	 */
	public void setPowerUp(PowerUpAI.PowerUpState powerUp){
		if (powerUp == null) {
			powerUpStack.setVisible(false);
			powerUpField.setVisible(false);
		} else {
			powerUpStack.setVisible(true);
			powerUpField.setVisible(true);
			powerUpImage.setDrawable(powerUpMap.get(powerUp.getId()));
			powerUpField.setText(powerUp.getName());
		}
	}
	
	/**
	 * Shows the provided boss name.
	 * 
	 * @param bossName
	 */
	public void setBossName(String bossName){
		bossField.setVisible(true);
		bossField.setText(bossName);
	}
}
