package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ai.PowerUpAI.PowerUpState;
import com.gff.spacenauts.ai.SteadyShooterAI.SteadyShooterState;
import com.gff.spacenauts.ashley.EntityBuilder;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.systems.AISystem;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.shoot.RandomizeAngle;
import com.gff.spacenauts.screens.GameScreen;

public class OchitaAI extends DefaultStateMachine<Entity> {

	public enum OchitaState implements State<Entity> {
		IDLE {

		},
		P1_COOLDOWN {
			
			private static final float COOLDOWN_DURATION = 2;
			
			@Override
			public void enter (Entity entity) {
				OchitaAI ai = (OchitaAI)Mappers.aim.get(entity).fsm;
				ai.removeShield();
			}
			
			@Override
			public void update (Entity entity) {
				OchitaAI ai = (OchitaAI)Mappers.aim.get(entity).fsm;
				ai.cooldownTimer += AISystem.DELTA;
				
				if (ai.cooldownTimer > COOLDOWN_DURATION) {
					ai.cooldownTimer = 0;
					ai.changeState(PHASE_1);
				}
			}
		},
		PHASE_1 {
			private static final float SHOOT_INTERVAL = 0.75f;

			@Override
			public void enter (Entity entity) {
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Steering str = Mappers.stm.get(entity);
				if (str.behavior == null) {
					Arrive<Vector2> arrive = new Arrive<Vector2>(str.adapter); 
					arrive.setArrivalTolerance(0.1f);
					arrive.setDecelerationRadius(1);
					str.behavior = arrive;
				}

				fsm.buildOchitaShield(entity);
				fsm.calculateDestination(entity);
			}

			@Override
			public void update (Entity entity) {
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;

				if (reachedTarget(entity)) {
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					fsm.changeState(P1_COOLDOWN);
				}

				Gun gun = Mappers.gm.get(entity);
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}
			}
		},
		P1_TO_P2 {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
				
				arrive.setTarget(SteeringMechanism.getQuickTarget(new Vector2(Globals.TARGET_CAMERA_WIDTH / 2, pos.value.y)));
				fsm.buildOchitaShield(entity);
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_2);
				}
			}
		},
		PHASE_2 {
			
			private final static float SHOOT_INTERVAL = 0.75f;
			
			@Override
			public void enter (Entity entity) {
				EntityBuilder b = GameScreen.getBuilder();
				SpacenautsEngine e = GameScreen.getEngine();
				OchitaAI ochitaAi = (OchitaAI) Mappers.aim.get(entity).fsm;
				final Entity fEntity = entity;
				
				for (int i = 0 ; i < 2 ; i++) {
					Entity minion = b.buildOchitaMinion1();
					Position pos = Mappers.pm.get(minion);
					SteadyShooterAI ai = (SteadyShooterAI)Mappers.aim.get(minion).fsm;
					Death death = Mappers.dem.get(minion);
					Vector2 cameraPos = e.getCameraPosition();
					
					int mod = i == 0 ? 1 : -1;
					
					pos.value.set(cameraPos).add(mod * 10, 0.5f);
					ai.getReachPosition().set(cameraPos).add(mod * 8, 0.5f);
					ai.changeState(SteadyShooterState.REACH);
					death.listeners.addListener(new DeathListener () {

						@Override
						public void onDeath(Entity entity) {
							OchitaAI ai = (OchitaAI) Mappers.aim.get(fEntity).fsm;
							ai.setMinionCount(ai.getMinionCount() - 1);
						}
						
					});
					e.addEntity(minion);
					ochitaAi.setMinionCount(ochitaAi.getMinionCount() + 1);
				}
				
				Gun gun = Mappers.gm.get(entity);
				
				for (GunData data : gun.guns) 
					data.gunShotListeners.addListener(new RandomizeAngle(- MathUtils.PI / 7, MathUtils.PI / 7));
				
			}
			
			@Override
			public void update (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}
				
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				
				if (fsm.getMinionCount() <= 0) 
					fsm.changeState(PHASE_3_RUSH);
			}
			
			@Override
			public void exit (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				
				for (GunData data : gun.guns) 
					data.gunShotListeners.getListeners().pop();
			}
		},
		PHASE_3_RUSH {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				Position playerPos = Mappers.pm.get(GameScreen.getEngine().getPlayer());
				
				steering.adapter.setMaxLinearSpeed(10);
				
				arrive.setTarget(SteeringMechanism.getQuickTarget(new Vector2(playerPos.value)));
				fsm.getPreRushPosition().set(pos.value);
				fsm.buildOchitaShield(entity);
				fsm.destroyBarrier();
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_3_RETREAT);
				}
			}
		},
		PHASE_3_RETREAT {
			@Override
			public void enter (Entity entity) {
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				
				steering.adapter.setMaxLinearSpeed(4);
				
				arrive.setTarget(SteeringMechanism.getQuickTarget(fsm.getPreRushPosition()));
				fsm.removeShield();
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_3_START_LASER);
				}
			}
		},
		PHASE_3_START_LASER {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				Vector2 destination = new Vector2(pos.value);
				destination.x = Globals.TARGET_CAMERA_WIDTH / 15;
				steering.adapter.setMaxLinearSpeed(2);
				arrive.setTarget(SteeringMechanism.getQuickTarget(destination));
				fsm.buildOchitaShield(entity);
				fsm.invokeBarrier();
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_3_LASER);
				}
			}
		},
		PHASE_3_LASER {
			
			private static final float SHOOT_INTERVAL = 0.1f;
			
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Gun gun = Mappers.gm.get(entity);
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				Vector2 destination = new Vector2(pos.value);
				destination.x = Globals.TARGET_CAMERA_WIDTH * 14 / 15;
				
				arrive.setTarget(SteeringMechanism.getQuickTarget(destination));
				
				for (GunData data : gun.guns) {
					data.bulletImage = GameScreen.getBuilder().getSpriteCache().get("bullet_red");
					data.bulletDamage = 30;
				}
				
				steering.adapter.setMaxLinearSpeed(2);
				
				fsm.removeShield();
			}
			
			@Override
			public void update (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}
				
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_3_RESET);
				}
			}
		},
		PHASE_3_RESET {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
				
				arrive.setTarget(SteeringMechanism.getQuickTarget(new Vector2(Globals.TARGET_CAMERA_WIDTH / 2, pos.value.y)));
				fsm.buildOchitaShield(entity);
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_3_RUSH);
				}
			}
		},
		P3_TO_P4 {
			@Override
			public void enter (Entity entity) {
				Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
				
				arrive.setTarget(SteeringMechanism.getQuickTarget(new Vector2(Globals.TARGET_CAMERA_WIDTH / 2, cameraPos.y + Globals.TARGET_CAMERA_HEIGHT / 3 )));
				fsm.buildOchitaShield(entity);
				fsm.destroyBarrier();
				
				/*
				 * Prepares the aura for the color debuff
				 * Addition and death triggering happen in two distinct phases
				 * Because the entity insertion in the Engine is delayed.
				 * Doing addEntity -> die immediately causes a strange bug
				 * where clearing the entity pools causes an infinite loop.
				 */
				
				Entity aura = GameScreen.getBuilder().buildAura(MathUtils.randomBoolean());
				GameScreen.getEngine().addEntity(aura);
				fsm.setAura(aura);
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_4);
				}
			}
		},
		PHASE_4 {
			
			private static final float PHASE_DURATION = 10;
			private static final float SHOOT_INTERVAL = 0.7f;
			
			@Override
			public void enter (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;
				Hittable playerHit = Mappers.hm.get(GameScreen.getEngine().getPlayer());
				gun.guns.clear();
				gun.guns.addAll(GameScreen.getBuilder().buildOchitaRedGreenGuns());
				
				//Create the aura, an overlay effect, and immediately destroy it to activate
				//its effect (fadeout + debuff)
				Entity aura = fsm.getAura();
				Death d = Mappers.dem.get(aura);
				d.listeners.onDeath(aura);
				fsm.cooldownTimer = 0;
				playerHit.health -= 20;
				if (playerHit.health <= 0) playerHit.health = 1;
			}
			
			@Override
			public void update (Entity entity) {
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;
				
				fsm.cooldownTimer += AISystem.DELTA;
				
				if (fsm.cooldownTimer >= PHASE_DURATION) {
					fsm.cooldownTimer = 0;
					fsm.changeState(PHASE_5);
				}
				
				Gun gun = Mappers.gm.get(entity);
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}
			}
		},
		PHASE_5 {
			
			//The angle increase at each step
			//The lasers are supposed to move by Pi / 2 in 30 seconds. 
			private final static float DELTA_ANGLE = MathUtils.PI / 60 * AISystem.DELTA;
			private final static float SHOOT_INTERVAL = 0.1f;
			
			@Override
			public void enter (Entity entity) {
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;
				PowerUpAI powerupAi = (PowerUpAI)Mappers.aim.get(GameScreen.getEngine().getPlayer()).fsm;
				Gun gun = Mappers.gm.get(entity);
				
				fsm.removeShield();
				powerupAi.changeState(PowerUpState.NORMAL);
				gun.guns.clear();
				gun.guns.addAll(GameScreen.getBuilder().buildOchitaCrossLasers());
			}
			
			@Override
			public void update (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				
				gun.guns.get(0).aOffset += DELTA_ANGLE;
				gun.guns.get(1).aOffset -= DELTA_ANGLE;
				
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}
			}
		},
		PHASE_6 {
			
			private final static float SHOOT_INTERVAL = 1.25f;
			
			@Override
			public void enter (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
			
				gun.guns.clear();
				gun.guns.addAll(GameScreen.getBuilder().buildOchitaSpreadGun());
				
				EntityBuilder b = GameScreen.getBuilder();
				SpacenautsEngine e = GameScreen.getEngine();
				
				//Spawns two last minion that will walk randomly
				for (int i = 0 ; i < 2 ; i++) {
					Entity minion = b.buildOchitaMinion2();
					Position pos = Mappers.pm.get(minion);
					Vector2 cameraPos = e.getCameraPosition();
					
					int mod = i == 0 ? 1 : -1;
					
					pos.value.set(cameraPos).add(mod * 10, 0.5f);
					e.addEntity(minion);
				}
			}
			
			@Override
			public void update (Entity entity) {
				Gun gun = Mappers.gm.get(entity);
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}
			}
		},
		GLOBAL {
			@Override
			public void update(Entity entity) {
				super.update(entity);
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;
				State<Entity> state = fsm.getCurrentState();
				Entity shield = fsm.getShield();
				Hittable hit = Mappers.hm.get(entity);
				float health = hit.getHealthPercent();

				if (health > 0.8 && state != PHASE_1 && state != P1_COOLDOWN) 
					fsm.changeState(PHASE_1);
				
				if (health <= 0.8 && health > 0.5
						&& state != P1_TO_P2
						&& state != PHASE_2
						&& state != PHASE_3_RUSH
						&& state != PHASE_3_RETREAT
						&& state != PHASE_3_START_LASER
						&& state != PHASE_3_LASER
						&& state != PHASE_3_RESET) {
					fsm.changeState(P1_TO_P2);
				}
				
				if (health <= 0.5 && health > 0.20
						&& state != P3_TO_P4
						&& state != PHASE_4
						&& state != PHASE_5) {
					fsm.changeState(P3_TO_P4);
				}
				
				if (health <= 0.20 && state != PHASE_6)
					fsm.changeState(PHASE_6);

				if (shield != null) {
					Position ocPos = Mappers.pm.get(entity);
					Angle ocAng = Mappers.am.get(entity);

					Position pos = Mappers.pm.get(shield);
					Angle ang = Mappers.am.get(shield);
					Body body = Mappers.bm.get(shield);

					pos.value.set(ocPos.value);
					ang.value = ocAng.value;
					body.polygon.setPosition(pos.value.x, pos.value.y);
					body.polygon.setRotation(ocAng.getAngleDegrees());
				}
			}
		};

		@Override
		public void enter(Entity entity) {

		}

		@Override
		public void update(Entity entity) {

		}

		@Override
		public void exit(Entity entity) {

		}

		@Override
		public boolean onMessage(Entity entity, Telegram telegram) {
			return false;
		}

		private static boolean reachedTarget (Entity entity) {
			Steering steering = Mappers.stm.get(entity);

			if (steering != null) {
				if (steering.behavior instanceof Arrive){
					Arrive<Vector2> behavior = (Arrive<Vector2>)steering.behavior;
					if (steering.adapter.getPosition().dst(behavior.getTarget().getPosition()) < behavior.getArrivalTolerance()){
						return true;
					}
				}
			}
			return false;
		}
	}

	private Entity shield;
	private Entity barrier;
	private Entity aura;
	public float cooldownTimer = 0;
	private int minionCount = 0;
	private Vector2 preRushPosition = new Vector2();

	public OchitaAI(Entity owner) {
		super(owner, OchitaState.IDLE, OchitaState.GLOBAL);
	}

	public void buildOchitaShield(Entity ochita) {
		if (shield == null) {
			shield = GameScreen.getBuilder().buildEnemyShield(ochita);
			Render r  = Mappers.rm.get(shield);
			if (r != null) r.scaleX = r.scaleY = 1.25f * Globals.UNITS_PER_PIXEL;
			GameScreen.getEngine().addEntity(shield);
		}
	}

	public Entity getShield () {
		return shield;
	}

	public void removeShield() {
		if (shield != null) GameScreen.getEngine().removeEntity(shield);
		shield = null;
	}

	public void calculateDestination (Entity entity) {
		Position pos = Mappers.pm.get(entity);
		Vector2 destination = new Vector2();
		if (pos.value.x <= Globals.TARGET_CAMERA_WIDTH / 2) {
			destination.set(Globals.TARGET_CAMERA_WIDTH * 9 / 10, pos.value.y);
		} else {
			destination.set(Globals.TARGET_CAMERA_WIDTH / 10, pos.value.y);
		}
		Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
		arrive.setTarget(SteeringMechanism.getQuickTarget(destination));
	}
	
	public int getMinionCount () {
		return minionCount;
	}
	
	public void setMinionCount(int minionCount) {
		this.minionCount = minionCount;
	}
	
	private Vector2 getPreRushPosition () {
		return preRushPosition;
	}
	
	public void invokeBarrier () {
		if (barrier == null) {
			barrier = GameScreen.getBuilder().buildOchitaBarrier();
			Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
			Position pos = Mappers.pm.get(barrier);
			SteadyShooterAI ai = (SteadyShooterAI)Mappers.aim.get(barrier).fsm;
			
			pos.value.set(cameraPos).sub(10, 4);
			//The barrier is placed at a x between [-1/3,1/3] of the camera width from the camera center
			//The y is fixed.
			float xPos = (2 * MathUtils.random() - 1) * Globals.TARGET_CAMERA_WIDTH / 3;			
			ai.getReachPosition().set(cameraPos).add(xPos, -4);
			ai.changeState(SteadyShooterState.REACH);
			GameScreen.getEngine().addEntity(barrier);
		}
	}
	
	public void destroyBarrier () {
		if (barrier != null) {
			Death d = Mappers.dem.get(barrier);
			d.listeners.onDeath(barrier);
			barrier = null;
		}
	}
	
	public void setAura(Entity aura) {
		this.aura = aura;
	}
	
	public Entity getAura () {
		return aura;
	}
}
