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
import com.gff.spacenauts.ashley.components.WorldCamera;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.death.DropPowerUp;
import com.gff.spacenauts.screens.GameScreen;

/**
 * <p>The AI for Anathor, the boss from Level 2.</p>
 * 
 * <h1>From 100% to 80% HP</h1>
 * <p>Anathor will stand still and spit fire in three directions. Minions won't respawn on death</p>
 * 
 * <h1>From 80% to 0%</h1>
 * <p>Anathor will move left to right and vice versa at top of the screen, while steel spitting fire.
 * Minions will respawn on occasion up to a certain limit and might yield power ups.</p>
 * 
 * <h1>At 50% and 25%</h1>
 * <p>Anathor will retreat and try to surprise the player, rushing mid-screen. Friendly known as
 * "Divebombs" (I wonder who will get the reference...)</p> 
 * 
 * @author Alessio
 *
 */
public class AnathorAI extends DefaultStateMachine<Entity> {

	public enum AnathorState implements State<Entity> {
		IDLE {

		},

		/**
		 * Move the entity from the left side of the screen to the right, and vice versa,
		 * using {@link Arrive}.
		 */
		LEFT_TO_RIGHT {	
			private Vector2 destination = new Vector2();
			private Steerable<Vector2> destinationSteerable = SteeringMechanism.getQuickTarget(destination);

			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Steering steering = GameScreen.getEngine().createComponent(Steering.class);

				steering.adapter = SteeringMechanism.getFor(entity);
				steering.adapter.setMaxLinearSpeed(3);
				steering.adapter.setMaxLinearAcceleration(10);
				steering.adapter.setMaxAngularSpeed(MathUtils.PI / 2);
				steering.adapter.setMaxAngularAcceleration(MathUtils.PI / 2);

				Arrive<Vector2> behavior = new Arrive<Vector2>(steering.adapter, destinationSteerable);
				behavior.setDecelerationRadius(3);
				behavior.setArrivalTolerance(0.1f);
				steering.behavior = behavior;

				entity.add(steering);
				
				//Set destination to current position so reachedTarget will be triggered
				destination.set(pos.value);	
			}

			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) 
					evaluateNextDestination(entity);
			}
			
			private void evaluateNextDestination (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Steering steering = Mappers.stm.get(entity);
				Arrive<Vector2> behavior = (Arrive<Vector2>)steering.behavior;

				if (pos.value.x <= Globals.TARGET_CAMERA_WIDTH / 2)
					destination.set(Globals.TARGET_CAMERA_WIDTH * 5 / 6, pos.value.y);

				else 
					destination.set(Globals.TARGET_CAMERA_WIDTH / 6, pos.value.y);

				behavior.setTarget(destinationSteerable);
			}
		},

		/**
		 * Save your current position, then use {@link Arrive} to retreat back to an unseen point.
		 * After that make a rush depending on you health : Left To Right if over 25% HP,
		 * Right to Left if under 25%.
		 */
		RETREAT {

			private Vector2 retreatDestination = new Vector2();
			private Steerable<Vector2> retreatSteerable = SteeringMechanism.getQuickTarget(retreatDestination);

			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				AnathorAI fsm = (AnathorAI)Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;

				fsm.disappearPoint.set(pos.value);
				retreatDestination.set(pos.value).sub(0, -20);
				arrive.setTarget(retreatSteerable);
			}

			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);					
					ai.fsm.changeState(RUSH);
				}
			}

		},

		/**
		 * Runs from one side of the screen to the other trying to get
		 * the player. The rush is done twice: left to right at 50%
		 * and right to left at 25%. 
		 */
		RUSH {
			
			private Vector2 rushDestination = new Vector2();
			private Steerable<Vector2> rushSteerable = SteeringMechanism.getQuickTarget(rushDestination);
			
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Angle ang  = Mappers.am.get(entity);
				Steering steering = Mappers.stm.get(entity);
				Hittable hit = Mappers.hm.get(entity);
				Arrive<Vector2> arrive = (Arrive<Vector2>)steering.behavior;
				Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
		
				int mod = hit.getHealthPercent() > 0.25 ? 1 : -1;
				
				/*
				 * Add (-20, -3) [left side of the screen] if over 25%,
				 * otherwise (+20, -3) [right side of the screen].
				 * 
				 * As per destination, it's (+40, 0) over 25%, (-40, 0) under.
				 * 
				 * The angle is set to 90d over 25%, otherwise -90d
				 */
				pos.value.set(cameraPos).add(-mod * 20, -3);
				rushDestination.set(pos.value).add(mod * 40, 0);
				ang.value = mod * MathUtils.PI / 2;
				steering.adapter.setMaxLinearSpeed(7);
				arrive.setTarget(rushSteerable);				
			}
		
			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					ai.fsm.changeState(RETURN);
				}
			}
		}, 

		/**
		 * Uses the position store before the rush to appear again from the top of the screen.
		 */
		RETURN {
			@Override
			public void enter (Entity entity) {
				Position pos = Mappers.pm.get(entity);
				Angle ang  = Mappers.am.get(entity);
				AnathorAI fsm = (AnathorAI)Mappers.aim.get(entity).fsm;
				Arrive<Vector2> arrive = (Arrive<Vector2>)Mappers.stm.get(entity).behavior;

				ang.value = 0;
				pos.value.set(fsm.disappearPoint).add(0, 20);
				arrive.setTarget(SteeringMechanism.getQuickTarget(fsm.disappearPoint));				
			}

			@Override
			public void update (Entity entity) {
				if (reachedTarget(entity)) {
					FSMAI ai = Mappers.aim.get(entity);
					ai.fsm.changeState(LEFT_TO_RIGHT);
				}
			}
		},

		/**
		 * Global state. It handles various stuff like spitting fire
		 * and changing behavior depending on HP percentage.
		 */
		GLOBAL {
			private static final float SHOOT_INTERVAL = 1f;

			@Override
			public void update (Entity entity) {
				Hittable hit = Mappers.hm.get(entity);
				AnathorAI fsm = (AnathorAI)Mappers.aim.get(entity).fsm;
				WorldCamera cameraComponent = Mappers.wcm.get(GameScreen.getEngine().getCamera());
				float health = hit.getHealthPercent();

				//This line handles the initial minion spawn.
				//They will only appear once the camera has stopped.
				if (cameraComponent.stopped	&& !fsm.initialSpawn) {
					while (fsm.minionCount < AnathorAI.MINION_LIMIT) 
						spawnMinion(fsm);
					
					fsm.initialSpawn = true;
				}

				//After health hits 80%, it checks whether there are less minions than thought
				//If so, there's a 1/600 chance to spawn a new one.
				//Since this is updated 100 times/sec it will take 6-ish seconds on average.
				if (health <= 0.8f) {
					if (fsm.getMinionCount() < AnathorAI.MINION_LIMIT) {
						if (MathUtils.random(599) == 0) {
							spawnMinion(fsm);
						}
					}
				}

				//Handles auto shooting
				Gun gun = Mappers.gm.get(entity);
				for (GunData data : gun.guns){
					if (data.shootingTimer > SHOOT_INTERVAL && !data.triggered){
						data.triggered = true;
					}
				}

				//Start zigzagging @80%
				if (0.5f < health && health <= 0.8f	&& fsm.getCurrentState() != LEFT_TO_RIGHT) 
					fsm.changeState(LEFT_TO_RIGHT);
				
				//Initiate Rush @50%
				if (0.25f < health && health <= 0.5f) {
					if (!fsm.madeFirstRush) {
						fsm.changeState(RETREAT);
						fsm.madeFirstRush = true;
					}
				}

				//Initiate Rush @25%
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

		/**
		 *  Spawns a wyvern and increases the minion count.
		 *  These wyverns will have a listener attached which will decrement 
		 *  the minion count on death. They are also given a powerup based on
		 *  chance.
		 */
		private static void spawnMinion (final AnathorAI anathor) {
			Entity minion = GameScreen.getBuilder().buildWyvern();
			Position pos = Mappers.pm.get(minion);
			FSMAI ai = Mappers.aim.get(minion);
			Death death = Mappers.dem.get(minion);
			final Vector2 destination = anathor.getSpots().pop();

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

			//Add a power up with random %
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

	private static final int MINION_LIMIT = 4;
	private static final float MINION_RADIUS = 5;
	
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
	
	/**
	 *  <h1>About the spotStack.</h1>
	 * 
	 *  <p>It's a stack containing locations for minions to spawn. 
	 *  The spots are positioned at the edge of a circle, centered to the camera's current position,
	 *  equally distributed, anti-clockwise. Ex (4 minions):</p>
	 *  
	 *  <pre>
	 *  c       d
	 *     cam
	 *  b       a
	 *  </pre>
	 */
	public Array<Vector2> getSpots () {
		
		if (spotStack == null) {
			spotStack = new Array<Vector2>(MINION_LIMIT);
			Vector2 camPos = GameScreen.getEngine().getCameraPosition();

			for (int i = 0 ; i < MINION_LIMIT ; i++) {
				Vector2 spot = new Vector2(camPos);
				float ang = - (i + 0.5f) / MINION_LIMIT * 2 * MathUtils.PI;		//That 0.5f is an offset to not start at 0r
				spot.add(MathUtils.cos(ang) * MINION_RADIUS, MathUtils.sin(ang) * MINION_RADIUS);
				spotStack.add(spot);
			}
		}
		
		return spotStack;
	}
}
