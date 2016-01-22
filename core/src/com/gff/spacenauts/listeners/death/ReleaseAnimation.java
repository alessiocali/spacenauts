package com.gff.spacenauts.listeners.death;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Removable;
import com.gff.spacenauts.ashley.components.Render;
import com.gff.spacenauts.listeners.DeathListener;
import com.gff.spacenauts.listeners.Remove;

/**
 * Creates a one-shot entity whose purpose is only that of playing an animation. Normally used to play explosions and such. 
 * 
 * @author Alessio Cali'
 *
 */
public class ReleaseAnimation implements DeathListener {
	
	private Animation animation;
	private PooledEngine engine;
	
	public ReleaseAnimation (Animation animation, PooledEngine engine){
		this.animation = animation;
		this.engine = engine;
	}

	@Override
	public void onDeath(Entity entity) {
		Position pos = Mappers.pm.get(entity);
		Angle ang = Mappers.am.get(entity);
		
		if (pos != null) {
			Entity animate = engine.createEntity();
			Render render = engine.createComponent(Render.class);
			Removable rem = engine.createComponent(Removable.class);
			Position animationPos = engine.createComponent(Position.class);
			Angle animationAngle = engine.createComponent(Angle.class);
			
			animate.add(render).add(rem).add(animationPos).add(animationAngle);
			
			render.sprite = Render.CACHE_SPRITE;
			render.animation = animation;
			
			render.listeners.add(new Remove(engine));
			animationPos.value.set(pos.value);
			animationAngle.value = ang.value;
			
			engine.addEntity(animate);
		}
	}
}
