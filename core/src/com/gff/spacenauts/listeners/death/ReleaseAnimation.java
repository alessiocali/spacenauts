package com.gff.spacenauts.listeners.death;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.gff.spacenauts.Globals;
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
	
	private static final float FADE_DURATION = 0.5f;
	
	private Animation animation;
	private PooledEngine engine;
	
	public ReleaseAnimation (Animation animation, PooledEngine engine){
		this.animation = animation;
		this.engine = engine;
	}
	
	public ReleaseAnimation(PooledEngine engine) {
		this(null, engine);
	}
	
	private Animation getFadeAnimation(Sprite sprite) {
		Sprite[] frames = new Sprite[5];
		
		for (int i = 0 ; i < 5 ; i++) {
			float alpha = (float) Interpolation.fade.apply(1, 0, (float)i / 5);
			frames[i] = new Sprite(sprite);
			frames[i].setAlpha(alpha);
		}
		
		return new Animation(FADE_DURATION / 5, frames);
	}

	@Override
	public void onDeath(Entity entity) {
		Position pos = Mappers.pm.get(entity);
		Angle ang = Mappers.am.get(entity);
		Render entityRender = Mappers.rm.get(entity);
		
		if (pos != null) {
			Entity animate = engine.createEntity();
			Render render = engine.createComponent(Render.class);
			Removable rem = engine.createComponent(Removable.class);
			Position animationPos = engine.createComponent(Position.class);
			Angle animationAngle = engine.createComponent(Angle.class);
			
			animate.add(render).add(rem).add(animationPos).add(animationAngle);
			
			render.sprite = Render.CACHE_SPRITE;
			render.animation = animation != null ? animation : getFadeAnimation(entityRender.sprite);
			//Null check to avoid issues with GameOver (Player's Render is removed beforehand)
			//Should be fixed anyway.
			render.scaleX = entityRender != null ? entityRender.scaleX : Globals.UNITS_PER_PIXEL;
			render.scaleY = entityRender != null ? entityRender.scaleY : Globals.UNITS_PER_PIXEL;
			
			render.listeners.add(new Remove(engine));
			animationPos.value.set(pos.value);
			animationAngle.value = ang != null ? ang.value : 0;
			
			engine.addEntity(animate);
		}
	}
}
