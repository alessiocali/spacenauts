package com.gff.spacenauts.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class HandCursorListener extends ClickListener {

	private Cursor linkCursor;
	
	public HandCursorListener (Cursor linkCursor) {
		this.linkCursor = linkCursor;
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
