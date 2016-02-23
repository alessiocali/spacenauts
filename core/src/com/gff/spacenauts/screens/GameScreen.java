package com.gff.spacenauts.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.AudioManager;
import com.gff.spacenauts.Controls;
import com.gff.spacenauts.Level;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.ashley.EntityBuilder;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.systems.AISystem;
import com.gff.spacenauts.ashley.systems.CameraSystem;
import com.gff.spacenauts.ashley.systems.CollisionSystem;
import com.gff.spacenauts.ashley.systems.DialogSystem;
import com.gff.spacenauts.ashley.systems.HitSystem;
import com.gff.spacenauts.ashley.systems.ImmunitySystem;
import com.gff.spacenauts.ashley.systems.MovementSystem;
import com.gff.spacenauts.ashley.systems.MultiplayerSystem;
import com.gff.spacenauts.ashley.systems.PhysicsSystem;
import com.gff.spacenauts.ashley.systems.RemovalSystem;
import com.gff.spacenauts.ashley.systems.RenderingSystem;
import com.gff.spacenauts.ashley.systems.ShootingSystem;
import com.gff.spacenauts.ashley.systems.SteeringSystem;
import com.gff.spacenauts.ashley.systems.TimerSystem;
import com.gff.spacenauts.data.LevelData;
import com.gff.spacenauts.screens.LoadingScreen.Loadable;
import com.gff.spacenauts.ui.GameUI;
import com.gff.spacenauts.ui.LevelSelecter.LevelSelectSet;

/**
 * The game's main screen. It manages all required resources, from assets to UI to the engine.
 * 
 * @author Alessio Cali'
 *
 */
public class GameScreen extends ScreenAdapter implements Loadable {

	private static SpacenautsEngine engine;
	private static EntityBuilder entityBuilder;

	private Game game;
	private Screen nextScreen;
	private Level currentLevel;
	private String mapFile;
	private InputMultiplexer input;
	private Controls controls;
	private AssetManager assets;
	private AudioManager audio;
	private GameUI ui;
	private boolean playing = true;
	private final boolean multiplayer;

	public GameScreen (String mapFile, Game game) {
		this(mapFile, game, false);
	}

	public GameScreen (String mapFile, Game game, boolean multiplayer) {
		this.mapFile = mapFile;
		this.game = game;
		this.multiplayer = multiplayer;
	}

	@Override
	public void show () {
		Gdx.input.setCatchBackKey(true);
		LevelData lData = LevelData.loadFromMap(assets.get(mapFile, TiledMap.class));
		GameOverScreen gameOver;

		//Set next screen
		if (multiplayer) {
			InitialScreen initial = new InitialScreen(game);
			nextScreen = new VictoryScreen(new LoadingScreen(initial, game, initial), game);
			gameOver = new GameOverScreen(null, game);
		} else {
			gameOver = new GameOverScreen(mapFile, game);
			if (lData.nextMap != null) {
				if (lData.nextMap.equals("MAIN_MENU")) { 
					InitialScreen initial = new InitialScreen(game);
					nextScreen = new VictoryScreen(new LoadingScreen(initial, game, initial), game);
				}
				else if (lData.nextMap.equals("ENDING")) {
					InitialScreen initial = new InitialScreen(game);
					LoadingScreen loader = new LoadingScreen(initial, game, initial);
					NarrativeScreen narrative = new NarrativeScreen("ending", loader, game);
					nextScreen = new VictoryScreen(narrative, game);
				}
				else {
					LevelSelectSet nextLevelSet = LevelSelectSet.forMap(lData.nextMap);
					if (nextLevelSet == null) throw new GdxRuntimeException("No LevelSet found for map: " + lData.nextMap);
					GameScreen nextLevelScreen = new GameScreen(nextLevelSet.getMapString(), game, false);
					LoadingScreen loader = new LoadingScreen(nextLevelScreen, game, nextLevelScreen);
					if (nextLevelSet.getCutscene() != null) {
						nextScreen = new VictoryScreen(new NarrativeScreen(nextLevelSet.getCutscene(), loader, game), game);
					}
					else nextScreen = new VictoryScreen(loader, game);
					
					((VictoryScreen)nextScreen).setUnlock(nextLevelSet.getPosition());
				}
			} else {
				InitialScreen initial = new InitialScreen(game);
				nextScreen = new VictoryScreen(new LoadingScreen(initial, game, initial), game);
			}
		}
		
		engine  = new SpacenautsEngine(this, gameOver, nextScreen, multiplayer, 100, 1000, 100, 1500);	
		initUI();
		currentLevel = new Level(lData);
		entityBuilder = new EntityBuilder(this);
		currentLevel.build();
		initSystems();
		initControls();
		initAudio();
	}

