package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * The amount of damage dealt by the entity in case of collision.
 * Also used for determining the damage dealt by a bullet.
 * 
 * @author Alessio Cali'
 *
 */
public class CollisionDamage implements Component, Poolable {

	public float damageDealt = 0;
	
	@Override
	public void reset(){
		damageDealt = 0;
	}
	
}
