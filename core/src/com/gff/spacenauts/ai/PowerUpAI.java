package com.gff.spacenauts.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.systems.AISystem;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.ui.GameUI;

/**
 * A FSM used by the player to handle PowerUps. Each state corresponds to a certain PowerUp, and the effects are handled through the {@link State} interface.
 * 
 * @author Alessio Cali'
 *
 */
public class PowerUpAI extends DefaultStateMachine<Entity> {

	public enum PowerUpState implements State<Entity> {

		NORMAL("NORMAL", 0) {
			@Override
			public void enter(Entity entity){
				entity.add(GameScreen.getBuilder().buildGunNormal());
				ui.setPowerUp(null);
			}
		},
		
		/**
		 * Triple laser powerup.
		 */
		TRI_GUN("TRIGUN", 15) {

			@Override
			public void enter(Entity entity){
				super.enter(entity);
				entity.add(GameScreen.getBuilder().buildGunTri());
			}

		},
		
		/**
		 * Automatic gun powerup. 
		 */
		AUTO_GUN("AUTOGUN", 15) {
			private static final float SHOOT_INTERVAL = 0.2f;
			
			@Override
			public void update (Entity entity) {
				super.update(entity);
				
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
		},
		
		/**
		 * A stronger kind of gun.
		 */
		HEAVY_GUN("HEAVYGUN", 15) {
			@Override
			public void enter (Entity entity) {
				super.enter(entity);
				entity.add(GameScreen.getBuilder().buildGunHeavy());
			}
		},
		
		/**
		 * A defensive shield
		 */
		SHIELD("SHIELD", 10) {
			@Override
			public void enter (Entity entity) {
				super.enter(entity);
				Entity shield = GameScreen.getBuilder().buildShield();
				FSMAI ai = Mappers.aim.get(entity);
				PowerUpAI fsm = (PowerUpAI)ai.fsm;
				fsm.setShield(shield);
				
				GameScreen.getEngine().addEntity(shield);
			}
			
			@Override
			public void update (Entity entity) {
				super.update(entity);
				Position plPos = Mappers.pm.get(entity);
				Angle plAng = Mappers.am.get(entity);
				FSMAI ai = Mappers.aim.get(entity);
				PowerUpAI fsm = (PowerUpAI)ai.fsm;
				
				Entity shield = fsm.getShield();
				
				if (shield == null) return;
				
				Position pos = Mappers.pm.get(shield);
				Angle ang = Mappers.am.get(shield);
				Body body = Mappers.bm.get(shield);
				
				pos.value.set(plPos.value);
				ang.value = plAng.value;
				body.polygon.setPosition(pos.value.x, pos.value.y);
				body.polygon.setRotation(plAng.getAngleDegrees());
			}
			
			@Override
			public void exit (Entity entity) {
				super.exit(entity);
				FSMAI ai = Mappers.aim.get(entity);
				PowerUpAI fsm = (PowerUpAI)ai.fsm;
				
				Entity shield = fsm.getShield();
				fsm.setShield(null);
				GameScreen.getEngine().removeEntity(shield);
			}
		},
		
		/**
		 * Restores 10% of the player's total health.
		 * 
		 */
		HEALTH_10("HEALTH10", 0) {
			@Override
			public void enter (Entity entity) {
				Hittable hit = Mappers.hm.get(entity);
				
				if (hit != null) 
					hit.health = Math.min(hit.health + 10, hit.maxHealth);
				
				FSMAI ai = Mappers.aim.get(entity);
				
				if (ai != null) ai.fsm.changeState(NORMAL);
			}
		},
		
		/**
		 * Restores 25% of the player's total health.
		 * 
		 */
		HEALTH_25("HEALTH25", 0) {
			@Override
			public void enter (Entity entity) {
				Hittable hit = Mappers.hm.get(entity);
				
				if (hit != null) 
					hit.health = Math.min(hit.health + 25, hit.maxHealth);
				
				FSMAI ai = Mappers.aim.get(entity);
				
				if (ai != null) ai.fsm.changeState(NORMAL);
			}
		},
		
		/**
		 * Restores 50% of the player's total health.
		 * 
		 */
		HEALTH_50("HEALTH50", 0) {
			@Override
			public void enter (Entity entity) {
				Hittable hit = Mappers.hm.get(entity);
				
				if (hit != null) 
					hit.health = Math.min(hit.health + 50, hit.maxHealth);
				
				FSMAI ai = Mappers.aim.get(entity);
				
				if (ai != null) ai.fsm.changeState(NORMAL);
			}
		};

		private String id;
		private float duration;

		private PowerUpState(String id, float duration){
			this.id = id;
			this.duration = duration;
		}

		public static PowerUpState getById(String id){
			for (PowerUpState pu : PowerUpState.values())
				if (pu.id.equals(id))
					return pu;

			return null;
		}

		public String getId(){
			return id;
		}
		
		private static void stepAndRevert(PowerUpAI puai, float duration) {
			if (puai != null){
				float timer = puai.getLocalTimer();
				
				if (timer >= duration && puai.getCurrentState() != NORMAL){
					puai.changeState(NORMAL);
				} else {
					puai.stepTimer(AISystem.DELTA);
				}
			}
		}

		@Override
		public void enter(Entity entity){
			((PowerUpAI)Mappers.aim.get(entity).fsm).startLocalTimer();
			ui.setPowerUp(this);
		}

		@Override
		public void update(Entity entity){
			PowerUpAI puai = (PowerUpAI)Mappers.aim.get(entity).fsm;
			stepAndRevert(puai, getDuration());
		}

		@Override
		public void exit(Entity entity) {	
		}

		@Override
		public boolean onMessage(Entity entity, Telegram telegram) {
			return false;
		}
		
		public float getDuration() {
			return duration;
		}
	}
	
	public static GameUI ui;
	private float timer;
	private Entity shield;

	public PowerUpAI(Entity player, GameUI ui){
		super(player, PowerUpState.NORMAL);
		if (!Families.PLAYER_FAMILY.matches(player) && !Families.COOP_FAMILY.matches(player)){
			throw new IllegalArgumentException("Invalid argument: PowerUp can only be instantiated for Player entities");
		}

		timer = 0;
		PowerUpAI.ui = ui;
	}
	
	public float getLocalTimer(){
		return timer;
	}
	
	/**
	 * Resets the local timer used by {@link PowerUpState}s.
	 */
	public void startLocalTimer(){
		timer = 0;
	}
	
	public void stepTimer(float delta){
		timer += delta;
	}
	
	public Entity getShield() {
		return shield;
	}
	
	public void setShield(Entity shield) {
		this.shield = shield;
	}
}
