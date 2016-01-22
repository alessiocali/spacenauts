package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.listeners.death.DeathListeners;

/**
 * An array of {@link com.gff.spacenauts.listeners.DeathListener DeathListener}s to activate on the entity's death.
 * 
 * @author Alessio Cali'
 *
 */
public class Death implements Component, Poolable {
	
	public DeathListeners listeners = new DeathListeners();

	@Override
	public void reset() {
		listeners.clear();
	}

}
