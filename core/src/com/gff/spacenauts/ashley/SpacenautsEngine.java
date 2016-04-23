package com.gff.spacenauts.ashley;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.systems.RenderingSystem;
import com.gff.spacenauts.listeners.timers.ScreenTransition;
import com.gff.spacenauts.net.NetworkAdapter.AdapterState;
import com.gff.spacenauts.screens.GameOverScreen;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.screens.VictoryScreen;

/**
 * An extended version of {@link PooledEngine} to fulfill common requests in the game's environment. 
 * 
 * @author Alessio Cali'
 *
 */
public class SpacenautsEngine extends PooledEngine {
	
	private static final String TAG = "SpacenautsEngine";
	
	private SteeringMechanism playerTarget;
	private boolean running = true;
	private boolean controlsEnabled = true;
	private boolean gameOver = false;
	private GameScreen gameScreen;
	private Screen gameOverScreen;
	private Screen nextScreen;
	private final boolean multiplayer;

	public SpacenautsEngine() {
		super();
		multiplayer = false;
	}

	public SpacenautsEngine(GameScreen gameScreen, Screen gameOverScreen, Screen nextScreen, boolean multiplayer, int entityPoolInitialSize, int entityPoolMaxSize,
			int componentPoolInitialSize, int componentPoolMaxSize) {
		super(entityPoolInitialSize, entityPoolMaxSize, componentPoolInitialSize,
				componentPoolMaxSize);
		this.gameScreen = gameScreen;
		this.gameOverScreen = gameOverScreen;
		this.nextScreen = nextScreen;
		this.multiplayer = multiplayer;
	}

