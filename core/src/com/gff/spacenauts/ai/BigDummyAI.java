package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.death.DropPowerUp;
import com.gff.spacenauts.screens.GameScreen;

/**
 * State Machine AI for Big Dummy. It's a scripted behavior that does the following:
 * 
 * <ul>
 * <li>From 100% to 50% health simply shoots 3 lasers after aiming to the player. Aiming is performed by {@link Face} behavior.</li>
 * <li>Two adds spawn at 50%. They're simple Dummies.</li>
 * <li>Two more adds spawn at 25%. Big Dummy shoots 5 lasers instead than 3.</li>
 * </ul>
 * 
 * @author Alessio Cali'
 *
 */
public class BigDummyAI extends DefaultStateMachine<Entity> {
	
	public enum BigDummyState implements State<Entity> {
		
		ATTACK_100_50 {
			
			@Override
			public void enter(Entity entity){
				Steering steering = GameScreen.getEngine().createComponent(Steering.class);
				
				steering.adapter = SteeringMechanism.getFor(entity);
				steering.adapter.setMaxAngularSpeed(MathUtils.PI / 6);
				steering.adapter.setMaxAngularAcceleration(MathUtils.PI / 12);
				
				Face<Vector2> behavior = new Face<Vector2>(steering.adapter, GameScreen.getEngine().getPlayerTarget());
				behavior.setAlignTolerance(0.05f);
				behavior.setDecelerationRadius(MathUtils.PI / 4);
				
				steering.behavior = behavior;
				entity.add(steering);
			}
			
			@Override
			public void update(Entity entity){
				autoshoot(entity);
				
				Hittable hit = Mappers.hm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (hit.getHealthPercent() < 0.5f){
					ai.fsm.changeState(ATTACK_50_25);
				}
			}
		},
		
		ATTACK_50_25 {
			
			@Override
			public void enter(Entity entity) {
				Entity[] minions = spawnMinions(entity);

				//First minion will drop a TRIGUN
				Mappers.dem.get(minions[0]).listeners.addListener(new DropPowerUp("TRIGUN"));				
				
				for (Entity minion : minions)
					GameScreen.getEngine().addEntity(minion);
			}
			
			@Override
			public void update(Entity entity) {
				autoshoot(entity);
				Hittable hit = Mappers.hm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (hit.health / hit.maxHealth < 0.25f)
					ai.fsm.changeState(ATTACK_25_0);
			}
			
		},
		
		ATTACK_25_0 {
			
			@Override
			public void enter(Entity entity) {
				Entity[] minions = spawnMinions(entity);
				
				//Second minion will drop a TRIGUN
				Mappers.dem.get(minions[1]).listeners.addListener(new DropPowerUp("TRIGUN"));
				
				for (int i = 0 ; i < minions.length ; i++) {
					AimAndShootAI minionAI = (AimAndShootAI)Mappers.aim.get(minions[i]).fsm;
					Position minionPos = Mappers.pm.get(minions[i]);
					
					//Lower the minions position so they don't overlap with previous ones.
					minionAI.getReachPosition().add(0, -5);
					minionPos.value.add(0, -5);
					GameScreen.getEngine().addEntity(minions[i]);
				}
				
				Gun gun = Mappers.gm.get(entity);
				GunData[] gunData = new GunData[2];
				
				for (int i = 0 ; i < 2 ; i++) {
					gunData[i] = GameScreen.getBuilder().buildGunDataBigDummy();					
					
					if (i == 0) {
						gunData[i].pOffset.set(0.5f, 2);
					} else if (i == 1) {
						gunData[i].pOffset.set(0.5f, -2);
					}		
				}
				
				if (gun != null)
					gun.guns.addAll(gunData);		
			}
			
			@Override
			public void update(Entity entity) {
				autoshoot(entity);
			}
			
		};
		
		private static final float SHOOT_INTERVAL = 1;
		
		private static Entity[] spawnMinions(Entity entity) {
			Position bossPosition = Mappers.pm.get(entity);
			
			Entity[] minions = new Entity[2];
			
			for (int i = 0 ; i < 2 ; i++) {
				minions[i] = GameScreen.getBuilder().buildDummy();
				AimAndShootAI minionAI = (AimAndShootAI)Mappers.aim.get(minions[i]).fsm;
				Position minionPos = Mappers.pm.get(minions[i]);
				
				minionPos.value.set(bossPosition.value.x, bossPosition.value.y);
				minionAI.changeState(AimAndShootAI.AimAndShootState.REACH);
				
				//The first minion is on the left, the second is on the right
				if (i == 0) {
					minionPos.value.set(bossPosition.value).add(-10,-10);
					minionAI.getReachPosition().set(bossPosition.value).add(-4, -10);
				} else if (i == 1) {
					minionPos.value.set(bossPosition.value).add(10,-10);
					minionAI.getReachPosition().set(bossPosition.value).add(4, -10);
				}
				
			}	
			
			return minions;
		}
		
		private static void autoshoot(Entity entity){
			Gun guns = Mappers.gm.get(entity);

			if (guns != null){
				for (GunData gun : guns.guns){
					//Trigger the gun if enough time has elapsed and it hasn't been triggered yet.
					if (gun.shootingTimer > SHOOT_INTERVAL && !gun.triggered){
						gun.triggered = true;
					}
				}	
			}
		}
		
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
	
	
	
	public BigDummyAI(Entity owner){
		super(owner);
		changeState(BigDummyState.ATTACK_100_50);
	}

}
