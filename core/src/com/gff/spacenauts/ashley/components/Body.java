package com.gff.spacenauts.ashley.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * An entity's body, represented by a convex {@link Polygon}.
 * 
 * @author Alessio Cali'
 *
 */
public class Body implements Component, Poolable {

	public Polygon polygon = new Polygon();

	@Override
	public void reset(){
		float[] vertices = polygon.getVertices();
		
		for(int i = 0 ; i < vertices.length ; i++) vertices[i] = 0;
		
		polygon.setOrigin(0, 0);
		polygon.setPosition(0, 0);
		polygon.setRotation(0);
		polygon.setScale(1, 1);
	}
	
}
