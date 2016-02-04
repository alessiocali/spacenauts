package com.gff.spacenauts;

import com.gff.spacenauts.ashley.EntityBuilder;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.DialogTrigger;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Obstacle;
import com.gff.spacenauts.data.DialogTriggerData;
import com.gff.spacenauts.data.LevelData;
import com.gff.spacenauts.data.SpawnerData;
import com.gff.spacenauts.dialogs.Dialog;
import com.gff.spacenauts.listeners.TimerListener.TimerType;
import com.gff.spacenauts.screens.GameScreen;

/**
 * The abstract representation of a level.<p>
 * All of the level's data are actually stored inside a {@link LevelData} structure, which is usually loaded from a TMX map file.
 * What this class do instead is adding all fundamental entities that constitute the level, that is:
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
	 * For testing purposes.
	 */
	public Level () {
		this(null);
		data = new LevelData();
		SpawnerData testSpawner = new SpawnerData();
		testSpawner.id = "pencil";
		testSpawner.timerType = TimerType.ONE_SHOT;
		data.enemies.add(testSpawner);
		targetHeight = 50;
	}
	
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
		for (SpawnerData spawner : data.enemies){
			GameScreen.getEngine().addEntity(GameScreen.getBuilder().buildSpawner(spawner));
		}
		
		for (float[] obstacleVertices : data.obstacles){
			Body obstacleBody = GameScreen.getEngine().createComponent(Body.class);
			Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
			Obstacle oTag = GameScreen.getEngine().createComponent(Obstacle.class);
			obstacleBody.polygon.setVertices(obstacleVertices);
			GameScreen.getEngine().addEntity(GameScreen.getEngine().createEntity().add(obstacleBody).add(oTag).add(hittable));
		}
		
		for (DialogTriggerData dtData : data.dialogTriggers){
			DialogTrigger dt = GameScreen.getEngine().createComponent(DialogTrigger.class);
			dt.area.set(dtData.area);
			dt.dialog = Dialog.loadDialogById(dtData.dialogID);
			GameScreen.getEngine().addEntity(GameScreen.getEngine().createEntity().add(dt));
		}
		
		GameScreen.getEngine().addEntity(GameScreen.getBuilder().buildPlayer(startingX, startingY));		
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
