/*
 * TODO
 * 
 * Spazio per ottimizzazione:
 * 
 * - Event Programming 
 * -- I Listener non sfruttano tecniche di Pooling. Di fatto, un bel po' di memoria viene sprecata creando
 * 		un gran numero di istanze che sostanzialmente fanno la stessa cosa (la personalizzazione avviene tramite il passaggio dell'entita')
 * -- Parzialmente ottimizzato rendendo statici alcuni listener, ove possibile.
 * - SteeringBehavior e Pooling
 * -- Tutti gli SteeringBehavior vengono istanziati a runtime, non tutti posseggono costruttori void e costruire una Pool di una classe astratta non sono neanche
 * 		sicuro abbia senso. Ad ogni modo le loro istanze non vengono riciclate.
 * - Collisioni
 * -- Le collisioni continuano a non essere il top. E' facile bypassare barriere e ostacoli "forzandovi" contro, e mandandoci contro la navicella
 * 		lo sprite diventa ballerino.
 *
 * -- Possibile Memory Leak: ApplySteering istanzia una nuova istanza a ogni sparo!
 * 
 * Da aggiungere:
 * 
 * - Menu' di conferma in capture del tasto BACK su Android
 * - Almeno altri 3 livelli prima dell'ultima release.
 */

package com.gff.spacenauts;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.gff.spacenauts.net.NetworkAdapter;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.screens.InitialScreen;

/**
 * The game's {@link Game} class.
 * 
 * @see com.badlogic.gdx.Game
 * 
 * @author Alessio Cali'
 *
 */
public class Spacenauts extends Game {
	
	private Screen startingScreen = null;
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
	}
	
	public void startLevel(String map){
		this.setScreen(new GameScreen(map, this));
	}
	
	@Override
	public void create(){
		initPrefs();
		startingScreen = new InitialScreen(this);
		setScreen(startingScreen);
	}
	
	@Override
	public void pause() {
		super.pause();
		if (Spacenauts.na != null && Gdx.app.getType() == ApplicationType.Android) Spacenauts.na.reset();
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
