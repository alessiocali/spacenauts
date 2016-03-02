package com.gff.spacenauts;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.ashley.EntityBuilder;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.DialogTrigger;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Obstacle;
import com.gff.spacenauts.data.DialogTriggerData;
import com.gff.spacenauts.data.LevelData;
import com.gff.spacenauts.data.SpawnerData;
import com.gff.spacenauts.dialogs.Dialog;
import com.gff.spacenauts.listeners.hit.PushAway;
import com.gff.spacenauts.screens.GameScreen;

/**
 * The abstract representation of a level.<br>
 * All of the level's data are actually stored inside a {@link LevelData} structure, which is usually loaded from a TMX map file.
 * What this class does instead is adding all fundamental entities that constitute the level, that is:
 * 
 *  <ul>
 *  <li>Spawners</li>
 *  <li>Obstacles</li>
 *  <li>Dialog triggers</li>
 *  </ul>
 *  
 *  It also stores a targetHeight value used to tell {@link com.gff.spacenauts.ashley.systems.CameraSystem CameraSystem} when to stop moving (vertical scrolling).
 * 
 * @author Alessio Cali'
 *
 */
public class Level {
	
	//private GameScreen gameScreen;
	private LevelData data;
	private float targetHeight = 0;
	private float startingX = Globals.STARTING_CAMERA_X, startingY = Globals.STARTING_CAMERA_Y - Globals.TARGET_CAMERA_HEIGHT / 4;
	
	/**
	 * Makes a new Level object out of the given {@link LevelData}.
	 * 
	 * @param levelData
	 * @param gameScreen
	 */
	public Level (LevelData levelData) {
		this.data = levelData;
		this.targetHeight = levelData.initialTargetHeight;
	}
	
	/**
	 * Extracts {@link LevelData} into entities by using {@link EntityBuilder} as a proxy.
	 * 
	 */
	public void build () {
		SpacenautsEngine engine = GameScreen.getEngine();
		EntityBuilder builder = GameScreen.getBuilder();
		
		for (SpawnerData spawner : data.enemies){
			engine.addEntity(builder.buildSpawner(spawner));
		}
		
		for (float[] obstacleVertices : data.obstacles){
			Body obstacleBody = engine.createComponent(Body.class);
			Hittable hittable = engine.createComponent(Hittable.class);
			Obstacle oTag = engine.createComponent(Obstacle.class);
			
			obstacleBody.polygon.setVertices(obstacleVertices);
			hittable.listeners.addListener(new PushAway(Families.ENEMY_FAMILY, Families.FRIENDLY_FAMILY));
			
			Entity obstacle = engine.createEntity().add(obstacleBody).add(oTag).add(hittable);
			engine.addEntity(obstacle);
		}
		
		for (DialogTriggerData dtData : data.dialogTriggers){
			DialogTrigger dt = engine.createComponent(DialogTrigger.class);
			dt.area.set(dtData.area);
			dt.dialog = Dialog.loadDialogById(dtData.dialogID);
			engine.addEntity(engine.createEntity().add(dt));
		}
		
		engine.addEntity(builder.buildPlayer(startingX, startingY));		
	}
	
	/**
	 * Obtains the current target height, defined as the level's height (in world coordinates) minus half the camera's {@link Globals#TARGET_CAMERA_HEIGHT target height}.<br>
	 * This will be the camera's final position before the end of the level.
	 * 
	 * @return the target height of this level.
	 */
	public float getTargetHeight(){
		return targetHeight;
	}
	
	public float getLevelWidth(){
		return data.levelWidth;
	}
	
	public float getLevelHeight(){
		return data.levelHeight;
	}
	
	public String getBGM() {
		return data.bgm;
	}
}
