package com.gff.spacenauts.listeners;

import com.badlogic.ashley.core.Entity;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.Timers;

/**
 * A multipurpose timer that schedules events for later, possibly on a regular basis. Mantained by {@link com.gff.spacenauts.ashley.systems.TimerSystem TimerSystem}.
 * 
 * @author Alessio Cali'
 *
 */
public abstract class TimerListener {
	
	/**
	 * How the timer iterates.
	 * 
	 * <ul>
	 * <li><b>ONE_SHOT</b>: The timer is fired only once, at its activation, then expires.</li>
	 * <li><b>INTERVAL</b>: The timer is fired at regular intervals, and never expires.</li>
	 * <li><b>INTERVAL_LIMITED</b>: The timer is fired at regular intervals, up to a certain number of times, then expires.</li>
	 * <li><b>EXPIRED</b>: The timer already expired and can no longer be triggered.</li>
	 * </ul>
	 * 
	 * @author Alessio
	 *
	 */
	public enum TimerType {
		ONE_SHOT("one_shot"),
		INTERVAL("interval"),
		INTERVAL_LIMITED("interval_limited"),
		EXPIRED("expired");
		
		private String typeName;
		
		private TimerType(String typeName){
			this.typeName = typeName;
		}
		
		public String getName(){
			return typeName;
		}
		
		public static TimerType getByName(String name){
			for (TimerType type : TimerType.values()){
				if (name.equals(type.getName()))
					return type;
			}
			
			Logger.log(LogLevel.WARNING, "TimeType", "Could not find member by name: " + name);
			return null;
		}
	}
	
	protected TimerType type = TimerType.EXPIRED;
	private float timer = 0;
	protected float duration = 0;
	private int count = 0;
	protected int limit;
	
	public TimerListener (TimerType type, float duration, int limit) {
		this.type = type;
		this.duration = duration;
		this.limit = limit;
	}
	
	public TimerListener (TimerType type, float duration) {
		this(type, duration, 0);
	}

	/**
	 * Called when the timer clicks.
	 * 
	 * @param entity
	 * @return Whether the timer was internally activated. If false, the timer is reset but not set as expired, nor is count updated.
	 */
	protected abstract boolean onActivation (Entity entity);
	
	public void update (float delta, Entity entity) {
		if (type == TimerType.EXPIRED) {
			Timers timers = Mappers.tm.get(entity);
			if (timers != null) timers.listeners.removeValue(this, true);
			return;
		}
		
		timer += delta;
		
		if (timer >= duration) {
			boolean result = onActivation(entity);
			timer = 0;
			
			if (type == TimerType.ONE_SHOT && result) type = TimerType.EXPIRED;
			
			else if (type == TimerType.INTERVAL_LIMITED && result) {
				count++;
				if (count >= limit) type = TimerType.EXPIRED;
			}
		}
	}

}
