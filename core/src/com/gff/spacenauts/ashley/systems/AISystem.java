package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.FSMAI;

/**
 * This system goes over every {@link FSMAI} Component and updates their 
 * relative {@link com.badlogic.gdx.ai.fsm.StateMachine StateMachine}s.
 * 
 * @author Alessio Cali'
 *
 */
public class AISystem extends IntervalIteratingSystem {
	
	public static final float DELTA = 0.01f;
	
	public AISystem(){
		super(Families.AI_FAMILY, DELTA);
	}
	
	@Override
	public void processEntity(Entity entity){
		FSMAI ai = Mappers.aim.get(entity);
		ai.fsm.update();
	}

}
