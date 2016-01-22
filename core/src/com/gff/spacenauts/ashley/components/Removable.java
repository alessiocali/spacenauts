package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Tag component for entities that can be removed by {@link com.gff.spacenauts.ashley.systems.RemovalSystem RemovalSystem}.
 * 
 * @author Alessio Cali'
 *
 */

public class Removable implements Component, Poolable {

	@Override
	public void reset() {
	}

}
