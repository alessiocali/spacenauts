package com.gff.spacenauts;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gff.spacenauts.net.NetworkAdapter;
import com.gff.spacenauts.screens.InitialScreen;
import com.gff.spacenauts.screens.LoadingScreen;

/**
 * The game's {@link Game} class.
 * 
 * @see com.badlogic.gdx.Game
 * 
 * @author Alessio Cali'
 *
 */
public class Spacenauts extends Game {
	
	private static NetworkAdapter na;
	
	public Spacenauts () {
		this(null);
	}
	
	public Spacenauts (NetworkAdapter na) {
		Spacenauts.na = na;
		if (na != null) na.reset();
	}
	
	private void initPrefs () {
		Preferences pref = Gdx.app.getPreferences(Globals.PREF_FILE);
		
		Globals.nickname = pref.getString("nickname", "Spacenaut");
		Globals.locale = pref.getString("locale", "en-EN");
		Globals.serverAddress = pref.getString("serverAddress", "localhost");
		Globals.timeout = pref.getInteger("timeout", 100);
		Globals.updateExpire();
		Globals.debug = pref.getBoolean("debug", false);
		Globals.godmode = pref.getBoolean("godmode", false);
		Globals.levelUnlocked = pref.getInteger("levelUnlocked", 0);
	}
	
	@Override
	public void create(){
		initPrefs();
		InitialScreen initial = new InitialScreen(this);
		LoadingScreen loaderScreen = new LoadingScreen(initial, this, initial);
		setScreen(loaderScreen);
	}
	
	@Override
	public void pause() {
		super.pause();
		if (Spacenauts.na != null && Gdx.app.getType() == ApplicationType.Android) 
			Spacenauts.na.reset();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (Spacenauts.na != null) Spacenauts.na.reset();
	}
	
	
	public static NetworkAdapter getNetworkAdapter() {
		return na;
	}
}
