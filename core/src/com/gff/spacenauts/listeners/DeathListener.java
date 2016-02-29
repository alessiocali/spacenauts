package com.gff.spacenauts.listeners;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Enemy;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A listener called whenever an entities dies (i.e. its health reaches zero). The most common behavior is {@link Remove}.
 * 
 * @author Alessio Cali'
 *
 */
public interface DeathListener {

	/**
	 * Common use listeners that can be declared as static instances.
	 * 
	 * @author Alessio
	 *
	 */
	public enum Commons implements DeathListener {

		INCREASE_SCORE {
			@Override
			public void onDeath(Entity entity) {
				Entity player = GameScreen.getEngine().getPlayer();
				Enemy enemy = Mappers.em.get(entity);

				if (player != null && enemy != null)
					Mappers.plm.get(player).score += enemy.score;	
			}
		},

		GAME_OVER {
			@Override
			public void onDeath(Entity entity) {
				GameScreen.getEngine().gameOver();
			}
		},
		
		VICTORY {
			@Override
			public void onDeath(Entity entity) {
				GameScreen.getEngine().initiateVictory();	
			}
		}

	}

	public void onDeath(Entity entity);

}
