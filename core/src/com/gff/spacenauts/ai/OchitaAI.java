package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.Steerable;
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
import com.gff.spacenauts.listeners.death.DeathListeners;
import com.gff.spacenauts.listeners.shoot.RandomizeAngle;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Aka the incarnation of evil, this AI scripts the behavior of Ochita Weapon
 * (courtesy of Momo Ochita). So in short, it's divided in many phases:
 * 
 * <h1>From 100% to 80%</h1>
 * <p>Ochita Weapon moves shooting, from the left to the right, and vice versa.
 * Once it reaches the edge it stops for 2 seconds, letting its shield down, and 
 * stops shooting. Then it repeats.</p>
 * 
 * <h1>At 80%</h1>
 * <p>It will let its shield up again and start shooting lasers at random directions. 
 * Two adds will spawn at the sides of the arena. Once they're dead, Ochita will 
 * transition into the next phase.</p>
 * 
 * <h1>From 80% to 50%</h1>
 * <p>The boss will try to rush on the player, dealing contact damage. After that it will
 * retreat back but it will lose its shield. Once it's back in position, the shield again up,
 * it will reach the leftmost edge of the screen. A small barrier will appear at the
 * lower edge of the screen. Ochita will start shooting a death ray, from left to right.
 * The player must hide behind the barrier or die brutally.</p>
 * 
 * <h1>From 50% to 20%</h1>
 * <p>At the beginning of this phase the boss will pop up its shield and emit a red or 
 * green aura (chosen randomly). The player will be infected with a red/green debuff. 
 * Ochita will start shooting red and green projectiles: if the color matches the player's 
 * debuff, it will heal x0.75 damage, up to 100% health. If not, it will deal 2x damage. 
 * After 10 seconds in this phase, the boss will start shooting two death lasers and depop 
 * its shield, starting horizontally, then slowly approaching the vertical axis covering 
 * a fan-shaped area.  If the player can't manage to bring down the boss to 20% within 
 * 30 seconds, the lasers will converge to the player, trapping him and causing death.</p>
 * 
 * <h1>From 20% to 0%</h1>
 * <p>Aka FULL DPS. Ochita will go nuts with bullets shooting 3 bullets at time who spread
 * into other 8 each; its shield will no longer be up. Furthermore, two other adds will
 * spawn and start shooting lasers while walking to random locations. Get that thing
 * dead ASAP.</p> 
 * 
 * @author Alessio
 *
 */
public class OchitaAI extends DefaultStateMachine<Entity> {

	public enum OchitaState implements State<Entity> {
		IDLE {

		},
		/**
		 * Stop moving and attacking for 2 seconds.
		 */
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
				ai.phaseTimer += AISystem.DELTA;

