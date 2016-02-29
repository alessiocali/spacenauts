package com.gff.spacenauts.listeners.timers;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.EntityBuilder;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.data.SpawnerData;
import com.gff.spacenauts.listeners.TimerListener;
import com.gff.spacenauts.listeners.death.DropPowerUp;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A timer that spawns entities on a fixed schedule, or one shot. It uses {@link EntityGameScreen.getBuilder()#buildById(String)} as a proxy to build entities.
 * 
 * @author Alessio Cali'
 *
 */
public class Spawn extends TimerListener {
	
	private SpawnerData data;
	
	public Spawn(SpawnerData data) {
		super(data.timerType, data.intervalTime, data.limit);
		this.data = data;
	}

	/**
	 * Spawns the entity. If the entity also has a {@link Position} component, then
	 * it sets it to the spawner position. If the entity also has a {@link Body} component,
	 * then it sets its position to the spawner's position. If the spawner initialVelocity attribute
	 * is non-zero, it also adds a Velocity component.
	 * 
	 * @param spawner The spawner entity
	 */
	@Override
	protected boolean onActivation(Entity spawner){
		SpacenautsEngine engine = GameScreen.getEngine();
		EntityBuilder builder = GameScreen.getBuilder();
		Position cameraPos = Mappers.pm.get(GameScreen.getEngine().getCamera());
		
		//Skip Entity if the camera is too distant.
		//This is done to prevent the player from shooting things he can't even see.
		if (Math.abs(data.initialPosition.y - cameraPos.value.y) > Globals.SPAWN_RADIUS)
			return false;
		
		Entity spawnedEntity = builder.buildById(data.id);
		Position spawnPosition = Mappers.pm.get(spawnedEntity);
		Velocity spawnVel = Mappers.vm.get(spawnedEntity);
		Body spawnBody = Mappers.bm.get(spawnedEntity);
		Angle ang = Mappers.am.get(spawnedEntity);
		
		if (spawnPosition != null)
			spawnPosition.value.set(data.initialPosition);
		
		if (spawnBody != null)
			spawnBody.polygon.setPosition(data.initialPosition.x, data.initialPosition.y);
		
		if (!data.initialVelocity.isZero()){
			
			if (spawnVel == null) {
				spawnVel = engine.createComponent(Velocity.class);
				spawnedEntity.add(spawnVel);
			}
			
			spawnVel.value.set(data.initialVelocity);
		}
		
		if (ang != null)
			ang.value = data.initialAngle;

		if (data.releasedPowerUp != null){
			Death death = Mappers.dem.get(spawnedEntity);
			
			if (death != null)
				death.listeners.addListener(new DropPowerUp(data.releasedPowerUp));
		}
		
		engine.addEntity(spawnedEntity);
		return true;
	}

}
