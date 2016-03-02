package com.gff.spacenauts.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * An ImageButton that is rendered using an animation.
 * 
 * @author Alessio
 *
 */
public class AnimatedButton extends ImageButton {

	private Animation animation;
	private float timer = 0;
	private boolean running;

	public AnimatedButton (Animation animation) {
		super(new TextureRegionDrawable(animation.getKeyFrame(0)));		
		this.animation = animation;
		running = false;
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (running) timer += delta;
		
		TextureRegionDrawable drawable = (TextureRegionDrawable)getImage().getDrawable();
		drawable.setRegion(animation.getKeyFrame(timer));
	}
	
	public void start () {
		running = true;
	}
	
	public void stop () {
		timer = 0;
		running = false;
	}
}
