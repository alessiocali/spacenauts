package com.gff.spacenauts.ui;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.screens.InitialScreen;
import com.gff.spacenauts.ui.listeners.HandCursorListener;
import com.gff.spacenauts.ui.listeners.LinkListener;

/**
 * A scrollable credit list
 * 
 * @author Alessio Cali'
 *
 */
public class Credits implements UISet {

	private Table creditsContainer;
	private ScrollPane creditsPane;
	private Table creditsTable;
	private Image logo;
	private ImageButton backButton;
	private AssetManager assets;
	private Label.LabelStyle styleSmall;
	private Label.LabelStyle styleBig;
	private Dialog urlDialog;
	private Label openUrlLabel;
	private LinkListener linkListener;
	private HandCursorListener handCursorListener;
	
	public Credits(AssetManager assets, final InitialScreen initial, final UISet from) {
		creditsContainer = new Table();
		creditsTable = new Table();
		creditsPane = new ScrollPane(creditsTable);
		creditsPane.setFillParent(true);
		creditsContainer.add(creditsPane);
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		BitmapFont a32 = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		BitmapFont a40 = assets.get(AssetsPaths.FONT_ATARI_40, BitmapFont.class);
		NinePatchDrawable pane = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("credits_logo")));
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				initial.setUI(from);
			}
		});
		
		this.assets = assets;

		linkListener = new LinkListener("");
		handCursorListener = new HandCursorListener(InitialScreen.getHandCursor());
		openUrlLabel = new Label("", new Label.LabelStyle(a32, Color.WHITE));
		openUrlLabel.setWrap(true);
		
		TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(null, null, null, a32);
		Button closeDialog = new TextButton("No", buttonStyle);
		closeDialog.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				urlDialog.hide();
			}
		});
		Button openLink = new TextButton("Yes", buttonStyle);
		openLink.addListener(linkListener);
		
		Window.WindowStyle urlDialogStyle = new Window.WindowStyle(a32, Color.WHITE, pane);
		urlDialog = new Dialog("", urlDialogStyle);
		urlDialog.getContentTable().add(openUrlLabel).width(1000);
		urlDialog.button(openLink);
		urlDialog.getButtonTable().add().padLeft(50);
		urlDialog.button(closeDialog);
		
		styleSmall = new Label.LabelStyle(a32, Color.WHITE);	
		styleBig = new Label.LabelStyle(a40, Color.WHITE);
		
		Label clickLinks = new Label("Tap to open the author's website", styleSmall);
		creditsTable.add(clickLinks).padBottom(50).row();
		
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
			label.addListener(handCursorListener);
			label.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					linkListener.setUrl(url);
					openUrlLabel.setText("Open URL: \n" + url + " ?");
					urlDialog.show(creditsTable.getStage());
				}
			});
		}
		
		creditsTable.add(label).pad(5).expandX().row();	
	}
	
	private void addImage(Element image) {
		Texture imageTexture = assets.get(image.getAttribute("val"), Texture.class);
		final String url = image.getAttribute("url", null);
		Image uiImage = new Image(imageTexture);
		creditsTable.add(uiImage).expandX().fillY().pad(5).row();
		
		if (url != null) {
			uiImage.addListener(handCursorListener);
			uiImage.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					linkListener.setUrl(url);
					openUrlLabel.setText("Open URL: \n" + url + " ?");
					urlDialog.show(creditsTable.getStage());
				}
			});
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
		
		creditsTable.add().expandX().height(30).fill().row();
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
		return creditsContainer;
	}
}
