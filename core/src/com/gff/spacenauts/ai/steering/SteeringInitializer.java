package com.gff.spacenauts.ai.steering;

import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.listeners.shoot.ApplySteering;

/**
 * This interface is used alongside the {@link ApplySteering} listener to
 * apply Steering Behaviors to entities. All it does is specify how to initialize
 * the given behavior for the given entity.
 * 
 * @author Alessio
 *
 */
public interface SteeringInitializer {

	public void init(SteeringBehavior<Vector2> behavior);
	
}
