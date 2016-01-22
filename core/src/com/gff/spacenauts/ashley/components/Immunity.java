package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Added when the entity becomes immune to damage.
 * 
 * @author Alessio Cali'
 *
 */
public class Immunity implements Component, Poolable {

	public float duration = 0.5f;
	public float timer = 0;
	
	@Override
	public void reset(){
		duration = 0.25f;
		timer = 0;
	}
	
}
