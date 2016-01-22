package com.gff.spacenauts.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.desktop.net.InetAdapter;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 480;
		config.height = 800;
		config.resizable = true;
		final Spacenauts game = new Spacenauts(new InetAdapter());
		new LwjglApplication(game, config);
	}
}
