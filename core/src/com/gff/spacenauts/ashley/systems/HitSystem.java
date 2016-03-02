package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.listeners.hit.HitListeners;

/**
 * This system complements the work of {@link CollisionSystem}. After 
 * the entity's colliders are calculated this systems iterates over them
 * and triggers all {@link HitListeners}.
 * 
 * @author Alessio
 *
 */
public class HitSystem extends IteratingSystem {

	public HitSystem() {
		super(Families.COLLIDERS_FAMILY);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		Hittable entityHittable = Mappers.hm.get(entity);
		
		for (Entity collider : entityHittable.colliders)
			entityHittable.listeners.onHit(entity, collider);
		
		entityHittable.colliders.clear();
	}

}