	/**
	 * Retrieves the player entity through component tagging.
	 * 
	 * @return the player entity.
	 */
	public Entity getPlayer(){
		ImmutableArray<Entity> playerArray = getEntitiesFor(Families.PLAYER_FAMILY);
		
		if (playerArray.size() != 0){
			return playerArray.first();
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves the coop player. It returns null if not in coop mode.
	 * 
	 * @return the coop player entity, or null.
	 */
	public Entity getCoopPlayer() {
		ImmutableArray<Entity> coopArray = getEntitiesFor(Families.COOP_FAMILY);
		
		if (coopArray.size() != 0){
			return coopArray.first();
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves the boss entity through component tagging.
	 * 
	 * @return the boss entity.
	 */
	public Entity getBoss(){
		ImmutableArray<Entity> bossArray = getEntitiesFor(Families.BOSS_FAMILY);
		
		if (bossArray.size() != 0){
			return bossArray.first();
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves the camera entity by, again, component tagging.
	 * 
	 * @return the camera entity.
	 */
	public Entity getCamera(){
		ImmutableArray<Entity> cameraArray = getEntitiesFor(Families.CAMERA_FAMILY);
		
		if (cameraArray.size() != 0){
			return cameraArray.first();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns a {@link SteeringMechanism} for the player to use as a target in {@link com.badlogic.gdx.ai.steer.SteeringBehavior SteeringBehavior}s.
	 * 
	 * @return A steering mechanism built on the player.
	 */
	public SteeringMechanism getPlayerTarget(){
		Entity player = getPlayer();
		
		if (player == null)
			return null;
		
		if (playerTarget == null)
			return playerTarget = SteeringMechanism.getFor(getPlayer());
		
		else
			return playerTarget;
	}
	
	public Vector2 getCameraPosition() {
		return Mappers.pm.get(getCamera()).value;
	}
	
	/**
	 * Cleans all resources from this engine.
	 * 
	 */
	public void clear(){
		removeAllEntities();
		
		for (EntitySystem system : getSystems())
			removeSystem(system);
		
		playerTarget = null;
	}

	public boolean controlsEnabled(){
		return controlsEnabled;
	}
	
	public void setControlsEnabled(boolean controlsEnabled){
		this.controlsEnabled = controlsEnabled;
	}
	
	/**
	 * Pauses the game. All systems except {@link RenderingSystem} are stopped, and controls are disabled.
	 */
	public void pause(){
		if (running) {
			for (EntitySystem system : getSystems()){
				if (system instanceof RenderingSystem)
					continue;
				else 
					system.setProcessing(false);
			}
			
			running = false;
			controlsEnabled = false;
		}
	}
	
	/**
	 * Resumes the game. Controls are enabled again, unless the game is over.
	 */
	public void resume(){
		if (!running) {
			for (EntitySystem system : getSystems())
				system.setProcessing(true);
			
			running = true;
			
			if (!gameOver)
				controlsEnabled = true;
		}
	}
	
	/**
	 * Pauses if running, and vice versa.
	 */
	public void togglePause() {
		if (running) 
			pause();
		else
			resume();
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * Initializes game over procedures. Controls are disabled, and the player is void of both its {@link Velocity} and {@link Render} components.
	 * Also, all its {@link com.gff.spacenauts.listeners.HitListener HitListener}s are cleared and the game is flagged for transition to a 
	 * {@link com.gff.spacenauts.screens.GameOverScreen GameOverScreen}.
	 */
	public void gameOver(){
		gameOver = true;
		setControlsEnabled(false);
		Entity player = getPlayer();
		
		if (player != null){
			player.remove(Velocity.class);
			player.remove(Render.class);

			Hittable hit = Mappers.hm.get(player);
			
			if (hit != null)
				hit.listeners.clear();
		}
		
		if (gameOverScreen instanceof GameOverScreen) {
			int score = Mappers.plm.get(player).score;
			((GameOverScreen)gameOverScreen).setScore(score);
		}
		
		transition(gameOverScreen);
	}
	
	public boolean isGameOver(){
		return gameOver;
	}

	/**
	 * Initializes victory sequence procedures. Controls are disabled, all of the player's {@link com.gff.spacenauts.listeners.HitListener HitListener}s 
	 * are cleared and it's set up to move forward, over the screen's edge. Also the game is flagged for transition to a
	 * {@link com.gff.spacenauts.screens.VictoryScreen VictoryScreen}.
	 */
	public void initiateVictory() {
		gameOver = true;
		setControlsEnabled(false);
		Entity player = getPlayer();
		
		if (player != null){
			Velocity vel = Mappers.vm.get(player);
			Angle ang = Mappers.am.get(player);
			Hittable hit = Mappers.hm.get(player);
			
			if (vel != null)
				vel.value.set(0, 10);
			
			if (ang != null)
				ang.value = MathUtils.PI / 2;
			
			if (hit != null)
				hit.listeners.clear();
		}
		
		if (nextScreen instanceof VictoryScreen) {
			int score = Mappers.plm.get(player).score;
			((VictoryScreen)nextScreen).setScore(score);
		}
		
		transition(nextScreen);
	}
	
	/**
	 * Sets up a {@link ScreenTransition} to a new {@link Screen}.
	 * 
	 * @param screen the new screen.
	 */
	private void transition (Screen screen) {
		Entity transTemp = createEntity();
		Timers timers = createComponent(Timers.class);
		
		timers.listeners.add(new ScreenTransition(gameScreen, screen, 5));
		transTemp.add(timers);
		
		addEntity(transTemp);
	}

	/**
	 * Sends a message to coop player if in multiplayer mode.
	 * 
	 * @param msg
	 */
	public void sendCoop (String msg) {
		if (multiplayer && Spacenauts.getNetworkAdapter() != null) {
			AdapterState status = Spacenauts.getNetworkAdapter().getState();
			
			if (status == AdapterState.GAME) {
				Spacenauts.getNetworkAdapter().send(msg);
			} 
			
			else if (status == AdapterState.FAILURE) {
				Logger.log(LogLevel.ERROR, TAG, "Connection error. Reason was: " + Spacenauts.getNetworkAdapter().getFailureReason());
				Spacenauts.getNetworkAdapter().reset();
			}
		}
	}
}
