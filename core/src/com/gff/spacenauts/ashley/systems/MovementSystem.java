package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.SpacenautsEngine;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.screens.GameScreen;


/**
 * <p>Moves entities according to their {@link Position} and {@link Velocity} components. Also clamps player's position to the viewable space.</p> 
 * 
 * <p>To prevent time aliasing it should wrapped inside {@link PhysicsSystem}.</p>
 * 
 * @author Alessio Cali'
 *
 */
public class MovementSystem extends IteratingSystem {
	
	private Vector2 oldPos = new Vector2();

	public MovementSystem() {
		super(Families.MOVEMENT_FAMILY);
	}

	@Override
	protected void processEntity(Entity entity, float delta) {
		SpacenautsEngine engine = GameScreen.getEngine();

		Position pos = Mappers.pm.get(entity);
		Velocity vel = Mappers.vm.get(entity);
		Angle ang = Mappers.am.get(entity);
		AngularVelocity angVel = Mappers.avm.get(entity);
		Body body = Mappers.bm.get(entity);
		
		pos.value.mulAdd(vel.value, delta);
		ang.value += angVel.value * delta;		
		
		if (body != null){
			body.polygon.setPosition(pos.value.x, pos.value.y);
			body.polygon.setRotation(ang.getAngleDegrees());
		}
		
		if (Families.PLAYER_FAMILY.matches(entity) && !engine.isGameOver()) {
			
			oldPos.set(pos.value);
			clamp(pos.value, Mappers.wcm.get(engine.getCamera()).stopped);
			
			engine.sendCoop("PLAYER_POS " + String.valueOf(pos.value.x) + " " + String.valueOf(pos.value.y) + " " + String.valueOf(ang.value));
		}
	}

	private void clamp(Vector2 position, boolean cameraStopped){
		Vector2 cPos = GameScreen.getEngine().getCameraPosition();
		float stoppedMod = cameraStopped ? 0.4f * Globals.TARGET_CAMERA_HEIGHT : 0f;
		
		position.set(MathUtils.clamp(position.x, cPos.x - Globals.TARGET_CAMERA_WIDTH / 2, cPos.x + Globals.TARGET_CAMERA_WIDTH / 2), 
					 MathUtils.clamp(position.y, cPos.y - Globals.TARGET_CAMERA_HEIGHT / 2, cPos.y + Globals.TARGET_CAMERA_HEIGHT / 2 - stoppedMod)); 
	}
}
