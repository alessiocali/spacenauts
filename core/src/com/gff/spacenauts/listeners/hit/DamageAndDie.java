package com.gff.spacenauts.listeners.hit;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.components.CollisionDamage;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Immunity;
import com.gff.spacenauts.listeners.HitListener;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A hit listener that damages its owner based on CollisionDamage. If health reaches zero, it triggers death.
 * If health does not reach zero but the entity is damaged, a short immunity period is activated instead.
 * 
 * @author Alessio Cali'
 *
 */
public class DamageAndDie implements HitListener {

	private Family[] filters;

	public DamageAndDie(Family... filters){
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

		Hittable hit = Mappers.hm.get(entity);
		Immunity immunity = Mappers.im.get(entity);
		Death death = Mappers.dem.get(entity);
		CollisionDamage damage = Mappers.cdm.get(collider);			

		SpacenautsEngine engine = GameScreen.getEngine();
		
		if (damage != null && immunity == null){
			hit.health -= damage.damageDealt;
			entity.add(engine.createComponent(Immunity.class)); 
			if (engine.getPlayer() == entity)
				engine.sendCoop("PLAYER_HIT");
		}

		if (hit.health <= 0 && death != null) {
			death.listeners.onDeath(entity);
			if (engine.getPlayer() == entity) 
				engine.sendCoop("PLAYER_DEAD");
		}
	}
}