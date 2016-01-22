package com.gff.spacenauts.ashley;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Velocity;

/**
 * A class that uses components to implement the {@link Steerable} interface.
 * 
 * @author Alessio Cali'
 *
 */
public class SteeringMechanism implements Steerable<Vector2>, Poolable {
	
	public static final Pool<SteeringMechanism> steeringPool = Pools.get(SteeringMechanism.class);
	
	private Position position;
	private Velocity velocity;
	private Angle angle;
	private AngularVelocity angularVelocity;
	private Body body;
	
	private float linearTreshold = 0.01f;
	
	private float maxLinearSpeed = 0;
	private float maxLinearAcceleration = 0;
	private float maxAngularSpeed = 0;
	private float maxAngularAcceleration = 0;
	private boolean tagged = false;
	
	public SteeringMechanism() {

	}

	public SteeringMechanism set(Position position, Velocity velocity, Angle angle,
			AngularVelocity angularVelocity, Body body) {
		this.position = position;
		this.velocity = velocity;
		this.angle = angle;
		this.angularVelocity = angularVelocity;
		this.body = body;
		return this;
	}

	public SteeringMechanism set(Position position, Velocity velocity, Angle angle,
			AngularVelocity angularVelocity, Body body, float maxLinearSpeed,
			float maxLinearAcceleration, float maxAngularSpeed,
			float maxAngularAcceleration, boolean tagged) {
		set(position, velocity, angle, angularVelocity, body);
		this.maxLinearSpeed = maxLinearSpeed;
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.maxAngularSpeed = maxAngularSpeed;
		this.maxAngularAcceleration = maxAngularAcceleration;
		this.tagged = tagged;
		return this;
	}
	
	/**
	 * Builds a steering mechanism for the given entity. Note that the Entity must provide all required components, namely:
	 * <ul>
	 * 	<li>Position</li>
	 * 	<li>Velocity</li>
	 * 	<li>Angle</li>
	 * 	<li>AngularVelocity</li>
	 * </ul>
	 * 
	 * If any of the given components is missing, null is returned instead. A Body is <i>not</i> required but it would
	 * help, since it's used to get the bounding radius.<br>
	 * Note that mechanisms are poolable objects and are thus reused when a Steering component is freed.
	 * 
	 * 
	 * @param entity The entity for which to build the SteeringMechanism.
	 * @return A SteeringMechanism for the given entity, or null if the Entity doesn't match requirements.
	 */
	public static SteeringMechanism getFor(Entity entity){
		//Checks 
		if (!Families.STEERABLE_FAMILY.matches(entity)){
			return null;
		} else {
			Position pos = entity.getComponent(Position.class);
			Velocity vel = entity.getComponent(Velocity.class);
			Angle angle = entity.getComponent(Angle.class);
			AngularVelocity angVel = entity.getComponent(AngularVelocity.class);
			Body body = entity.getComponent(Body.class);
			
			SteeringMechanism retVal = steeringPool.obtain();
			
			return retVal.set(pos,vel,angle,angVel,body);
		}
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
	public Vector2 getPosition() {
		return position.value;
	}

	@Override
	public float getOrientation() {
		return angle.value;
	}

	@Override
	public Vector2 getLinearVelocity() {
		return velocity.value;
	}

	@Override
	public float getAngularVelocity() {
		return angularVelocity.value;
	}

	@Override
	public float getBoundingRadius() {
		if (body != null) {
			Rectangle boundingRectangle = body.polygon.getBoundingRectangle();
			return Math.max(boundingRectangle.width, boundingRectangle.height);
		} else {
			return 0;
		}
	}

	@Override
	public boolean isTagged() {
		return tagged;
	}

	@Override
	public void setTagged(boolean tagged) {
		this.tagged = tagged;		
	}

	@Override
	public float vectorToAngle(Vector2 vector) {
		return (float)Math.atan2(vector.y, vector.x);
	}

	@Override
	public Vector2 angleToVector(Vector2 outVector, float angle) {
		float x = (float)Math.cos(angle);
		float y = (float)Math.sin(angle);
		
		outVector.set(x, y);
		return outVector;
	}

	@Override
	public void reset() {
		position = null;
		velocity = null;
		angle = null;
		angularVelocity = null;
		body = null;
		maxLinearSpeed = 0;
		maxLinearAcceleration = 0;
		maxAngularSpeed = 0;
		maxAngularAcceleration = 0;
		tagged = false;
	}
	
	/**
	 * Returns a quick {@link SteerableAdapter} to use as a target for a certain location.
	 * @param pos
	 * @return
	 */
	public static Steerable<Vector2> getQuickTarget(final Vector2 pos){
		SteerableAdapter<Vector2> adapter = new SteerableAdapter<Vector2>(){
			@Override
			public Vector2 getPosition(){
				return pos;
			}
		};
		
		return adapter;
	}

	@Override
	public void setOrientation(float orientation) {
		angle.value = orientation;
	}

	@Override
	public Location<Vector2> newLocation() {
		return null;
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
