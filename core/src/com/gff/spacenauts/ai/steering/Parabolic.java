package com.gff.spacenauts.ai.steering;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A steering behavior that realizes parabolic movement, using the x-axis as the up-axis.
 * 
 * @author Alessio Cali'
 *
 */
public class Parabolic extends SteeringBehavior<Vector2> {
	
	private float horAccel;

	public Parabolic(Steerable<Vector2> owner) {
		super(owner);
	}
	
	public void setHorizontalAccel(float horAccel) {
		this.horAccel = horAccel;
	}

	@Override
	protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
		float max = this.getOwner().getMaxLinearAcceleration();
		float min = - max;
		float clmp = MathUtils.clamp(horAccel, min, max);
		steering.linear.set(clmp, 0);
		return steering;
	}
}
