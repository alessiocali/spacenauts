package com.gff.spacenauts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Angle;
import com.gff.spacenauts.ashley.components.AngularVelocity;
import com.gff.spacenauts.ashley.components.Body;
import com.gff.spacenauts.ashley.components.Gun;
import com.gff.spacenauts.ashley.components.Position;
import com.gff.spacenauts.ashley.components.Velocity;
import com.gff.spacenauts.ashley.components.WorldCamera;
import com.gff.spacenauts.ashley.systems.PhysicsSystem;
import com.gff.spacenauts.data.GunData;
import com.gff.spacenauts.screens.GameScreen;

/**
 * Input adapter for both keyboard and touch/mouse controls.<p>
 * Multi-touch is supported but not required. The first finger is bound to drag the spaceship around, while any time the screen
 * is tapped the spaceship will shoot in the given direction. 
 * 
 * @author Alessio Cali'
 *
 */
public class Controls extends InputAdapter {

	//A short cooldown to stop people from pointlessly mashing the screen.
	private final static float SHOOT_REST = 0.100f;

	private Vector2 touchedVector = new Vector2();
	private Vector2 startVector = new Vector2();
	private Vector2 translationVector = new Vector2();
	private Vector2 lastPlayerPos = new Vector2();
	private int firstFingerPointer = -1;
	private float shootTimer = 0;

	/**
	 * <p>First screen coordinates are stored inside touchedVector, which is then unprojected to the game world.</p>
	 * 
	 * <p>
	 * Then firstFingerPointer is checked. If -1 is found, it means that no touch has been registered yet as "first finger".
	 * If so, firstFingerPointer is updated to the value in pointer. Also, screen coordinates are stored inside startVector as a
	 * later reference (see {@link #touchDragged}).
	 * </p>
	 * 
	 * <p>
	 * Finally, the player's entity is retrieved from the Engine. If this succeeds, the oriented vector player-to-touchedPoint is calculated
	 * (touchedVector.sub(pos.position)). From this vector the new orientation is obtained and the player's angle is set accordingly; 
	 * also, all of the player's guns are triggered.
	 * </p>
	 * 
	 * @see com.badlogic.gdx.InputAdapter#touchDown(int, int, int, int)
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		if (!GameScreen.getEngine().controlsEnabled())
			return false;

		Entity cameraEntity = GameScreen.getEngine().getCamera();
		WorldCamera cameraComponent = Mappers.wcm.get(cameraEntity); 

		touchedVector.set(screenX, screenY);
		cameraComponent.viewport.unproject(touchedVector);

		//Record firstFinger
		if (firstFingerPointer == -1){
			firstFingerPointer = pointer;
			startVector.set(screenX, screenY);
		}

		Entity player = GameScreen.getEngine().getPlayer();

		if (player == null)
			return false;

		Position pos = Mappers.pm.get(player);
		Angle ang = Mappers.am.get(player);
		Body body = Mappers.bm.get(player);
		Gun guns = Mappers.gm.get(player);

		//Halt here if the player simply touched the ship. Don't shoot.
		if (body.polygon.contains(touchedVector.x, touchedVector.y))
			return true;

		touchedVector.sub(pos.value);
		ang.value = touchedVector.angleRad();
		body.polygon.setRotation(touchedVector.angle());

		if (shootTimer >= SHOOT_REST) {
			shootTimer = 0;
			for (GunData gun : guns.guns) gun.triggered = true;
			GameScreen.getEngine().sendCoop("PLAYER_SHOT");
		}

		return true;
	}

	/**
	 * When the finger recorded as first is lifted, then firstFingerPointer is cleared (set to -1).
	 * 
	 * @see com.badlogic.gdx.InputAdapter#touchUp(int, int, int, int)
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){		
		if (pointer == firstFingerPointer) {
			firstFingerPointer = -1;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>This method is the one responsible for the player's movement.</p>
	 * 
	 * <p>
	 * First, it will act only on the first finger. It works by comparing the new screen coordinates with the last recorded (stored inside startVector).
	 * Both are unprojected in this occasion, then the translation vector is calculated by their difference. Contrary to previous implementations the movement 
	 * is unbounded. However a check is performed against all nearby obstacles to prevent tunneling. Check {@link #tunnelCheck(Entity)}.
	 * After this check, startVector is updated with the new screen coordinates.
	 * </p>
	 * 
	 * @see com.badlogic.gdx.InputAdapter#touchDragged(int, int, int)
	 */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		if (!GameScreen.getEngine().controlsEnabled())
			return false;

		//Ignore if not the first finger.
		if (pointer != firstFingerPointer)
			return false;

		Entity player = GameScreen.getEngine().getPlayer();

		if (player == null)
			return false;

		Entity cameraEntity = GameScreen.getEngine().getCamera();
		WorldCamera cameraComponent = Mappers.wcm.get(cameraEntity);

		touchedVector.set(screenX, screenY);
		cameraComponent.viewport.unproject(touchedVector);
		cameraComponent.viewport.unproject(startVector);

		Position playerPos = Mappers.pm.get(player);
		Body playerBody = Mappers.bm.get(player);

