package com.gff.spacenauts.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
	private Cursor linkCursor;
	
	public LinkListener (String url, Cursor linkCursor) {
		this.url = url;
		this.linkCursor = linkCursor;
	}
	
	@Override
	public void clicked(InputEvent e, float x, float y) {
		Gdx.net.openURI(url);
	}
	
	@Override
	public void enter(InputEvent e, float x, float y, int pointer, Actor fromActor) {
		Gdx.graphics.setCursor(linkCursor);
	}
	
	@Override
	public void exit(InputEvent e, float x, float y, int pointer, Actor toActor) {
		Gdx.graphics.setCursor(null);
	}

}
