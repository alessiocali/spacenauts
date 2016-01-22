package com.gff.spacenauts.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.net.NetworkAdapter;
import com.gff.spacenauts.net.NetworkAdapter.AdapterState;
import com.gff.spacenauts.net.NetworkAdapter.Host;
import com.gff.spacenauts.screens.GameScreen;
import com.gff.spacenauts.screens.InitialScreen;
import com.gff.spacenauts.ui.LevelSelecter.LevelSelectSet;

public class MultiplayerMenu implements UISet {

	private final Game gameRef;

	private Table mainTable;
	private Image logo;
	private ImageButton backButton;
	private AssetManager assets;
	private NetworkAdapter na;
	private Table hostList;
	private ScrollPane scrollPane;
	private Table internalTable;
	private AnimatedButton updateButton;
	private ConnectionField connectToField;
	private ArrayList<Host> hosts;
	private TextButton registerButton;
	private TextButton connectButton;
	private LevelSelecter multiplayerLevelSelecter;
	private Dialog networkStatusDialog;

	public MultiplayerMenu (AssetManager assets, Game gameRef, final InitialScreen initial, final UISet from) {
		this.na = Spacenauts.getNetworkAdapter();
		if (na == null) throw new GdxRuntimeException("Illegal State: network adapter can't be null");
		multiplayerLevelSelecter = new LevelSelecter(gameRef, assets, LevelSelectSet.LEVEL_1, initial, this, true);
		hosts = new ArrayList<Host>();
		this.assets = assets;
		this.gameRef = gameRef;
		initUI(initial, from); 
	}

	private void initUI(final InitialScreen initial, final UISet from) {
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);

