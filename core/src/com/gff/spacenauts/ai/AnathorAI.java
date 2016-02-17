package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ai.SteadyShooterAI.SteadyShooterState;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Steering;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.death.DropPowerUp;
import com.gff.spacenauts.screens.GameScreen;

public class AnathorAI extends DefaultStateMachine<Entity> {

	public enum AnathorState implements State<Entity> {
		IDLE {
			
		},
		
		REACH {
			@Override
			public void enter (Entity entity) {
				FSMAI ai = Mappers.aim.get(entity);
				Steering steering = GameScreen.getEngine().createComponent(Steering.class);
				AnathorAI fsm = (AnathorAI)ai.fsm;

				steering.adapter = SteeringMechanism.getFor(entity);
				steering.adapter.setMaxLinearSpeed(3);
				steering.adapter.setMaxLinearAcceleration(10);
				steering.adapter.setMaxAngularSpeed(MathUtils.PI / 2);
				steering.adapter.setMaxAngularAcceleration(MathUtils.PI / 2);
				Arrive<Vector2> behavior = new Arrive<Vector2>(steering.adapter, fsm.getDestination());
				behavior.setDecelerationRadius(3);
				behavior.setArrivalTolerance(0.1f);
				steering.behavior = behavior;
				entity.add(steering);
			}

			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					Steering steering = Mappers.stm.get(entity);
					Arrive<Vector2> behavior = (Arrive<Vector2>)steering.behavior;
					Position pos = Mappers.pm.get(entity);
					Vector2 destination = new Vector2();
					if (pos.value.x <= Globals.TARGET_CAMERA_WIDTH / 2) {
						destination.set(Globals.TARGET_CAMERA_WIDTH * 5 / 6, pos.value.y);
					} else {
						destination.set(Globals.TARGET_CAMERA_WIDTH / 6, pos.value.y);
					}
					behavior.setTarget(SteeringMechanism.getQuickTarget(destination));
				}
			}
		},

		RETREAT {
			
			@Override
			public void enter (Entity entity) {
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
				Position pos = Mappers.pm.get(entity);
				AnathorAI fsm = (AnathorAI)Mappers.aim.get(entity).fsm;
				
				fsm.disappearPoint.set(pos.value);
				Vector2 retreatDestination = new Vector2(pos.value).sub(0, -20);
				arrive.setTarget(SteeringMechanism.getQuickTarget(retreatDestination));
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					Hittable hit = Mappers.hm.get(entity);
					if (hit.getHealthPercent() > 0.25) {
						ai.fsm.changeState(RUSH_LTOR);
					} else {
						ai.fsm.changeState(RUSH_RTOL);
					}
				}
			}
			
		},
		
		RETURN {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Angle ang  = Mappers.am.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;
				AnathorAI fsm = (AnathorAI)Mappers.aim.get(entity).fsm;
				
				ang.value = 0;
				pos.value.set(fsm.disappearPoint).add(0, 20);
				arrive.setTarget(SteeringMechanism.getQuickTarget(fsm.disappearPoint));				
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					ai.fsm.changeState(REACH);
				}
			}
		},
		
		RUSH_LTOR {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Angle ang  = Mappers.am.get(entity);
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
				
				ang.value = MathUtils.PI / 2;
				pos.value.set(cameraPos).sub(20, 3);
				steering.adapter.setMaxLinearSpeed(7);
				Vector2 dest = new Vector2(pos.value).add(40, 0);
				arrive.setTarget(SteeringMechanism.getQuickTarget(dest));				
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					ai.fsm.changeState(RETURN);
				}
			}
		},
		
		RUSH_RTOL {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Angle ang  = Mappers.am.get(entity);
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
				
				ang.value = - MathUtils.PI / 2;
				pos.value.set(cameraPos).add(20, -3);
				steering.adapter.setMaxLinearSpeed(7);
				Vector2 dest = new Vector2(pos.value).sub(40, 0);
				arrive.setTarget(SteeringMechanism.getQuickTarget(dest));				
			}
			
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					ai.fsm.changeState(RETURN);
				}
			}
		},

		GLOBAL {
			private static final float SHOOT_INTERVAL = 1f;

			@Override
			public void update (Entity entity) {
				Hittable hit = Mappers.hm.get(entity);
				float health = hit.getHealthPercent();
				AnathorAI fsm = (AnathorAI)Mappers.aim.get(entity).fsm;
				
				if (Mappers.wcm.get(GameScreen.getEngine().getCamera()).stopped
						&& !fsm.initialSpawn) {
					
					while (fsm.minionCount < AnathorAI.MINION_LIMIT) {
						spawnMinion(fsm);
					}
					fsm.initialSpawn = true;
				}

				//After health hits 80%, it checks whether there are less minions than thought
				//If so, there's a 1/600 chance to spawn a new one.
				//Since this is updated 100 times/sec it will take 6-ish seconds on average.
				if (health <= 0.8f) {
					if (fsm.getMinionCount() < AnathorAI.MINION_LIMIT) {
						if (MathUtils.random(600) == 0) {
							spawnMinion(fsm);
						}
					}
				}
				
				Gun gun = Mappers.gm.get(entity);
					for (GunData data : gun.guns){
						if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
							data.triggered = true;
						}
					}
					
				if (0.5f < health && health <= 0.8f) {
					Position pos = Mappers.pm.get(entity);
					Vector2 destination = new Vector2();
					if (pos.value.x <= Globals.TARGET_CAMERA_WIDTH / 2) {
						destination.set(Globals.TARGET_CAMERA_WIDTH / 6, pos.value.y);
					} else {
						destination.set(Globals.TARGET_CAMERA_WIDTH * 5 / 6, pos.value.y);
					}
					fsm.setDestination(SteeringMechanism.getQuickTarget(destination));
					if (fsm.getCurrentState() != REACH) fsm.changeState(REACH);
				}
				
				if (0.25f < health && health <= 0.5f) {
					if (!fsm.madeFirstRush) {
						fsm.changeState(RETREAT);
						fsm.madeFirstRush = true;
					}
				}
				
				if (health <= 0.25f) {
					if (!fsm.madeSecondRush) {
						fsm.changeState(RETREAT);
						fsm.madeSecondRush = true;
					}
				}

			};
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

		private static void spawnMinion (final AnathorAI anathor) {
			Entity minion = GameScreen.getBuilder().buildWyvern();
			final Vector2 destination = anathor.getSpots().pop();
			Position pos = Mappers.pm.get(minion);
			FSMAI ai = Mappers.aim.get(minion);
			Death death = Mappers.dem.get(minion);

			Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
			pos.value.set(cameraPos).sub(0, -20);
			SteadyShooterAI fsm = (SteadyShooterAI)ai.fsm;
			fsm.getReachPosition().set(destination);
			fsm.changeState(SteadyShooterState.REACH);
			
			death.listeners.addListener(new DeathListener () {
				@Override
				public void onDeath(Entity entity) {
					anathor.setMinionCount(anathor.getMinionCount() - 1);
					anathor.spotStack.add(destination);
				}
			});
			
			int powerUpChance = MathUtils.random(1000);
			String powerup;
			
			if (powerUpChance < 250) {
				if (100 < powerUpChance) powerup = "HEALTH10";
				else if (50 < powerUpChance) powerup = "TRIGUN";
				else if (25 < powerUpChance) powerup = "HEALTH25";
				else powerup = "SHIELD";
				
				death.listeners.addListener(new DropPowerUp(powerup));
			}
			
			anathor.setMinionCount(anathor.minionCount + 1);
			
			GameScreen.getEngine().addEntity(minion);
		}

	}

	public static final int MINION_LIMIT = 4;
	private static final float RADIUS = 5;

	/*
	 *  About the spotStack:
	 * 
	 *  It's a stack containing locations for minions to spawn. 
	 *  The spots are positioned at the edge of a circle, centered to the camera's current position,
	 *  equally distributed, anti-clockwise. Ex (4 minions):
	 *  
	 *    
	 *  c       d
	 *     cam
	 *  b       a
	 */

	private Steerable<Vector2> destination;
	private Array<Vector2> spotStack;
	private Vector2 disappearPoint = new Vector2();
	private int minionCount = 0;
	private boolean initialSpawn = false;
	private boolean madeFirstRush = false;
	private boolean madeSecondRush = false;

	public AnathorAI(Entity owner) {
		super(owner, AnathorState.IDLE, AnathorState.GLOBAL);
		destination = null;
	}

	public Steerable<Vector2> getDestination() {
		return destination;
	}

	public void setDestination(Steerable<Vector2> destination) {
		this.destination = destination;
	}

	public int getMinionCount () {
		return minionCount;
	}

	public void setMinionCount (int minionCount) {
		this.minionCount = minionCount;
	}

	public Array<Vector2> getSpots () {
		if (spotStack == null) {
			spotStack = new Array<Vector2>(MINION_LIMIT);
			Vector2 camPos = GameScreen.getEngine().getCameraPosition();

			for (int i = 0 ; i < MINION_LIMIT ; i++) {
				Vector2 spot = new Vector2(camPos);
				float ang = - (i + 0.5f) / MINION_LIMIT * 2 * MathUtils.PI;
				spot.add(MathUtils.cos(ang) * RADIUS, MathUtils.sin(ang) * RADIUS);
				spotStack.add(spot);
			}
		}
		return spotStack;
	}
}
