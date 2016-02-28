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
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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
import com.gff.spacenauts.ai.AnathorAI;
import com.gff.spacenauts.ai.BigDummyAI;
import com.gff.spacenauts.ai.ErraticKamikazeAI;
import com.gff.spacenauts.ai.FirstLineAI;
import com.gff.spacenauts.ai.OchitaAI;
import com.gff.spacenauts.ai.PowerUpAI;
import com.gff.spacenauts.ai.PowerUpAI.PowerUpState;
import com.gff.spacenauts.ai.SteadyShooterAI;
import com.gff.spacenauts.ai.SteadyShooterAI.SteadyShooterState;
import com.gff.spacenauts.ai.steering.LinearWavePath;
import com.gff.spacenauts.ai.steering.Parabolic;
import com.gff.spacenauts.ai.steering.RandomWalk;
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
import com.gff.spacenauts.ashley.components.Obstacle;
import com.gff.spacenauts.ashley.components.Player;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Removable;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.components.WorldCamera;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.data.SpawnerData;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.Remove;
import com.gff.spacenauts.listeners.ShotListener;
import com.gff.spacenauts.listeners.death.ActivatePowerUp;
import com.gff.spacenauts.listeners.death.DeathListeners;
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

/**
 * An EntityBuilder is a blue print for entities. It creates new Entities with a given configuration.<p>
 * 
 * It needs a {@link Spacenautsengine} to work; this engine is provided by {@link GameScreen#getengine()} method.<p>
 * 
 * @author Alessio Cali'
 *
 */
public class EntityBuilder {

	//Creatures and their related
	private static final String SPRITE_PLAYER = "player";
	private static final String SPRITE_COOP_PLAYER = "coop";
	private static final String SPRITE_SHIELD = "shield_sprite";
	private static final String SPRITE_DUMMY = "dummy";
	private static final String SPRITE_BIG_DUMMY = "big_dummy";
	private static final String SPRITE_BLACK_INTERCEPTOR = "black_interceptor";
	private static final String SPRITE_GREEN_TANK = "green_tank";
	private static final String SPRITE_BLUE_CRUISER = "blue_cruiser";
	private static final String SPRITE_PURPLE_BOMBER = "purple_bomber";
	private static final String SPRITE_FIRST_LINE = "first_line";
	private static final String SPRITE_ROCK = "rock";
	private static final String SPRITE_EYE = "eye";
	private static final String SPRITE_OCHITA = "ochita";
	private static final String SPRITE_ENEMY_SHIELD = "enemy_shield";
	private static final String SPRITE_OCHITA_BARRIER = "ochita_barrier";

	//Animations
	private static final String ANIM_EXPLOSION_BLUE = "proj_explosion_blue";
	private static final String ANIM_EXPLOSION_YELLOW = "proj_explosion_yellow";
	private static final String ANIM_EXPLOSION_RED = "proj_explosion_red";
	private static final String ANIM_EXPLOSION_SPACESHIP = "spaceship_explosion";
	private static final String ANIM_ROCK_DESTROY = "rock_animation";
	private static final String ANIM_BAT = "bat";
	private static final String ANIM_SLIME = "slime";
	private static final String ANIM_DORVER = "dorver";
	private static final String ANIM_WYVERN = "wyvern";
	private static final String ANIM_ANATHOR = "anathor";
	private static final String ANIM_ALARM = "alarm";
	private static final String ANIM_GUARD = "guard";
	private static final String ANIM_WORKER = "worker";
	private static final String ANIM_PROTECTOR = "protector";

	//Projectiles
	private static final String SPRITE_BULLET_BLUE = "bullet_blue";
	private static final String SPRITE_BULLET_YELLOW = "bullet_yellow";
	private static final String SPRITE_BULLET_RED = "bullet_red";
	private static final String SPRITE_BULLET_GREEN = "bullet_green";
	private static final String SPRITE_BULLET_BALL_BLUE = "bullet_ball_blue";
	private static final String SPRITE_BULLET_BALL_YELLOW = "bullet_ball_yellow";
	private static final String SPRITE_BULLET_BALL_RED = "bullet_ball_red";
	private static final String SPRITE_BULLET_FLAME = "bullet_flame";

	//Overworld powerups
	private static final String SPRITE_PWUP_TRIGUN = "TRIGUN";
	private static final String SPRITE_PWUP_AUTOGUN = "AUTOGUN";
	private static final String SPRITE_PWUP_HEAVYGUN = "HEAVYGUN";
	private static final String SPRITE_PWUP_HEALTH10 = "HEALTH10";
	private static final String SPRITE_PWUP_HEALTH25 = "HEALTH25";
	private static final String SPRITE_PWUP_HEALTH50 = "HEALTH50";
	private static final String SPRITE_PWUP_SHIELD = "SHIELD";

	//Effects
	private static final String SPRITE_AURA_GREEN = "aura_green";
	private static final String SPRITE_AURA_RED = "aura_red";

	//Contain reusable data for entity creation.
	private ObjectMap<String, Method> buildMap = new ObjectMap<String, Method>(20);
	private ObjectMap<String, float[]> vertexMap = new ObjectMap<String, float[]>(20);
	private ObjectMap<String, Sprite> spriteMap = new ObjectMap<String, Sprite>(20);
	private ObjectMap<String, Animation> animationMap = new ObjectMap<String, Animation>(20);

	private AssetManager assets;
	private TextureAtlas textures;
	private GameScreen game;

