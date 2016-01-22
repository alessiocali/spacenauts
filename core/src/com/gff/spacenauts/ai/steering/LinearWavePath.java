package com.gff.spacenauts.ai.steering;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * This behavior applies elastic steering to the x-axis, resulting in armonic motion (if the entity is not on the y-axis).
 * A given offset can be set to decide the center of the motion. Furthermore, y-axis velocity can be non-zero, which is
 * intended to result in linear, wave-like path.
 * 
 * @author Alessio Cali'
 *
 */
public class LinearWavePath extends SteeringBehavior<Vector2> {
	
	//The elastic constant
	private float k = 0;
	private float x0 = 0;
	private float verticalVelocity = 0;

	public LinearWavePath(Steerable<Vector2> owner, boolean enabled) {
		super(owner, enabled);
		initialize();
	}

	public LinearWavePath(Steerable<Vector2> owner, Limiter limiter, boolean enabled) {
		super(owner, limiter, enabled);
		initialize();
	}

	public LinearWavePath(Steerable<Vector2> owner, Limiter limiter) {
		super(owner, limiter);
		initialize();
	}

	public LinearWavePath(Steerable<Vector2> owner) {
		super(owner);
		initialize();
	}
	
	private void initialize () {
		x0 = this.getOwner().getPosition().x;
		setVerticalVelocity(verticalVelocity);
	}
	
	public void setX0(float x0) {
		this.x0 = x0;
		this.getOwner().getLinearVelocity().x = 0;
	}
	
	public void offset(float offset) {
		setX0(x0 + offset);
	}
	
	public void setVerticalVelocity (float verticalVelocity) {
		float maxSpeed = this.getOwner().getMaxLinearSpeed();
		float clampedVelocity = MathUtils.clamp(verticalVelocity, - maxSpeed, maxSpeed);
		this.verticalVelocity = clampedVelocity;
		this.getOwner().getLinearVelocity().set(0, clampedVelocity);
	}
	
	public void setElasticConstant (float k) { 
		this.k = k;
	}

	@Override
	protected SteeringAcceleration<Vector2> calculateRealSteering (SteeringAcceleration<Vector2> steering) {
		steering.angular = 0;
		float x = getOwner().getPosition().x;
		float maxAccel = getOwner().getMaxLinearAcceleration();
		float elasticAccel = MathUtils.clamp(- k * (x - x0), - maxAccel, maxAccel);
		steering.linear.set(elasticAccel, 0);
		return steering;
	}	
	
}
