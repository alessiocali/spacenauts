package com.gff.spacenauts.listeners.death;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Releases an on-world PowerUp to be grabbed by the player when called. Usually invoked by enemies that drop PowerUps.
 * 
 * @author Alessio Cali'
 *
 */
public class DropPowerUp implements DeathListener {
	
	private String releasedPowerUp;
	
	public DropPowerUp (String releasedPowerUp) {
		this.releasedPowerUp = releasedPowerUp;
	}

	@Override
	public void onDeath(Entity entity) {
		Position pos = Mappers.pm.get(entity);
		
		if (pos != null)
			GameScreen.getEngine()
			.addEntity(GameScreen.getBuilder()
			.buildPowerUp(releasedPowerUp, pos.value.x, pos.value.y));
	}

}
