package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.listeners.TimerListener;

/**
 * Iterates over all {@link Timers} components and updates their listeners based on deltaTime.
 * 
 * @author Alessio Cali'
 *
 */
public class TimerSystem extends IteratingSystem {

	public TimerSystem() {
		super(Families.TIMER_FAMILY, 999);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		Timers timers = Mappers.tm.get(entity);
		
		for (TimerListener listener : timers.listeners) listener.update(deltaTime, entity);
	}

}
