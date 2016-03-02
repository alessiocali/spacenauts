package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Bullet;
import com.gff.spacenauts.ashley.components.CollisionDamage;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Enemy;
import com.gff.spacenauts.ashley.components.Friendly;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Hittable;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Removable;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.listeners.shoot.ShotListeners;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Iterates over all {@link Gun} components and instantiate bullet entities for every {@link GunData} triggered.
 * Also triggers all related {@link ShotListeners}.
 * 
 * @author Alessio Cali'
 *
 */
public class ShootingSystem extends IteratingSystem {

	private AssetManager assets;
	private Vector2 offsetBuffer = new Vector2();

	public ShootingSystem(GameScreen game){
		super(Families.SHOOTER_FAMILY);
		this.assets = game.getAssets();
	}

	@Override
	protected void processEntity(Entity entity, float delta) {
		SpacenautsEngine engine = GameScreen.getEngine();
		Gun guns = Mappers.gm.get(entity);

		for (GunData data : guns.guns){
			if (data.triggered){
				data.triggered = false;

				Position pos = Mappers.pm.get(entity);			
				Angle ang = Mappers.am.get(entity);
				Friendly fFaction = Mappers.fm.get(entity);
				Enemy eFaction = Mappers.em.get(entity);

				Entity bulletEntity = engine.createEntity();
				
				Position bulletPos = engine.createComponent(Position.class);
				Angle bulletAng = engine.createComponent(Angle.class);
				Velocity vel = engine.createComponent(Velocity.class);
				AngularVelocity angVel = engine.createComponent(AngularVelocity.class);
				CollisionDamage cd = engine.createComponent(CollisionDamage.class);
				Hittable hit = engine.createComponent(Hittable.class);
				Death death = engine.createComponent(Death.class);
				Render render = engine.createComponent(Render.class);
				Bullet bullet = engine.createComponent(Bullet.class);
				Removable rem = engine.createComponent(Removable.class);
				
				/*  
				 *  The bullet is repositioned by a given offset. This offset is relative to object space
				 *  and must thus be rotated to match the object's current rotation. However, it must
				 *  retain its angle relative to the object space x-axis. To achieve this, I prepare local buffer
				 *  that will host the pOffset data, and rotate it by the given object's angle.
				 * 
				 *  Without the buffer, the bullet would start spinning at every iteration. Which is quite
				 *  funny to see actually.
				 */ 
				
				bulletPos.value.set(pos.value).add(offsetBuffer.set(data.pOffset).rotateRad(ang.value));
				bulletAng.value = ang.value + data.aOffset;
				vel.value.set((float)Math.cos(bulletAng.value), (float)Math.sin(bulletAng.value)).scl(data.speed);				
			
				cd.damageDealt = data.bulletDamage;
				
				hit.listeners.addAll(data.bulletHitListeners.getListeners());
				
				death.listeners.addAll(data.bulletDeathListeners.getListeners());
				
				render.sprite = data.bulletImage;
				render.scaleX = data.scaleX;
				render.scaleY = data.scaleY;

				bulletEntity.add(bulletPos).add(bulletAng).add(vel).add(angVel).add(cd).add(hit).add(death)
							.add(render).add(bullet).add(rem);

				if (fFaction != null)
					bulletEntity.add(engine.createComponent(Friendly.class));
				
				else if (eFaction != null)
					bulletEntity.add(engine.createComponent(Enemy.class));
				
				engine.addEntity(bulletEntity);
				
				if (data.shotSound != "") assets.get(data.shotSound, Sound.class).play();
				
				data.shootingTimer = 0;
				
				data.gunShotListeners.onShooting(entity, bulletEntity);
			}
			
			data.shootingTimer += delta;
		}
	}
}
