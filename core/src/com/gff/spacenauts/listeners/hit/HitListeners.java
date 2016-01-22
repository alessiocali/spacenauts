package com.gff.spacenauts.listeners.hit;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.gff.spacenauts.listeners.HitListener;

/**
 * A collection of {@link HitListener}s that itself implements said interface.
 * 
 * @author Alessio Cali'
 *
 */
public class HitListeners implements HitListener {

	private Array<HitListener> listeners;
	
	public HitListeners() {
		listeners = new Array<HitListener>();
	}
	
	public HitListeners(HitListener... listeners){
		this.listeners = new Array<HitListener>(listeners.length);
		this.listeners.addAll(listeners);
	}
	
	public void addListener(HitListener listener){
		listeners.add(listener);
	}
	
	public void addAll(HitListener... listeners){
		this.listeners.addAll(listeners);
	}
	
	public void addAll(Array<? extends HitListener> listeners){
		for (HitListener listener : listeners)
			addListener(listener);
	}
	
	public void removeListener(HitListener listener){
		listeners.removeValue(listener, true);
	}
	
	public void removeAll(Array<? extends HitListener> listeners){
		this.listeners.removeAll(listeners, true);
	}
	
	public void clear(){
		listeners.clear();
	}
	
	public Array<HitListener> getListeners(){
		return listeners;
	}
	
	@Override
	public void onHit(Entity entity, Entity collider) {
		for (HitListener listener : listeners)
			listener.onHit(entity, collider);
	}

}
