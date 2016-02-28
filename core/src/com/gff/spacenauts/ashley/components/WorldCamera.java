package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The camera entity's {@link Viewport}. Additionally a boolean flag 
 * is provided to mark when the camera stops at the end of a level.
 * 
 * @author Alessio Cali'
 *
 */
public class WorldCamera implements Component, Poolable {
	
	public Viewport viewport = new FitViewport(1,1);
	public boolean stopped = false;
	
	@Override
	public void reset(){
		viewport.setWorldSize(1, 1);
		stopped = false;
	}

}
