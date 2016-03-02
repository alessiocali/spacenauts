package com.gff.spacenauts.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ai.PowerUpAI;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Boss;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.dialogs.Dialog;
import com.gff.spacenauts.screens.GameScreen;

/**
 * The game's UI. It includes both the player's and the boss's health bars, the current score,
 * labels which state the current PowerUp and the boss's name and the DialogTable.
 * 
 * @author Alessio Cali'
 *
 */
public class GameUI extends Stage {

	public static final String DEFAULT_PANE_NAME = "default_pane";
	public static final String OVER_PANE_NAME = "over_pane";

	private Stack root;
	private Image pauseShade;
	private Table uiTable;
	private TopBar topBar;
	private SubBar subBar;
	private PauseMenu pauseMenu;
	private DialogTable dialogTable;

	public GameUI (GameScreen gameScreen) {
		super(new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT));
		
		AssetManager assets = gameScreen.getAssets();
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI);
	
		root = new Stack();
		root.setFillParent(true);
		addActor(root);
		
		//The pause shade is a gray image that overlays the game environment during the pause status.
		pauseShade = new Image(new TextureRegionDrawable(uiAtlas.findRegion("black_bg")));
		pauseShade.addAction(new Action() {
			@Override
			public boolean act (float delta) {
				Actor actor = getActor();
				
				if (GameScreen.getEngine().isRunning() && actor.isVisible()) 
					actor.setVisible(false);
				
				else if (!GameScreen.getEngine().isRunning() && !getActor().isVisible())
					actor.setVisible(true);
				
				return false;
			}
		});
		pauseShade.setFillParent(true);
		root.add(pauseShade);

		uiTable = new Table();
		uiTable.setFillParent(true);
		root.add(uiTable);

		topBar = new TopBar(assets);
		uiTable.add(topBar).height(80).fill();
		uiTable.row();

		subBar = new SubBar(assets);
		uiTable.add(subBar).height(80).fill();
		uiTable.row();
		
		pauseMenu = new PauseMenu(assets, gameScreen.getGame());
		pauseMenu.addListener(new InputListener () {
			@Override
			public boolean keyDown(InputEvent e, int keycode) {
				if (keycode == Keys.BACK) {
					Gdx.app.exit();
					return true;
				} else return false;
			}
		});
		uiTable.add(pauseMenu).center().expand().fill();
		uiTable.row();

		dialogTable = new DialogTable(assets);
		uiTable.add(dialogTable).bottom().left().height(300).fillX();
		dialogTable.setVisible(false);
	}

	public void resize(int width, int height){
		getViewport().update(width, height, true);
	}

	public void render(float delta){
		setDebugAll(Globals.debug);
		act(delta);
		draw();
	}

	/**
	 * When called it will set the DialogTable to show a new dialog.
	 * 
	 * @param dialog
	 */
	public void triggerDialog(Dialog dialog){
		dialogTable.setDialog(dialog);
		dialogTable.setVisible(true);
	}

	public void hideDialog(){
		dialogTable.setVisible(false);
	}

	/**
	 * Activates the PowerUp label by invoking a method from SubBar.
	 * 
	 * @param powerUp
	 */
	public void setPowerUp(PowerUpAI.PowerUpState powerUp){
		subBar.setPowerUp(powerUp);
	}

	public void setBossName(String bossName){
		subBar.setBossName(bossName);
	}

	/**
	 * Updates the two health bars, and automatically shows the boss health bar and label when it pops.
	 * 
	 * @see com.badlogic.gdx.scenes.scene2d.Stage#act(float)
	 */
	@Override
	public void act(float delta){
		super.act(delta);
		Entity player = GameScreen.getEngine().getPlayer();
		Entity boss = GameScreen.getEngine().getBoss();

		if (player != null) {
			topBar.setScore(Mappers.plm.get(player).score);

			Hittable hit = Mappers.hm.get(player);

			if (hit != null)
				topBar.setPlayerHealth(hit.getHealthPercent() * 100);
			else
				topBar.setPlayerHealth(0);
		}

		if (boss != null) {
			Hittable hit = Mappers.hm.get(boss);
			Boss bossComponent = Mappers.bom.get(boss);
			
			subBar.setBossName(bossComponent.name);

			if (hit != null)
				topBar.setBossHealth(hit.getHealthPercent() * 100);
		}
	}
	
	@Override
	public boolean keyDown(int keyCode) {
		//Fire events first
		if (super.keyDown(keyCode)) return true;
		
		else {
			//Handle Back Key otherwise
			if (keyCode == Keys.BACK) {
				
				//Pressed back during exit confirm, hide and resume play
				if (pauseMenu.isConfirmingExit()) 
					pauseMenu.toggleExit(false);
				
				//Pressed back during gameplay, show the exit confirm message
				else
					pauseMenu.toggleExit(true);
				
				return true;
			} 
			
			else return false;
		}
	}
}
