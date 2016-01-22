package com.gff.spacenauts.ashley;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Geometry;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.ai.AimAndShootAI;
import com.gff.spacenauts.ai.BigDummyAI;
import com.gff.spacenauts.ai.ErraticKamikazeAI;
import com.gff.spacenauts.ai.FirstLineAI;
import com.gff.spacenauts.ai.PowerUpAI;
import com.gff.spacenauts.ai.SteadyShooterAI;
import com.gff.spacenauts.ai.steering.LinearWavePath;
import com.gff.spacenauts.ai.steering.Parabolic;
import com.gff.spacenauts.ai.steering.SteeringInitializer;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Boss;
import com.gff.spacenauts.ashley.components.CollisionDamage;
import com.gff.spacenauts.ashley.components.CoopPlayer;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Enemy;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Friendly;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Player;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Removable;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.components.WorldCamera;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.data.SpawnerData;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.Remove;
import com.gff.spacenauts.listeners.death.ActivatePowerUp;
import com.gff.spacenauts.listeners.death.EmitSound;
import com.gff.spacenauts.listeners.death.ReleaseAnimation;
import com.gff.spacenauts.listeners.hit.DamageAndDie;
import com.gff.spacenauts.listeners.hit.Die;
import com.gff.spacenauts.listeners.hit.PushAway;
import com.gff.spacenauts.listeners.shoot.ApplySteering;
import com.gff.spacenauts.listeners.shoot.Propagate;
import com.gff.spacenauts.listeners.shoot.RandomizeAngle;
import com.gff.spacenauts.listeners.timers.Spawn;
import com.gff.spacenauts.screens.GameScreen;

/*
 * APPUNTI SULLE ROUTINE COMPORTAMENTALI
 * 
 * DUMMY: Manichino del tutorial. Usa Face Behavior per puntare il giocatore e sparare.
 * SX_00-BIG_DUMMY: Boss del tutorial. Analogamente a DUMMY punta il giocatore e spara, ma lancia tre proiettili alla volta.
 * 				Se messo alle strette richiama dei DUMMY e attiva due laser ausiliari.
 * 
 * BLACK_INTERCEPTOR: Postazione immobile. Si limita a lanciare due laser di fronte a se'.
 * GREEN_TANK: Postazione immobile. Si limita a lanciare sei laser (tre per lato) ai propri fianchi.
 * BLUE_CRUISER: Rapido nemico pop-corn. Esegue manovre a zigzag ed esplode vicino al giocatore infliggendo pesanti danni.
 * purple_bomber: Ruota su se stesso lanciando proiettili.
 * SX_01-FIRST_LINE: Boss del primo stage. Inizia il combattimento a 0d, lanciando due laser rettilinei e un laser a diffusione che esplode in 3 proiettili
 * 					 dopo pochi secondi. Al 70p comincia a ruotare fino a raggiungere i 180d. In questa seconda configurazione rilascia un  cruiser
 * 					 ogni 7 secondi che insegue il giocatore. Al 60p i blue cruiser diventano due. Al 40p diventano 3. Al 30p comincia a ruotare
 * 					 fino a raggiungere i 270d. In questa configurazione combina i comportamenti precedenti: due cruiser compaiono dal retro per lanciarsi
 * 					 sul giocatore, mentre 4 proiettili compiono una traiettoria "a L" limitando gli spostamenti del giocatore.
 * 
 */


/**
 * An EntityBuilder is a blue print for entities. It creates new Entities with a given configuration.<p>
 * 
 * It needs a {@link SpacenautsGameScreen.getEngine()} to work; this GameScreen.getEngine() is provided by {@link GameScreen#getGameScreen.getEngine()()} method.<p>
 * 
 * @author Alessio Cali'
 *
 */
public class EntityBuilder {

	private static final String VERTICES_DATA = "vertices.data";

	private static final String SPRITE_PLAYER = "spaceship_sprite";
	private static final String SPRITE_COOP_PLAYER = "spaceship_sprite_coop";
	private static final String SPRITE_DUMMY = "enemy_sprite";
	private static final String SPRITE_BIG_DUMMY = "big_dummy";
	private static final String SPRITE_BLACK_INTERCEPTOR = "black_interceptor";
	private static final String SPRITE_GREEN_TANK = "green_tank";
	private static final String SPRITE_BLUE_CRUISER = "blue_cruiser";
	private static final String SPRITE_PURPLE_BOMBER = "purple_bomber";
	private static final String SPRITE_FIRST_LINE = "first_line";

