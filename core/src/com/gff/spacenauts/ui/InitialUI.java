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

/**
 * The UISet shown as the first menu. It has buttons to start a New Game,
 * select a Level, check the Extras or modify the Options.
 * 
 * @author Alessio
 *
 */
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
		levelSelect = new LevelSelecter(gameRef, assets, initial, this);
		options = new OptionsMenu(assets, initial, this);
		extras = new ExtrasMenu(assets, initial, this);
		
		newGameButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("newgame")));
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(newGame);
			}
		});
		mainTable.add(newGameButton).center().row();
		
		levelSelectButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("selectlevel")));
		levelSelectButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent e, float x, float y) {
				levelSelect.refresh();
				initial.setUI(levelSelect);
			}
		});
		mainTable.add(levelSelectButton).center().row();
		
		optionsButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("options")));
		optionsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				options.synch();
				initial.setUI(options);
			}
		});
		mainTable.add(optionsButton).center().row();
		
		extrasButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("extras")));
		extrasButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(extras);
			}
		});
		mainTable.add(extrasButton).center().row();
		
	}
	
	@Override
	public Image logo() {
		return logo;
	}

	@Override
	public ImageButton lowerLeft() {
		return gPlayButton;	//Maybe hide?
	}

	@Override
	public ImageButton lowerRight() {
		return gffButton; //Maybe hide?
	}

	@Override
	public Table main() {
		return mainTable;
	}

}
