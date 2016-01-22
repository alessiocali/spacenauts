package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Component tag for player.
 * 
 * @author Alessio Cali'
 *
 */

public class Player implements Component, Poolable {	
	
	public int score = 0;
	
	@Override
	public void reset() {
		score = 0;
	}
	
}
