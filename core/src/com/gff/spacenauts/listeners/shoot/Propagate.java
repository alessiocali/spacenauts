package com.gff.spacenauts.listeners.shoot;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pools;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.CollisionDamage;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Enemy;
import com.gff.spacenauts.ashley.components.Friendly;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Timers;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.Remove;
import com.gff.spacenauts.listeners.ShotListener;
import com.gff.spacenauts.listeners.TimerListener;
import com.gff.spacenauts.listeners.TimerListener.TimerType;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Applies a timer to every bullet that will make it split into a certain number of copies after a given delay.
 * All copies are shot in a range between [-75d,75d] of the original bullet.
 * 
 * @author Alessio Cali'
 *
 */
public class Propagate implements ShotListener {

	private float delay;
	private int times;
	
	public Propagate(float delay, int times) {
		this.delay = delay;
		this.times = times;
	}

	@Override
	public void onShooting(Entity gun, Entity bullet) {
		Timers timers = Mappers.tm.get(bullet);
		final Entity fBullet = bullet;
		
		if (timers == null) {
			timers = GameScreen.getEngine().createComponent(Timers.class);
			bullet.add(timers);
		}
		
		timers.listeners.add(new TimerListener(TimerType.ONE_SHOT, delay) {
			
			@Override
			public boolean onActivation (Entity entity) {
				//Creates a temporary gun entity that will shoot the spread lasers.
				SpacenautsEngine engine = GameScreen.getEngine();
				Entity tempGun = engine.createEntity();
				
				GunData bulletData = extractData(entity);
				Angle bulletAng = Mappers.am.get(entity);
				Position bulletPos = Mappers.pm.get(entity);
				Friendly friendly = Mappers.fm.get(entity);
				
				Angle ang = engine.createComponent(Angle.class);
				Position pos = engine.createComponent(Position.class);
				Gun gun = engine.createComponent(Gun.class);
				GunData[] data = new GunData[times];
				
				tempGun.add(ang).add(pos).add(gun);
				
				if (friendly != null) tempGun.add(engine.createComponent(Friendly.class));
				else tempGun.add(engine.createComponent(Enemy.class));
						
				ang.value = bulletAng != null ? bulletAng.value : 0;
				if (bulletPos.value != null) pos.value.set(bulletPos.value);
				
				for (int i = 0 ; i < data.length ; i++){
					data[i] = bulletData.clone();
					
					//Creates a fan of bullets placed between [-75d, +75d] (75d = pi / 2,4 radians) 
					// - 75d + (150d / times) * i = 75d * (2i / times - 1) 
					data[i].aOffset = MathUtils.PI / 2.4f * (2f*i / times - 1);
					data[i].triggered = true;
				}
				
				data[0].gunShotListeners.addListener(new Remove(engine));
				gun.guns.addAll(data);
				
				engine.addEntity(tempGun);
				engine.removeEntity(fBullet);
				return true;
			}
			
			/**
			 * Builds a GunData structure based off the given entity.
			 * 
			 * @param entity the bullet to copy.
			 * @return a GunData that copies the given bullet.
			 */
			private GunData extractData (Entity entity) {
				GunData retVal = Pools.get(GunData.class).obtain();
				
				CollisionDamage cd = Mappers.cdm.get(entity);
				Death death = Mappers.dem.get(entity);
				Hittable hit = Mappers.hm.get(entity);
				Render render = Mappers.rm.get(entity);
				Velocity vel = Mappers.vm.get(entity);
				
				retVal.aOffset = 0;
				retVal.bulletDamage = cd != null ? cd.damageDealt : 0;
				if (death != null) retVal.bulletDeathListeners.addAll(death.listeners.getListeners());
				if (hit != null) retVal.bulletHitListeners.addAll(hit.listeners.getListeners());
				retVal.bulletImage = render != null ? render.sprite : null;
				retVal.pOffset.setZero();
				retVal.scaleX = render != null ? render.scaleX : Globals.UNITS_PER_PIXEL;
				retVal.scaleY = render != null ? render.scaleY : Globals.UNITS_PER_PIXEL;
				retVal.speed = vel != null ? vel.value.len() : 0;
				
				return retVal;
			}
			
		});
	}

}
