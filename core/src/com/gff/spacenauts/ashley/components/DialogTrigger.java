package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gff.spacenauts.dialogs.Dialog;

/**
 * A rectangular area that triggers a {@link Dialog} when the camera is inside it.
 * 
 * @author Alessio Cali'
 *
 */
public class DialogTrigger implements Component, Poolable {
	
	public Dialog dialog = null;
	public Rectangle area = new Rectangle();
	public boolean started = false;
	
	@Override
	public void reset(){
		dialog = null;
		area.setSize(0, 0);
		area.setPosition(0, 0);
		started = false;
	}
	
}
