package com.gff.spacenauts.listeners.hit;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.listeners.HitListener;

/**
 * A simple hit listener that instantly triggers death.
 * 
 * @author Alessio Cali'
 *
 */
public class Die implements HitListener {

	//Filter families of entities that can hit this entity.
	private Family[] filters;

	public Die(Family... filters){
		this.filters = filters;
	}

	@Override
	public void onHit(Entity entity, Entity collider){
		boolean matches = false;
		
		//Continue if the entity matches any of the given filters.
		for (Family filter : filters) { 
			if (filter != null) {
				if (filter.matches(collider))
					matches = true;
			}
		}
		
		if (!matches) return;

		Death death = Mappers.dem.get(entity);

		if (death != null)
			death.listeners.onDeath(entity);
	}

}