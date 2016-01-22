package com.gff.spacenauts.listeners;

import com.badlogic.ashley.core.Entity;

/**
 * A listener called whenever a GunData shoots a bullet.
 * 
 * @author Alessio Cali'
 *
 */
public interface ShotListener {

	public void onShooting (Entity gun, Entity bullet);
	
}
