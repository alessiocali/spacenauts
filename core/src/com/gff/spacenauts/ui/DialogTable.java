package com.gff.spacenauts.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.dialogs.Dialog;

/**
 * An UI element made by a larger pane hosting a DialogPiece's text, and a smaller pane on its top with the DialogPiece's speaker label.
 * 
 * @author Alessio Cali'
 *
 */
public class DialogTable extends Table {	
	
	private Label speaker;
	private TextArea dialogText;
	private Dialog dialog;
	
	public DialogTable(AssetManager assets){
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI);
		NinePatchDrawable overPane = new NinePatchDrawable(uiAtlas.createPatch(GameUI.OVER_PANE_NAME));
		NinePatchDrawable defaultPanel = new NinePatchDrawable(uiAtlas.createPatch(GameUI.DEFAULT_PANE_NAME));
		BitmapFont font = assets.get(AssetsPaths.FONT_ATARI_32);
		
		Label.LabelStyle speakerStyle = new LabelStyle();
		speakerStyle.background = overPane;
		speakerStyle.font = font;
		speakerStyle.fontColor = Color.WHITE;
		
		TextField.TextFieldStyle dialogStyle = new TextFieldStyle();
		dialogStyle.background = defaultPanel;
		dialogStyle.font = font;
		dialogStyle.fontColor = Color.WHITE;
		
		speaker = new Label("???", speakerStyle);
		speaker.setTouchable(Touchable.disabled);
		dialogText = new TextArea("???", dialogStyle);
		dialogText.setTouchable(Touchable.disabled);
		//dialogText.setPrefRows(5);
		
		this.add(speaker).left().bottom().height(75);
		this.row();
		this.add(dialogText).left().bottom().expand().fill();
	}
	
	public void setDialog(Dialog dialog){
		this.dialog = dialog;
	}
	
	@Override
	public void act(float delta){
		super.act(delta);
		
		if (dialog != null){
			speaker.setText(dialog.getCurrent().getSpeaker());
			dialogText.setText(dialog.getCurrent().getText());
		}
	}
	
	public Dialog getDialog() {
		return dialog;
	}
}
