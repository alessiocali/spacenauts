package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Immunity;

/*
 * Note to self: after the introduction of timers this is more or less deprecated...
 * I should totally write some adaptation code.
 */

/**
 * Updates {@link Immunity} components' timers.
 * 
 * @author Alessio Cali'
 *
 */
public class ImmunitySystem extends IntervalIteratingSystem {

	private static final float DELTA = 0.01f;

	public ImmunitySystem(){
		super(Families.IMMUNE_FAMILY, DELTA);
	}

	@Override
	protected void processEntity(Entity entity) {
		Immunity im = Mappers.im.get(entity);

		if (im != null) {
			im.timer += DELTA;

			if (im.timer > im.duration)
				entity.remove(Immunity.class);
		}
	}
}
