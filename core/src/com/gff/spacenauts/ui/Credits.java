package com.gff.spacenauts.ui;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.screens.InitialScreen;
import com.gff.spacenauts.ui.listeners.LinkListener;

/**
 * A scrollable credit list
 * 
 * @author Alessio Cali'
 *
 */
public class Credits extends ScrollPane implements UISet {

	private Table root;
	private Image logo;
	private ImageButton backButton;
	private AssetManager assets;
	private Label.LabelStyle styleSmall;
	private Label.LabelStyle styleBig;
	
	public Credits(AssetManager assets, final InitialScreen initial, final UISet from) {
		super(new Table());
		setOverscroll(false, false);
		root = (Table)this.getWidget();
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("credits_logo")));
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				initial.setUI(from);
			}
		});
		
		this.assets = assets;
		
		styleSmall = new Label.LabelStyle(assets.get(AssetsPaths.FONT_ATARI_28, BitmapFont.class), Color.WHITE);	
		styleBig = new Label.LabelStyle(assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class), Color.WHITE);
		
		try {
			Element creditFile = new XmlReader().parse(Gdx.files.internal("credits.xml"));
			
			for (Element group : creditFile.getChildrenByName("group")) {
				
				expandGroup(group);
				
				for (Element subgroup : group.getChildrenByName("subgroup")) {
					expandGroup(subgroup);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a new label, optionally with a link, based on the name attribute of the given xml Element
	 * 
	 * @param line The xml element
	 * @param big If true, 24pt fixedsys will be used. 20pt otherwise
	 */
	private void addLine (Element line, boolean big) {
		final String url = line.getAttribute("url", null);
		String text = line.getAttribute("name", "");
		
		Label label = big ? new Label(text, styleBig) : new Label(text, styleSmall);
		if (url != null) {
			label.addListener(new LinkListener(url, InitialScreen.getHandCursor()));
		}
		
		root.add(label).expandX().row();	
	}
	
	private void addImage(Element image) {
		Texture imageTexture = assets.get(image.getAttribute("val"), Texture.class);
		final String url = image.getAttribute("url", null);
		Image uiImage = new Image(imageTexture);
		root.add(uiImage).expandX().fillY().pad(5).row();
		
		if (url != null) {
			uiImage.addListener(new LinkListener(url, InitialScreen.getHandCursor()));
		}
	}
	
	private void expandGroup (Element group) {
		addLine(group, true);
		
		for (Element line : group.getChildrenByName("line")) {
			addLine(line, false);
		}
		
		for (Element image : group.getChildrenByName("image")) {
			addImage(image);
		}
		
		root.add().expandX().height(30).fill().row();
	}
	
	@Override
	public Image logo() {
		return logo;
	}
	
	@Override
	public ImageButton lowerLeft() {
		return backButton;
	}
	
	@Override
	public ImageButton lowerRight () {
		return null;
	}
	
	@Override
	public Table main() {
		return root;
	}
}
