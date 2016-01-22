package com.gff.spacenauts.data;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.listeners.TimerListener.TimerType;

/**
 * A data structure representing a spawner. It includes the power up released by its entities, their initial position, velocity and angle,
 * and the spawner type (whether one shot or continuous).
 * 
 * @author Alessio Cali'
 *
 */
public class SpawnerData implements Serializable {
	private static final long serialVersionUID = -4149526808617252991L;
	public String id = "NULL";
	public Vector2 initialPosition = new Vector2(0,0);
	public Vector2 initialVelocity = new Vector2(0,0);
	public float initialAngle = 0;
	public float intervalTime = 0;
	public int limit = 0;
	public TimerType timerType = TimerType.ONE_SHOT;
	public String releasedPowerUp = null;
	
	public SpawnerData clone(){
		SpawnerData retVal = new SpawnerData();
		retVal.id = id;
		retVal.initialPosition.set(initialPosition);
		retVal.initialVelocity.set(initialVelocity);
		retVal.intervalTime = intervalTime;
		retVal.limit = limit;
		retVal.timerType = timerType;
		retVal.releasedPowerUp = releasedPowerUp;
		return retVal;
	}
}