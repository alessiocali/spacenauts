package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.gff.spacenauts.Level;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.components.WorldCamera;
import com.gff.spacenauts.screens.GameScreen;

/**
 * This system has two purposes: first, it ensures that the camera's {@link com.badlogic.gdx.graphics.Camera Camera} object is updated properly,
 * by setting its combined matrix's position to its {@link Position} component.<p>
 * 
 * Second, it checks the current {@link Level}'s target height (see {@link Level#getTargetHeight()}) against the camera's current position.
 * If the target height has been reached, it will stop the camera and remove the player's inertia (in other words, it stops the vertical scrolling effect). 
 * 
 * 
 * @author Alessio Cali'
 *
 */
public class CameraSystem extends EntitySystem {

	private Level currentLevel = null;

	public CameraSystem(GameScreen game){
		this.currentLevel = game.getLevel();
	}

	@Override
	public void update(float delta){
		Entity cameraEntity;
		if ((cameraEntity = GameScreen.getEngine().getCamera()) != null){
			Position pos = Mappers.pm.get(cameraEntity);
			Velocity vel = Mappers.vm.get(cameraEntity);
			Entity player = GameScreen.getEngine().getPlayer();
			
			WorldCamera camera = Mappers.wcm.get(cameraEntity);
			camera.viewport.getCamera().position.set(pos.value.x, pos.value.y, 0);
			camera.viewport.getCamera().update();

			if (pos.value.y > currentLevel.getTargetHeight()){
				pos.value.y = currentLevel.getTargetHeight();
				if (vel != null) {
					vel.value.setZero();
					camera.stopped = true;
				}
				//Also, stop the inertia
				if (player != null){
					Velocity playerVelocity = Mappers.vm.get(player);
					if (playerVelocity != null)
						playerVelocity.value.sub(0, Globals.baseCameraSpeed);
				}
			}
		}
	}

}
