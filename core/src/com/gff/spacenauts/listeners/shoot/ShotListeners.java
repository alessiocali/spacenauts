package com.gff.spacenauts.listeners.shoot;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.listeners.ShotListener;

/**
 * A collection of {@link ShotListener} that itself implements said interface.
 * 
 * @author Alessio Cali'
 *
 */
public class ShotListeners implements ShotListener {

	private Array<ShotListener> listeners;
	
	public ShotListeners() {
		listeners = new Array<ShotListener>();
	}
	
	public ShotListeners(ShotListener... listeners){
		this.listeners = new Array<ShotListener>(listeners.length);
		this.listeners.addAll(listeners);
	}
	
	public void addListener(ShotListener listener){
		listeners.add(listener);
	}
	
	public void addAll(ShotListener... listeners){
		this.listeners.addAll(listeners);
	}
	
	public void addAll(Array<? extends ShotListener> listeners){
		for (ShotListener listener : listeners)
			addListener(listener);
	}
	
	public void removeListener(ShotListener listener){
		listeners.removeValue(listener, true);
	}
	
	public void removeAll(Array<? extends ShotListener> listeners){
		this.listeners.removeAll(listeners, true);
	}
	
	public void clear(){
		listeners.clear();
	}
	
	public Array<ShotListener> getListeners(){
		return listeners;
	}
	
	@Override
	public void onShooting(Entity gun, Entity bullet) {
		for (ShotListener listener : listeners)
			listener.onShooting(gun, bullet);
	}

}
