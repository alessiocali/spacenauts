
package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.ashley.SteeringMechanism;

/**
 * An entity's steering, both in the form of its {@link com.badlogic.gdx.ai.steer.Steerable Steerable} provided by {@link SteeringMechanism}
 * and its {@link SteerinBehavior}.
 * 
 * @author Alessio Cali'
 *
 */
public class Steering implements Component, Poolable {
	
	public SteeringBehavior<Vector2> behavior = null;
	public SteeringMechanism adapter = null;
	
	@Override
	public void reset(){
		behavior = null;
		if (adapter != null)
			SteeringMechanism.steeringPool.free(adapter);
		adapter = null;
	}

}
