package com.gff.spacenauts.listeners.timers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gff.spacenauts.listeners.TimerListener;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A timer that switches a {@link Game}'s screen after a given period.
 * 
 * @author Alessio Cali'
 *
 */
public class ScreenTransition extends TimerListener {

	private GameScreen gameScreen;
	private Screen nextScreen;
	
	public ScreenTransition(GameScreen gameScreen, Screen nextScreen, float timeout) {
		super(TimerType.ONE_SHOT, timeout);
		
		if (gameScreen == null || nextScreen == null) 
			throw new GdxRuntimeException(new IllegalArgumentException("Screens can't be null."));
		
		this.gameScreen = gameScreen;
		this.nextScreen = nextScreen;
	}
	
	@Override
	public boolean onActivation (Entity entity) {
		gameScreen.exit(nextScreen);
		return true;
	}

}
