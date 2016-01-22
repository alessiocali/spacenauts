package com.gff.spacenauts.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.net.NetworkAdapter;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.screens.InitialScreen;

public class NewGameMenu implements UISet {

	private Table mainTable;
	private Image logo;
	private ImageButton backButton;
	private Dialog unsupportedDialog;
	private ImageButton singlePlayer;
	private ImageButton multiPlayer;
	private MultiplayerMenu multiplayerMenu;
	
	public NewGameMenu (AssetManager assets, final Game GAME_REF, final InitialScreen initial, final UISet from) {
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		mainTable = new Table();
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("newgame_logo")));
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				initial.setUI(from);
			}
		});
		
		Window.WindowStyle dialogStyle = new Window.WindowStyle();
		dialogStyle.background =  new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		dialogStyle.titleFont = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		dialogStyle.titleFontColor = Color.WHITE;
		unsupportedDialog = new Dialog("", dialogStyle);
		unsupportedDialog.button("Ok", null, new TextButton.TextButtonStyle(null, null, null, assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class)));
		TextArea dialogText = new TextArea("Multiplayer feature is disabled for this platform.", 
											new TextField.TextFieldStyle(assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class), Color.WHITE, null, null, null));
		dialogText.setAlignment(Align.center);
		dialogText.setTouchable(Touchable.disabled);
		unsupportedDialog.getContentTable()
							.add(dialogText)
							.center().size(600, 150);
		singlePlayer = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("singleplayer")));
		singlePlayer.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				GAME_REF.setScreen(new GameScreen("maps/tutorial.tmx", GAME_REF));
			}
		});
		multiPlayer = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("multiplayer")));
		multiPlayer.addListener(new ClickListener () {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				NetworkAdapter na = Spacenauts.getNetworkAdapter();
				if (na == null) unsupportedDialog.show(e.getStage());
				else {
					multiplayerMenu.reset();
					initial.setUI(multiplayerMenu);
				}
			}
		});
		if (Spacenauts.getNetworkAdapter() != null) {
			multiplayerMenu = new MultiplayerMenu(assets, GAME_REF, initial, this);
		}
		mainTable.add(singlePlayer).center().fill().row();
		mainTable.add(multiPlayer).center().fill();
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
