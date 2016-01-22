package com.gff.spacenauts;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

/**
 * Utility class for various geometric calculations.
 * 
 * @author Alessio Cali'
 *
 */
public final class Geometry {

	/**
	 * Builds a new {@link Polygon} based on a {@link Rectangle}. The new Polygon is origin-centered.
	 * 
	 * @param width the rectangle's width.
	 * @param height the rectangle's height.
	 * @return a new Polygon based on the given parameters.
	 */
	public static Polygon getRectangleAsPolygon(float width, float height){
		return new Polygon(new float[]{-width/2, -height/2,
									   width/2, -height/2,
									   width/2, height/2,
									   -width/2, height/2});
	}
	
	/**
	 * @see #getRectangleAsPolygon(float, float)
	 */
	public static Polygon getRectangleAsPolygon(Rectangle rect){
		return getRectangleAsPolygon(rect.width, rect.height);
	}
	
	/**
	 * Utility method to copy from the vertices arrays
	 * 
	 * @param origin the original vector.
	 * @return a copy of this vector.
	 */
	public static float[] copy(float[] origin){
		float[] copy = new float[origin.length];
		
		for (int i = 0 ; i < origin.length ; i++)
			copy[i] = origin[i];
		
		return copy;
	}
	
}
