package com.gff.spacenauts.ui;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.screens.InitialScreen;

/**
 * A simple list of options. Currently hosts only the option for Debug Mode.
 * 
 * @author Alessio Cali'
 *
 */
public class OptionsMenu implements UISet {

	private CheckBox debugCheckbox;
	private Table mainTable;
	private Image logo;
	private ImageButton backButton;
	private Label nicknameLabel;
	private TextField nicknameField;
	private Label timeoutLabel;
	private TextField timeoutField;
	private Label serverLabel;
	private TextField serverField;

	public OptionsMenu(AssetManager assets, final UISet from, final InitialScreen initial) {		
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI);
		BitmapFont a32 = assets.get(AssetsPaths.FONT_ATARI_32);

		mainTable = new Table();
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("options_logo")));
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(from);
				Preferences prefs = Gdx.app.getPreferences(Globals.PREF_FILE);
				prefs.putBoolean("debug", Globals.debug);
				prefs.putString("nickname", Globals.nickname);
				prefs.putString("serverAddress", Globals.serverAddress);
				prefs.putInteger("timeout", Globals.timeout);
				prefs.flush();
			} 
		});

		Drawable checkBoxOff = new TextureRegionDrawable(uiAtlas.findRegion("checkbox_off"));
		Drawable checkBoxOn = new TextureRegionDrawable(uiAtlas.findRegion("checkbox_tick"));

		CheckBox.CheckBoxStyle cStyle = new CheckBoxStyle(checkBoxOff, checkBoxOn, a32, Color.WHITE);

		debugCheckbox = new CheckBox("Debug Mode", cStyle);
		debugCheckbox.setChecked(Globals.debug);
		debugCheckbox.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox) actor;
				Globals.debug = checkBox.isChecked();
			}

		});

		mainTable.add(debugCheckbox).left().expandX().pad(5).row();

		Label.LabelStyle lStyle = new Label.LabelStyle(a32, Color.WHITE);
		TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle(a32, Color.WHITE, 
				new TextureRegionDrawable(uiAtlas.findRegion("cursor")), 
				new TextureRegionDrawable(uiAtlas.findRegion("selection")), 
				new NinePatchDrawable(uiAtlas.createPatch("default_pane")));
		tfStyle.cursor.setMinWidth(2);

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.Android) {

			nicknameLabel = new Label("Nickname", lStyle);
			nicknameField = new TextField(Globals.nickname, tfStyle);
			nicknameField.setMaxLength(30);
			nicknameField.setBlinkTime(1);
			nicknameField.addListener(new ChangeListener () {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					TextField tf = (TextField) actor;
					Globals.nickname = tf.getText();
				}

			});

			mainTable.add(nicknameLabel).left().pad(5).row();
			mainTable.add(nicknameField).left().pad(5).fillX().width(800).row();

			timeoutLabel = new Label("Timeout", lStyle);
			timeoutField = new TextField(String.valueOf(Globals.timeout), tfStyle);
			timeoutField.setMaxLength(3);
			timeoutField.setBlinkTime(1);
			timeoutField.addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					TextField tf = (TextField) actor;
					String timeoutString = tf.getText();
					int timeout = 100;
					try {
						timeout = Integer.valueOf(timeoutString); 
					} catch (NumberFormatException e) {
						timeout = 100;
					}
					if (timeout <= 10 || timeout > 300) timeout = 100;
					Globals.timeout = timeout;
					Globals.updateExpire();
				}
			});

			mainTable.add(timeoutLabel).left().pad(5).row();
			mainTable.add(timeoutField).left().pad(5).width(200).row();

		}

		if (Gdx.app.getType() == ApplicationType.Desktop) {	
			serverLabel = new Label("Multiplayer Server Address", lStyle);
			serverField = new TextField(Globals.serverAddress, tfStyle);
			serverField.setMaxLength(15);
			serverField.setBlinkTime(1);
			serverField.addListener(new ChangeListener () {

				private static final String IP_REGEX = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					TextField tf = (TextField) actor;
					String address = tf.getText();

					if (!address.matches(IP_REGEX) && !address.equals("localhost"))
						address = "localhost";

					Globals.serverAddress = address;					
				}

			});

			mainTable.row();
			mainTable.add(serverLabel).left().pad(5).row();
			mainTable.add(serverField).left().pad(5).width(800).fillX().row();
		}
	}

	public void synch () {
		debugCheckbox.setChecked(Globals.debug);
		nicknameField.setText(Globals.nickname);
		timeoutField.setText(String.valueOf(Globals.timeout));
		if (Gdx.app.getType() == ApplicationType.Desktop)
			serverField.setText(Globals.serverAddress);
	}

	@Override
	public Image logo () {
		return logo;
	}

	@Override
	public ImageButton lowerLeft() {
		return backButton;
	}

	@Override
	public ImageButton lowerRight() {
		return null;
	}

	@Override
	public Table main () {
		return mainTable;
	}
}
