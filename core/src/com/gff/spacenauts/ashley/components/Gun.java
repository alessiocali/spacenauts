package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.gff.spacenauts.data.GunData;

/**
 * PEW PEW! Ahem.
 * An array of {@link GunData} each representing a gun owned by the entity. 
 * 
 * @author Alessio Cali'
 *
 */
public class Gun implements Component, Poolable {
	
	public Array<GunData> guns = new Array<GunData>();
	
	/**
	 * Returns if any gun has been triggered
	 * @return
	 */
	public boolean triggered () {
		for (GunData gun : guns) {
			if (gun.triggered) return true;
		}
		
		return false;
	}

	@Override
	public void reset() {
		for (GunData gun : guns)
			Pools.get(GunData.class).free(gun);
		
		guns.clear();
	}

}
