package com.gff.spacenauts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.gff.spacenauts.screens.GameScreen;

/**
 * This class handles major pains with the audio. It handles both fadein and fadeout effects for
 * the two BGMs of each level (main theme and boss theme). It has two flags for each BGM, one for fadein
 * and one for fadeout, plus float values that represent them.<p>
 * 
 * The boss theme is "Runaway Technology" regardless of the map, while the level BGM is passed to the constructor.
 */

public class AudioManager  {

	//A scaling factor that slows down or speeds up the fade.
	//E.g: FADE_SCALE = 1/7 <-> The fade will take about 7 seconds to go from 0 (silent) to 1 (full volume) and vice versa.
	private static final float FADE_SCALE = 1/7f;

	private Music levelBGM;
	private Music bossBGM;

	private float levelFadein = 0;
	private float bossFadein = 0;
	private float levelFadeout = 1;
	private float bossFadeout = 1;

	private boolean flagLevelFadein = false;
	private boolean flagBossFadein = false;
	private boolean flagLevelFadeout = false;
	private boolean flagBossFadeout = false;

	public AudioManager(GameScreen game, String levelBGMPath){
		AssetManager assets = game.getAssets();

		levelBGM = assets.get(levelBGMPath, Music.class);
		levelBGM.setLooping(true);

		bossBGM = assets.get(AssetsPaths.BGM_RUNAWAY_TECHNOLOGY);
		bossBGM.setLooping(true);
	}

	/**
	 * Triggers fade effects for level BGM.
	 * 
	 * @param in If true, it's fadein. Otherwise, fadeout.
	 */
	public void startLevelFade(boolean in){
		if (in) flagLevelFadein = true;
		else flagLevelFadeout = true;
	}

	/**
	 * Triggers fade effects for boss BGM.
	 * 
	 * @param in If true, it's fadein. Otherwise, fadeout.
	 */
	public void startBossFade(boolean in){
		if (in) flagBossFadein = true;
		else flagLevelFadeout = true;
	}

	/**
	 * <p>Updated from {@link GameScreen#render(float)} method.</p>
	 * 
	 * <p>
	 * First, it queries the boss from the engine. If the boss is found, level fadeout and boss fadein are triggered.
	 * After that it handles fade effects: a float value is stepped by delta * FADE_SCALE, ranging from 1 to 0 or the opposite.
	 * The resulting [0,1] value is passed to a fade interpolator (see {@link Interpolation}), and than taken down by a half 
	 * (otherwise it would be too loud). As for fadeouts, once the volume is low enough (< 0.05) the BGM is stopped.
	 * </p>
	 * 
	 * @param delta
	 */
	public void update(float delta){
		Entity boss = GameScreen.getEngine().getBoss();

		//Boss found, fade out BGM fade in boss theme
		if (boss != null && !bossBGM.isPlaying()) {
			startLevelFade(false);
			startBossFade(true);
			bossBGM.play();
			bossBGM.setVolume(bossFadein);
		}

		//Level BGM is fading out, decrease its volume.
		if (flagLevelFadeout){
			levelFadeout -= delta * FADE_SCALE;
			levelBGM.setVolume(Interpolation.fade.apply(MathUtils.clamp(levelFadeout, 0, 1)) * 0.5f);

			if ( levelBGM.getVolume() < 0.05 ){
				flagLevelFadeout = false;
				levelFadeout = 1;
				levelBGM.stop();
			}
		}

		//Boss BGM is fading out, decrease its volume.
		if (flagBossFadeout){
			bossFadeout -= delta * FADE_SCALE;
			bossBGM.setVolume(Interpolation.fade.apply(MathUtils.clamp(bossFadeout, 0, 1)) * 0.5f);
			if ( bossBGM.getVolume() < 0.05 ){
				flagBossFadeout = false;
				bossFadeout = 1;
				bossBGM.stop();
			}
		}

		//Level BGM is fading in, increase volume
		if (flagLevelFadein){
			levelFadein += delta * FADE_SCALE;
			levelBGM.setVolume(Interpolation.fade.apply(MathUtils.clamp(levelFadein, 0, 1)) * 0.5f);
			
			if (levelFadein >= 1) {
				flagLevelFadein = false;
				levelFadein = 0;
			}
		}
		
		//Boss BGM is fading in, increase volume
		if (flagBossFadein){
			bossFadein += delta * FADE_SCALE;
			bossBGM.setVolume(Interpolation.fade.apply(MathUtils.clamp(bossFadein, 0, 1)) * 0.5f);
			
			if (bossFadein >= 1) {
				flagBossFadein = false;
				bossFadein = 0;
			}
		}
	}

	/**
	 * Stops all BGMs
	 * 
	 */
	public void stopAll(){
		levelBGM.stop();
		bossBGM.stop();
	}

	/**
	 * Called at the game's start.
	 * 
	 */
	public void start() {
		levelBGM.play();
		levelBGM.setVolume(levelFadein);
	}
}