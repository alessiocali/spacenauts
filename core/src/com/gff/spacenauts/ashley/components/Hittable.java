package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.listeners.hit.HitListeners;

/**
 * An array of {@link com.gff.spacenauts.listeners.HitListener HitListener}s to be called when the entity is hit, plus
 * the entity's health.
 * 
 * @author Alessio Cali'
 *
 */
public class Hittable implements Component, Poolable {

	public float health = 0;
	public float maxHealth = 0;
	public HitListeners listeners = new HitListeners();
	public Array<Entity> colliders = new Array<Entity>();
	
	public float getHealthPercent () {
		return health/maxHealth;
	}
	
	@Override
	public void reset(){
		health = 0;
		maxHealth = 0;
		listeners.clear();
		colliders.clear();
	}
	
}