	private static final String ANIM_EXPLOSION_BLUE = "proj_explosion_blue";
	private static final String ANIM_EXPLOSION_YELLOW = "proj_explosion_yellow";
	private static final String ANIM_EXPLOSION_SPACESHIP = "spaceship_explosion";

	private static final String SPRITE_PROJ_BLUE = "projectile_sprite";
	private static final String SPRITE_PROJ_YELLOW = "projectile_sprite_enemy";
	private static final String SPRITE_PROJ_RED = "projectile_sprite_red";
	private static final String SPRITE_PROJ_BALL = "projectile_sprite_ball";
	private static final String SPRITE_PROJ_BALL_YELLOW ="projectile_sprite_ball_yellow";

	private static final String SPRITE_PWUP_TRIGUN = "trigun";
	private static final String SPRITE_PWUP_AUTOGUN = "autogun";
	private static final String SPRITE_PWUP_HEAVYGUN = "heavygun";
	private static final String SPRITE_PWUP_HEALTH10 = "health10";

	private ObjectMap<String, Method> buildMap = new ObjectMap<String, Method>(20);
	private ObjectMap<String, float[]> vertexMap = new ObjectMap<String, float[]>(20);
	private ObjectMap<String, Sprite> spriteCache = new ObjectMap<String, Sprite>(20);
	private ObjectMap<String, Animation> animationCache = new ObjectMap<String, Animation>(20);
	private AssetManager assets;
	private TextureAtlas textures;
	private GameScreen game;

