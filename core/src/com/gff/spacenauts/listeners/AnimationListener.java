package com.gff.spacenauts.listeners;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;

/**
 * A listener triggered when animation related events occur.
 * 
 * @author Alessio Cali'
 *
 */
public interface AnimationListener {

	public void onStart(Entity entity, Animation animation);
	public void onEnd(Entity entity, Animation animation);
	
}
