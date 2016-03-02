package com.gff.spacenauts.ui;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
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
 * A UISet with a scrollable credit list. The credits list is stored
 * inside data/credits.xml. It is divided in groups, which in turn
 * can be divided in subgroups. Each group/subgroup is made of lines
 * containing some text (the credit itself) or images, and optionally
 * a URL pointing to the author's website. Clicking on the text/image
 * opens said URL.
 * 
 * @author Alessio Cali'
 *
 */
public class Credits implements UISet {

	private InitialScreen initial;
	private UISet from;
	
	private Container<ScrollPane> creditsContainer;
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
	
	public Credits(AssetManager assets, InitialScreen initial, UISet from) {
		this.assets = assets;
		this.initial = initial;
		this.from = from;
		
		initUI();
		parseCreditsXML();
	}
	
	private void initUI() {
		//Assets retrieval
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		BitmapFont a32 = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		BitmapFont a40 = assets.get(AssetsPaths.FONT_ATARI_40, BitmapFont.class);
		NinePatchDrawable pane = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		Cursor handCursor = Gdx.graphics.newCursor(assets.get("cursors/hand_cursor.png", Pixmap.class), 0, 0);
		
		//Styles
		TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(null, null, null, a32);
		Window.WindowStyle urlDialogStyle = new Window.WindowStyle(a32, Color.WHITE, pane);
		styleSmall = new Label.LabelStyle(a32, Color.WHITE);	
		styleBig = new Label.LabelStyle(a40, Color.WHITE);
		
		//The table containing all credits
		creditsTable = new Table();

		//The scroll pane that wraps the credits
		creditsPane = new ScrollPane(creditsTable);
		creditsPane.setFillParent(true);
		
		//The table holding the scroll pane
		creditsContainer = new Container<ScrollPane>(creditsPane);
		
		//The credits logo
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("credits_logo")));
		
		//The back button redirects to the previous menu
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				initial.setUI(from);
			}
		});
		
		//The listener used to open the URL when the user clicks "YES" in the URL dialog
		linkListener = new LinkListener("");
		
		//The listener used to show the hand cursor
		handCursorListener = new HandCursorListener(handCursor);
		
		//The label used in the URL dialog
		openUrlLabel = new Label("", new Label.LabelStyle(a32, Color.WHITE));
		openUrlLabel.setWrap(true);
		
		//The NO button in the URL dialog
		Button closeDialog = new TextButton("No", buttonStyle);
		closeDialog.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				urlDialog.hide();
			}
		});
		
		//The YES button in the open URL dialog.
		Button openLink = new TextButton("Yes", buttonStyle);
		openLink.addListener(linkListener);
		
		//The open URL dialog
		urlDialog = new Dialog("", urlDialogStyle);
		urlDialog.getContentTable().add(openUrlLabel).width(1000);
		urlDialog.button(openLink);
		urlDialog.getButtonTable().add().padLeft(50);
		urlDialog.button(closeDialog);
		
		//The label instructing the user to tap on lines to open websites
		Label clickLinks = new Label("Tap to open the author's website", styleSmall);
		creditsTable.add(clickLinks).padBottom(50).row();
	}
	
	private void parseCreditsXML() {
		try {
			Element creditFile = new XmlReader().parse(Gdx.files.internal(AssetsPaths.DATA_CREDITS));
			
			for (Element group : creditFile.getChildrenByName("group")) {
				expandGroup(group);
				
				for (Element subgroup : group.getChildrenByName("subgroup"))
					expandGroup(subgroup);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a new label, optionally with a link, based on the name attribute of the given xml Element
	 * 
	 * @param line The xml element
	 * @param big If true, 24pt Atari will be used. 20pt otherwise
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
					//Setup info for the URL dialog and show it.
					linkListener.setUrl(url);
					openUrlLabel.setText("Open URL: \n" + url + " ?");
					urlDialog.show(creditsTable.getStage());
				}
			});
		}
		
		creditsTable.add(label).pad(5).expandX().row();	
	}
	
	/**
	 * Adds a new image, optionally with a link, based on the val attribute of the given xml Element
	 * 
	 * @param line The xml element
	 * @param big If true, 24pt Atari will be used. 20pt otherwise
	 */
	private void addImage(Element image) {
		final String url = image.getAttribute("url", null);
		Texture imageTexture = assets.get(image.getAttribute("val"), Texture.class);

		Image uiImage = new Image(imageTexture);	
		
		if (url != null) {
			uiImage.addListener(handCursorListener);
			uiImage.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					//Setup info for the URL dialog and show it.
					linkListener.setUrl(url);
					openUrlLabel.setText("Open URL: \n" + url + " ?");
					urlDialog.show(creditsTable.getStage());
				}
			});
		}
		
		creditsTable.add(uiImage).expandX().fillY().pad(5).row();
	}
	
	/**
	 * Adds the group caption with a big font by calling addLine(group, true)
	 * and then proceeds adding all lines/images. 
	 * 
	 * @param group
	 */
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
	public Container<ScrollPane> main() {
		return creditsContainer;
	}
}
