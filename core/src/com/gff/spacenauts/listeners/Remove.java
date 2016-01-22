package com.gff.spacenauts.listeners;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;

/**
 * A convenient multi-purpose listener to remove the entity when called. If used as a {@link ShotListener} it will remove the gun, but not the bullet.
 * 
 * @author Alessio Cali'
 *
 */

public class Remove implements AnimationListener, DeathListener, ShotListener {
	
	private Engine engine;
	
	public Remove (Engine engine) {
		this.engine = engine;
	}

	@Override
	public void onStart(Entity entity, Animation animation) {

	}

	@Override
	public void onEnd(Entity entity, Animation animation) {
		remove(entity);
	}

	@Override
	public void onShooting(Entity gun, Entity bullet) {
		remove(gun);
	}

	@Override
	public void onDeath(Entity entity) {
		remove(entity);		
	}
	
	private void remove(Entity entity) {
		engine.removeEntity(entity);
	}

}