		mainTable = new Table();
		mainTable.addAction(new Action() {
			private AdapterState oldStatus;

			@Override
			public boolean act(float delta) {
				na.updateState(delta);
				AdapterState newStatus = na.getState();

				if (newStatus != oldStatus) {
					oldStatus = newStatus;

					if (newStatus != AdapterState.IDLE && newStatus != AdapterState.UPDATING)
						networkStatusDialog.show(mainTable.getStage());
				}

				return false;
			}

		});
		logo = new Image(new TextureRegionDrawable(uiAtlas.findRegion("newgame_logo")));
		backButton = new ImageButton(new TextureRegionDrawable(uiAtlas.findRegion("back_button")));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent e, float x, float y) {
				initial.setUI(from);
				na.reset();
			}
		});

		hostList = new Table();
		hostList.setBackground(new NinePatchDrawable(uiAtlas.createPatch("default_pane")));
		internalTable = new Table();
		Animation updateAnimation = new Animation(0.07f, uiAtlas.findRegion("update_button").split(82, 82)[0]);
		updateAnimation.setPlayMode(PlayMode.LOOP);
		updateButton = new AnimatedButton(updateAnimation);
		updateButton.addAction(new Action() {
			@Override
			public boolean act (float delta) {
				if (na.getState() == AdapterState.UPDATING) updateButton.start();
				else updateButton.stop();
				return false;
			}
		});

		updateButton.addListener(new ClickListener() {			
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if (na.getState() == AdapterState.IDLE) na.updateHosts();
				else if (na.getState() == AdapterState.UPDATING) na.stopHostUpdate();
			}
		});

		final Label.LabelStyle hostStyle = new Label.LabelStyle();
		hostStyle.font = assets.get(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		hostStyle.fontColor = Color.WHITE;

		scrollPane = new ScrollPane(internalTable, new ScrollPane.ScrollPaneStyle());
		scrollPane.addAction(new Action() {
			private float timer = 0;
			private static final float INTERVAL = 1;
			private static final int NICKNAME_LIMIT = 30;
			private static final int DATA_LIMIT = 100;
			
			private Array<Label> hostLabels = new Array<Label>(100);
			//Data should be in format LEVEL_NAME LEVEL_FILE

			@Override
			public boolean act(float delta) {			
				timer += delta;
				if (timer > INTERVAL) {
					timer = 0;
					hosts.clear();
					internalTable.clear();
					na.getHosts(hosts);
					
					if (hosts.size() > hostLabels.size) {						
						for (int j = hostLabels.size ; j < hosts.size() ; j++) {
							final Label label = new Label("", hostStyle);
							final int index = j;
							label.addListener(new ClickListener () {
								@Override
								public void clicked (InputEvent e, float x, float y) {
									connectToField.setText(label.getText().toString());
									connectToField.setHost(hosts.get(index));
								}
							});
							hostLabels.add(label);
						}
					}

					for (int i = 0; i < hosts.size() ; i++) {
						final Host host = hosts.get(i);
						if (!hostIntegrity(host)) {
							Logger.log(LogLevel.WARNING, "MultiplayerMenu", "Received host was not valid");
							Logger.log(LogLevel.WARNING, "MultiplayerMenu", String.format("%s %s %s", host.nickname, host.connectionCookie, host.data));
							continue;
						}
						Label label = hostLabels.get(i);
						label.setText(String.format("%s - %s", host.nickname, host.data.split("\\s")[0]));
						internalTable.add(label).expandX().top().left().row();
					}

				}
				return false;
			}

			/**
			 * Determines whether the host is valid
			 * 
			 * @param host
			 * @return
			 */
			public boolean hostIntegrity(Host host) {
				if (host.nickname.length() > NICKNAME_LIMIT) return false;
				if (host.data.length() > DATA_LIMIT) return false;

				String[] data = host.data.split("\\s");

				if (data.length < 2) return false;
				if (!LevelSelectSet.containsMap(data[1])) return false;

				return true;
			}
		});
		hostList.add(updateButton).expandX().right().row();
		hostList.add(scrollPane).expand().size(800,800);
		mainTable.add(hostList).expand().size(900,900).colspan(2).row();

		TextFieldStyle connectStyle = new TextFieldStyle();
		connectStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		connectStyle.font = assets.get(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		connectStyle.fontColor = Color.WHITE;
		connectToField = new ConnectionField("", connectStyle);
		connectToField.setTouchable(Touchable.disabled);
		mainTable.add(connectToField).expandX().width(900).colspan(2).row();

		Window.WindowStyle dialogStyle = new Window.WindowStyle();
		dialogStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		dialogStyle.titleFont = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		final TextField.TextFieldStyle plainMessageStyle = new TextField.TextFieldStyle(assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class), Color.WHITE, null, null, null);
		final TextButton.TextButtonStyle plainButtonStyle = new TextButton.TextButtonStyle();
		plainButtonStyle.font = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		plainButtonStyle.fontColor = Color.WHITE;

		final TextButton resetNetworkButton = new TextButton("CANCEL", plainButtonStyle);
		resetNetworkButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				na.reset();
				networkStatusDialog.hide();
			}
		});
		final TextArea networkMessage = new TextArea("Connecting...", plainMessageStyle);
		networkMessage.setTouchable(Touchable.disabled);
		networkMessage.addAction(new Action() {
			private AdapterState oldStatus;

			@Override
			public boolean act(float delta) {					
				AdapterState newStatus = na.getState();

				if (oldStatus != newStatus) {
					oldStatus = newStatus;

					switch (newStatus) {
					case CONNECTING :
						networkMessage.setText("Connecting...");
						resetNetworkButton.setVisible(true);
						break;
					case WAITING:
						networkMessage.setText("Waiting for players...");
						resetNetworkButton.setVisible(true);
						break;
					case FINALIZING:
						networkMessage.setText("Starting soon...");
						resetNetworkButton.setVisible(false);
						break;
					case GAME:
						gameRef.setScreen(new GameScreen(na.getData().split("\\s")[1], gameRef, true));
						break;
					case FAILURE:
						String error = na.getFailureReason();
						networkMessage.setText("An error occured. Reson was: " + error);
						resetNetworkButton.setVisible(true);
						break;
					default:
						networkStatusDialog.hide();
						break;
					}						
				} 
				return false;
			} 
		});

		networkStatusDialog = new Dialog("", dialogStyle);
		networkStatusDialog.getContentTable().add(networkMessage).center().size(800, 150);
		networkStatusDialog.button(resetNetworkButton);

		TextButton.TextButtonStyle tbStyle = new TextButton.TextButtonStyle();
		tbStyle.font = assets.get(AssetsPaths.FONT_KARMATIC_64, BitmapFont.class);
		tbStyle.fontColor = Color.WHITE;
		registerButton = new TextButton("REGISTER", tbStyle);
		registerButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				initial.setUI(multiplayerLevelSelecter);
			}
		});
		mainTable.add(registerButton).expandX();
		connectButton = new TextButton("CONNECT", tbStyle);
		connectButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if (!connectToField.getText().isEmpty() && connectToField.getHost() != null) {
					na.connect(connectToField.getHost());
					connectToField.setText(null);
					connectToField.setHost(null);
				}
			}
		});
		mainTable.add(connectButton).expandX();
	}

	public void reset () {
		if (na != null) na.reset();
		internalTable.clear();
		connectToField.setText(null);
		connectToField.setHost(null);
	}

	@Override
	public Image logo() {
		return logo;
	}

	@Override
	public ImageButton lowerLeft () {
		return backButton;
	}

	@Override
	public ImageButton lowerRight () {
		return null;
	}

	@Override
	public Table main() {
		return mainTable;
	}

}
