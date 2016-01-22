package com.gff.spacenauts.listeners;

import com.badlogic.ashley.core.Entity;

/**
 * A listener called when two entities collide.
 * 
 * @author Alessio Cali'
 *
 */
public interface HitListener {

	public void onHit(Entity entity, Entity collider);
	
}
