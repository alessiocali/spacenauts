package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.listeners.TimerListener;

/**
 * A list of {@link TimerListener}s owned by the entity.
 * 
 * @author Alessio Cali'
 *
 */
public class Timers implements Component, Poolable {

	public Array<TimerListener> listeners = new Array<TimerListener>();
	
	@Override
	public void reset () {
		listeners.clear();
	}

}
