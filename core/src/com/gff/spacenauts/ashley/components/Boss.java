package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Tag component for bosses.
 * 
 * @author Alessio Cali'
 *
 */
public class Boss implements Component, Poolable {
	
	public String name = "";

	@Override
	public void reset(){
		name = "";
	}
}
