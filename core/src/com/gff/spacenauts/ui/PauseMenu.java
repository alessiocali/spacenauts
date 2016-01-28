package com.gff.spacenauts.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.screens.InitialScreen;
import com.gff.spacenauts.screens.LoadingScreen;

/**
 * The central menu that is shown during the game's pause. So far it only has a button for going back to the main menu.
 * 
 * @author Alessio Cali'
 *
 */
public class PauseMenu extends Table {
	
	private TextureAtlas uiAtlas;
	private BitmapFont font;
	
	private Cell<Table> rootCell;
	private Table pauseTable;
	private Table exitTable;
	private Label resume;
	private Label backToMenu;
	private Label exit;
	private Label yes;
	private Label no;
	
	public PauseMenu (AssetManager assets, final Game game) {
		super();
		uiAtlas = assets.get(AssetsPaths.ATLAS_UI);
		font = assets.get(AssetsPaths.FONT_KARMATIC_64);
		
		setFillParent(true);
		
		pauseTable = new Table();
		pauseTable.setFillParent(true);
		rootCell = add(pauseTable).expand().fill();
		
		Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
		style.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		
		resume = new Label("RESUME", style);
		resume.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				GameScreen.getEngine().resume();
			}
		});
		pauseTable.add(resume).pad(5).row();
		backToMenu = new Label ("BACK TO MENU", style);
		backToMenu.addListener (new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				InitialScreen initial = new InitialScreen(game);
				game.setScreen(new LoadingScreen(initial, game, initial));
			}
		});
		pauseTable.add(backToMenu).pad(5);
		
		exitTable = new Table();
		
		exit = new Label("EXIT?", style);
		yes = new Label("YES", style);
		yes.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				Gdx.app.exit();
			}
		});
		no = new Label("NO", style);
		no.addListener(new ClickListener () {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				toggleExit(false);
			}
		});
		
		exitTable.add(exit).colspan(2).pad(5).row();
		exitTable.add(yes).pad(5);
		exitTable.add(no).pad(5);
	}
	
	public boolean isConfirmingExit() {
		return rootCell.getActor() == exitTable;
	}
	
	public void toggleExit(boolean show) {
		if (show) {
			rootCell.setActor(exitTable); 
			GameScreen.getEngine().pause();
		} else {
			rootCell.setActor(pauseTable);
			GameScreen.getEngine().resume();
		}
	}
	
	@Override
	public void act (float delta) {
		if (GameScreen.getEngine().isRunning()) {
			setVisible(false);
			rootCell.setActor(pauseTable);
		} else {
			setVisible(true);
		}
	}
}
