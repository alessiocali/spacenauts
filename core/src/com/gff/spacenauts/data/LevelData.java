package com.gff.spacenauts.data;

import java.io.Serializable;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.listeners.TimerListener.TimerType;

/**
 * All data needed to build a level. This includes {@link SpawnerData} structures for spawners, {@link DialogTriggerData} for dialog triggers,
 * obstacles vertices, the level's dimensions, its BGM and next map (as file paths).
 * 
 * @author Alessio Cali'
 *
 */
public class LevelData implements Serializable {

	private static final long serialVersionUID = -3183310677673871783L;
	
	private static final String SPAWNER_LAYER = "spawners";
	private static final String DIALOG_LAYER = "dialogs";
	private static final String OBSTACLE_LAYER = "obstacles";
	
	public Array<SpawnerData> enemies = new Array<SpawnerData>();
	public Array<DialogTriggerData> dialogTriggers = new Array<DialogTriggerData>();
	public Array<float[]> obstacles = new Array<float[]>();
	public float initialTargetHeight = 0;
	public int levelHeight = 0;
	public int levelWidth = 0;
	public String bgm;
	public String nextMap;
	
	/**
	 * Builds a new LevelData off a {@link TiledMap}. The game uses TMX maps from Tiled as a reference.
	 * This will also set WorldGlobals.baseCameraSpeed. Originally I planned to use a one-size-fits-all speed but this has proven to be
	 * unfeasible, since the tutorial needed a slower pace, while the first level felt better at higher speeds. 
	 * 
	 * @param map the TiledMap to load.
	 * @return LevelData extracted from the map.
	 */
	public static LevelData loadFromMap(TiledMap map){
		LevelData data = new LevelData();
		MapProperties properties = map.getProperties();
		Globals.baseCameraSpeed = Float.valueOf(properties.get("camera_speed", "2.5", String.class));
		data.bgm = "bgm/" + properties.get("bgm", "Urban-Future.mp3", String.class);
		data.nextMap = properties.get("nextScreen", String.class);
		data.levelHeight = properties.get("height", 30, int.class);
		data.levelWidth = properties.get("width", 15, int.class);
		data.initialTargetHeight = data.levelHeight - Globals.TARGET_SCREEN_HEIGHT / Globals.PIXELS_PER_UNIT / 2;
		data.enemies = loadEnemies(map);
		data.dialogTriggers = loadDialogs(map);
		data.obstacles = loadObstacles(map);
		
		return data;
	}
	
	/**
	 * Loads spawner data structures from the TiledMap's {@link #SPAWNER_LAYER}.
	 * 
	 * @param map the map to load from.
	 * @return all spawners from the spawner layer.
	 */
	private static Array<SpawnerData> loadEnemies(TiledMap map){
		MapLayer layer = map.getLayers().get(SPAWNER_LAYER);
		Array<SpawnerData> enemies = new Array<SpawnerData>();
		
		for (MapObject object : layer.getObjects()){
			MapProperties objectProperties = object.getProperties();
			SpawnerData data = new SpawnerData();
			
			data.id = objectProperties.get("spawnID", "NULL", String.class);
			data.initialAngle = Float.valueOf(objectProperties.get("angle", String.valueOf(-90), String.class)) / 180 * MathUtils.PI;
			data.initialPosition.set(objectProperties.get("x", -11f, float.class), objectProperties.get("y", -11f, float.class)).scl(Globals.UNITS_PER_PIXEL);
			data.initialVelocity.set(Float.valueOf(objectProperties.get("velX", "0", String.class)), 
									 Float.valueOf(objectProperties.get("velY", "0", String.class)));
			data.intervalTime = Float.valueOf(objectProperties.get("interval", "0", String.class));
			data.limit = Integer.valueOf(objectProperties.get("limit", "0", String.class));
			data.timerType = TimerType.getByName(objectProperties.get("spawner_type", "one_shot", String.class));
			data.releasedPowerUp = objectProperties.get("released_power_up", null, String.class);
			
			enemies.add(data);
		}
		
		return enemies;
	}
	
	/**
	 * Loads dialog data from the TiledMap's {@link #DIALOG_LAYER}.
	 * 
	 * @param map the map to load from.
	 * @return all dialog triggers from the dialog layer.
	 */
	private static Array<DialogTriggerData> loadDialogs(TiledMap map){
		MapLayer layer = map.getLayers().get(DIALOG_LAYER);
		Array<DialogTriggerData> triggers = new Array<DialogTriggerData>();
		
		for (MapObject object : layer.getObjects()){
			MapProperties objectProperties = object.getProperties();
			DialogTriggerData data = new DialogTriggerData();
			
			data.area.width = objectProperties.get("width", 0f, float.class) * Globals.UNITS_PER_PIXEL;
			data.area.height = objectProperties.get("height", 0f, float.class) * Globals.UNITS_PER_PIXEL;
			data.area.x = objectProperties.get("x", -11f, float.class) * Globals.UNITS_PER_PIXEL;
			data.area.y = objectProperties.get("y", -11f, float.class) * Globals.UNITS_PER_PIXEL;
			data.dialogID = objectProperties.get("dialogID", "", String.class);
			
			triggers.add(data);
		}
		
		return triggers;
	}
	
	/**
	 * Loads obstacles from the TiledMap's {@link #OBSTACLE_LAYER}.
	 * 
	 * @param map the map to load from.
	 * @return a list of floats arrays representing the obstacles.
	 */
	private static Array<float[]> loadObstacles(TiledMap map){
		MapLayer layer = map.getLayers().get(OBSTACLE_LAYER);
		Array<float[]> obstacles = new Array<float[]>();
		
		for (MapObject object : layer.getObjects()) {
			float[] obstacleData = ((PolygonMapObject)object).getPolygon().getTransformedVertices();
			
			for (int i = 0 ; i < obstacleData.length ; i++)
				obstacleData[i] *= Globals.UNITS_PER_PIXEL;
			
			obstacles.add(obstacleData);
		}
		
		return obstacles;
	}

}