		lastPlayerPos.set(playerPos.value);
		playerPos.value.add(translationVector.set(touchedVector).sub(startVector));
		playerBody.polygon.setPosition(playerPos.value.x, playerPos.value.y);

		tunnelCheck(player);

		startVector.set(screenX, screenY);

		return true;
	}

	/**
	 * Handles keyboard movement. WASD and Up/Down keys move the player by increasing its speed in a certain direction.
	 * Q, E or Left, Right rotate the player, again by increasing angular speed. Spacebar makes the spaceship shoot.
	 * 
	 * @see com.badlogic.gdx.InputAdapter#keyDown(int)
	 */
	@Override
	public boolean keyDown(int keycode){

		Entity player = GameScreen.getEngine().getPlayer();

		if (player == null || GameScreen.getEngine().isGameOver())
			return false;

		Velocity vel = Mappers.vm.get(player);
		AngularVelocity angVel = Mappers.avm.get(player);
		Gun gun = Mappers.gm.get(player);

		switch(keycode){

		case (Input.Keys.A):
			vel.value.add(-Globals.PLAYER_KEYBOARD_SPEED, 0);
		return true;

		case (Input.Keys.D):
			vel.value.add(Globals.PLAYER_KEYBOARD_SPEED, 0);
		return true;

		case (Input.Keys.DOWN):
		case (Input.Keys.S):
			vel.value.add(0, -Globals.PLAYER_KEYBOARD_SPEED);
		return true;

		case (Input.Keys.UP):
		case (Input.Keys.W):
			vel.value.add(0, Globals.PLAYER_KEYBOARD_SPEED);
		return true;

		case (Input.Keys.LEFT):
		case (Input.Keys.Q):
			angVel.value += Globals.PLAYER_KEYBOARD_ANG_SPEED;
		return true;

		case (Input.Keys.RIGHT):
		case (Input.Keys.E):
			angVel.value -= Globals.PLAYER_KEYBOARD_ANG_SPEED;
		return true;

		case (Input.Keys.SPACE):
			if (shootTimer >= SHOOT_REST) {
				shootTimer = 0;
				for (GunData gunData : gun.guns) gunData.triggered = true;
				GameScreen.getEngine().sendCoop("PLAYER_SHOT");
			}
		return true;

		default:
			return false;
		}
	}

	/**
	 * Works exactly the same as {@link #keyDown(int)}, except the directions are swapped, so that the player stops moving
	 * in the direction of the lifted key.
	 * 
	 * @see com.badlogic.gdx.InputAdapter#keyUp(int)
	 */
	@Override
	public boolean keyUp(int keycode){

		Entity player = GameScreen.getEngine().getPlayer();

		if (player == null || GameScreen.getEngine().isGameOver())
			return false;

		Velocity vel = Mappers.vm.get(player);
		AngularVelocity angVel = Mappers.avm.get(player);

		switch(keycode){
		
		case (Input.Keys.A):
			vel.value.add(Globals.PLAYER_KEYBOARD_SPEED, 0);
		return true;
		
		case (Input.Keys.D):
			vel.value.add(-Globals.PLAYER_KEYBOARD_SPEED, 0);
		return true;
		
		case (Input.Keys.DOWN):
		case (Input.Keys.S):
			vel.value.add(0, Globals.PLAYER_KEYBOARD_SPEED);
		return true;
		
		case (Input.Keys.UP):
		case (Input.Keys.W):
			vel.value.add(0, -Globals.PLAYER_KEYBOARD_SPEED);
		return true;
		
		case (Input.Keys.LEFT):
		case (Input.Keys.Q):
			angVel.value -= Globals.PLAYER_KEYBOARD_ANG_SPEED;
		return true;
		
		case (Input.Keys.RIGHT):
		case (Input.Keys.E):
			angVel.value += Globals.PLAYER_KEYBOARD_ANG_SPEED;
		return true;
		
		default:
			return false;
		}
	}

	/**
	 * Called to update the cooldown timer.
	 * 
	 * @param delta
	 */
	public void update(float delta) {
		if (shootTimer < SHOOT_REST)
			shootTimer += delta;
	}

	/**
	 * It's a dirty fix but it works. Whenever the player is dragged around {@link PhysicsSystem#getNearbyObstacles()} is called,
	 * getting all local obstacles. All obstacles are tested against the player's body and the translation vector 
	 * (this to avoid large translation vectors bypassing an obstacle). If they collide, then the translation is undone.
	 * 
	 * @param player
	 */
	private void tunnelCheck(Entity player) {
		PhysicsSystem ps = GameScreen.getEngine().getSystem(PhysicsSystem.class);

		Position playerPos = Mappers.pm.get(player);
		Body playerBody = Mappers.bm.get(player);

		for (Entity obstacle : ps.getNearbyObstacles()) {
			Body b = Mappers.bm.get(obstacle);
			
			//Undo movement if player overlaps or bypasses an obstacle
			if (Intersector.overlapConvexPolygons(b.polygon, playerBody.polygon)
					|| Intersector.intersectSegmentPolygon(lastPlayerPos, playerPos.value, b.polygon)) {
				playerPos.value.sub(translationVector);
				playerBody.polygon.setPosition(playerPos.value.x, playerPos.value.y);
				break;
			}
		}
	}
}
