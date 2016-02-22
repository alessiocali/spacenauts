package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.ashley.components.Velocity;

/**
 * Applies {@link com.badlogic.gdx.ai.steer.SteeringBehavior SteeringBehavior}s from {@link Steering} components to entities.
 * To avoid time aliasing this should be wrapped inside a {@link PhysicsSystem}.
 * 
 * @author Alessio Cali'
 *
 */
public class SteeringSystem extends IteratingSystem {
	
	private SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
	
	public SteeringSystem(){
		super(Families.STEERING_FAMILY);
	}

	@Override
	public void processEntity(Entity entity, float delta){		
		Steering steering = Mappers.stm.get(entity);
		Velocity vel = Mappers.vm.get(entity);
		AngularVelocity av = Mappers.avm.get(entity);
		
		if (steering.behavior != null) {
			steering.behavior.calculateSteering(steeringOutput);
		
			vel.value.mulAdd(steeringOutput.linear, delta);
			av.value += steeringOutput.angular * delta;
		}
	}
}
