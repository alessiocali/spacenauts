package com.gff.spacenauts;

import java.util.UUID;

import com.badlogic.gdx.math.MathUtils;

/**
 * Container for all globals.
 * 
 * @author Alessio Cali'
 *
 */
public final class Globals {

	//Preferences
	public static boolean debug = false;
	public static String nickname = "Spacenaut";
	public static String serverAddress = "localhost";
	public static String locale = "en-EN";
	public static int timeout = 100;	//seconds
	public static float expireCheck;
	
	//Network globals
	public static final int MULTIPLAYER_PORT = 8400;
	public static final UUID SERVICE_UUID = UUID.fromString("a552622e-8098-41a1-9369-d2ced0ddafc8");
	
	//Key game values
	public static float baseCameraSpeed = 2.5f;
	public static final String PREF_FILE = "prefs.ini";
	public static final float PIXELS_PER_UNIT = 60;
	public static final float UNITS_PER_PIXEL = 1f / 60f;
	public static final int TARGET_SCREEN_WIDTH = 1080;
	public static final int TARGET_SCREEN_HEIGHT = 1920;
	public static final float TARGET_CAMERA_WIDTH = TARGET_SCREEN_WIDTH / PIXELS_PER_UNIT;
	public static final float TARGET_CAMERA_HEIGHT = TARGET_SCREEN_HEIGHT / PIXELS_PER_UNIT;
	public static final float STARTING_CAMERA_X = TARGET_CAMERA_WIDTH / 2;
	public static final float STARTING_CAMERA_Y = TARGET_CAMERA_HEIGHT / 2;
	public static final float PLAYER_KEYBOARD_SPEED = 5f;
	public static final float PLAYER_TOUCH_SPEED = 5f;
	public static final float PLAYER_KEYBOARD_ANG_SPEED = MathUtils.PI;
	public static final float REMOVAL_TOLERANCE_RADIUS = 10;
	public static final float SPAWN_RADIUS = 25f;
	
	public static void updateExpire () {
		expireCheck = timeout < 50 ? (float)Math.pow(1.03, timeout) : 10 - 9 * (float)Math.pow(1.009, -timeout);
	}
}
