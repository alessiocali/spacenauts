package com.gff.spacenauts.listeners.hit;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Death;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.listeners.HitListener;
import com.gff.spacenauts.screens.GameScreen;

/**
 * This listener is used with solid obstacles to realize the actual collision. It invokes 
 * {@link Intersector#overlapConvexPolygons(float[], float[], MinimumTranslationVector) overlapConvexPolygons}
 * to get the minimum translation vector needed to move the entity away from the obstacles, then applies said translation.
 * 
 * @author Alessio Cali'
 *
 */
public class PushAway implements HitListener {

	private MinimumTranslationVector mtv = new MinimumTranslationVector();
	private Family[] filters;

	public PushAway(Family... filters){
		this.filters = filters;
	}

	@Override
	public void onHit(Entity entity, Entity collider) {
		boolean matches = false;
		
		//Continue if the entity matches any of the given filters.
		for (Family filter : filters) { 
			if (filter != null) {
				if (filter.matches(collider))
					matches = true;
			}
		}

		if (!matches) return;

		Body entityBody = Mappers.bm.get(entity);
		Body colliderBody = Mappers.bm.get(collider);
		Position colliderPosition = Mappers.pm.get(collider);

		if (entityBody != null && colliderBody != null){
			if (Intersector.overlapConvexPolygons(colliderBody.polygon, entityBody.polygon, mtv)){
				Vector2 transVector = mtv.normal.scl(mtv.depth);
				colliderBody.polygon.translate(transVector.x, transVector.y);
				
				if (colliderPosition != null)
					colliderPosition.value.add(transVector);

				if (GameScreen.getEngine().getPlayer() == collider) outboundCheck(collider);
			}
		}
	}

	/**
	 * Checks whether the player was outbounded during the push away process. In which case... BOOM!
	 */
	private void outboundCheck(Entity entity) {
		Position pos = Mappers.pm.get(entity);
		Death death = Mappers.dem.get(entity);

		if (pos != null && death != null) {

			Vector2 cPos = GameScreen.getEngine().getCameraPosition();
			/*
			if (pos.value.x < cPos.x - Globals.TARGET_CAMERA_WIDTH / 2 || 
				pos.value.x > cPos.x + Globals.TARGET_CAMERA_WIDTH / 2 ||
				pos.value.y < cPos.y - Globals.TARGET_CAMERA_HEIGHT / 2 || 
				pos.value.y > cPos.y + Globals.TARGET_CAMERA_HEIGHT / 2)*/
			if (pos.value.y < cPos.y - Globals.TARGET_CAMERA_HEIGHT / 2)
			{
				if (!GameScreen.getEngine().isGameOver()) death.listeners.onDeath(entity);
			}
		}
	}
}
