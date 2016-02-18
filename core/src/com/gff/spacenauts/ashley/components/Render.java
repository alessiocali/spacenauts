package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.listeners.AnimationListener;

/**
 * An entity's visual representation. Usually only a sprite is sufficient, but if
 * the given animation is non null the {@link com.gff.spacenauts.systems.RenderingSystem RenderingSystem} will
 * render the animation's current frame instead.
 * 
 * @author Alessio Cali'
 *
 */
public class Render implements Component, Poolable {
	
	//A support sprite for renders that only use an animation. E.g.: explosions.
	public static final Sprite CACHE_SPRITE = new Sprite();
	
	public float scaleX = Globals.UNITS_PER_PIXEL;
	public float scaleY = Globals.UNITS_PER_PIXEL;
	public Sprite sprite = null;
	public Animation animation;
	public Array<AnimationListener> listeners = new Array<AnimationListener>();
	public float animationTimer = 0;
	
	@Override
	public void reset(){
		scaleX = Globals.UNITS_PER_PIXEL;
		scaleY = Globals.UNITS_PER_PIXEL;
		sprite = null;
		animation = null;
		animationTimer = 0;
		listeners.clear();
	}
	

}
