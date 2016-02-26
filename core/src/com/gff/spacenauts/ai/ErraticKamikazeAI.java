package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.gff.spacenauts.ai.steering.LinearWavePath;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A FSM behavior that waits for the player to get in range, than storms in a sinusoidal pattern
 * by using {@link LinearWavePath} steering. 
 * 
 * @author Alessio Cali'
 *
 */
public class ErraticKamikazeAI extends DefaultStateMachine<Entity> {
	
	public enum ErraticKamikazeState implements State<Entity> {
		IDLE {
			private float SIGHT_RADIUS = 20f;
			
			@Override
			public void update (Entity entity) {
				Entity camera = GameScreen.getEngine().getCamera();
				
				if (camera != null) {
					Position entityPos = Mappers.pm.get(entity);
					Position cameraPos = Mappers.pm.get(camera);
					
					if (entityPos != null && cameraPos != null) { 
						StateMachine<Entity> ai = Mappers.aim.get(entity).fsm;
						
						if (entityPos.value.dst(cameraPos.value) < SIGHT_RADIUS) {
							ai.changeState(ErraticKamikazeState.ERRATIC);
						}
					}
				}
			}
		},
		ERRATIC {
			@Override
			public void enter (Entity entity) { 
				Steering steering = GameScreen.getEngine().createComponent(Steering.class);
				
				steering.adapter = SteeringMechanism.getFor(entity);
				steering.adapter.setMaxLinearSpeed(20);
				steering.adapter.setMaxLinearAcceleration(20);
				
				LinearWavePath behavior = new LinearWavePath(steering.adapter);
				behavior.setVerticalVelocity(-10);
				behavior.setElasticConstant(10);
				behavior.offset(2);
				steering.behavior = behavior;
				
				entity.add(steering);
			}
		};
		
		@Override
		public void enter (Entity entity) {
			
		}
		
		@Override
		public void update (Entity entity) {
			
		}
		
		@Override
		public void exit (Entity entity) {
			
		}
		
		@Override
		public boolean onMessage (Entity entity, Telegram Message) {
			return false;
		}
	}
	
	public ErraticKamikazeAI (Entity owner) {
		super(owner, ErraticKamikazeState.IDLE);
	}
}
