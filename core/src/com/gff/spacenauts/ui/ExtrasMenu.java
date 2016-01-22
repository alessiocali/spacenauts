package com.gff.spacenauts.ui;

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

public class ExtrasMenu implements UISet {

	private Table mainTable;
	private Image logo;
	private ImageButton backButton;
	private ImageButton creditsButton;
	private Credits credits;
	
	public ExtrasMenu(AssetManager assets, final InitialScreen initial, final UISet from) {
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		
		mainTable = new Table();
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("extras_logo")));
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(from);
			}
		});
		
		credits = new Credits(assets, initial, this);
		creditsButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("credits")));
		creditsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(credits);
			}
		});
		mainTable.add(creditsButton).center().fill();
	}
	
	@Override
	public Image logo() {
		return logo;
	}

	@Override
	public ImageButton lowerLeft() {
		return backButton;
	}

	@Override
	public ImageButton lowerRight() {
		return null;
	}

	@Override
	public Table main() {
		return mainTable;
	}

}
