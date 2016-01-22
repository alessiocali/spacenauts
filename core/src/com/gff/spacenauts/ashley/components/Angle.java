package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * An entity's orientation, in radians.
 * 
 * @author Alessio Cali'
 *
 */
public class Angle implements Component, Poolable {
	
	public float value = 0;
	
	public float getAngleDegrees(){
		return value / MathUtils.PI * 180;
	}

	@Override
	public void reset() {
		value = 0;
	}

}
