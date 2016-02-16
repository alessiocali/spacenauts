package com.gff.spacenauts.data;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.listeners.death.DeathListeners;
import com.gff.spacenauts.listeners.hit.HitListeners;
import com.gff.spacenauts.listeners.shoot.ShotListeners;

/**
 * All data needed to instantiate bullets from a gun. In fact, this structure more or less is the abstraction of a spaceship's gun.<br>
 * GunData implements Poolable, so its instances are managed.
 * 
 * @author Alessio Cali'
 *
 */
public class GunData implements Poolable {
	
	public boolean triggered;
	public float speed;
	public float bulletDamage;
	public float shootingTimer;
	public HitListeners bulletHitListeners;
	public DeathListeners bulletDeathListeners;
	public ShotListeners gunShotListeners;
	public Sprite bulletImage;
	public Vector2 pOffset;
	public float aOffset;
	public String shotSound;
	public float scaleX;
	public float scaleY;
	
	public GunData() {
		triggered = false;
		speed = 0;
		bulletDamage = 0;
		bulletHitListeners = new HitListeners();
		bulletDeathListeners = new DeathListeners();
		gunShotListeners = new ShotListeners();
		bulletImage = new Sprite();
		shootingTimer = 0;
		pOffset = new Vector2(0,0);
		aOffset = 0;
		shotSound = "";
		scaleX = scaleY = Globals.UNITS_PER_PIXEL;
	}
	
	@Override
	public void reset() {
		triggered = false;		
		speed = 0;
		bulletDamage = 0;
		bulletHitListeners.clear();
		bulletDeathListeners.clear();
		gunShotListeners.clear();
		bulletImage = null;
		shootingTimer = 0;
		pOffset.setZero();
		aOffset = 0;
		shotSound = "";
		scaleX = scaleY = Globals.UNITS_PER_PIXEL;
	}
	
	public GunData clone () {
		GunData retVal = new GunData();
		
		retVal.aOffset = aOffset;
		retVal.bulletDamage = bulletDamage;
		retVal.bulletDeathListeners.addAll(bulletDeathListeners.getListeners());
		retVal.bulletHitListeners.addAll(bulletHitListeners.getListeners());
		retVal.bulletImage = bulletImage;
		retVal.pOffset.set(pOffset);
		retVal.scaleX = scaleX;
		retVal.scaleY = scaleY;
		retVal.shootingTimer = 0;
		retVal.shotSound = shotSound;
		retVal.speed = speed;
		retVal.triggered = false;
		
		return retVal;
	}
}