package com.gff.spacenauts.listeners.death;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.audio.Sound;
import com.gff.spacenauts.listeners.DeathListener;

/**
 * Plays a sound effect when invoked. Normally used to play explosion sounds when a spaceship dies.
 * 
 * @author Alessio Cali'
 *
 */
public class EmitSound implements DeathListener {
	
	private Sound sfx;
	
	public EmitSound (Sound sfx) {
		this.sfx = sfx;
	}

	@Override
	public void onDeath(Entity entity){
		sfx.play();
	}

}
