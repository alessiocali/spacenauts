package com.gff.spacenauts.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.screens.InitialScreen;
import com.gff.spacenauts.ui.LevelSelecter.LevelSelectSet;

public class InitialUI implements UISet {

	private Table mainTable;
	private Image logo;
	private ImageButton gPlayButton;
	private ImageButton gffButton;
	private NewGameMenu newGame;
	private LevelSelecter levelSelect;
	private OptionsMenu options;
	private ExtrasMenu extras;
	private ImageButton newGameButton;
	private ImageButton levelSelectButton;
	private ImageButton optionsButton;
	private ImageButton extrasButton;
	
	public InitialUI (AssetManager assets, final Game gameRef, final InitialScreen initial) {
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		
		mainTable = new Table();
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("logo")));
		gPlayButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("google_play_logo")));
		gffButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("g4f")));
		
		newGame = new NewGameMenu(assets, gameRef, initial, this);
		levelSelect = new LevelSelecter(gameRef, assets, LevelSelectSet.TUTORIAL, initial, this);
		options = new OptionsMenu(assets, this, initial);
		extras = new ExtrasMenu(assets, initial, this);
		
		newGameButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("newgame")));
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(newGame);
			}
		});
		levelSelectButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("selectlevel")));
		levelSelectButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(levelSelect);
			}
		});
		optionsButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("options")));
		optionsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				options.synch();
				initial.setUI(options);
			}
		});
		extrasButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("extras")));
		extrasButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(extras);
			}
		});
		
		mainTable.add(newGameButton).center().row();
		mainTable.add(levelSelectButton).center().row();
		mainTable.add(optionsButton).center().row();
		mainTable.add(extrasButton).center().row();
	}
	
	@Override
	public Image logo() {
		return logo;
	}

	@Override
	public ImageButton lowerLeft() {
		return gPlayButton;
	}

	@Override
	public ImageButton lowerRight() {
		return gffButton;
	}

	@Override
	public Table main() {
		return mainTable;
	}

}
