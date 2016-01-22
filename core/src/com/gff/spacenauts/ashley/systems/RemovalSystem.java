package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Bullet;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Removes entities that are no longer needed, like bullets out of the camera's view or enemies that are outside the world boundaries.
 * 
 * @author Alessio Cali'
 *
 */
public class RemovalSystem extends IteratingSystem {

	public RemovalSystem(){
		super(Families.REMOVABLE_FAMILY);
	}

	@Override
	public void processEntity(Entity entity, float delta){
		Position pos = Mappers.pm.get(entity);
		Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
/*
		if (pos.value.x < - WorldGlobals.REMOVAL_TOLERANCE_RADIUS ||
				pos.value.x > levelWidth + WorldGlobals.REMOVAL_TOLERANCE_RADIUS ||
				pos.value.y < - WorldGlobals.REMOVAL_TOLERANCE_RADIUS ||
				pos.value.y > levelHeight + WorldGlobals.REMOVAL_TOLERANCE_RADIUS)
*/
		if (pos.value.dst(cameraPos) > Globals.SPAWN_RADIUS + Globals.REMOVAL_TOLERANCE_RADIUS)
			GameScreen.getEngine().removeEntity(entity);

		else {
			Bullet bullet = Mappers.bum.get(entity);
			
			//Additional check to remove bullets who are away from the camera for more than a camera size.
			if (bullet != null) {

				if (pos.value.x < cameraPos.x - Globals.TARGET_CAMERA_WIDTH / 2 ||
						pos.value.x > cameraPos.x + Globals.TARGET_CAMERA_WIDTH / 2 ||
						pos.value.y < cameraPos.y - Globals.TARGET_CAMERA_HEIGHT / 2 ||
						pos.value.y > cameraPos.y + Globals.TARGET_CAMERA_HEIGHT / 2)

					GameScreen.getEngine().removeEntity(entity);
			}
		}
	}

}
