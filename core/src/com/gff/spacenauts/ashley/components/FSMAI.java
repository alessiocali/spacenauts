package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * A {@link StateMachine} associated with the Entity.
 * 
 * @author Alessio Cali'
 *
 */
public class FSMAI implements Component, Poolable {
	
	public StateMachine<Entity> fsm = null;
	public Object extra = null;
	
	@Override
	public void reset(){
		fsm = null;
		extra = null;
	}

}
