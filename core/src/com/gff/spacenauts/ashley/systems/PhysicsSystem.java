package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IntervalSystem;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Wraps together {@link MovementSystem}, {@link SteeringSystem} and {@link CollisionSystem} using a time accumulator
 * to avoid time aliasing.
 * 
 * @author Alessio Cali'
 *
 */
public class PhysicsSystem extends IntervalSystem {

	private final static float DELTA = 0.01f;
	
	private MovementSystem ms;
	private SteeringSystem ss;
	private CollisionSystem cs;
	
	public PhysicsSystem(MovementSystem ms, SteeringSystem ss, CollisionSystem cs) {
		super(DELTA);
		this.ms = ms;
		this.ss = ss;
		this.cs = cs;
	}

	public PhysicsSystem(int priority, MovementSystem ms, SteeringSystem ss, CollisionSystem cs) {
		super(priority);
		this.ms = ms;
		this.ss = ss;
		this.cs = cs;
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		ms.addedToEngine(engine);
		ss.addedToEngine(engine);
		cs.addedToEngine(engine);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		ms.removedFromEngine(engine);
		ss.removedFromEngine(engine);
		cs.removedFromEngine(engine);
	}

	

	@Override
	protected void updateInterval() {
		ss.update(DELTA);
		ms.update(DELTA);
		cs.update(DELTA);		
	}

	public Array<Entity> getNearbyObstacles() {
		return cs.getPotentialColliders(GameScreen.getEngine().getPlayer(), Families.OBSTACLE_FAMILY);
	}
}
