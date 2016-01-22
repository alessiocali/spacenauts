package com.gff.spacenauts.data;

import java.io.Serializable;

import com.badlogic.gdx.math.Rectangle;

/**
 * Intermediary host for DialogTriggers. Loaded from LevelData and used by Level during the building process.
 * 
 * @author Alessio Cali'
 *
 */
public class DialogTriggerData implements Serializable {
	
	private static final long serialVersionUID = -6204022790180452482L;
	
	public Rectangle area = new Rectangle();
	public String dialogID = "";
	
}
