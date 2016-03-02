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
import com.gff.spacenauts.screens.LoadingScreen;
import com.gff.spacenauts.screens.NarrativeScreen;

/**
 * A simple UI set that allows one to choose between a single player
 * session or a multiplayer one. Choosing Single Player goes to a new
 * game starting from the Tutorial, while choosing Multiplayer
 * goes to the {@link MultiplayerMenu}.
 * 
 * @author Alessio
 *
 */
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
		
		if (Spacenauts.getNetworkAdapter() != null)
			multiplayerMenu = new MultiplayerMenu(assets, GAME_REF, initial, this);
		
		//Styles
		Window.WindowStyle dialogStyle = new Window.WindowStyle();
		dialogStyle.background =  new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		dialogStyle.titleFont = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		dialogStyle.titleFontColor = Color.WHITE;
		
		TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle(assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class), Color.WHITE, null, null, null);
		
		TextButton.TextButtonStyle tbStyle = new TextButton.TextButtonStyle(null, null, null, assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class));
		
		//UISet values
		mainTable = new Table();
		
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("newgame_logo")));
		
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				initial.setUI(from);
			}
		});

		//This text is shown if multiplayer is not available for the given platform
		TextArea dialogText = new TextArea("Multiplayer feature is disabled for this platform.", tfStyle);
		dialogText.setAlignment(Align.center);
		dialogText.setTouchable(Touchable.disabled);
		
		//This is the dialog shown when multiplayer is not available
		unsupportedDialog = new Dialog("", dialogStyle);
		unsupportedDialog.button("Ok", null, tbStyle);	
		unsupportedDialog.getContentTable().add(dialogText).center().size(600, 150);
		
		//Single player button.
		singlePlayer = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("singleplayer")));
		singlePlayer.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				GameScreen gameScreen = new GameScreen("maps/tutorial.tmx", GAME_REF); 
				LoadingScreen loadingScreen = new LoadingScreen(gameScreen, GAME_REF, gameScreen);
				
				if (LevelSelecter.LevelSelectSet.TUTORIAL.getCutscene() != null)
					GAME_REF.setScreen(new NarrativeScreen(LevelSelecter.LevelSelectSet.TUTORIAL.getCutscene(), loadingScreen, GAME_REF));
				
				else
					GAME_REF.setScreen(loadingScreen);
			}
		});
		mainTable.add(singlePlayer).center().fill().row();
		
		//Multiplayer button
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
