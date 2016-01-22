package com.gff.spacenauts.listeners.shoot;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.gff.spacenauts.ai.steering.SteeringInitializer;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.listeners.ShotListener;
import com.gff.spacenauts.screens.GameScreen;

/*
 * This is currently bugged since the same behavior will constantly switch owner to the newest bullet, and all previous bullets calculations
 * will be based off the new owner. I didn't notice this since it was used with Parabolic behavior only. I must totally fix it.
 */

/**
 * Applies a {@link SteeringBehavior} to every shot bullet when called. Beware that this
 * will result in object allocation at every shot, thus leading to potential memory leaks.
 * 
 * 
 * @author Alessio Cali'
 *
 */
public class ApplySteering implements ShotListener, Limiter {

	private Class<? extends SteeringBehavior<Vector2>> behavior;
	private SteeringInitializer initializer;
	private float maxLinearSpeed;
	private float maxLinearAcceleration;
	private float maxAngularSpeed;
	private float maxAngularAcceleration;
	private float linearTreshold = 0.01f;
	
	public ApplySteering(Class<? extends SteeringBehavior<Vector2>> behavior, 
						 SteeringInitializer initializer) {
		this.behavior = behavior;
		this.initializer = initializer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onShooting(Entity gun, Entity bullet) {
		Steering steering = GameScreen.getEngine().createComponent(Steering.class);
		steering.adapter = SteeringMechanism.getFor(bullet);
		steering.adapter.setMaxLinearSpeed(maxLinearSpeed);
		steering.adapter.setMaxLinearAcceleration(maxLinearAcceleration);
		steering.adapter.setMaxAngularSpeed(maxAngularSpeed);
		steering.adapter.setMaxAngularAcceleration(maxAngularAcceleration);
		try {
			steering.behavior = (SteeringBehavior<Vector2>)ClassReflection.getConstructor(behavior, Steerable.class).newInstance(steering.adapter);
			initializer.init(steering.behavior);
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		bullet.add(steering);
	}

	@Override
	public float getMaxLinearSpeed() {
		return maxLinearSpeed;
	}

	@Override
	public void setMaxLinearSpeed(float maxLinearSpeed) {
		this.maxLinearSpeed = maxLinearSpeed;
	}

	@Override
	public float getMaxLinearAcceleration() {
		return maxLinearAcceleration;
	}

	@Override
	public void setMaxLinearAcceleration(float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	@Override
	public float getMaxAngularSpeed() {
		return maxAngularSpeed;
	}

	@Override
	public void setMaxAngularSpeed(float maxAngularSpeed) {
		this.maxAngularSpeed = maxAngularSpeed;
	}

	@Override
	public float getMaxAngularAcceleration() {
		return maxAngularAcceleration;
	}

	@Override
	public void setMaxAngularAcceleration(float maxAngularAcceleration) {
		this.maxAngularAcceleration = maxAngularAcceleration;
	}

	@Override
	public float getZeroLinearSpeedThreshold() {
		return linearTreshold;
	}

	@Override
	public void setZeroLinearSpeedThreshold(float value) {
		linearTreshold = value;		
	}

}
