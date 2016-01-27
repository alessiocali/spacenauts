package com.gff.spacenauts.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.ui.InitialUI;
import com.gff.spacenauts.ui.UISet;

/**
 * The initial screen which hosts the menu for options, level selection and others. Since it extends Stage it is host to its own UI.
 * 
 * @author Alessio Cali'
 *
 */
public class InitialScreen extends Stage implements Screen {

	private AssetManager assets;

	private TextureAtlas textures;
	private TextureRegion nebula;
	private Music bgm;
	
	private UISet currentSet;

	//Cells
	private Cell<? extends Actor> logoCell;
	private Cell<? extends Actor> centralCell;
	private Cell<? extends Actor> lowerLeftCell;
	private Cell<? extends Actor> lowerRightCell;

	//Main Menu elements
	private Stack root;
	private Table uiSpace;
	private Image spaceshipImage;

	private final Game gameRef;
	private static Cursor handCursor;

	public InitialScreen(Game game) {
		super(new FitViewport(Globals.TARGET_SCREEN_WIDTH, Globals.TARGET_SCREEN_HEIGHT));
		gameRef = game;
	}

	private void loadAssets() {
		assets = new AssetManager();
		assets.load(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		assets.load(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		assets.load(AssetsPaths.ATLAS_PREVIEWS, TextureAtlas.class);
		assets.load(AssetsPaths.TEXTURE_ASHLEY, Texture.class);
		assets.load(AssetsPaths.TEXTURE_GDXAI, Texture.class);
		assets.load(AssetsPaths.TEXTURE_LIBGDX, Texture.class);
		assets.load(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		assets.load(AssetsPaths.FONT_KARMATIC_64, BitmapFont.class);
		assets.load(AssetsPaths.FONT_ATARI_40, BitmapFont.class);
		assets.load(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		assets.load(AssetsPaths.FONT_ATARI_28, BitmapFont.class);
		assets.load(AssetsPaths.TEXTURE_NEBULA, Texture.class);
		assets.load(AssetsPaths.BGM_DIGITAL_FALLOUT, Music.class);
		assets.load(AssetsPaths.SFX_LASER_4, Sound.class);
		assets.load(AssetsPaths.CURSOR_HAND, Pixmap.class);
		assets.finishLoading();

		textures = assets.get(AssetsPaths.ATLAS_TEXTURES, TextureAtlas.class);
		nebula = new TextureRegion(assets.get(AssetsPaths.TEXTURE_NEBULA, Texture.class));
		bgm = assets.get("bgm/Digital-Fallout_v001.mp3", Music.class);
	}

	private void loadUI() {		
		root = new Stack();
		root.setFillParent(true);
		addActor(root);

		root.add(generateBackground());

		uiSpace = new Table();
		uiSpace.setFillParent(true);
		root.add(uiSpace);

		logoCell = uiSpace.add(new Image()).center().height(310).colspan(3).fill();
		uiSpace.row();

		centralCell = uiSpace.add(new Table()).height(1300).colspan(3);
		uiSpace.row();

		lowerLeftCell = uiSpace.add(new ImageButton(new TextureRegionDrawable())).center().pad(5).size(128, 128);

		spaceshipImage = new Image(new TextureRegionDrawable(textures.findRegion("spaceship_sprite")));
		spaceshipImage.setOrigin(spaceshipImage.getImageX() + spaceshipImage.getWidth() / 2, spaceshipImage.getImageY() + spaceshipImage.getHeight() / 2);
		spaceshipImage.setRotation(90);
		uiSpace.add(spaceshipImage).center().expandX();

		lowerRightCell = uiSpace.add(new ImageButton(new TextureRegionDrawable())).center().pad(5).size(128, 128);
		
		currentSet = new InitialUI(assets, gameRef, this);
		setUI(currentSet);
		
		bgm.setLooping(true);
		bgm.play();
	}

	private Table generateBackground() {
		Table bgTable = new Table();
		int hor = MathUtils.ceil((float)Globals.TARGET_SCREEN_WIDTH / nebula.getRegionWidth());
		int ver = MathUtils.ceil((float)Globals.TARGET_SCREEN_HEIGHT / nebula.getRegionHeight());

		for (int i = 0 ; i < ver ; i++) {
			for (int j = 0 ; j < hor ; j++) {
				bgTable.add(new Image(nebula)).size(nebula.getRegionWidth(), nebula.getRegionHeight());
			}
			bgTable.row();
		}

		return bgTable;
	}

	@Override
	public void show() {
		loadAssets();
		Gdx.input.setInputProcessor(this);
		handCursor = Gdx.graphics.newCursor(assets.get("cursors/hand_cursor.png", Pixmap.class), 0, 0);
		loadUI();
	}

	@Override
	public void render(float delta) {
		setDebugAll(Globals.debug);

		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		act(delta);
		draw();
	}

	@Override
	public void resize(int width, int height) {
		getViewport().update(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		dispose();
	}

	@Override
	public void dispose() {
		if (assets != null)
			assets.dispose();
	}

	public static Cursor getHandCursor() {
		return handCursor;
	}
	
	public void setUI (UISet ui) {
		if (ui == null) return;
		currentSet = ui;
		logoCell.setActor(ui.logo());
		centralCell.setActor(ui.main());
		lowerLeftCell.setActor(ui.lowerLeft());
		lowerRightCell.setActor(ui.lowerRight());
	}
}
