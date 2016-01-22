package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.screens.GameScreen;

/**
 * An AI that simply implements auto-attack, without rotation or any kind of motion implied, for entities.
 * 
 * @author Alessio Cali'
 *
 */
public class SteadyShooterAI extends DefaultStateMachine<Entity> {

	private static final float SIGHT_RADIUS = 15f;

	private enum SteadyShooterState implements State<Entity> {

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

	public SteadyShooterAI(Entity owner) {
		super(owner, SteadyShooterState.IDLE);
	}

}
