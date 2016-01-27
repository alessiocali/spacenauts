package com.gff.spacenauts.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * A ClickListener that opens a URL when clicked on.
 * 
 * @author Alessio Cali'
 *
 */
public class LinkListener extends ClickListener {
	
	private String url;
	
	public LinkListener (String url) {
		this.url = url;
	}
	
	@Override
	public void clicked(InputEvent e, float x, float y) {
		Gdx.net.openURI(url);
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

}
