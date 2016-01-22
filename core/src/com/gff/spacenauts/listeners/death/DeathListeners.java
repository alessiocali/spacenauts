package com.gff.spacenauts.listeners.death;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.listeners.DeathListener;

/**
 * A collection of {@link DeathListener}s that itself implements said interface.
 * 
 * @author Alessio Cali'
 *
 */
public class DeathListeners implements DeathListener {

	private Array<DeathListener> listeners;
	
	public DeathListeners() {
		listeners = new Array<DeathListener>();
	}
	
	public DeathListeners(DeathListener... listeners){
		this.listeners = new Array<DeathListener>(listeners.length);
		this.listeners.addAll(listeners);
	}
	
	public void addListener(DeathListener listener){
		listeners.add(listener);
	}
	
	public void addAll(DeathListener... listeners){
		this.listeners.addAll(listeners);
	}
	
	public void addAll(Array<? extends DeathListener> listeners){
		for (DeathListener listener : listeners)
			addListener(listener);
	}
	
	public void removeListener(DeathListener listener){
		listeners.removeValue(listener, true);
	}
	
	public void removeAll(Array<? extends DeathListener> listeners){
		for (DeathListener listener : listeners)
			removeListener(listener);
	}
	
	public void clear(){
		listeners.clear();
	}
	
	public Array<DeathListener> getListeners(){
		return listeners;
	}
	
	@Override
	public void onDeath(Entity entity) {
		for (DeathListener listener : listeners)
			listener.onDeath(entity);
	}
}
