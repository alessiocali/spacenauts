package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * The entity's velocity.
 * 
 * @author Alessio Cali'
 *
 */
public class Velocity implements Component, Poolable {

	public Vector2 value = new Vector2(0,0);
	
	@Override
	public void reset(){
		value.set(0,0);
	}
}
