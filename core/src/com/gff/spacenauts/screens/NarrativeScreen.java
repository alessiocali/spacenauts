package com.gff.spacenauts.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.dialogs.Dialog;
import com.gff.spacenauts.ui.DialogTable;

/**
 * <p>
 * A screen used to show dialogs outside of the game proper, in order to create
 * a narrative context. This screen loads scenes inside a "cutscenes" file; every
 * scene is basically the union of multiple sections holding reference to a background
 * image, the position and size of the dialog window and a dialog contained inside
 * the dialog file (the same used for game dialogs). All sections within the same
 * scene have a common BGM which is played in the meantime, and an unique ID.
 * </p>
 * 
 * <p>
 * Tapping the screen advances the dialog, while tapping the "SKIP" buttons skips it
 * entirely. Once the scene is over a new Screen is loaded, which must be provided
 * by constructor.
 * </p>
 * 
 * @author Alessio
 *
 */
public class NarrativeScreen extends ScreenAdapter {
	
	private String sceneId;
	private Screen nextScreen;
	private Game game;
	
	//Assets
	private AssetManager assets;
	private BitmapFont k40;
	
	//UI
	private Stage ui;
	private Stack root;
	private Texture backgroundTexture;
	private Image background;
	private Table uiTable;
	private Cell<DialogTable> dialogTableCell;
	private DialogTable dialogTable;
	private TextButton skip;
	private Label tapToContinue;

	//JSON related data
	private JsonValue cutscenes;
	private JsonValue scene;
	private Music bgm;
	private JsonValue sections;
	int currentSection = 0;
	
	public NarrativeScreen(String sceneId, Screen nextScreen, Game game) {
		this.sceneId = sceneId;
		this.nextScreen = nextScreen;
		this.game = game;
	}

	@Override
	public void show() {
		//Load basic assets
		assets = new AssetManager();
		assets.load(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		assets.load(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		assets.load(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		assets.finishLoading();
		
		//Parse the cutscene JSON
		cutscenes  = new JsonReader().parse(Gdx.files.internal(AssetsPaths.DATA_CUTSCENES)).get("cutscenes");
		
		k40 = assets.get(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		
		initUI();
		
		Gdx.input.setInputProcessor(ui);
				
		//Find the scene to be shown
		for (JsonValue scene : cutscenes) {
			if (scene.getString("id", "").equals(sceneId)){
				this.scene = scene;
				break;
			}
		}
		
		if (scene == null) throw new GdxRuntimeException("No valid cutscene with id: " + sceneId);
		
		//Find and set the BGM
		String bgmPath = scene.getString("bgm", "");
		
		if (!bgmPath.equals("")) {
			bgm = Gdx.audio.newMusic(Gdx.files.internal(bgmPath));
			bgm.setLooping(true);
			bgm.play();
		}
		
		//Load all sections and parse the first
		sections = scene.get("sections");
		if (sections.size < 1) throw new GdxRuntimeException("No sections found inside cutscene: " + sceneId);
		parseSection(sections.get(currentSection++));
	}
	
	@Override
	public void render (float delta) {
		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.setDebugAll(Globals.debug);
		ui.act(delta);
		ui.draw();
	}
	
	@Override
	public void resize (int width, int height) {
		ui.getViewport().update(width, height);
	}
	
	@Override
	public void hide () {
		if (assets != null) assets.dispose();
		if (backgroundTexture != null) backgroundTexture.dispose();
		if (k40 != null) k40.dispose();
		if (bgm != null) {
			bgm.stop();
			bgm.dispose();
		}
		if (ui != null) ui.dispose();
	}
	
	private void initUI() {
		ui = new Stage(new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT));
		
		//Root stack
		root = new Stack();
		root.setFillParent(true);
		ui.addActor(root);
		
		//Background image. Tapping it causes the dialog to advance.
		background = new Image();
		background.setFillParent(true);
		background.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				advanceDialog();
			}
		});
		root.add(background);
		
		//Main UI table
		uiTable = new Table();
		uiTable.setFillParent(true);
		root.add(uiTable);

		//The table showing the dialog
		dialogTable = new DialogTable(assets);
		dialogTableCell = uiTable.add(dialogTable).colspan(3).expand();
		uiTable.row();
		
		//Style init
		Label.LabelStyle lStyle = new Label.LabelStyle(k40, Color.WHITE);
		TextButton.TextButtonStyle tbStyle = new TextButton.TextButtonStyle(null, null, null, k40);
		tbStyle.fontColor = Color.WHITE;
		
		//Skip button. Clicking it will go to the next screen.
		skip = new TextButton("SKIP", tbStyle);
		skip.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				setNextScreen();
			}
		});
		uiTable.add(skip).pad(10);
		
		//Blank space between the SKIP button and the TAP TO CONTINUE label
		uiTable.add().expandX();
		
		//TAP TO CONTINUE label
		tapToContinue = new Label("TAP TO CONTINUE", lStyle);
		uiTable.add(tapToContinue).pad(10);
	}
	
	/**
	 * Retrieves data from a section within a scene JSON value, that 
	 * is the scene background and the bounds of the dialog table.	 * 
	 * 
	 * @param section
	 */
	private void parseSection(JsonValue section) {
		String bg = section.getString("bg", "");
		
		if (!bg.equals("")) {
			if (backgroundTexture != null) backgroundTexture.dispose();
			backgroundTexture = new Texture("textures/cutscenes/" + bg);
			background.setDrawable(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
		}
		
		int x = section.getInt("x", 0);
		int y = section.getInt("y", 0);
		int width = section.getInt("width", 100);
		int height = section.getInt("height", 100);

		/*
		 * This will look like a crazy mess on the debug lines but it is the ONLY way I've got it working!
		 * Normally only the second line (dialogTable.setBounds()) should suffice, but whatever's
		 * the reason it will ALWAYS be ignored for the first section in a scene. Instead, I invoke
		 * the first line which sets the dialog table to the bottom left of its cell and
		 * pads it to its (x,y) position. By the way, again whatever's the reason, the second
		 * line is ignored for all subsequent scenes. Mysteries of Scene2d.
		 */
		dialogTableCell.bottom().left().padLeft(x).padBottom(y).size(width, height);
		dialogTable.setBounds(x, y, width, height);
		
		String dialogId = section.getString("dialog", "");
		
		if (!dialogId.equals("")) {
			Dialog d = Dialog.loadDialogById(dialogId);
			d.next();
			dialogTable.setDialog(d); 
		}
	}
	
	private void setNextScreen() {
		game.setScreen(nextScreen);
	}
	
	private void advanceDialog() {
		Dialog d = dialogTable.getDialog();
		
		//Advance the dialog proper if it continues
		if (d.hasNext()) d.next();
		
		//Go to the next section (if any) if the dialog's over
		else if (currentSection < sections.size)
			parseSection(sections.get(currentSection++));
		
		//If there are no more sections load the next screen
		else setNextScreen();
	}
}
