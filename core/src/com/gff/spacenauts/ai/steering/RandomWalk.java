package com.gff.spacenauts.ai.steering;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ashley.SteeringMechanism;
import com.gff.spacenauts.screens.GameScreen;

/**
 * A steering behavior that chooses a random target within the camera view, then
 * uses {@link Arrive} to reach it, and repeats.
 * 
 * @author Alessio Cali'
 *
 */
public class RandomWalk extends SteeringBehavior<Vector2> {

	private Arrive<Vector2> arrive;
	private Vector2 nextTarget = new Vector2();
	
	public RandomWalk(Steerable<Vector2> owner) {
		super(owner);
		arrive = new Arrive<Vector2>(owner);
		randomTarget();
		arrive.setTarget(SteeringMechanism.getQuickTarget(nextTarget));
		arrive.setArrivalTolerance(0.1f);
		arrive.setDecelerationRadius(2);
	}

	@Override
	protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
		Location<Vector2> target = arrive.getTarget();
		
		if (owner.getPosition().dst(target.getPosition()) <= arrive.getArrivalTolerance()) {
			randomTarget();
		}
		
		return arrive.calculateSteering(steering);
	}
	
	private void randomTarget() {
		Vector2 cameraPos = GameScreen.getEngine().getCameraPosition();
		float randomX = (2 * MathUtils.random() - 1) * Globals.TARGET_CAMERA_WIDTH / 2;
		float randomY = (2 * MathUtils.random() - 1) * Globals.TARGET_CAMERA_HEIGHT / 2;
		nextTarget.set(cameraPos).add(randomX, randomY);
	}
}
