package com.gff.spacenauts.ashley.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.gff.spacenauts.ashley.Families;
import com.gff.spacenauts.ashley.Mappers;
import com.gff.spacenauts.ashley.components.DialogTrigger;
import com.gff.spacenauts.dialogs.Dialog;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.ui.GameUI;

/**
 * Checks whether the camera enters a DialogTrigger, and if so it invokes UI methods for showing it.
 * 
 * @author Alessio Cali'
 *
 */
public class DialogSystem extends IteratingSystem {
	
	private GameUI ui;
	private Vector2 cameraPos;
	private Dialog currentDialog;
	
	private float timer = 0;

	public DialogSystem(GameScreen game){
		super(Families.DIALOG_FAMILY);
		this.ui = game.getUI();
	}
	
	@Override
	public void update(float delta){
		cameraPos = Mappers.pm.get(GameScreen.getEngine().getCamera()).value;
		
		if (currentDialog != null){
			timer += delta;
			
			if (timer > currentDialog.getCurrent().getDuration()){
				
				if (currentDialog.hasNext()) {
					currentDialog.next();
				} else {
					ui.hideDialog();
					currentDialog = null;
				}
				
				timer = 0;
			}
		}
		super.update(delta);
	}
	
	@Override
	public void processEntity(Entity entity, float delta){
		DialogTrigger trigger = Mappers.dm.get(entity);
		
		if (trigger.area.contains(cameraPos) && !trigger.started){
			currentDialog = trigger.dialog;
			currentDialog.next();
			ui.triggerDialog(currentDialog);
			trigger.started = true;
			entity.remove(DialogTrigger.class);
		}
	}
	
}
