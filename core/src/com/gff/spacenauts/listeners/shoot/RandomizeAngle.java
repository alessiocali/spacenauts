package com.gff.spacenauts.listeners.shoot;

import java.util.Random;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.listeners.ShotListener;

/**
 * Randomizes bullet orientation between min and max (radians).
 * 
 * @author Alessio Cali'
 *
 */
public class RandomizeAngle implements ShotListener {

	private static final Random RANDOM = new Random();
	
	private float min;
	private float max;
	
	public RandomizeAngle(float min, float max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public void onShooting(Entity gun, Entity bullet) {
		Angle ang = Mappers.am.get(bullet);
		Velocity vel = Mappers.vm.get(bullet);
		
		float deviation = min + RANDOM.nextFloat() * (max - min);
		if (ang != null) {
			ang.value += deviation;
			if (vel != null) vel.value.setAngleRad(ang.value);
		}
		
	}

}