				if (ai.phaseTimer > COOLDOWN_DURATION) {
					ai.phaseTimer = 0;
					ai.changeState(PHASE_1);
				}
			}
		},

		/**
		 * Go left and right, cooling down every time you reach the edge.
		 */
		PHASE_1 {
			private static final float SHOOT_INTERVAL = 0.75f;

			private Vector2 destination = new Vector2();
			private Steerable<Vector2> destinationSteerable = SteeringMechanism.getQuickTarget(destination);

			@Override
			public void enter (Entity entity) {
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Steering str = Mappers.stm.get(entity);

				//Init behavior if not done.
				if (str.behavior == null) {
					Arrive<Vector2> arrive = new Arrive<Vector2>(str.adapter); 
					arrive.setArrivalTolerance(0.1f);
					arrive.setDecelerationRadius(1);
					str.behavior = arrive;
				}

				fsm.buildOchitaShield(entity);
				calculateDestination(entity);
			}

			@Override
			public void update (Entity entity) {
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;

				if (reachedTarget(entity)) {
					Velocity vel = Mappers.vm.get(entity);

					vel.value.setZero();
					fsm.changeState(P1_COOLDOWN);
				}

				autoshoot(entity, SHOOT_INTERVAL);
			}

			private void calculateDestination (Entity entity) {
				Position pos = Mappers.pm.get(entity);

				if (pos.value.x <= Globals.TARGET_CAMERA_WIDTH / 2) 
					destination.set(Globals.TARGET_CAMERA_WIDTH * 9 / 10, pos.value.y);

				else 
					destination.set(Globals.TARGET_CAMERA_WIDTH / 10, pos.value.y);

				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
				arrive.setTarget(destinationSteerable);
			}
		},

		/**
		 * Retreat to the top-center of the screen and raise shields.
		 */
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

		/**
		 * Spawn the minions and attack randomly.
		 */
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

					//Place outside the screen, to reach slighly under the camera center, to the sides
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
				autoshoot(entity, SHOOT_INTERVAL);

				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;

				if (fsm.getMinionCount() <= 0) 
					fsm.changeState(PHASE_3_RUSH);
			}

			@Override
			public void exit (Entity entity) {
				Gun gun = Mappers.gm.get(entity);

				//Remove the random shooting listeners 
				for (GunData data : gun.guns) 
					data.gunShotListeners.getListeners().pop();
			}
		},

		/**
		 * Record the player position, and use {@link Arrive} to reach it.
		 */
		PHASE_3_RUSH {

			private Vector2 rushDestination = new Vector2();
			private Steerable<Vector2> rushSteerable = SteeringMechanism.getQuickTarget(rushDestination);

			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Position playerPos = Mappers.pm.get(GameScreen.getEngine().getPlayer());

				steering.adapter.setMaxLinearSpeed(10);

				rushDestination.set(playerPos.value);
				arrive.setTarget(rushSteerable);

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

		/**
		 * Retrat back to the pre-rush position, slowly, with shields down.
		 */
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

		/**
		 * Slowly reach the left side. Also summon the barrier.
		 */
		PHASE_3_START_LASER {

			private Vector2 destination = new Vector2();
			private Steerable<Vector2> destinationSteerable = SteeringMechanism.getQuickTarget(destination);

			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;

				destination.set(pos.value);
				destination.x = Globals.TARGET_CAMERA_WIDTH / 15;

				steering.adapter.setMaxLinearSpeed(2);

				arrive.setTarget(destinationSteerable);

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

		/**
		 * Shoot dat death laser!
		 */
		PHASE_3_LASER {

			private static final float SHOOT_INTERVAL = 0.1f;
			private Vector2 destination = new Vector2();
			private Steerable<Vector2> destinationSteerable = SteeringMechanism.getQuickTarget(destination);

			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Gun gun = Mappers.gm.get(entity);
				Steering steering = Mappers.stm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;

				steering.adapter.setMaxLinearSpeed(2);

				destination.set(pos.value);
				destination.x = Globals.TARGET_CAMERA_WIDTH * 14 / 15;				
				arrive.setTarget(destinationSteerable);

				for (GunData data : gun.guns) {
					data.bulletImage = GameScreen.getBuilder().getSpriteCache().get("bullet_red");
					data.bulletDamage = 30;
				}

				fsm.removeShield();
			}

			@Override
			public void update (Entity entity) {
				autoshoot(entity, SHOOT_INTERVAL);

				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Velocity vel = Mappers.vm.get(entity);
					vel.value.setZero();
					ai.fsm.changeState(PHASE_3_RESET);
				}
			}
		},

		/**
		 * Return back to the center.
		 */
		PHASE_3_RESET {

			private Vector2 destination = new Vector2();
			private Steerable<Vector2> destinationSteerable = SteeringMechanism.getQuickTarget(destination);

			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;

				destination.set(Globals.TARGET_CAMERA_WIDTH / 2, pos.value.y);
				arrive.setTarget(destinationSteerable);

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

		/**
		 * Returns to the upper center, and goes to P4.
		 * Since info on how much high "upper" is might be lost, it is
		 * set arbitrarily to cameraPos.y + TARGET_CAMERA_HEIGHT / 3
		 */
		P3_TO_P4 {

			private Vector2 destination = new Vector2();
			private Steerable<Vector2> destinationSteerable = SteeringMechanism.getQuickTarget(destination);

			@Override
			public void enter (Entity entity) {
				Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
				OchitaAI fsm = (OchitaAI) Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;

				destination.set(Globals.TARGET_CAMERA_WIDTH / 2, cameraPos.y + Globals.TARGET_CAMERA_HEIGHT / 3 );
				arrive.setTarget(destinationSteerable);

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

		/**
		 * Red and green lasers.
		 */
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

				//Get the aura, an overlay effect, and immediately destroy it to activate
				//its effect (fadeout + debuff)
				Entity aura = fsm.getAura();
				Death d = Mappers.dem.get(aura);
				d.listeners.onDeath(aura);

				fsm.phaseTimer = 0;

				//Lower player's health by 20 points, but don't kill him
				playerHit.health -= 20;
				if (playerHit.health <= 0) playerHit.health = 1;
			}

			@Override
			public void update (Entity entity) {
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;

				fsm.phaseTimer += AISystem.DELTA;

				if (fsm.phaseTimer >= PHASE_DURATION) {
					fsm.phaseTimer = 0;
					fsm.changeState(PHASE_5);
				}

				autoshoot(entity, SHOOT_INTERVAL);
			}
		},

		/**
		 * Death lasers coming at you!
		 */
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

				autoshoot(entity, SHOOT_INTERVAL);
			}
		},

		/**
		 * FULL DPS!
		 */
		PHASE_6 {

			private final static float SHOOT_INTERVAL = 1.25f;

			@Override
			public void enter (Entity entity) {
				Gun gun = Mappers.gm.get(entity);

				gun.guns.clear();
				gun.guns.addAll(GameScreen.getBuilder().buildOchitaSpreadGun());

				EntityBuilder b = GameScreen.getBuilder();
				SpacenautsEngine e = GameScreen.getEngine();

				//Spawns two last minion; they will walk randomly
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
				autoshoot(entity, SHOOT_INTERVAL);
			}
		},
		/**
		 * The Global state. Its main concern is switching to the correct
		 * phase starter depending on health. It also syncs the shield
		 * with Ochita.
		 */
		GLOBAL {
			@Override
			public void update(Entity entity) {
				super.update(entity);

				Hittable hit = Mappers.hm.get(entity);
				OchitaAI fsm = (OchitaAI)Mappers.aim.get(entity).fsm;
				State<Entity> state = fsm.getCurrentState();
				Entity shield = fsm.getShield();

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

		/**
		 * Determines whether this entity's Arrive behavior (supposed it has one)
		 * reached is target.
		 * 
		 * @param entity
		 * @return
		 */
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

		/**
		 * Simply compares each gun's timer with a fixed value, and 
		 * if that timer is higher the gun is triggered.
		 * 
		 * @param entity The entity for which guns must be shot
		 * @param delta The interval of time over which autoshooting
		 */
		private static void autoshoot (Entity entity, float delta) {
			Gun gun = Mappers.gm.get(entity);

			for (GunData data : gun.guns){
				if (data.shootingTimer > delta && !data.triggered){
					data.triggered = true;
				}
			}
		}
	}

	//Stored entity references
	private Entity shield;
	private Entity barrier;
	private Entity aura;
	
	//Various helper variables
	public float phaseTimer = 0;
	private int minionCount = 0;
	private Vector2 preRushPosition = new Vector2();

	public OchitaAI(Entity owner) {
		super(owner, OchitaState.IDLE, OchitaState.GLOBAL);
	}

	/**
	 * Builds an enemy shield from EntityBuilder and sizes it to
	 * Ochita (basically sets scale to 1.25).
	 * 
	 * @param ochita
	 */
	public void buildOchitaShield(Entity ochita) {
		if (shield == null) {
			shield = GameScreen.getBuilder().buildEnemyShield(ochita);
			Render r  = Mappers.rm.get(shield);
			if (r != null) r.scaleX = r.scaleY = 1.25f * Globals.UNITS_PER_PIXEL;
			GameScreen.getEngine().addEntity(shield);
		}
	}

	/**
	 * Returns the shield, if one is currently available.
	 * 
	 * @return the shield, or null if the shield is disabled.
	 */
	public Entity getShield () {
		return shield;
	}

	/**
	 * Tears down the shield, removing it from the engine.
	 */
	public void removeShield() {
		if (shield != null) GameScreen.getEngine().removeEntity(shield);
		shield = null;
	}

	/**
	 * Minion count getter. Used to transition to phase 3.
	 * 
	 * @return the minion count.
	 */
	public int getMinionCount () {
		return minionCount;
	}

	/**
	 * Minion count setter. Used to transition to phase 3.
	 * 
	 * @param minionCount the minion count.
	 */
	public void setMinionCount(int minionCount) {
		this.minionCount = minionCount;
	}

	/**
	 * Used to store position before a rush in phase 3.
	 * 
	 * @return
	 */
	private Vector2 getPreRushPosition () {
		return preRushPosition;
	}

	/**
	 * Builds a barrier using EntityBuilder and then sets its Arrive
	 * parameters so that:
	 * 
	 * <ul>
	 * <li>It appears from the left</li>
	 * <li>It reaches a point in the lower end of the screen, around the center.</li>
	 * </ul>
	 */
	public void invokeBarrier () {
		if (barrier == null) {
			barrier = GameScreen.getBuilder().buildOchitaBarrier();
			
			Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
			Position pos = Mappers.pm.get(barrier);
			SteadyShooterAI ai = (SteadyShooterAI)Mappers.aim.get(barrier).fsm;

			//The barrier is placed at a x between [-1/3,1/3] of the camera width from the camera center
			//The y is fixed.
			pos.value.set(cameraPos).sub(10, 4);
			float xPos = (2 * MathUtils.random() - 1) * Globals.TARGET_CAMERA_WIDTH / 3;
			
			ai.getReachPosition().set(cameraPos).add(xPos, -4);
			ai.changeState(SteadyShooterState.REACH);
			
			GameScreen.getEngine().addEntity(barrier);
		}
	}

	/**
	 * Removes the current barrier by calling its {@link DeathListeners}.
	 */
	public void destroyBarrier () {
		if (barrier != null) {
			Death d = Mappers.dem.get(barrier);
			d.listeners.onDeath(barrier);
			barrier = null;
		}
	}

	/**
	 * Setter for the aura Entity from Phase 4
	 * 
	 * @param aura
	 */
	public void setAura(Entity aura) {
		this.aura = aura;
	}

	/**
	 * Getter for the aura Entity from Phase 4
	 * 
	 * @return the aura Entity previously stored.
	 */
	public Entity getAura () {
		return aura;
	}
}