	public EntityBuilder(GameScreen game){
		this.game = game;
		textures = game.getAssets().get(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		assets = game.getAssets();

		try {
			buildVertexMap();
			buildSpriteMap();
			buildAnimationMap();
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
		buildMap.put("rock", ClassReflection.getDeclaredMethod(clazz, "buildRock"));
		buildMap.put("bat", ClassReflection.getDeclaredMethod(clazz, "buildBat"));
		buildMap.put("slime", ClassReflection.getDeclaredMethod(clazz, "buildSlime"));
		buildMap.put("dorverR", ClassReflection.getDeclaredMethod(clazz, "buildDorverR"));
		buildMap.put("dorverL", ClassReflection.getDeclaredMethod(clazz, "buildDorverL"));
		buildMap.put("wyvern", ClassReflection.getDeclaredMethod(clazz, "buildWyvern"));
		buildMap.put("anathor", ClassReflection.getDeclaredMethod(clazz, "buildAnathor"));
		buildMap.put("towerLaser", ClassReflection.getDeclaredMethod(clazz, "buildTowerLaser"));
		buildMap.put("eye", ClassReflection.getDeclaredMethod(clazz, "buildEye"));
		buildMap.put("alarm", ClassReflection.getDeclaredMethod(clazz, "buildAlarm"));
		buildMap.put("guard", ClassReflection.getDeclaredMethod(clazz, "buildGuard"));
		buildMap.put("worker", ClassReflection.getDeclaredMethod(clazz, "buildWorker"));
		buildMap.put("protector", ClassReflection.getDeclaredMethod(clazz, "buildProtector"));
		buildMap.put("ochita", ClassReflection.getDeclaredMethod(clazz, "buildOchita"));
	}

	/**
	 * The vertex map links any enemy object to its polygon, by searching per ID.
	 * Data is loaded from the internal file data/vertices.data and is parsed by Strings.
	 * 
	 * @throws IOException
	 */
	private void buildVertexMap() throws IOException {
		FileHandle verticesData = Gdx.files.internal(AssetsPaths.DATA_VERTICES);
		BufferedReader reader = new BufferedReader(verticesData.reader());
		String nextLine;

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

					for (int i = 0 ; i < parse2.length ; i++)
						vertices[i] = Float.valueOf(parse2[i]);

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
	private void buildSpriteMap() {
		//Spaceship sprites
		spriteMap.put(SPRITE_PLAYER, textures.createSprite(SPRITE_PLAYER));
		spriteMap.put(SPRITE_COOP_PLAYER, textures.createSprite(SPRITE_COOP_PLAYER));
		spriteMap.put(SPRITE_DUMMY, textures.createSprite(SPRITE_DUMMY));
		spriteMap.put(SPRITE_BIG_DUMMY, textures.createSprite(SPRITE_BIG_DUMMY));
		spriteMap.put(SPRITE_BLACK_INTERCEPTOR, textures.createSprite(SPRITE_BLACK_INTERCEPTOR));
		spriteMap.put(SPRITE_GREEN_TANK, textures.createSprite(SPRITE_GREEN_TANK));
		spriteMap.put(SPRITE_BLUE_CRUISER, textures.createSprite(SPRITE_BLUE_CRUISER));
		spriteMap.put(SPRITE_PURPLE_BOMBER, textures.createSprite(SPRITE_PURPLE_BOMBER));
		spriteMap.put(SPRITE_FIRST_LINE, textures.createSprite(SPRITE_FIRST_LINE));
		spriteMap.put(SPRITE_EYE, textures.createSprite(SPRITE_EYE));
		spriteMap.put(SPRITE_OCHITA, textures.createSprite(SPRITE_OCHITA));
		spriteMap.put(SPRITE_ENEMY_SHIELD, textures.createSprite(SPRITE_ENEMY_SHIELD));
		spriteMap.put(SPRITE_OCHITA_BARRIER, textures.createSprite(SPRITE_OCHITA_BARRIER));

		//Statics
		spriteMap.put(SPRITE_ROCK, textures.createSprite(SPRITE_ROCK));

		//Bullet sprites
		spriteMap.put(SPRITE_BULLET_BLUE, textures.createSprite(SPRITE_BULLET_BLUE));
		spriteMap.put(SPRITE_BULLET_YELLOW, textures.createSprite(SPRITE_BULLET_YELLOW));
		spriteMap.put(SPRITE_BULLET_RED, textures.createSprite(SPRITE_BULLET_RED));
		spriteMap.put(SPRITE_BULLET_BALL_BLUE, textures.createSprite(SPRITE_BULLET_BALL_BLUE));
		spriteMap.put(SPRITE_BULLET_BALL_YELLOW, textures.createSprite(SPRITE_BULLET_BALL_YELLOW));
		spriteMap.put(SPRITE_BULLET_BALL_RED, textures.createSprite(SPRITE_BULLET_BALL_RED));
		spriteMap.put(SPRITE_BULLET_FLAME, textures.createSprite(SPRITE_BULLET_FLAME));
		spriteMap.put(SPRITE_BULLET_GREEN, textures.createSprite(SPRITE_BULLET_GREEN));

		//Powerups
		spriteMap.put(SPRITE_PWUP_TRIGUN, textures.createSprite(SPRITE_PWUP_TRIGUN));
		spriteMap.put(SPRITE_PWUP_AUTOGUN, textures.createSprite(SPRITE_PWUP_AUTOGUN));
		spriteMap.put(SPRITE_PWUP_HEAVYGUN, textures.createSprite(SPRITE_PWUP_HEAVYGUN));
		spriteMap.put(SPRITE_PWUP_HEALTH10, textures.createSprite(SPRITE_PWUP_HEALTH10));
		spriteMap.put(SPRITE_PWUP_HEALTH25, textures.createSprite(SPRITE_PWUP_HEALTH25));
		spriteMap.put(SPRITE_PWUP_HEALTH50, textures.createSprite(SPRITE_PWUP_HEALTH50));
		spriteMap.put(SPRITE_PWUP_SHIELD, textures.createSprite(SPRITE_PWUP_SHIELD));

		//More
		spriteMap.put(SPRITE_SHIELD, textures.createSprite(SPRITE_SHIELD));
		spriteMap.put(SPRITE_AURA_GREEN, textures.createSprite(SPRITE_AURA_GREEN));
		spriteMap.put(SPRITE_AURA_RED, textures.createSprite(SPRITE_AURA_RED));
	}

	/**
	 * Loads all {@link Animation}s and stores them into a {@link HashMap}.
	 * 
	 */
	private void buildAnimationMap() {
		animationMap.put(ANIM_EXPLOSION_BLUE, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_EXPLOSION_BLUE).split(16, 16))));
		animationMap.put(ANIM_EXPLOSION_YELLOW, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_EXPLOSION_YELLOW).split(16, 16))));
		animationMap.put(ANIM_EXPLOSION_RED, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_EXPLOSION_RED).split(16, 16))));
		animationMap.put(ANIM_EXPLOSION_SPACESHIP, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_EXPLOSION_SPACESHIP).split(68, 68))));
		animationMap.put(ANIM_ROCK_DESTROY, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_ROCK_DESTROY).split(64,64))));
		animationMap.put(ANIM_BAT, new Animation(0.15f, extractKeyFrames(textures.findRegion(ANIM_BAT).split(32, 32))));
		animationMap.put(ANIM_SLIME, new Animation(0.15f, extractKeyFrames(textures.findRegion(ANIM_SLIME).split(32, 32))));
		animationMap.put(ANIM_DORVER, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_DORVER).split(180, 144))));
		animationMap.put(ANIM_WYVERN, new Animation(0.07f, extractKeyFrames(textures.findRegion(ANIM_WYVERN).split(220, 144))));
		animationMap.put(ANIM_ANATHOR, new Animation(0.075f, extractKeyFrames(textures.findRegion(ANIM_ANATHOR).split(344, 200))));
		animationMap.put(ANIM_ALARM, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_ALARM).split(96, 96))));
		animationMap.put(ANIM_GUARD, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_GUARD).split(96, 96))));
		animationMap.put(ANIM_WORKER, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_WORKER).split(96, 96))));
		animationMap.put(ANIM_PROTECTOR, new Animation(0.1f, extractKeyFrames(textures.findRegion(ANIM_PROTECTOR).split(96, 96))));
	}

	private Array<TextureRegion> extractKeyFrames(TextureRegion[][] regions) {
		Array<TextureRegion> keyFrames = new Array<TextureRegion>();

		for (TextureRegion[] row : regions) 
			keyFrames.addAll(row);

		return keyFrames;
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
	public Entity buildWorldCamera() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity cameraEntity = engine.createEntity();

		WorldCamera worldCamera = engine.createComponent(WorldCamera.class);
		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle ang = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);

		worldCamera.viewport.setWorldSize(Globals.TARGET_CAMERA_WIDTH, Globals.TARGET_CAMERA_HEIGHT);

		pos.value.set(Globals.STARTING_CAMERA_X, Globals.STARTING_CAMERA_Y);

		vel.value.set(0, Globals.baseCameraSpeed);

		cameraEntity.add(worldCamera).add(pos).add(vel).add(ang).add(angVel);

		return cameraEntity;
	}

	/**
	 * Builds a player entity. The player's speed is set to be the same as the camera's, so to appear solidal to it.<br>
	 * A special death listener is added to the player, namely {@link GameOver}. It triggers the game over sequence on the player's death.<br>
	 * A special AI is also included, which is {@link PowerUpAI}. While not a proper AI, it's still a FSM that handles PowerUps. 
	 * 
	 * @param x player's initial x coordinate.
	 * @param y player's initial y coordinate.
	 * @return the player entity.
	 */
	public Entity buildPlayer(float x, float y){	
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Player player = engine.createComponent(Player.class);
		Friendly friendly = engine.createComponent(Friendly.class);
		FSMAI powerUps = engine.createComponent(FSMAI.class);
		Render render = engine.createComponent(Render.class);
		Gun gun = buildGunNormal();
		Death death = engine.createComponent(Death.class);

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

		hittable.health = Globals.godmode ? 100000000 : 100;
		hittable.maxHealth = hittable.health;
		hittable.listeners.addListener(new DamageAndDie(Families.ENEMY_FAMILY));

		death.listeners.addListener(DeathListener.Commons.GAME_OVER);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));	

		powerUps.fsm =  new PowerUpAI(entity, game.getUI());

		render.sprite = spriteMap.get(SPRITE_PLAYER);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;

		return entity;
	}

	public Entity buildCoopPlayer(float x, float y){	
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Angle angle = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		CoopPlayer player = engine.createComponent(CoopPlayer.class);
		Friendly friendly = engine.createComponent(Friendly.class);
		FSMAI powerUps = engine.createComponent(FSMAI.class);
		Render render = engine.createComponent(Render.class);
		Gun gun = buildGunNormal();
		Death death = engine.createComponent(Death.class);

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

		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));

		powerUps.fsm =  new PowerUpAI(entity, game.getUI());

		render.sprite = spriteMap.get(SPRITE_COOP_PLAYER);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;

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
		SpacenautsEngine engine = GameScreen.getEngine();
		Gun gun = buildGunNormal();

		gun.guns.get(0).bulletDamage = Globals.godmode ? 100000 : 30;
		gun.guns.get(0).bulletImage = spriteMap.get(SPRITE_BULLET_RED);

		DeathListeners dl = gun.guns.get(0).bulletDeathListeners;
		dl.clear();
		dl.addListener(new Remove(engine));
		dl.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));

		return gun;
	}

	/**
	 * Builds a {@link Gun} component for a regular player Gun.
	 * 
	 * @return A regular player Gun.
	 */
	public Gun buildGunNormal() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Gun gun = engine.createComponent(Gun.class);

		gun.guns.add(buildGunDataNormal());

		return gun;
	}

	/**
	 * Builds {@link GunData} for the player's regular gun. It's also used as a base for PowerUp guns.
	 * 
	 * @return a regular gun's data.
	 */
	public GunData buildGunDataNormal(){
		SpacenautsEngine engine = GameScreen.getEngine();
		GunData gunData = Pools.get(GunData.class).obtain();

		gunData.bulletDamage = Globals.godmode ? 10000 : 10;
		gunData.bulletHitListeners.addListener(new Die(Families.ENEMY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(engine));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_BLUE), engine));
		gunData.speed = 10;
		gunData.bulletImage = spriteMap.get(SPRITE_BULLET_BLUE);
		gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
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
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity puEntity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Angle ang = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		Hittable hit = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Render render = engine.createComponent(Render.class);
		Removable rem = engine.createComponent(Removable.class);

		puEntity.add(pos).add(ang).add(body).add(hit).add(death).add(render).add(rem);

		pos.value.set(x, y);
		body.polygon.setVertices(Geometry.copy(vertexMap.get("powerup")));
		body.polygon.setPosition(pos.value.x, pos.value.y);

		hit.listeners.addListener(new Die(Families.FRIENDLY_FAMILY));

		death.listeners.addListener(new ActivatePowerUp(id));
		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_POWERUP, Sound.class)));

		render.sprite = spriteMap.get(id);

		return puEntity;
	}

	/**
	 * Builds a shield used in {@link PowerUpAI}. Its body has
	 * the same shape of the player;
	 * 
	 * @return the shield Entity
	 * @see PowerUpState#SHIELD
	 */
	public Entity buildShield() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity shield = engine.createEntity();
		Entity player = engine.getPlayer();
		Body plBody = Mappers.bm.get(player);

		Position pos = engine.createComponent(Position.class);
		Angle ang = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		Friendly friendly = engine.createComponent(Friendly.class);
		Hittable hit = engine.createComponent(Hittable.class);
		Render render = engine.createComponent(Render.class);

		shield.add(pos).add(ang).add(body).add(hit).add(render).add(friendly);

		body.polygon.setVertices(Geometry.copy(plBody.polygon.getVertices()));
		body.polygon.scale(0.5f);

		render.sprite = spriteMap.get(SPRITE_SHIELD);

		return shield;
	}

	/**
	 * Builds a spawner entity out of the given {@link SpawnerData}.
	 * 
	 * @param data
	 * @return the spawner entity.
	 */
	public Entity buildSpawner(SpawnerData data){
		SpacenautsEngine engine = GameScreen.getEngine();
		Timers timerComponent = engine.createComponent(Timers.class);
		Position spawnerPosition = engine.createComponent(Position.class);

		timerComponent.listeners.add(new Spawn(data));

		spawnerPosition.value.set(data.initialPosition);

		return engine.createEntity().add(timerComponent).add(spawnerPosition);
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
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(collisionDamage)
		.add(hittable).add(death).add(enemy).add(gun).add(rem).add(render).add(ai);

		angle.value = -MathUtils.PI / 2;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("dummy")));
		body.polygon.setRotation(angle.value);

		collisionDamage.damageDealt = 0;

		enemy.score = 20;

		hittable.health = 50;
		hittable.maxHealth = 50;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));

		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.pOffset.set(1, 0);
		gunData.bulletDamage = 1;
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(engine));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
		gunData.speed = 10;
		gunData.bulletImage = spriteMap.get(SPRITE_BULLET_YELLOW);
		gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
		gunData.shotSound = AssetsPaths.SFX_LASER_4;
		gun.guns.add(gunData);

		render.sprite = spriteMap.get(SPRITE_DUMMY);

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
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Boss boss = engine.createComponent(Boss.class);
		Gun gun = engine.createComponent(Gun.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(collisionDamage).add(hittable)
		.add(death).add(enemy).add(boss).add(gun).add(render).add(ai);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("big_dummy")));
		body.polygon.setScale(5f, 2);

		collisionDamage.damageDealt = 3;

		hittable.health = 1000;
		hittable.maxHealth = 1000;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));
		death.listeners.addListener(DeathListener.Commons.VICTORY);

		enemy.score = 500;
		boss.name = "SX-00 : BIG_DUMMY";

		GunData[] gunData = new GunData[3];

		for (int i = 0 ; i < 3 ; i++)
			gunData[i] = buildGunDataBigDummy();

		gunData[0].pOffset.set(7, 0);
		gunData[1].pOffset.set(4, -2.5f);
		gunData[2].pOffset.set(4, 2.5f);

		gun.guns.addAll(gunData);

		render.sprite = spriteMap.get(SPRITE_BIG_DUMMY);

		ai.fsm = new BigDummyAI(entity);

		return entity;

	}

	/**
	 * Builds GunData for Big Dummy.
	 * 
	 * @return Big Dummy's gun data.
	 */
	public GunData buildGunDataBigDummy(){
		SpacenautsEngine engine = GameScreen.getEngine();

		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 1;
		gunData.bulletDeathListeners.addListener(new Remove(engine));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_BLUE), engine));
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletImage = spriteMap.get(SPRITE_BULLET_BALL_BLUE);
		gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
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
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

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
		death.listeners.addListener(new Remove(engine));

		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));

		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 1;
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(engine));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
		gunData.speed = 20;
		gunData.shotSound = AssetsPaths.SFX_LASER_4;
		gunData.bulletImage = spriteMap.get(SPRITE_BULLET_YELLOW);
		gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
		gunData.pOffset.set(2, 0);
		gun.guns.add(gunData);

		render.sprite = spriteMap.get(SPRITE_BLACK_INTERCEPTOR);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;

		ai.fsm = new SteadyShooterAI(entity);	

		return entity;
	}

	/**
	 * Builds a Green Tank. Similar to Black Interceptor in its behavior, a green tank shoots steadily six lasers from its flanks.<br>
	 * See {@link SteadyShooterAI}.
	 * 
	 * @return a Green Tank.
	 */
	public Entity buildGreenTank() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

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

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));

		for (int i = 0 ; i < 6 ; i++) {
			GunData gunData = Pools.get(GunData.class).obtain();
			gunData.bulletDamage = 1;
			gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData.bulletDeathListeners.addListener(new Remove(engine));
			gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
			gunData.speed = 10;
			gunData.shotSound = AssetsPaths.SFX_LASER_4;
			gunData.bulletImage = spriteMap.get(SPRITE_BULLET_YELLOW);
			gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;

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

		render.sprite = spriteMap.get(SPRITE_GREEN_TANK);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;
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
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(rem).add(render).add(ai);

		enemy.score = 10;

		angle.value = - MathUtils.PI / 2;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("blue_cruiser")));
		body.polygon.setRotation(angle.value);

		collisionDamage.damageDealt = 3;

		hittable.listeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));

		render.sprite = spriteMap.get(SPRITE_BLUE_CRUISER);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;

		ai.fsm = new ErraticKamikazeAI(entity);	

		return entity;
	}

	/**
	 * Builds a Purple Bomber. It shoots 4 lasers in orthogonal directions, and rotates at a fixed speed.
	 * 
	 * @return a Purple Bomber.
	 */
	public Entity buildPurpleBomber() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

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

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));

		GunData[] gunData = new GunData[4];

		for (int i = 0 ; i < 4 ; i++) {
			gunData[i] = Pools.get(GunData.class).obtain();
			gunData[i].bulletDamage = 1;
			gunData[i].bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData[i].bulletDeathListeners.addListener(new Remove(engine));
			gunData[i].bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
			gunData[i].speed = 20;
			gunData[i].shotSound = AssetsPaths.SFX_LASER_4;
			gunData[i].bulletImage = spriteMap.get(SPRITE_BULLET_YELLOW);
			gunData[i].scaleX = gunData[i].scaleY = Globals.UNITS_PER_PIXEL;
			gunData[i].aOffset = i * MathUtils.PI / 2;
		}

		gun.guns.addAll(gunData);

		render.sprite = spriteMap.get(SPRITE_PURPLE_BOMBER);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;

		ai.fsm = new SteadyShooterAI(entity);	

		return entity;
	}

	/**
	 * Builds First Line, Level 1's boss. For its behavioral pattern please see {@link FirstLineAI}.
	 * 
	 * @return First Line.
	 */
	public Entity buildFirstLine () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();
		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Boss boss = engine.createComponent(Boss.class);
		Gun gun = engine.createComponent(Gun.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(collisionDamage).add(hittable)
		.add(death).add(enemy).add(boss).add(gun).add(render).add(ai);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("first_line")));
		body.polygon.setScale(1.2f, 2.2f);

		collisionDamage.damageDealt = 3;

		hittable.health = 2000;
		hittable.maxHealth = 2000;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_EXPLOSION, Sound.class)));
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_SPACESHIP), engine));
		death.listeners.addListener(DeathListener.Commons.VICTORY);

		enemy.score = 500;

		boss.name = "SX-01 : FIRST_LINE";

		render.sprite = spriteMap.get(SPRITE_FIRST_LINE);
		render.scaleX = render.scaleY = Globals.UNITS_PER_PIXEL;

		ai.fsm = new FirstLineAI(entity);

		return entity;
	}

	/**
	 * Builds GunData structures for First Line while it's from 100% to 70% health.
	 * 
	 * @return GunData for FirstLine, from 100% to 70%
	 */
	public GunData[] buildGunDataFL100To70 () {
		SpacenautsEngine engine = GameScreen.getEngine();
		GunData[] gunData = new GunData[3];

		for (int i = 0 ; i < gunData.length ; i++) {
			gunData[i] = Pools.get(GunData.class).obtain();
			gunData[i].aOffset = -MathUtils.PI / 2;
			gunData[i].bulletDamage = 2;
			gunData[i].bulletImage = spriteMap.get(SPRITE_BULLET_BALL_YELLOW);
			gunData[i].shotSound = AssetsPaths.SFX_LASER_4;
			gunData[i].speed = 7;
			gunData[i].bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData[i].bulletDeathListeners.addListener(new Remove(engine));
			gunData[i].bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
			gunData[i].gunShotListeners.addListener(new RandomizeAngle(-MathUtils.PI / 6, MathUtils.PI / 6));
		}

		gunData[0].pOffset.set(0, -1);
		gunData[1].pOffset.set(-2.5f, -1);
		gunData[2].pOffset.set(2.5f, -1);

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
		SpacenautsEngine engine = GameScreen.getEngine();
		GunData[] gunData = new GunData[6];

		SteeringInitializer leftInit = new SteeringInitializer() {
			@Override
			public void init (SteeringBehavior<Vector2> behavior) {
				Parabolic pBehavior = (Parabolic)behavior;
				pBehavior.setHorizontalAccel(4.5f);
			}
		};

		SteeringInitializer rightInit = new SteeringInitializer() {
			@Override
			public void init (SteeringBehavior<Vector2> behavior) {
				Parabolic pBeahavior = (Parabolic)behavior;
				pBeahavior.setHorizontalAccel(-4.5f);
			}
		};

		for (int i = 0 ; i < gunData.length ; i++) {
			int invert = i < 3 ? -1 : +1;
			gunData[i] = Pools.get(GunData.class).obtain();
			gunData[i].aOffset = invert * MathUtils.PI / 6;
			gunData[i].bulletDamage = 2;
			gunData[i].bulletImage = spriteMap.get(SPRITE_BULLET_BALL_YELLOW);
			gunData[i].shotSound = AssetsPaths.SFX_LASER_4;
			gunData[i].speed = 10;
			gunData[i].bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData[i].bulletDeathListeners.addListener(new Remove(engine));
			gunData[i].bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
			gunData[i].gunShotListeners.addListener(new RandomizeAngle(-MathUtils.PI / 6, MathUtils.PI / 6));

			Parabolic par = new Parabolic(null);
			par.setHorizontalAccel(-invert * 5);
			ApplySteering as;

			if (invert == -1) 
				as = new ApplySteering(Parabolic.class, leftInit);

			else 
				as = new ApplySteering(Parabolic.class, rightInit);

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

	/**
	 * A rock. Yeah. It can be destroyed but it's solid so it 
	 * can be placed as an obstacle.
	 * 
	 * @return
	 */
	public Entity buildRock () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Angle ang = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Obstacle obstacle = engine.createComponent(Obstacle.class);
		Render render = engine.createComponent(Render.class);

		entity.add(pos).add(ang).add(body).add(hittable).add(death).add(enemy).add(obstacle).add(render);

		ang.value = 0;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("rock")));
		body.polygon.setRotation(ang.getAngleDegrees());

		hittable.health = 50;
		hittable.maxHealth = 50;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		hittable.listeners.addListener(new PushAway(Families.FRIENDLY_FAMILY, Families.ENEMY_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_ROCK_DESTROY), engine));

		enemy.score = 10;

		render.sprite = spriteMap.get(SPRITE_ROCK);
		render.scaleX = render.scaleY = 1/32f;

		return entity;
	}

	/**
	 * A bat that moves following {@link ErraticKamikazeAI}, pretty much the same as a
	 * Blue Cruiser. Of course there are some difference in appearance but the
	 * concept is the same.
	 * 
	 * @return
	 */
	public Entity buildBat () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(rem).add(render).add(ai);

		enemy.score = 10;

		angle.value = 0;

		body.polygon.setVertices(Geometry.copy(vertexMap.get("bat")));
		body.polygon.setRotation(angle.value);

		collisionDamage.damageDealt = 3;

		hittable.listeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new ReleaseAnimation(null, engine));

		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_BAT);
		render.animation.setPlayMode(PlayMode.LOOP);
		render.scaleX = render.scaleY = 1/20f;

		ai.fsm = new ErraticKamikazeAI(entity);	

		return entity;
	}

	/**
	 * A slime that shoots lasers in 8 directions.
	 * 
	 * @return
	 */
	public Entity buildSlime() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Angle angle = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(angle).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 10;

		angle.value = 0;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("slime")));
		body.polygon.setRotation(angle.value);

		collisionDamage.damageDealt = 1;

		hittable.health = 30;
		hittable.maxHealth = 30;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));

		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new ReleaseAnimation(null, engine));

		for (int i = 0 ; i < 8 ; i++){
			GunData gunData = Pools.get(GunData.class).obtain();
			float aOffset = i * MathUtils.PI / 4;
			
			gunData.bulletDamage = 1;
			gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData.bulletDeathListeners.addListener(new Remove(engine));
			gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
			gunData.speed = 20;
			gunData.bulletImage = spriteMap.get(SPRITE_BULLET_BALL_YELLOW);
			gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;		
			gunData.pOffset.set(MathUtils.cos(aOffset), MathUtils.sin(aOffset));
			gunData.aOffset = aOffset;
			gun.guns.add(gunData);
		}

		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_SLIME);
		render.animation.setPlayMode(PlayMode.LOOP);
		render.scaleX = render.scaleY = 1/20f;
		
		ai.fsm = new SteadyShooterAI(entity);	

		return entity;
	}

	/**
	 * A big angry monster that spits fire down and right from it. This builder makes it face right.
	 * 
	 * @return
	 */
	public Entity buildDorverR() {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Angle angle = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(angle).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 50;
		
		angle.value = 0;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("dorver")));
		body.polygon.setRotation(angle.value);
		body.polygon.setScale(1.5f, 1);
		
		collisionDamage.damageDealt = 1;
		
		hittable.health = 100;
		hittable.maxHealth = 100;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		
		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new ReleaseAnimation(null, engine));
		
		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 3;
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(engine));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
		gunData.speed = 10;
		gunData.bulletImage = spriteMap.get(SPRITE_BULLET_FLAME);
		gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
		
		for (int i = 0 ; i < 3 ; i++){
			GunData curGun = gunData.clone();
			float aOffset = - i * MathUtils.PI / 5;
			curGun.pOffset.set(MathUtils.cos(aOffset), MathUtils.sin(aOffset)).scl(2.5f);
			curGun.aOffset = aOffset;
			gun.guns.add(curGun);
		}
		
		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_DORVER);
		render.animation.setPlayMode(PlayMode.LOOP);
		render.scaleX = render.scaleY = 1/25f;
		
		ai.fsm = new SteadyShooterAI(entity);	

		return entity;
	}

	/**
	 * Same as {@link #buildDorverR()}, but faces left.
	 * Temporary measure until I set something to specify orientation
	 * from the level markers.
	 * 
	 * @return
	 */
	public Entity buildDorverL() {
		Entity entity = buildDorverR();
		Render r = Mappers.rm.get(entity);
		Gun g = Mappers.gm.get(entity);

		r.scaleX *= -1;

		for (GunData gData : g.guns) {
			gData.pOffset.x *= -1;
			gData.aOffset = MathUtils.PI - gData.aOffset;
		}

		return entity;
	}

	/**
	 * A wyvern that spits fire in front of it.
	 * Also used as a minion for Anathor.
	 * 
	 * @return
	 */
	public Entity buildWyvern () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 100;
		
		angle.value = 0;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("wyvern")));
		body.polygon.setRotation(angle.value);
		
		collisionDamage.damageDealt = 2;
		
		hittable.health = 50;
		hittable.maxHealth = 50;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		
		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(new ReleaseAnimation(engine));
		
		GunData gunData = Pools.get(GunData.class).obtain();
		gunData.bulletDamage = 5;
		gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
		gunData.bulletDeathListeners.addListener(new Remove(engine));
		gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
		gunData.speed = 10;
		gunData.bulletImage = spriteMap.get(SPRITE_BULLET_FLAME);
		gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
		gunData.pOffset.set(0, -1);
		gunData.aOffset = - MathUtils.PI / 2;
		gun.guns.add(gunData);
		
		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_WYVERN);
		render.animation.setPlayMode(PlayMode.LOOP);
		render.scaleX = render.scaleY = 1.7f * Globals.UNITS_PER_PIXEL;
		
		ai.fsm = new SteadyShooterAI(entity);	

		return entity;		
	}

	/**
	 * Sets up Anathor, the boss from Level 2. More details on its behavior on
	 * {@link AnathorAI}.
	 * 
	 * @return
	 */
	public Entity buildAnathor () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Boss boss = engine.createComponent(Boss.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body)
		.add(collisionDamage).add(hittable).add(death).add(enemy).add(boss)
		.add(gun).add(rem).add(render).add(ai);

		enemy.score = 500;
		boss.name = "ARCHDRAKE - ANATHOR";
		
		angle.value = 0;
		body.polygon.setVertices(Geometry.copy(vertexMap.get("anathor")));
		body.polygon.setRotation(angle.value);
		
		collisionDamage.damageDealt = 8;
		
		hittable.health = 1750;
		hittable.maxHealth = 1750;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		
		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(DeathListener.Commons.VICTORY);
		death.listeners.addListener(new ReleaseAnimation(engine));
		
		for (int i = 0 ; i < 3 ; i++) {
			GunData gunData = Pools.get(GunData.class).obtain();
			gunData.bulletDamage = 5;
			gunData.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gunData.bulletDeathListeners.addListener(new Remove(engine));
			gunData.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
			gunData.speed = 10;
			gunData.bulletImage = spriteMap.get(SPRITE_BULLET_FLAME);
			gunData.scaleX = gunData.scaleY = Globals.UNITS_PER_PIXEL;
			gunData.aOffset = - (i + 1) * MathUtils.PI / 4;
			gunData.pOffset.set(MathUtils.cos(gunData.aOffset), MathUtils.sin(gunData.aOffset)).scl(2);
			gun.guns.add(gunData);
		}
		
		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_ANATHOR);
		render.animation.setPlayMode(PlayMode.LOOP);
		render.scaleX = render.scaleY = 1.7f * Globals.UNITS_PER_PIXEL;
		
		ai.fsm = new AnathorAI(entity);	

		return entity;	
	}

	/**
	 * Two bodyless lasers that shoot at +- PI / 8. They're used to make towers
	 * from level 3 appear as they're the one shooting. It can't be killed tough.
	 * 
	 * @return
	 */
	public Entity buildTowerLaser () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Angle ang = engine.createComponent(Angle.class);
		Gun gun = engine.createComponent(Gun.class);
		FSMAI ai = engine.createComponent(FSMAI.class);
		Enemy enemy = engine.createComponent(Enemy.class);

		entity.add(pos).add(ang).add(gun).add(ai).add(enemy);

		for (int i = 0 ; i < 2 ; i++) {
			GunData gdata = Pools.get(GunData.class).obtain();
			gdata.bulletDamage = 1;
			gdata.aOffset = MathUtils.PI / 8 - i * MathUtils.PI / 4;
			gdata.bulletImage = spriteMap.get(SPRITE_BULLET_BALL_YELLOW);
			gdata.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gdata.bulletDeathListeners.addListener(new Remove(engine));
			gdata.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_YELLOW), engine));
			gdata.scaleX = gdata.scaleY = Globals.UNITS_PER_PIXEL;
			gdata.shotSound = AssetsPaths.SFX_LASER_4;
			gdata.speed = 8;
			gun.guns.add(gdata);
		}

		ai.fsm = new SteadyShooterAI(entity);

		return entity;
	}

	/**
	 * Basically a reskin of purple bomber. Not much effort here.
	 * 
	 * @return
	 */
	public Entity buildEye () {
		Entity entity = buildPurpleBomber();
		Render render = Mappers.rm.get(entity);

		render.sprite = spriteMap.get(SPRITE_EYE);
		return entity;
		//And there you go. New enemy. Zero effort. Shame on me.
	}

	/**
	 * Another reskin... of Black Interceptor.
	 * 
	 * @return
	 */
	public Entity buildAlarm () {
		Entity entity = buildBlackInterceptor();

		Gun gun = Mappers.gm.get(entity);
		Body body = Mappers.bm.get(entity);
		Render render = Mappers.rm.get(entity);

		gun.guns.get(0).pOffset.set(0, -1);
		gun.guns.get(0).aOffset = - MathUtils.PI / 2;

		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_ALARM);
		render.animation.setPlayMode(PlayMode.LOOP);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("alarm")));

		return entity;
	}

	/**
	 * A robot that shoots random lasers that spread into 5 more to its left.
	 * Black Interceptor is used as a base.
	 * 
	 * @return
	 */
	public Entity buildGuard () {
		Entity entity = buildBlackInterceptor();

		Gun gun = Mappers.gm.get(entity);
		Body body = Mappers.bm.get(entity);
		Render render = Mappers.rm.get(entity);
		Hittable hit = Mappers.hm.get(entity);

		gun.guns.get(0).pOffset.set(0, - 0.5f);
		gun.guns.get(0).aOffset = - MathUtils.PI;
		gun.guns.get(0).speed = 5;
		gun.guns.get(0).gunShotListeners.addListener(new RandomizeAngle(- MathUtils.PI / 8, MathUtils.PI / 8));
		gun.guns.get(0).gunShotListeners.addListener(new Propagate(1f, 5));

		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_GUARD);
		render.animation.setPlayMode(PlayMode.LOOP);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("guard")));

		hit.health = 60;
		hit.maxHealth = 60;

		return entity;
	}

	/**
	 * An android that shoots lasers to its right that propagate into 5 more.
	 * Black interceptor is used as a base.
	 * 
	 * @return
	 */
	public Entity buildWorker () {
		Entity entity = buildBlackInterceptor();

		Gun gun = Mappers.gm.get(entity);
		Body body = Mappers.bm.get(entity);
		Render render = Mappers.rm.get(entity);
		Hittable hit = Mappers.hm.get(entity);

		GunData gdata = gun.guns.get(0);
		gdata.pOffset.set(0.5f, 0);
		gdata.aOffset = 0;
		gdata.speed = 5;
		gdata.bulletImage = spriteMap.get(SPRITE_BULLET_BALL_YELLOW);
		gdata.gunShotListeners.addListener(new Propagate(1f, 5));

		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_WORKER);
		render.animation.setPlayMode(PlayMode.LOOP);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("worker")));

		hit.health = 60;
		hit.maxHealth = 60;

		return entity;
	}

	/**
	 * A robot that shoots random laser to its right.
	 * 
	 * @return
	 */
	public Entity buildProtector () {
		Entity entity = buildBlackInterceptor();

		Gun gun = Mappers.gm.get(entity);
		Body body = Mappers.bm.get(entity);
		Render render = Mappers.rm.get(entity);
		Hittable hit = Mappers.hm.get(entity);

		gun.guns.get(0).pOffset.set(0, 0.5f);
		gun.guns.get(0).gunShotListeners.addListener(new RandomizeAngle(-MathUtils.PI / 8, MathUtils.PI / 8));

		render.sprite = new Sprite();
		render.animation = animationMap.get(ANIM_PROTECTOR);
		render.animation.setPlayMode(PlayMode.LOOP);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("protector")));

		hit.health = 60;
		hit.maxHealth = 60;

		return entity;
	}

	/**
	 * Inits Ochita Weapon, the boss from Level 3. For more details check {@link OchitaAI}.
	 * 
	 * @return
	 */
	public Entity buildOchita () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle angle = engine.createComponent(Angle.class);
		AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
		Steering steering = engine.createComponent(Steering.class);
		Body body = engine.createComponent(Body.class);
		CollisionDamage collisionDamage = engine.createComponent(CollisionDamage.class);
		Hittable hittable = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Enemy enemy = engine.createComponent(Enemy.class);
		Boss boss = engine.createComponent(Boss.class);
		Gun gun = engine.createComponent(Gun.class);
		Removable rem = engine.createComponent(Removable.class);
		Render render = engine.createComponent(Render.class);
		FSMAI ai = engine.createComponent(FSMAI.class);
		Timers timers = engine.createComponent(Timers.class);

		entity.add(pos).add(vel).add(angle).add(angVel).add(body).add(steering)
		.add(collisionDamage).add(hittable).add(death).add(enemy).add(boss)
		.add(gun).add(rem).add(render).add(ai).add(timers);

		enemy.score = 500;
		
		boss.name = "ANCIENT CANNON - OCHITA WEAPON";
		
		body.polygon.setVertices(Geometry.copy(vertexMap.get("ochita")));
		body.polygon.setRotation(angle.value);
		
		steering.adapter = SteeringMechanism.getFor(entity);
		steering.adapter.setMaxLinearAcceleration(10);
		steering.adapter.setMaxLinearSpeed(3);
		
		collisionDamage.damageDealt = 8;
		
		hittable.health = 1000;
		hittable.maxHealth = 1000;
		hittable.listeners.addListener(new DamageAndDie(Families.FRIENDLY_FAMILY));
		
		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(DeathListener.Commons.INCREASE_SCORE);
		death.listeners.addListener(DeathListener.Commons.VICTORY);
		death.listeners.addListener(new ReleaseAnimation(engine));
		
		for (int i = 0 ; i < 3 ; i++) {
			GunData gdata = Pools.get(GunData.class).obtain();
			gdata.aOffset = 0;
			gdata.bulletDamage = 5;
			gdata.bulletImage = spriteMap.get(SPRITE_BULLET_YELLOW);
			gdata.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gdata.bulletDeathListeners.addListener(new Remove(engine));
			gdata.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
			gdata.shotSound = AssetsPaths.SFX_LASER_4;
			gdata.speed = 7;
			gun.guns.add(gdata);
		}
		
		gun.guns.get(0).pOffset.set(4, 0.4f);
		gun.guns.get(1).pOffset.set(3.5f, -0.4f);
		gun.guns.get(2).pOffset.set(3.5f, 1.2f);

		render.sprite = spriteMap.get(SPRITE_OCHITA);
		render.scaleX = render.scaleY = 2.5f * Globals.UNITS_PER_PIXEL;
		
		ai.fsm = new OchitaAI(entity);	

		return entity;	
	}

	/**
	 * Used for shields protecting enemies, but it's really used by Ochita Weapon only as of now.
	 * Works the same the player's shield, with an entity surrounding the user and an AI synching
	 * its position.
	 * 
	 * @param enemy
	 * @return
	 */
	public Entity buildEnemyShield (Entity enemy) {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity shield = engine.createEntity();
		Body enBody = Mappers.bm.get(enemy);

		Position pos = engine.createComponent(Position.class);
		Angle ang = engine.createComponent(Angle.class);
		Body body = engine.createComponent(Body.class);
		Enemy enemyComponent = engine.createComponent(Enemy.class);
		Hittable hit = engine.createComponent(Hittable.class);
		Render render = engine.createComponent(Render.class);

		shield.add(pos).add(ang).add(body).add(hit).add(render).add(enemyComponent);

		body.polygon.setVertices(Geometry.copy(enBody.polygon.getVertices()));
		body.polygon.scale(0.5f);
		
		render.sprite = spriteMap.get(SPRITE_ENEMY_SHIELD);

		return shield;
	}

	/**
	 * Builds Ochita Minions for the 80% mark. They're the same as Eyes but
	 * slightly sturdier and shoot in 6 directions rather than 4.
	 * 
	 * @return
	 */
	public Entity buildOchitaMinion1 () {
		Entity e = buildEye();

		Gun gun = Mappers.gm.get(e);
		Hittable hit = Mappers.hm.get(e);

		GunData base = gun.guns.first();

		GunData data = base.clone();
		data.aOffset = MathUtils.PI / 4;
		gun.guns.add(data);
		
		data = base.clone();
		data.aOffset = MathUtils.PI * 5 / 4;
		gun.guns.add(data);

		hit.health = 50;
		hit.maxHealth = 50;

		return e;
	}

	/**
	 * Builds Ochita Minions for the last phase. They're the same as Eyes
	 * but use the {@link RandomWalk} behavior.
	 * 
	 * @return
	 */
	public Entity buildOchitaMinion2 () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity e = buildEye();

		Steering steering = engine.createComponent(Steering.class);
		
		steering.adapter = SteeringMechanism.getFor(e);
		steering.adapter.setMaxLinearAcceleration(10);
		steering.adapter.setMaxLinearSpeed(3);
		steering.behavior = new RandomWalk(steering.adapter);
		
		e.add(steering);

		return e;
	}

	/**
	 * The tiny barrier that saves the player from the death laser in phase 3.
	 * It's taken from the Level 3 tileset and is short more than an obstacle.
	 * While it has a SteadyShooterAI it's done only to exploit the {@link SteadyShooterState#REACH REACH}
	 * state. Ehy, if Bethesda made a train helmet why can't I do that?
	 * 
	 * @return
	 */
	public Entity buildOchitaBarrier () {
		SpacenautsEngine engine = GameScreen.getEngine();
		Entity entity = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Velocity vel = engine.createComponent(Velocity.class);
		Angle ang = engine.createComponent(Angle.class);
		AngularVelocity av = engine.createComponent(AngularVelocity.class);
		Body body = engine.createComponent(Body.class);
		Render render = engine.createComponent(Render.class);
		Hittable hit = engine.createComponent(Hittable.class);
		Death death = engine.createComponent(Death.class);
		Obstacle obstacle = engine.createComponent(Obstacle.class);
		FSMAI ai = engine.createComponent(FSMAI.class);

		entity.add(pos).add(vel).add(ang).add(av)
		.add(body).add(render).add(hit).add(death)
		.add(obstacle).add(ai);

		body.polygon.setVertices(Geometry.copy(vertexMap.get("ochita_barrier")));
		
		render.sprite = spriteMap.get(SPRITE_OCHITA_BARRIER);
		
		hit.listeners.addListener(new PushAway(Families.FRIENDLY_FAMILY, Families.ENEMY_FAMILY));
		
		death.listeners.addListener(new Remove(engine));
		death.listeners.addListener(new ReleaseAnimation(engine));

		//Temporary measure just to get the "REACH" state. Ofc there are no guns.		
		ai.fsm = new SteadyShooterAI(entity);


		return entity;
	}

	/**
	 * Just an on-screen effect that marks the Green/Red Ochita debuff. Nothing more
	 * than a green/red square scaled to cover the whole screen. The AI takes care
	 * of invoking this method and then tear the entity down to call its death
	 * listeners, who are similar to a PowerUp being taken.
	 * 
	 * @param green
	 * @return
	 */
	public Entity buildAura(boolean green) {
		SpacenautsEngine engine = GameScreen.getEngine();

		Entity e = engine.createEntity();

		Position pos = engine.createComponent(Position.class);
		Render r = engine.createComponent(Render.class);
		Death d = engine.createComponent(Death.class);
		
		String powerup = green ? "OCHITA_GREEN" : "OCHITA_RED";

		e.add(pos).add(r).add(d);

		r.sprite = green ? spriteMap.get(SPRITE_AURA_GREEN) : spriteMap.get(SPRITE_AURA_RED);
		r.scaleX = r.scaleY = 30;
				
		d.listeners.addListener(new ReleaseAnimation(engine));
		d.listeners.addListener(new ActivatePowerUp(powerup));
		d.listeners.addListener(new EmitSound(assets.get(AssetsPaths.SFX_POWERUP, Sound.class)));
		d.listeners.addListener(new Remove(engine));

		return e;
	}

	/**
	 * The two guns that pair with the red/green debuff mechanic. More on that on
	 * {@link OchitaAI}.
	 * 
	 * @return
	 */
	public GunData[] buildOchitaRedGreenGuns () {
		final SpacenautsEngine engine = GameScreen.getEngine();
		
		GunData[] guns = new GunData[2];
		
		for (int i = 0 ; i < 2 ; i++) {
			GunData gdata = Pools.get(GunData.class).obtain();
			gdata.aOffset = 0;
			gdata.bulletDamage = 5;
			gdata.bulletImage = spriteMap.get(SPRITE_BULLET_YELLOW);
			gdata.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gdata.bulletDeathListeners.addListener(new Remove(engine));
			gdata.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
			gdata.shotSound = AssetsPaths.SFX_LASER_4;
			gdata.speed = 7;
			gdata.gunShotListeners.addListener(new ShotListener () {

				/**
				 * Modifies the laser on a 50:50 chance. Depending on the color
				 * and the player's debuff, it will either healr or damage the player.
				 */
				@Override
				public void onShooting(Entity gun, Entity bullet) {
					Render r = Mappers.rm.get(bullet);
					CollisionDamage cd = Mappers.cdm.get(bullet);
					PowerUpAI powerup = (PowerUpAI)Mappers.aim.get(engine.getPlayer()).fsm;

					if (MathUtils.randomBoolean()) {
						//Green
						r.sprite = spriteMap.get(SPRITE_BULLET_GREEN);
						
						if (powerup.getCurrentState() == PowerUpState.OCHITA_GREEN)
							cd.damageDealt *= -0.75;
						
						else
							cd.damageDealt *= 2;
					} 
					
					else {
						//Red
						r.sprite = spriteMap.get(SPRITE_BULLET_RED);
						
						if (powerup.getCurrentState() == PowerUpState.OCHITA_RED)
							cd.damageDealt *= -0.75;
						
						else
							cd.damageDealt *= 2;
					}
				}	
			});

			gdata.gunShotListeners.addListener(new RandomizeAngle(-MathUtils.PI / 7, MathUtils.PI / 7));
			guns[i] = gdata;
		}
		
		guns[0].pOffset.set(3.5f, -0.4f);
		guns[1].pOffset.set(3.5f, 1.2f);

		return guns;
	}

	/**
	 * The two death lasers that aim to trap the player unless he gets the boss down to 20%.
	 * 
	 * @return
	 */
	public GunData[] buildOchitaCrossLasers() {
		SpacenautsEngine engine = GameScreen.getEngine();
		GunData[] guns = new GunData[2];

		for (int i = 0 ; i < 2 ; i++) {
			GunData gdata = Pools.get(GunData.class).obtain();
			gdata.aOffset = 0;
			gdata.bulletDamage = 30;
			gdata.bulletImage = spriteMap.get(SPRITE_BULLET_RED);
			gdata.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gdata.bulletDeathListeners.addListener(new Remove(engine));
			gdata.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
			gdata.shotSound = AssetsPaths.SFX_LASER_4;
			gdata.speed = 7;
			guns[i] =  gdata;
		}
		guns[0].pOffset.set(3.5f, -0.4f);
		guns[0].aOffset = - MathUtils.PI / 2;
		guns[1].pOffset.set(3.5f, 1.2f);
		guns[1].aOffset = MathUtils.PI / 2;

		return guns;
	}

	/**
	 * The three guns that shoot spread bullets in the final phase. They spread into 8 more projectiles.
	 * 
	 * @return
	 */
	public GunData[] buildOchitaSpreadGun () {
		SpacenautsEngine engine = GameScreen.getEngine();
		GunData[] guns = new GunData[3];

		for (int i = 0 ; i < 3 ; i++) {
			GunData gdata = Pools.get(GunData.class).obtain();
			gdata.aOffset = 0;
			gdata.bulletDamage = 5;
			gdata.bulletImage = spriteMap.get(SPRITE_BULLET_BALL_YELLOW);
			gdata.bulletHitListeners.addListener(new Die(Families.FRIENDLY_FAMILY, Families.OBSTACLE_FAMILY));
			gdata.bulletDeathListeners.addListener(new Remove(engine));
			gdata.bulletDeathListeners.addListener(new ReleaseAnimation(animationMap.get(ANIM_EXPLOSION_RED), engine));
			gdata.gunShotListeners.addListener(new RandomizeAngle(- MathUtils.PI / 7, MathUtils.PI / 7));
			gdata.gunShotListeners.addListener(new Propagate(0.6f, 8));
			gdata.shotSound = AssetsPaths.SFX_LASER_4;
			gdata.speed = 7;
			guns[i] = gdata;
		}
		
		guns[0].pOffset.set(4, 0.4f);
		guns[1].pOffset.set(3.5f, -0.4f);
		guns[2].pOffset.set(3.5f, 1.2f);

		return guns;
	}

	public ObjectMap<String, Sprite> getSpriteCache() {
		return spriteMap;
	}

	public Entity buildNull(){
		Logger.log(LogLevel.WARNING, this.toString(), "Null entity created");
		return GameScreen.getEngine().createEntity();
	}
}