	public EntityBuilder(GameScreen game){
		this.game = game;
		textures = game.getAssets().get(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		assets = game.getAssets();

		try {
			buildVertexMap();
			buildSpriteCache();
			buildAnimationCache();
			buildFactoryMap();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the {@link ObjectMap} that links a certain builder method of EntityBuilder to a given ID through reflection.
	 * 
	 * @throws ReflectionException 
	 */
	private void buildFactoryMap() throws ReflectionException {
		Class<EntityBuilder> clazz = EntityBuilder.class;
		buildMap.put("NULL", ClassReflection.getDeclaredMethod(clazz, "buildNull"));
		buildMap.put("dummy", ClassReflection.getDeclaredMethod(clazz, "buildDummy"));
		buildMap.put("big_dummy", ClassReflection.getDeclaredMethod(clazz, "buildBigDummy"));
		buildMap.put("black_interceptor", ClassReflection.getDeclaredMethod(clazz, "buildBlackInterceptor"));
		buildMap.put("green_tank", ClassReflection.getDeclaredMethod(clazz, "buildGreenTank"));
		buildMap.put("blue_cruiser", ClassReflection.getDeclaredMethod(clazz, "buildBlueCruiser"));
		buildMap.put("purple_bomber", ClassReflection.getDeclaredMethod(clazz, "buildPurpleBomber"));
		buildMap.put("first_line", ClassReflection.getDeclaredMethod(clazz, "buildFirstLine"));
	}

	/**
	 * The vertex map links any enemy object to its polygon, by searching per ID.
	 * Data is loaded from the internal file /vertices.data and is parsed by Strings.
	 * 
	 * @throws IOException
	 */
	private void buildVertexMap() throws IOException {
		FileHandle verticesData = Gdx.files.internal(VERTICES_DATA);
		String nextLine;
		BufferedReader reader = new BufferedReader(verticesData.reader());

		while((nextLine = reader.readLine()) != null){
			//Skip comments
			if(nextLine.startsWith("#"))
				continue;

			//Test against regex pattern id=vx1,vx2...
			//Formerly, any character sequence, followed by any number of white spaces, then "=", white spaces, and character sequence
			if(nextLine.matches(".+\\s??=\\s??.+")){
				//Remove '=' and ' '
				String[] parse1 = nextLine.split("[= ]");
				String id = parse1[0];
				String verticesString = parse1[1];

				//While it looks crazy, this regex tests against couples of floating point values (x.y), separated by commas
				//and ensures there are at least 3 couples (to build a polygon)
				if (verticesString.matches("(([+-]??\\d+?\\.\\d+?),([+-]??\\d+?\\.\\d+?),??){3,}?")){
					//Split by commas
					String[] parse2 = verticesString.split(",");
					float[] vertices = new float[parse2.length];

					for (int i = 0 ; i < parse2.length ; i++){
						vertices[i] = Float.valueOf(parse2[i]);
					}

					vertexMap.put(id, vertices);
				} else {
					Logger.log(LogLevel.WARNING, this.toString(), "Vertex string does not match pattern. Vertex string was: \n " + verticesString);
				}
			} else {
				Logger.log(LogLevel.WARNING, this.toString(), "Data string does not match pattern. Data string was: \n " + nextLine);
			}
		}

		reader.close();
	}

	/**
	 * Loads all spaceship's sprites and stores them inside a {@link HashMap}.
	 * 
	 */
	private void buildSpriteCache() {
		//Spaceship sprites
		spriteCache.put("player", textures.createSprite(SPRITE_PLAYER));
		spriteCache.put("coop", textures.createSprite(SPRITE_COOP_PLAYER));
		spriteCache.put("dummy", textures.createSprite(SPRITE_DUMMY));
		spriteCache.put("big_dummy", textures.createSprite(SPRITE_BIG_DUMMY));
		spriteCache.put("black_interceptor", textures.createSprite(SPRITE_BLACK_INTERCEPTOR));
		spriteCache.put("green_tank", textures.createSprite(SPRITE_GREEN_TANK));
		spriteCache.put("blue_cruiser", textures.createSprite(SPRITE_BLUE_CRUISER));
		spriteCache.put("purple_bomber", textures.createSprite(SPRITE_PURPLE_BOMBER));
		spriteCache.put("first_line", textures.createSprite(SPRITE_FIRST_LINE));

		//Bullet sprites
		spriteCache.put("bullet_blue", textures.createSprite(SPRITE_PROJ_BLUE));
		spriteCache.put("bullet_yellow", textures.createSprite(SPRITE_PROJ_YELLOW));
		spriteCache.put("bullet_red", textures.createSprite(SPRITE_PROJ_RED));
		spriteCache.put("bullet_ball", textures.createSprite(SPRITE_PROJ_BALL));
		spriteCache.put("bullet_ball_yellow", textures.createSprite(SPRITE_PROJ_BALL_YELLOW));

		//Powerups
		spriteCache.put("TRIGUN", textures.createSprite(SPRITE_PWUP_TRIGUN));
		spriteCache.put("AUTOGUN", textures.createSprite(SPRITE_PWUP_AUTOGUN));
		spriteCache.put("HEAVYGUN", textures.createSprite(SPRITE_PWUP_HEAVYGUN));
		spriteCache.put("HEALTH10", textures.createSprite(SPRITE_PWUP_HEALTH10));
	}

	/**
	 * Loads all {@link Animation}s and stores them into a {@link HashMap}.
	 * 
	 */
	private void buildAnimationCache() {
		animationCache.put(ANIM_EXPLOSION_BLUE, new Animation(0.1f, textures.findRegion(ANIM_EXPLOSION_BLUE).split(16, 16)[0]));
		animationCache.put(ANIM_EXPLOSION_YELLOW, new Animation(0.1f, textures.findRegion(ANIM_EXPLOSION_YELLOW).split(16, 16)[0]));
		animationCache.put(ANIM_EXPLOSION_SPACESHIP, new Animation(0.1f, textures.findRegion(ANIM_EXPLOSION_SPACESHIP).split(68, 68)[0]));
	}

	/**
	 * Builds the camera entity. This entity holds:
	 * 
	 * <ul>
	 * <li>A {@link WorldCamera} to project from world to screen coordinates.</li>
	 * <li>A {@link Position} to store its center</li>
	 * <li>A fixed {@link Velocity} to achieve vertical scrolling (see {@link com.gff.spacenauts.ashley.systems.CameraSystem CameraSystem}).</li>
	 * <li>Both an {@link Angle} and an {@link AngularVelocity} to be processed by {@link com.gff.spacenauts.ashley.systems.MovementSystem MovementSystem}.</li>
	 * </ul>
	 * 
	 * @return The camera entity.
	 */
	public Entity buildWorldCamera(){
		Entity cameraEntity = GameScreen.getEngine().createEntity();

		WorldCamera worldCamera = GameScreen.getEngine().createComponent(WorldCamera.class);
		worldCamera.viewport.setWorldSize(Globals.TARGET_CAMERA_WIDTH, Globals.TARGET_CAMERA_HEIGHT);

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		pos.value.set(Globals.STARTING_CAMERA_X, Globals.STARTING_CAMERA_Y);

		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		vel.value.set(0, Globals.baseCameraSpeed);

		Angle ang = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);

		cameraEntity.add(worldCamera).add(pos).add(vel).add(ang).add(angVel);

		return cameraEntity;
	}

	/**
	 * Builds a player entity. The player's speed is set to be the same as the camera's, so to appear solidal to it.<br>
	 * A special death listener is added to the player, namely {@link GameOver}. It triggers the game over sequence on the player's death.<br>
	 * A special AI is also included, which is {@link PowerUpAI}. While not a proper AI, it's still a FSM tha handles PowerUps. 
	 * 
	 * @param x player's initial x coordinate.
	 * @param y player's initial y coordinate.
	 * @return the player entity.
	 */
	public Entity buildPlayer(float x, float y){		
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Player player = GameScreen.getEngine().createComponent(Player.class);
		Friendly friendly = GameScreen.getEngine().createComponent(Friendly.class);
		FSMAI powerUps = GameScreen.getEngine().createComponent(FSMAI.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		Gun gun = buildGunNormal();
		Death death = GameScreen.getEngine().createComponent(Death.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(hittable).add(player).add(friendly).add(powerUps)
		.add(render).add(gun).add(death);

		pos.value.set(x, y);
		vel.value.set(0, Globals.baseCameraSpeed);
		angle.value = MathUtils.PI / 2;
		body.polygon.setPosition(pos.value.x, pos.value.y);
		body.polygon.setVertices(Geometry.copy(vertexMap.get("player")));
		body.polygon.setRotation(angle.value);
		body.polygon.setScale(0.75f, 0.75f);
		hittable.health = 100;
		hittable.maxHealth = 100;
		hittable.listeners.addListener(new DamageAndDie(Families.ENEMY_FAMILY));
		hittable.listeners.addListener(new PushAway(Families.OBSTACLE_FAMILY));
		powerUps.fsm =  new PowerUpAI(entity, game.getUI());
		render.sprite = spriteCache.get("player");
		render.scale = Globals.UNITS_PER_PIXEL;
		death.listeners.addListener(DeathListener.Commons.GAME_OVER);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));

		return entity;
	}
	
	public Entity buildCoopPlayer(float x, float y){		
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		CoopPlayer player = GameScreen.getEngine().createComponent(CoopPlayer.class);
		Friendly friendly = GameScreen.getEngine().createComponent(Friendly.class);
		FSMAI powerUps = GameScreen.getEngine().createComponent(FSMAI.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		Gun gun = buildGunNormal();
		Death death = GameScreen.getEngine().createComponent(Death.class);

		entity.add(pos).add(angle).add(body)
		.add(hittable).add(player).add(friendly).add(powerUps)
		.add(render).add(gun).add(death);

		pos.value.set(x, y);
		angle.value = MathUtils.PI / 2;
		body.polygon.setPosition(pos.value.x, pos.value.y);
		body.polygon.setVertices(Geometry.copy(vertexMap.get("player")));
		body.polygon.setRotation(angle.value);
		body.polygon.setScale(0.75f, 0.75f);
		hittable.health = 100;
		hittable.maxHealth = 100;
		powerUps.fsm =  new PowerUpAI(entity, game.getUI());
		render.sprite = spriteCache.get("coop");
		render.scale = Globals.UNITS_PER_PIXEL;
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));

		return entity;
	}

	/**
	 * Builds a {@link Gun} component for the PowerUp: Trigun.
	 * 
	 * @return A Trigun Gun.
	 */
	public Gun buildGunTri() {
		Gun gun = buildGunNormal();
		GunData gun2 = buildGunDataNormal();
		GunData gun3 = buildGunDataNormal();

		gun2.pOffset.set(0.9f, -1f);
		gun3.pOffset.set(0.9f, 1f);

		gun.guns.addAll(gun2, gun3);
		return gun;
	}
	
	/**
	 * Builds a {@link Gun} component for the PowerUp: Heavygun.
	 * 
	 * @return A Heavygun Gun.
	 */
	public Gun buildGunHeavy() {
		Gun gun = buildGunNormal();
		
		gun.guns.get(0).bulletDamage = 30;
		gun.guns.get(0).bulletImage = spriteCache.get("bullet_red");
		return gun;
	}

	/**
	 * Builds a {@link Gun} component for a regular player Gun.
	 * 
	 * @return A regular player Gun.
	 */
	public Gun buildGunNormal() {
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		gun.guns.add(buildGunDataNormal());
		return gun;
	}

	/**
	 * Builds {@link GunData} for the player's regular gun. It's also used as a base for PowerUp guns.
	 * 
	 * @return a regular gun's data.
	 */
	private GunData buildGunDataNormal(){
		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 10;
		gunData.bulletHitListeners.addListener(new Die(Families.ENEMY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_BLUE), GameScreen.getEngine()));
		gunData.speed = 10;
		gunData.bulletImage = spriteCache.get("bullet_blue");
		gunData.scale = Globals.UNITS_PER_PIXEL;
		gunData.pOffset.set(0.75f, 0);
		gunData.shotSound = AssetsPaths.SFX_LASER_4;
		return gunData;
	}

