package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * A {@link StateMachine} associated with the Entity. Although
 * many implementations allows for multiple entities to have the
 * same StateMachine type, Bosses AI make the assumption that
 * only one entity for that AI is currently in play. This is
 * primarily due to States sometimes holding entity-specific 
 * data buffers.
 * 
 * @author Alessio Cali'
 *
 */
public class FSMAI implements Component, Poolable {
	
	public StateMachine<Entity> fsm = null;
	
	@Override
	public void reset(){
		fsm = null;
	}

}
