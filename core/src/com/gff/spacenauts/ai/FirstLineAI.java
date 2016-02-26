package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.data.SpawnerData;
import com.gff.spacenauts.listeners.TimerListener.TimerType;
import com.gff.spacenauts.listeners.timers.Spawn;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Scripted AI for First Line. It does the following:
 * 
 * <ul>
 * <li>It starts in horizontal position (angle = 0). It shoots 3 lasers which later propagate into 9 lasers through {@link com.gff.spacenauts.listeners.timers.Propagate Propagate}.</li>
 * <li>At 70% it rotates until it reaches 180d. Like before it shoots 3 propagation lasers, but also spawns 2 blue cruisers on the left and right.
 * At 40% another cruiser is added to the middle.</li>
 * <li>At 30% it rotates until it reaches 270d. At this points it shoots 6 lasers from his sides in parabolic motion, by using {@link com.gff.spacenauts.ai.steering.Parabolic Parabolic}. </li>
 * </ul>
 * 
 * @author Alessio Cali'
 *
 */
public class FirstLineAI extends DefaultStateMachine<Entity> {

	private enum FirstLineState implements State<Entity> {
		
		ATTACK_100_70 {	
			
			@Override
			public void enter (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				
				if (gun != null) {
					gun.guns.clear();
					gun.guns.addAll(GameScreen.getBuilder().buildGunDataFL100To70());
				}
			}
			
			@Override
			public void update (Entity entity) {
				autoshoot(entity);
				
				Hittable hit = Mappers.hm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (hit != null && ai != null) {
					if (hit.getHealthPercent() < 0.7f) {
						ai.fsm.changeState(ROTATE_TO_180);
					}
				}
			}
		},
		
		ROTATE_TO_180 {
			@Override
			public void enter (Entity entity) {
				AngularVelocity angVel = Mappers.avm.get(entity);
				if (angVel != null) angVel.value = MathUtils.PI / 6;
			}
			
			@Override
			public void update (Entity entity) {
				Angle ang = Mappers.am.get(entity);
				AngularVelocity angVel = Mappers.avm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (ang != null) {
					if (ang.value > MathUtils.PI) {
						ang.value = MathUtils.PI;
						angVel.value = 0;
						if (ai != null) ai.fsm.changeState(ATTACK_70_40);
					}
				}
			}
			
			@Override
			public void exit (Entity entity) {
				Gun guns = Mappers.gm.get(entity);
				
				if (guns != null) {
					for (GunData gun : guns.guns) {
						gun.aOffset += MathUtils.PI;
						gun.pOffset.add(0, +4);
					}
				}
			}
		},
		
		ATTACK_70_40 {
			@Override
			public void enter (Entity entity) {
				Timers timers = Mappers.tm.get(entity);
				Position pos = Mappers.pm.get(entity);
				Vector2 actualPos = pos != null ? pos.value : new Vector2(0,0);	//Are all these null checks necessary?
				
				if (timers == null) {
					timers = GameScreen.getEngine().createComponent(Timers.class);
					entity.add(timers);
				}
				
				SpawnerData[] minions = new SpawnerData[2];
				
				for (int i = 0 ; i < minions.length ; i++) {
					minions[i] = getMinion(actualPos);
				}
				
				minions[0].initialPosition.add(-6, -3);
				minions[1].initialPosition.add(4, -3);
			
				for (SpawnerData data : minions)
					timers.listeners.add(new Spawn(data));
			}
			
			@Override
			public void update (Entity entity) {
				autoshoot(entity);
				
				Hittable hit = Mappers.hm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (hit != null && ai != null) {
					if (hit.health / hit.maxHealth < 0.4f)
						ai.fsm.changeState(ATTACK_40_30);
				}
			}
		},
		
		ATTACK_40_30 {
			@Override
			public void enter (Entity entity) {
				Timers timers = Mappers.tm.get(entity);
				Position pos = Mappers.pm.get(entity);
				Vector2 actualPos = pos != null ? pos.value : new Vector2(0,0);
				
				if (timers == null) {
					timers = GameScreen.getEngine().createComponent(Timers.class);
					entity.add(timers);
				}
				
				SpawnerData minion = getMinion(actualPos);
				minion.initialPosition.add(-1, -3);
				timers.listeners.add(new Spawn(minion));
			}
			
			@Override
			public void update (Entity entity) {
				autoshoot(entity);
				
				Hittable hit = Mappers.hm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (hit != null && ai != null) {
					if (hit.getHealthPercent() < 0.3f)
						ai.fsm.changeState(ROTATE_TO_270);
				}
			}
		},
		
		ROTATE_TO_270 {
			@Override
			public void enter (Entity entity) {
				Mappers.gm.get(entity).guns.clear();
				Mappers.tm.get(entity).listeners.clear();
				
				AngularVelocity angVel = Mappers.avm.get(entity);
				if (angVel != null) angVel.value = MathUtils.PI / 12;			
			}
			
			@Override
			public void update (Entity entity) {
				Angle ang = Mappers.am.get(entity);
				AngularVelocity angVel = Mappers.avm.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				
				if (ang != null) {
					if (ang.value > 3f / 2f * MathUtils.PI) {
						ang.value = 3f / 2f * MathUtils.PI;
						angVel.value = 0;
						if (ai != null) ai.fsm.changeState(ATTACK_30_0);
					}
				}
			}
		},
		
		ATTACK_30_0 {
			@Override
			public void enter (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				
				if (gun != null) {
					gun.guns.clear();
					gun.guns.addAll(GameScreen.getBuilder().buildGunDataFL30To0());
				}
				
				Timers timers = Mappers.tm.get(entity);
				Position pos = Mappers.pm.get(entity);
				Vector2 actualPos = pos != null ? pos.value : new Vector2(0,0);
				
				if (timers == null) {
					timers = GameScreen.getEngine().createComponent(Timers.class);
					entity.add(timers);
				}
				
				SpawnerData minion = getMinion(actualPos);
				minion.initialPosition.add(-2, -5);
				timers.listeners.add(new Spawn(minion));
			}
			
			@Override
			public void update (Entity entity) {
				autoshoot(entity);
			}
		};
		
		private static final float SHOOT_INTERVAL = 1;
		
		private static void autoshoot (Entity entity){
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
		
		private static SpawnerData getMinion (Vector2 ownerPos) {
			SpawnerData minion = new SpawnerData();
			minion.id = "blue_cruiser";
			minion.initialPosition.set(ownerPos);
			minion.initialAngle = - MathUtils.PI / 2;
			minion.intervalTime = 3;
			minion.timerType = TimerType.INTERVAL;
			
			return minion;
		}
		
		@Override
		public void update (Entity entity) {
			
		}
		
		@Override
		public void enter (Entity entity) {
			
		}
		
		@Override
		public void exit (Entity entity) {
			
		}
		
		@Override
		public boolean onMessage (Entity entity, Telegram msg) {
			return false;
		}
		
	}

	
	public FirstLineAI(Entity owner) {
		super(owner);
		changeState(FirstLineState.ATTACK_100_70);
	}
}
