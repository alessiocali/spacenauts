package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A basic AI that implements auto shoot while aiming for the player. Also has a "reach" state
 * that reaches a certain position.
 * 
 * @author Alessio Cali'
 *
 */
public class AimAndShootAI extends DefaultStateMachine<Entity> {

	private static final float SIGHT_RADIUS = 30f;
	
	private Vector2 reachPosition = new Vector2();

	public enum AimAndShootState implements State<Entity>{		
		IDLE() {
			@Override
			public void update(Entity entity){
				Vector2 cameraPos = Mappers.pm.get(GameScreen.getEngine().getCamera()).value;
				Position pos = Mappers.pm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);

				if (pos != null){
					if (cameraPos.dst(pos.value) < SIGHT_RADIUS)
						ai.fsm.changeState(AimAndShootState.ATTACK);
				}
			}
		},

		ATTACK() {
			private static final float SHOOT_INTERVAL = 1;

			@Override
			public void enter(Entity entity){
				Steering steering = GameScreen.getEngine().createComponent(Steering.class);
				steering.adapter = SteeringMechanism.getFor(entity);
				steering.adapter.setMaxAngularSpeed(MathUtils.PI / 2);
				steering.adapter.setMaxAngularAcceleration(MathUtils.PI / 2);
				Face<Vector2> behavior = new Face<Vector2>(steering.adapter, GameScreen.getEngine().getPlayerTarget());
				behavior.setAlignTolerance(0.05f);
				behavior.setDecelerationRadius(MathUtils.PI / 4);
				steering.behavior = behavior;
				entity.add(steering);
			}

			@Override
			public void update(Entity entity){
				Gun guns = Mappers.gm.get(entity);
				Vector2 playerPos = GameScreen.getEngine().getPlayerTarget().getPosition();
				FSMAI ai = Mappers.aim.get(entity);
				Steering steering = Mappers.stm.get(entity);
				AngularVelocity angVel = Mappers.avm.get(entity);

				if (guns != null){
					for (GunData gun : guns.guns){
						//Trigger the gun if enough time has elapsed and it hasn't been triggered yet.
						if (gun.shootingTimer > SHOOT_INTERVAL && !gun.triggered){
							gun.triggered = true;
						}
					}	
				}

				if (steering != null){
					if (playerPos.dst(steering.adapter.getPosition()) >= SIGHT_RADIUS){
						ai.fsm.changeState(AimAndShootState.IDLE);
						
						if (angVel != null)
							angVel.value = 0;
					}
				}		
			}

			@Override
			public void exit(Entity entity){
				entity.remove(Steering.class);
			}
		},
		
		REACH() {
			
			@Override
			public void enter(Entity entity){
				AimAndShootAI ai = (AimAndShootAI)Mappers.aim.get(entity).fsm;
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
			
		};

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

	public AimAndShootAI(Entity owner){
		super(owner, AimAndShootState.IDLE);
	}
	
	public Vector2 getReachPosition(){
		return reachPosition;
	}

}
