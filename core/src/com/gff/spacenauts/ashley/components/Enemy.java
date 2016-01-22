package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Tag component for enemies
 * 
 * @author Alessio Cali'
 *
 */
public class Enemy implements Component, Poolable {
	
	public int score = 0;
	
	@Override
	public void reset(){
		score = 0;
	}

}
