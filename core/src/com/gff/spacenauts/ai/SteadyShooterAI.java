package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.screens.GameScreen;

/**
 * An AI that simply implements auto-attack, without rotation or any kind of motion implied, for entities.
 * 
 * @author Alessio Cali'
 *
 */
public class SteadyShooterAI extends DefaultStateMachine<Entity> {

	private static final float SIGHT_RADIUS = 30f;

	public enum SteadyShooterState implements State<Entity> {

		IDLE {
			@Override
			public void update(Entity entity){
				Vector2 cameraPos = Mappers.pm.get(GameScreen.getEngine().getCamera()).value;
				Position pos = Mappers.pm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);

				if (pos != null){
					if (cameraPos.dst(pos.value) < SIGHT_RADIUS)
						ai.fsm.changeState(SteadyShooterState.ATTACK);
				}
			}
		},

		ATTACK {
			private static final float SHOOT_INTERVAL = 1;

			@Override
			public void update(Entity entity){
				Gun guns = Mappers.gm.get(entity);
				Vector2 playerPos = GameScreen.getEngine().getPlayerTarget().getPosition();
				Position pos = Mappers.pm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);

				if (guns != null){
					for (GunData gun : guns.guns){
						//Trigger the gun if enough time has elapsed and it hasn't been triggered yet.
						if (gun.shootingTimer > SHOOT_INTERVAL && !gun.triggered){
							gun.triggered = true;
						}
					}
				}

				if (pos != null){
					if (playerPos.dst(pos.value) >= SIGHT_RADIUS)
						ai.fsm.changeState(SteadyShooterState.IDLE);
				}
			}
		},
		
		REACH {
			
			@Override
			public void enter(Entity entity){
				SteadyShooterAI ai = (SteadyShooterAI)Mappers.aim.get(entity).fsm;
				Steering steering = GameScreen.getEngine().createComponent(Steering.class);
				steering.adapter = SteeringMechanism.getFor(entity);
				steering.adapter.setMaxLinearSpeed(7);
				steering.adapter.setMaxLinearAcceleration(10);
				steering.adapter.setMaxAngularSpeed(MathUtils.PI / 2);
				steering.adapter.setMaxAngularAcceleration(MathUtils.PI / 2);
				Arrive<Vector2> behavior = new Arrive<Vector2>(steering.adapter, SteeringMechanism.getQuickTarget(ai.getReachPosition()));
				behavior.setDecelerationRadius(5);
				behavior.setArrivalTolerance(0.1f);
				steering.behavior = behavior;
				entity.add(steering);
			}
			
			@Override
			public void update(Entity entity){
				Steering steering = Mappers.stm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (steering != null && ai != null) {
					if (steering.behavior instanceof Arrive){
						Arrive<Vector2> behavior = (Arrive<Vector2>)steering.behavior;
						if (steering.adapter.getPosition().dst(behavior.getTarget().getPosition()) < behavior.getArrivalTolerance()){
							steering.adapter.getLinearVelocity().setZero();
							ai.fsm.changeState(IDLE);
						}
					}
				}
			}
			
		};;

		@Override
		public void enter(Entity entity){

		}

		@Override
		public void exit(Entity entity){

		}

		@Override
		public boolean onMessage(Entity entity, Telegram telegram){
			return false;
		}
	}
	
	private Vector2 reachPosition = new Vector2();

	public SteadyShooterAI(Entity owner) {
		super(owner, SteadyShooterState.IDLE);
	}
	
	public Vector2 getReachPosition () {
		return reachPosition;
	}

}
