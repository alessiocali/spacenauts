package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * An entity's angular velocity, if it can rotate.
 * 
 * @author Alessio Cali'
 *
 */
public class AngularVelocity implements Component, Poolable {
	
	public float value = 0;

	@Override
	public void reset() {
		value = 0;
	}

}
