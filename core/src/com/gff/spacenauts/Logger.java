package com.gff.spacenauts;

import com.badlogic.gdx.Gdx;

/**
 * A simple logger that used Gdx's {@link com.badlogic.gdx.Application#log(String tag, String message) log} tool.
 * 
 * @author Alessio Cali'
 *
 */
public class Logger {
	
	public enum LogLevel {
		WARNING, ERROR, UPDATE
	}

	public static boolean logging = true;
	
	public static void log(LogLevel level, String caller, String message){
		if (logging)
			Gdx.app.log(caller, level + " : " + message);
	}
}
