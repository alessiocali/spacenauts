package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
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
import com.gff.spacenauts.screens.GameScreen;

/**
 * Iterates over all {@link Gun} components and instantiate bullet entities for every {@link GunData} triggered.
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
		Gun guns = Mappers.gm.get(entity);

		for (GunData data : guns.guns){
			if (data.triggered){
				data.triggered = false;

				Position pos = Mappers.pm.get(entity);			
				Angle ang = Mappers.am.get(entity);
				Position bulletPos = GameScreen.getEngine().createComponent(Position.class);
				Angle bulletAng = GameScreen.getEngine().createComponent(Angle.class);

				/*  The bullet is repositioned by a given offset. This offset is relative to object space
				 *  and must thus be rotated to match the object's current rotation. However, it must
				 *  retain its angle relative to the object space x-axis. To achieve this, I prepare local buffer
				 *  that will host the pOffset data, and rotate it by the given object's angle.
				 * 
				 *  Without the buffer, the bullet would start spinning at every iteration.
				 */ 
				bulletPos.value.set(pos.value).add(offsetBuffer.set(data.pOffset).rotateRad(ang.value));
				bulletAng.value = ang.value + data.aOffset;

				Friendly fFaction = Mappers.fm.get(entity);
				Enemy eFaction = Mappers.em.get(entity);

				Velocity vel = GameScreen.getEngine().createComponent(Velocity.class);
				vel.value.set((float)Math.cos(bulletAng.value), (float)Math.sin(bulletAng.value)).scl(data.speed);

				AngularVelocity angVel = GameScreen.getEngine().createComponent(AngularVelocity.class);

				CollisionDamage cd = GameScreen.getEngine().createComponent(CollisionDamage.class);
				cd.damageDealt = data.bulletDamage;
				
				Hittable hit = GameScreen.getEngine().createComponent(Hittable.class);
				hit.listeners.addAll(data.bulletHitListeners.getListeners());
				
				Death death = GameScreen.getEngine().createComponent(Death.class);
				death.listeners.addAll(data.bulletDeathListeners.getListeners());

				Render render = GameScreen.getEngine().createComponent(Render.class);
				render.sprite = data.bulletImage;
				render.scale = data.scale;

				Bullet bullet = GameScreen.getEngine().createComponent(Bullet.class);
				Removable rem = GameScreen.getEngine().createComponent(Removable.class);

				Entity bulletEntity = GameScreen.getEngine().createEntity();

				bulletEntity.add(bulletPos).add(bulletAng).add(vel).add(angVel).add(cd).add(hit).add(death)
							.add(render).add(bullet).add(rem);

				if (fFaction != null){
					bulletEntity.add(GameScreen.getEngine().createComponent(Friendly.class));
				} else if (eFaction != null){
					bulletEntity.add(GameScreen.getEngine().createComponent(Enemy.class));
				}

				GameScreen.getEngine().addEntity(bulletEntity);
				if (data.shotSound != "") assets.get(data.shotSound, Sound.class).play();
				data.shootingTimer = 0;
				
				data.gunShotListeners.onShooting(entity, bulletEntity);
			}
			
			data.shootingTimer += delta;
		}
	}
}
