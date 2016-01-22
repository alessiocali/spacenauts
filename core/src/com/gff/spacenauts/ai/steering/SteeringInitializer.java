package com.gff.spacenauts.ai.steering;

import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;

public interface SteeringInitializer {

	public void init(SteeringBehavior<Vector2> behavior);
	
}
