package com.gff.spacenauts.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

/**
 * A drawable that used an animation. Kudos to <a href="http://badlogicgames.com/forum/viewtopic.php?t=6735&p=31332#p31340">bach</a> for this.
 * 
 * @author bach
 *
 */
public class AnimationDrawable extends BaseDrawable {
	   private Animation animation;
	   private float stateTime;
	   private boolean looping;

	   public AnimationDrawable (Animation animation, boolean looping) {
	      setAnimation(animation, looping);
	   }

	   public AnimationDrawable (AnimationDrawable drawable) {
	      super(drawable);
	      setAnimation(drawable.animation, drawable.looping);
	   }

	   public void draw (SpriteBatch batch, float x, float y, float width, float height) {
	      stateTime += Gdx.graphics.getDeltaTime();
	      batch.draw(animation.getKeyFrame(stateTime, this.looping), x, y, width, height);
	   }

	   public void setAnimation (Animation animation, boolean looping) {
	      this.animation = animation;
	      this.looping = looping;
	      setMinWidth(Math.abs(animation.getKeyFrame(0).getRegionWidth()));
	      setMinHeight(Math.abs(animation.getKeyFrame(0).getRegionHeight()));
	   }

	   public Animation getAnimation () {
	      return animation;
	   }
	}