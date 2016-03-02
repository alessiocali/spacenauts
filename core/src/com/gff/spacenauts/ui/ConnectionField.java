package com.gff.spacenauts.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.gff.spacenauts.net.NetworkAdapter.Host;

/**
 * A TextField that keeps reference to a Host. Used
 * in {@link MultiplayerMenu}.
 * 
 * @author Alessio
 *
 */
public class ConnectionField extends TextField {
	
	private Host host;

	public ConnectionField(String text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}

	public ConnectionField(String text, Skin skin) {
		super(text, skin);
	}

	public ConnectionField(String text, TextFieldStyle style) {
		super(text, style);
	}
	
	public void setHost(Host host) {
		this.host = host;
	}
	
	public Host getHost () {
		return host;
	}

}
