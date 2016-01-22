package com.gff.spacenauts.listeners.death;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.ai.PowerUpAI.PowerUpState;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.FSMAI;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Sets the player's {@link com.gff.spacenauts.ai.PowerUpAI PowerUpAI} to a certain state when triggered.
 * Usually called by on-world PowerUps when the player shoots or collides with them.
 * 
 * @author Alessio Cali'
 *
 */
public class ActivatePowerUp implements DeathListener {
	
	private String activatedPowerUp;
	
	public ActivatePowerUp (String activatedPowerUp) {
		this.activatedPowerUp = activatedPowerUp;
	}
	
	@Override
	public void onDeath(Entity entity){
		Entity player = GameScreen.getEngine().getPlayer();
		
		if (player != null && activatedPowerUp != null){
			FSMAI powerUpAi = Mappers.aim.get(player);
			
			if (powerUpAi != null) {
				powerUpAi.fsm.changeState(PowerUpState.getById(activatedPowerUp));
				GameScreen.getEngine().sendCoop("PLAYER_PWUP " +activatedPowerUp);
			}
			
			//To avoid multiple activations
			activatedPowerUp = null;
		}
	}
}