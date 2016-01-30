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
		assets = new AssetManager();
		assets.load(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		assets.load(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		assets.load(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		assets.finishLoading();
		k40 = assets.get(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		
		initUi();
		Gdx.input.setInputProcessor(ui);
		
		cutscenes  = new JsonReader().parse(Gdx.files.internal(AssetsPaths.DATA_CUTSCENES)).get("cutscenes");
		
		for (JsonValue scene : cutscenes) {
			if (scene.getString("id", "").equals(sceneId)){
				this.scene = scene;
				break;
			}
		}
		
		if (scene == null) throw new GdxRuntimeException("No valid cutscene with id: " + sceneId);
		
		String bgmPath = scene.getString("bgm", "");
		
		if (!bgmPath.equals("")) {
			bgm = Gdx.audio.newMusic(Gdx.files.internal(bgmPath));
			bgm.setLooping(true);
			bgm.play();
		}
		
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
	
	private void initUi() {
		ui = new Stage(new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT));
		root = new Stack();
		root.setFillParent(true);
		ui.addActor(root);
		
		background = new Image();
		background.setFillParent(true);
		background.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				advanceDialog();
			}
		});
		root.add(background);
		
		uiTable = new Table();
		uiTable.setFillParent(true);
		root.add(uiTable);

		dialogTable = new DialogTable(assets);
		dialogTableCell = uiTable.add(dialogTable).colspan(3).expand();
		uiTable.row();
		
		TextButton.TextButtonStyle tbStyle = new TextButton.TextButtonStyle(null, null, null, k40);
		tbStyle.fontColor = Color.WHITE;
		skip = new TextButton("SKIP", tbStyle);
		skip.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				setNextScreen();
			}
		});
		uiTable.add(skip).pad(10);
		
		uiTable.add().expandX();
		
		Label.LabelStyle lStyle = new Label.LabelStyle(k40, Color.WHITE);
		tapToContinue = new Label("TAP TO CONTINUE", lStyle);
		uiTable.add(tapToContinue).pad(10);
	}
	
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

		//This will look like a crazy mess on the debug lines but it is the ONLY way I got it working!
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
		if (d.hasNext()) {
			d.next();
		} else if (currentSection < sections.size) {
			parseSection(sections.get(currentSection++));
		} else {
			setNextScreen();
		}
	}
}
