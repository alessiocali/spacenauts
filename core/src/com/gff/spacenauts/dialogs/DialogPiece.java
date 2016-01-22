package com.gff.spacenauts.dialogs;

import java.io.Serializable;

/**
 * A String couple defining the speaker and the text of a conversation. 
 * Also has a fixed duration that determines how long the dialog piece will be shown before moving forward.
 * 
 * @author Alessio Cali'
 *
 */
public class DialogPiece implements Serializable {

	private static final long serialVersionUID = 3203179728073031684L;

	private String speaker;
	private String text;
	private String locale;
	private boolean hasNext;
	private float duration;
	
	public DialogPiece(String speaker, String text, String locale, boolean hasNext, float duration) {
		super();
		this.speaker = speaker;
		this.text = text;
		this.locale = locale;
		this.hasNext = hasNext;
		this.duration = duration;
	}

	public String getSpeaker() {
		return speaker;
	}


	public String getText() {
		return text;
	}


	public String getLocale() {
		return locale;
	}
	
	public boolean hasNext(){
		return hasNext;
	}
	
	public float getDuration(){
		return duration;
	}
	
}