	@Override
	public void hide(){
		Gdx.input.setCatchBackKey(false);
		audio.stopAll();
		Gdx.input.setInputProcessor(null);
		if (multiplayer) engine.sendCoop("CLOSE");
		dispose();
	}

	@Override
	public void resize(int width, int height){
		Entity camera = engine.getCamera();

		if (camera != null)
			Mappers.wcm.get(camera).viewport.update(width, height);

		if (ui != null)
			ui.resize(width, height);
	}

	@Override
	public void render (float delta) {	
		if (playing) {
			controls.update(delta);
			audio.update(delta);
			engine.update(delta);
		} else {
			if (Spacenauts.getNetworkAdapter() != null) Spacenauts.getNetworkAdapter().reset();
			if (nextScreen instanceof Loadable) {
				game.setScreen(new LoadingScreen(nextScreen, game, (Loadable)nextScreen));
			} else {
				game.setScreen(nextScreen);
			}
		}
	}

	@Override
	public void pause () {
		super.pause();
		//Pause only in single player or android.
		if (!multiplayer || Gdx.app.getType() == ApplicationType.Android) engine.pause();
	}

	@Override
	public void dispose(){
		if (assets != null)	assets.dispose();
		engine.clear();
		engine.clearPools();
		engine = null;
		entityBuilder = null;
		ui.dispose();
		if (Spacenauts.getNetworkAdapter() != null) Spacenauts.getNetworkAdapter().reset();
	}

	@Override
	public void preload(AssetManager assets) {
		assets.load(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		assets.load(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		assets.load(AssetsPaths.FONT_ATARI_28, BitmapFont.class);
		assets.load(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		assets.load(AssetsPaths.FONT_KARMATIC_32, BitmapFont.class);
		assets.load(AssetsPaths.FONT_KARMATIC_64, BitmapFont.class);
		assets.load(AssetsPaths.BGM_RUNAWAY_TECHNOLOGY, Music.class);
		assets.load(AssetsPaths.BGM_URBAN_FUTURE, Music.class);
		assets.load(AssetsPaths.BGM_UNCERTAIN_FUTURE, Music.class);
		assets.load(AssetsPaths.BGM_RANDOM_PROCESSES, Music.class);
		assets.load(AssetsPaths.BGM_REBUILDING_THEMSELVES, Music.class);
		assets.load(AssetsPaths.SFX_LASER_4, Sound.class);
		assets.load(AssetsPaths.SFX_EXPLOSION, Sound.class);
		assets.load(AssetsPaths.SFX_POWERUP, Sound.class);
		assets.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		assets.load(mapFile, TiledMap.class);
	}

	@Override
	public void handAssets(AssetManager assets) {
		this.assets = assets;
	}

	private void initUI(){
		ui = new GameUI(this);
	}

	private void initSystems(){	
		engine.addEntity(entityBuilder.buildWorldCamera());
		CameraSystem cs = new CameraSystem(this);
		SteeringSystem ss = new SteeringSystem();
		MovementSystem ms = new MovementSystem();
		CollisionSystem cls = new CollisionSystem();
		HitSystem hs = new HitSystem();
		PhysicsSystem ps = new PhysicsSystem(ms, ss, cls);
		RenderingSystem drs = new RenderingSystem(this);
		ShootingSystem shs = new ShootingSystem(this);
		RemovalSystem rs = new RemovalSystem();
		AISystem ais = new AISystem();
		DialogSystem ds = new DialogSystem(this);
		ImmunitySystem is = new ImmunitySystem();
		TimerSystem ts = new TimerSystem();

		if (multiplayer) engine.addSystem(new MultiplayerSystem());
		engine.addSystem(cs);
		engine.addSystem(ps);
		engine.addSystem(drs);
		engine.addSystem(hs);
		engine.addSystem(shs);
		engine.addSystem(rs);
		engine.addSystem(ais);
		engine.addSystem(ds);
		engine.addSystem(is);
		engine.addSystem(ts);
	}

	private void initControls(){
		input = new InputMultiplexer();
		input.addProcessor(ui);
		input.addProcessor((controls = new Controls()));
		Gdx.input.setInputProcessor(input);
	}

	private void initAudio() {
		audio = new AudioManager(this, currentLevel.getBGM());
		audio.start();
		audio.startLevelFade(true);
	}

	public static SpacenautsEngine getEngine(){
		return engine;
	}

	public static EntityBuilder getBuilder(){
		return entityBuilder;
	}

	public Level getLevel(){
		return currentLevel;
	}

	public TiledMap getMap(){
		return assets.get(mapFile, TiledMap.class);
	}

	public AssetManager getAssets(){
		return assets;
	}

	public GameUI getUI(){
		return ui;
	}

	public Game getGame() {
		return game;
	}
	
	public void exit (Screen nextScreen) {
		playing = false;
		this.nextScreen = nextScreen;
	}
}