	/**
	 * Builds an acquirable powerup in world space.
	 * 
	 * @param id the powerup's id.
	 * @param x the powerup's x coordinate.
	 * @param y the powerup's y coordinate.
	 * @return
	 */
	public Entity buildPowerUp(String id, float x, float y){
		Entity puEntity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Angle ang = GameScreen.getEngine().createComponent(Angle.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		Hittable hit = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		Removable rem = GameScreen.getEngine().createComponent(Removable.class);

		puEntity.add(pos).add(ang).add(body).add(hit).add(death).add(render).add(rem);

		pos.value.set(x, y);
		body.polygon.setVertices(Geometry.copy(vertexMap.get(id)));
		body.polygon.setPosition(pos.value.x, pos.value.y);
		hit.listeners.addListener(new Die(Families.FRIENDLY_FAMILY));
		death.listeners.addListener(new ActivatePowerUp(id));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_POWERUP, Sound.class)));
		render.sprite = spriteCache.get(id);

		return puEntity;
	}

	/**
	 * Builds a spawner entity out of the given {@link SpawnerData}.
	 * 
	 * @param data
	 * @return the spawner entity.
	 */
	public Entity buildSpawner(SpawnerData data){
		Timers timerComponent = GameScreen.getEngine().createComponent(Timers.class);
		timerComponent.listeners.add(new Spawn(data));

		Position spawnerPosition = GameScreen.getEngine().createComponent(Position.class);
		spawnerPosition.value.set(data.initialPosition);

		return GameScreen.getEngine().createEntity().add(timerComponent).add(spawnerPosition);
	}

	/**
	 * Builds an entity with a certain configuration.
	 * 
	 * @param id The entity ID.
	 * @return The built entity, or null if the entity ID is invalid.
	 */
	public Entity buildById(String id){
		if (buildMap.containsKey(id)){
			try {
				return (Entity) buildMap.get(id).invoke(this);
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}

		Logger.log(LogLevel.ERROR, this.toString(), "No builder found for ID: " + id);
		return null;
	}

	/**
	 * Builds a dummy enemy. This spaceship appears during the game's tutorial, and its basic AI simply aims and shoot towards the player.<br>
	 * Also see {@link AimAndShootAI}.
	 * 
	 * 
	 * @return a Dummy entity.
	 */
	public Entity buildDummy(){
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		Removable rem = GameScreen.getEngine().createComponent(Removable.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(collisionDamage)
		.add(hittable).add(death).add(enemy).add(gun).add(rem).add(render).add(ai);

		enemy.score = 20;
		angle.value = -MathUtils.PI / 2;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("dummy")));
		body.polygon.setRotation(angle.value);
		collisionDamage.damageDealt = 0;
		hittable.health = 50;
		hittable.maxHealth = 50;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		hittable.listeners.addListener(new PushAway(Families.OBSTACLE_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));
		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.pOffset.set(1, 0);
		gunData.bulletDamage = 1;
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_YELLOW), GameScreen.getEngine()));
		gunData.speed = 10;
		gunData.bulletImage = spriteCache.get("bullet_yellow");
		gunData.scale = Globals.UNITS_PER_PIXEL;
		gunData.shotSound = AssetsPaths.SFX_LASER_4;
		gun.guns.add(gunData);
		render.sprite = spriteCache.get("dummy");
		ai.fsm = new AimAndShootAI(entity);	

		return entity;
	}

	/**
	 * Builds Big Dummy, the tutorial's boss. It's AI is very simplistic, basically shoots 3 lasers towards the player and
	 * spawns some minions at certain points. See {@link BigDummyAI}.
	 * 
	 * @return Big Dummy.
	 */
	public Entity buildBigDummy(){
		Entity entity = GameScreen.getEngine().createEntity();
		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Boss boss = GameScreen.getEngine().createComponent(Boss.class);
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(collisionDamage).add(hittable)
		.add(death).add(enemy).add(boss).add(gun).add(render).add(ai);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("big_dummy")));
		body.polygon.setScale(7.5f, 4);
		collisionDamage.damageDealt = 3;
		hittable.health = 1000;
		hittable.maxHealth = 1000;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.VICTORY);
		enemy.score = 500;
		boss.name = "SX-00 : BIG_DUMMY";
		GunData[] gunData = new GunData[3];
		for (int i = 0 ; i < 3 ; i++){
			gunData[i] = buildGunDataBigDummy();
		}
		gunData[0].pOffset.set(7, 0);
		gunData[1].pOffset.set(4, -2.5f);
		gunData[2].pOffset.set(4, 2.5f);
		gun.guns.addAll(gunData);
		render.sprite = spriteCache.get("big_dummy");
		ai.fsm = new BigDummyAI(entity);

		return entity;

	}

	/**
	 * Builds GunData for Big Dummy.
	 * 
	 * @return Big Dummy's gun data.
	 */
	public GunData buildGunDataBigDummy(){
		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 1;
		gunData.bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_BLUE), GameScreen.getEngine()));
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletImage = spriteCache.get("bullet_ball");
		gunData.scale = Globals.UNITS_PER_PIXEL;
		gunData.speed = 10;
		gunData.shotSound = AssetsPaths.SFX_LASER_4;

		return gunData;
	}

	/**
	 * Builds a Black Interceptor. This enemy simply shoot a single laser in front of him, at regular intervals.<br>
	 * See {@link SteadyShooterAI}.
	 * 
	 * @return A Black Interceptor
	 */
	public Entity buildBlackInterceptor(){		
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		Removable rem = GameScreen.getEngine().createComponent(Removable.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 10;
		angle.value = -MathUtils.PI / 2;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("black_interceptor")));
		body.polygon.setRotation(angle.value);
		collisionDamage.damageDealt = 1;
		hittable.health = 30;
		hittable.maxHealth = 30;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		hittable.listeners.addListener(new PushAway(Families.OBSTACLE_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));
		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 1;
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_YELLOW), GameScreen.getEngine()));
		gunData.speed = 20;
		gunData.shotSound = AssetsPaths.SFX_LASER_4;
		gunData.bulletImage = spriteCache.get("bullet_yellow");
		gunData.scale = Globals.UNITS_PER_PIXEL;
		gunData.pOffset.set(2, 0);
		gun.guns.add(gunData);
		render.sprite = spriteCache.get("black_interceptor");
		render.scale = Globals.UNITS_PER_PIXEL;
		ai.fsm = new SteadyShooterAI(entity);	

		return entity;
	}

	/**
	 * Builds a Green Tank. Similar to Black Interceptor in its behavior, a green tank shoot steadily six lasers from its flanks.<br>
	 * See {@link SteadyShooterAI}.
	 * 
	 * @return a Green Tank.
	 */
	public Entity buildGreenTank() {
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		Removable rem = GameScreen.getEngine().createComponent(Removable.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 30;
		angle.value = -MathUtils.PI / 2;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("green_tank")));
		body.polygon.setRotation(angle.value);
		collisionDamage.damageDealt = 1;
		hittable.health = 50;
		hittable.maxHealth = 50;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		hittable.listeners.addListener(new PushAway(Families.OBSTACLE_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));

		for (int i = 0 ; i < 6 ; i++) {
			GunData gunData = Pools.get(GunData.class).obtain();
			gunData.bulletDamage = 1;
			gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData.bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
			gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_YELLOW), GameScreen.getEngine()));
			gunData.speed = 10;
			gunData.shotSound = AssetsPaths.SFX_LASER_4;
			gunData.bulletImage = spriteCache.get("bullet_yellow");
			gunData.scale = Globals.UNITS_PER_PIXEL;

			switch(i) {
			case 0:
				gunData.pOffset.set(-1,-1);
				gunData.aOffset = - MathUtils.PI / 2;
				break;
			case 1:
				gunData.pOffset.set(0, -1);
				gunData.aOffset = - MathUtils.PI / 2;
				break;
			case 2:
				gunData.pOffset.set(1, -1);
				gunData.aOffset = - MathUtils.PI / 2;
				break;
			case 3:
				gunData.pOffset.set(-1, 1);
				gunData.aOffset = MathUtils.PI / 2;
				break;
			case 4:
				gunData.pOffset.set(0, 1);
				gunData.aOffset = MathUtils.PI / 2;
				break;
			case 5:
				gunData.pOffset.set(1, 1);
				gunData.aOffset = MathUtils.PI / 2;
				break;
			}

			gun.guns.add(gunData);
		}

		render.sprite = spriteCache.get("green_tank");
		render.scale = Globals.UNITS_PER_PIXEL;
		ai.fsm = new SteadyShooterAI(entity);
		return entity;
	}

	/**
	 * Builds a Blue Cruiser. It runs downward in a wave-like pattern given by {@link LinearWavePath} and explodes when it reaches the player.<br>
	 * See {@link ErraticKamikazeAI}.
	 * 
	 * 
	 * @return a Blue Cruiser.
	 */
	public Entity buildBlueCruiser () {
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Removable rem = GameScreen.getEngine().createComponent(Removable.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(rem).add(render).add(ai);

		enemy.score = 10;
		angle.value = - MathUtils.PI / 2;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("blue_cruiser")));
		body.polygon.setRotation(angle.value);
		collisionDamage.damageDealt = 3;
		hittable.listeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));
		render.sprite = spriteCache.get("blue_cruiser");
		render.scale = Globals.UNITS_PER_PIXEL;
		ai.fsm = new ErraticKamikazeAI(entity);	

		return entity;
	}

	/**
	 * Builds a Purple Bomber. It shoots 4 lasers in orthogonal directions, and rotates at a fixed speed.
	 * 
	 * @return a Purple Bomber.
	 */
	public Entity buildPurpleBomber() {
		Entity entity = GameScreen.getEngine().createEntity();

		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		Removable rem = GameScreen.getEngine().createComponent(Removable.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 10;
		angle.value = -MathUtils.PI / 2;
		angVel.value = - MathUtils.PI / 3;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("purple_bomber")));
		body.polygon.setRotation(angle.value);
		collisionDamage.damageDealt = 1;
		hittable.health = 30;
		hittable.maxHealth = 30;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		hittable.listeners.addListener(new PushAway(Families.OBSTACLE_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));
		GunData[] gunData = new GunData[4];

		for (int i = 0 ; i < 4 ; i++) {
			gunData[i] = Pools.get(GunData.class).obtain();
			gunData[i].bulletDamage = 1;
			gunData[i].bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData[i].bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
			gunData[i].bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_YELLOW), GameScreen.getEngine()));
			gunData[i].speed = 20;
			gunData[i].shotSound = AssetsPaths.SFX_LASER_4;
			gunData[i].bulletImage = spriteCache.get("bullet_yellow");
			gunData[i].scale = Globals.UNITS_PER_PIXEL;
			gunData[i].aOffset = i * MathUtils.PI / 2;
		}
		gun.guns.addAll(gunData);
		render.sprite = spriteCache.get("purple_bomber");
		render.scale = Globals.UNITS_PER_PIXEL;
		ai.fsm = new SteadyShooterAI(entity);	

		return entity;
	}

	/**
	 * Builds First Line, Level 1's boss. For its behavioral pattern please see {@link FirstLineAI}.
	 * 
	 * @return First Line.
	 */
	public Entity buildFirstLine () {
		Entity entity = GameScreen.getEngine().createEntity();
		Position pos = GameScreen.getEngine().createComponent(Position.class);
		Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
		Angle angle = GameScreen.getEngine().createComponent(Angle.class);
		AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);
		Body body = GameScreen.getEngine().createComponent(Body.class);
		CollisionDamage collisionDamage = GameScreen.getEngine().createComponent(CollisionDamage.class);
		Hittable hittable = GameScreen.getEngine().createComponent(Hittable.class);
		Death death = GameScreen.getEngine().createComponent(Death.class);
		Enemy enemy = GameScreen.getEngine().createComponent(Enemy.class);
		Boss boss = GameScreen.getEngine().createComponent(Boss.class);
		Gun gun = GameScreen.getEngine().createComponent(Gun.class);
		Render render = GameScreen.getEngine().createComponent(Render.class);
		FSMAI ai = GameScreen.getEngine().createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(collisionDamage).add(hittable)
		.add(death).add(enemy).add(boss).add(gun).add(render).add(ai);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("first_line")));
		body.polygon.setScale(2.2f, 2.2f);
		collisionDamage.damageDealt = 3;
		hittable.health = 2000;
		hittable.maxHealth = 2000;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		death.listeners.addListener(new Remove(GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_SPACESHIP), GameScreen.getEngine()));
		death.listeners.addListener(DeathListener.Commons.VICTORY);
		enemy.score = 500;
		boss.name = "SX-01 : FIRST_LINE";
		render.sprite = spriteCache.get("first_line");
		render.scale = Globals.UNITS_PER_PIXEL;
		ai.fsm = new FirstLineAI(entity);

		return entity;
	}

	/**
	 * Builds GunData structures for First Line while it's from 100% to 70% health.
	 * 
	 * @return GunData for FirstLine, from 100% to 70%
	 */
	public GunData[] buildGunDataFL100To70 () {
		GunData[] gunData = new GunData[3];

		for (int i = 0 ; i < gunData.length ; i++) {
			gunData[i] = Pools.get(GunData.class).obtain();
			gunData[i].aOffset = -MathUtils.PI / 2;
			gunData[i].bulletDamage = 2;
			gunData[i].bulletImage = spriteCache.get("bullet_ball_yellow");
			gunData[i].shotSound = AssetsPaths.SFX_LASER_4;
			gunData[i].speed = 7;
			gunData[i].bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData[i].bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
			gunData[i].bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_YELLOW), GameScreen.getEngine()));
			gunData[i].gunShotListeners.addListener(new RandomizeAngle(-MathUtils.PI / 8, MathUtils.PI / 8));
		}

		gunData[0].pOffset.set(0, -1);
		gunData[1].pOffset.set(-2, -1);
		gunData[2].pOffset.set(2, -1);

		gunData[0].gunShotListeners.addListener(new Propagate(0.4f, 3));
		gunData[1].gunShotListeners.addListener(new Propagate(1f, 3));
		gunData[2].gunShotListeners.addListener(new Propagate(1.6f, 3));

		return gunData;
	}

	/**
	 * Builds GunData structures for First Line while it's from 30% to 0% health.
	 * 
	 * @return GunData for FirstLine, from 30% to 0%
	 */
	public GunData[] buildGunDataFL30To0 () {
		GunData[] gunData = new GunData[6];
		
		SteeringInitializer leftInit = new SteeringInitializer() {
			@Override
			public void init (SteeringBehavior<Vector2> behavior) {
				Parabolic pBehavior = (Parabolic)behavior;
				pBehavior.setHorizontalAccel(5);
			}
		};
		
		SteeringInitializer rightInit = new SteeringInitializer() {
			@Override
			public void init (SteeringBehavior<Vector2> behavior) {
				Parabolic pBeahavior = (Parabolic)behavior;
				pBeahavior.setHorizontalAccel(-5);
			}
		};

		for (int i = 0 ; i < gunData.length ; i++) {
			int invert = i < 3 ? -1 : +1;
			gunData[i] = Pools.get(GunData.class).obtain();
			gunData[i].aOffset = invert * MathUtils.PI / 6;
			gunData[i].bulletDamage = 2;
			gunData[i].bulletImage = spriteCache.get("bullet_ball_yellow");
			gunData[i].shotSound = AssetsPaths.SFX_LASER_4;
			gunData[i].speed = 10;
			gunData[i].bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData[i].bulletDeathListeners.addListener(new Remove(GameScreen.getEngine()));
			gunData[i].bulletDeathListeners.addListener(new ReleaseAnimation(animationCache.get(ANIM_EXPLOSION_YELLOW), GameScreen.getEngine()));
			gunData[i].gunShotListeners.addListener(new RandomizeAngle(-MathUtils.PI / 8, MathUtils.PI / 8));
			Parabolic par = new Parabolic(null);
			par.setHorizontalAccel(-invert * 5);
			ApplySteering as;
			if (invert == -1) as = new ApplySteering(Parabolic.class, leftInit);
			else as = new ApplySteering(Parabolic.class, rightInit);
			as.setMaxLinearAcceleration(10);
			as.setMaxLinearSpeed(30);
			gunData[i].gunShotListeners.addListener(as);
		}

		gunData[0].pOffset.set(0, -2);
		gunData[1].pOffset.set(2, -2);
		gunData[2].pOffset.set(4, -2);
		gunData[3].pOffset.set(0, 2);
		gunData[4].pOffset.set(2, 2);
		gunData[5].pOffset.set(4, 2);

		return gunData;
	}

	public Entity buildNull(){
		Logger.log(LogLevel.WARNING, this.toString(), "Null entity created");
		return GameScreen.getEngine().createEntity();
	}

}
