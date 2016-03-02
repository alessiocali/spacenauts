package com.gff.spacenauts.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.gff.spacenauts.screens.InitialScreen;

/**
 * A set of four actors that compose the UI of {@link InitialScreen}.
 * Null values are allowed (in which case the content is just hidden).
 *  
 * @author Alessio
 *
 */
public interface UISet {

	/**
	 * The logo of the current selection. It will be shown on the top.
	 * @return
	 */
	public Actor logo();
	
	/**
	 * The main content of this set. It will be shown in the middle.
	 * @return
	 */
	public Actor main();
	
	/**
	 * An actor to be placed in the lower left corner. Typically the back button.
	 * @return
	 */
	public Actor lowerLeft();
	
	/**
	 * An actor to be placed in the lower right corner. Usually hidden.
	 * @return
	 */
	public Actor lowerRight();
	
	
}
