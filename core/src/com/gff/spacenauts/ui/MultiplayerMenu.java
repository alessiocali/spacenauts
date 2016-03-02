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
import com.gff.spacenauts.screens.LoadingScreen;
import com.gff.spacenauts.ui.LevelSelecter.LevelSelectSet;

/**
 * This menu is used to start a Multiplayer game. It works by invoking methods from the given
 * {@link NetworkAdapter}. Clicking the Register button in the lower left corner opens
 * a LevelSelecter with multiplayer mode set to true, which after selection returns back
 * to this UI. Clicking the refresh button in the upper right corner calls the 
 * {@link NetworkAdapter#updateHosts()} and prints a list of all hosts found. By selecting
 * a given host from the list that one is shown in the text field near the lower end of
 * the screen, while pressing the Connect button invokes {@link NetworkAdapter#connect(Host)}
 * based on the selected host. 
 * 
 * @author Alessio
 *
 */
public class MultiplayerMenu implements UISet {

	private final Game gameRef;

	private Table mainTable;
	private Image logo;
	private ImageButton backButton;
	private AssetManager assets;
	private NetworkAdapter na;
	private Table updateTable;
	private ScrollPane hostsScrollPane;
	private Table hostListTable;
	private AnimatedButton updateButton;
	private ConnectionField connectToField;
	private ArrayList<Host> hosts;
	private TextButton registerButton;
	private TextButton connectButton;
	private LevelSelecter multiplayerLevelSelecter;
	private Dialog networkStatusDialog;

	public MultiplayerMenu (AssetManager assets, Game gameRef, final InitialScreen initial, final UISet from) {
		na = Spacenauts.getNetworkAdapter();
		if (na == null) throw new GdxRuntimeException("Illegal State: network adapter can't be null");
		
		this.assets = assets;
		this.gameRef = gameRef;
		
		multiplayerLevelSelecter = new LevelSelecter(gameRef, assets, initial, this, true);
		hosts = new ArrayList<Host>();
				
		initUI(initial, from); 
	}

	private void initUI(final InitialScreen initial, final UISet from) {
		TextureAtlas uiAtlas = assets.get(AssetsPaths.ATLAS_UI, TextureAtlas.class);
		
		//Styles
		final Label.LabelStyle hostStyle = new Label.LabelStyle();
		hostStyle.font = assets.get(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		hostStyle.fontColor = Color.WHITE;
		
		TextFieldStyle connectStyle = new TextFieldStyle();
		connectStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		connectStyle.font = assets.get(AssetsPaths.FONT_KARMATIC_40, BitmapFont.class);
		connectStyle.fontColor = Color.WHITE;
		
		Window.WindowStyle dialogStyle = new Window.WindowStyle();
		dialogStyle.background = new NinePatchDrawable(uiAtlas.createPatch("default_pane"));
		dialogStyle.titleFont = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		
		final TextField.TextFieldStyle plainMessageStyle = new TextField.TextFieldStyle(assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class), Color.WHITE, null, null, null);
		
		final TextButton.TextButtonStyle plainButtonStyle = new TextButton.TextButtonStyle();
		plainButtonStyle.font = assets.get(AssetsPaths.FONT_ATARI_32, BitmapFont.class);
		plainButtonStyle.fontColor = Color.WHITE;
		
		TextButton.TextButtonStyle tbStyle = new TextButton.TextButtonStyle();
		tbStyle.font = assets.get(AssetsPaths.FONT_KARMATIC_64, BitmapFont.class);
		tbStyle.fontColor = Color.WHITE;

		//UISet values
		mainTable = new Table();
		mainTable.addAction(new Action() {
			private AdapterState oldStatus;

			@Override
			public boolean act(float delta) {
				na.updateState(delta);
				AdapterState newStatus = na.getState();

				if (newStatus != oldStatus) {
					oldStatus = newStatus;

					//Unless it's IDLE or UPDATING, show the status dialog
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

		//The table containing the update button and the host list pane
		updateTable = new Table();
		updateTable.setBackground(new NinePatchDrawable(uiAtlas.createPatch("default_pane")));
		mainTable.add(updateTable).expand().size(900,900).colspan(2).row();
		
		//The table containing all hosts, wrapped inside a scroll pane
		hostListTable = new Table();
		
		//The update button
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
				if (na.getState() == AdapterState.IDLE) 
					na.updateHosts();
				
				else if (na.getState() == AdapterState.UPDATING) 
					na.stopHostUpdate();
			}
		});
		updateTable.add(updateButton).expandX().right().row();

		//The scroll pane containing the host list
		hostsScrollPane = new ScrollPane(hostListTable, new ScrollPane.ScrollPaneStyle());
		hostsScrollPane.addAction(new Action() {

			private static final float INTERVAL = 1;
			private static final int NICKNAME_LIMIT = 30;
			private static final int DATA_LIMIT = 100;
			
			private Array<Label> hostLabels = new Array<Label>(100);
			private float timer = 0;
			
			/**
			 * <p>
			 * The host list synchronizer. It keeps a pool of labels to reuse.
			 * When the host list is updated this Action ensures there are
			 * enough Labels instantiated, clears the hostListTable and then
			 * adds a label from the pool and sets its value. The labels
			 * always point to the correct host since it is retrieved by 
			 * index rather then value.
			 * </p>
			 * 
			 * <p>Data should be given as LEVEL_NAME LEVEL_FILE</p>
			 */
			@Override
			public boolean act(float delta) {			
				timer += delta;

				if (timer > INTERVAL) {
					timer = 0;
					hosts.clear();
					hostListTable.clear();
					na.getHosts(hosts);
					
					//There are more hosts then labels instantiated, create new ones.
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
							Logger.log(LogLevel.WARNING, "MultiplayerMenu", host.nickname + " " +  host.connectionCookie + " " + host.data);
							continue;
						}
						
						String[] data = host.data.split("\\s");
						
						//If the given level has not been unlocked, skip
						if (!LevelSelectSet.forMap(data[1]).isUnlocked()) continue;
						
						Label label = hostLabels.get(i);
						label.setText(host.nickname + " - " + data[0]);
						hostListTable.add(label).expandX().top().left().row();
					}

				}
				
				return false;
			}

			/**
			 * Determines whether the host is valid, in which case
			 * it is returned whether the given map has been unlocked or not.
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
		updateTable.add(hostsScrollPane).expand().size(800,800);
		
		//The connecting field, showing the selected host.
		connectToField = new ConnectionField("", connectStyle);
		connectToField.setTouchable(Touchable.disabled);
		mainTable.add(connectToField).expandX().width(900).colspan(2).row();

		//The CANCEL button on the network status dialog
		final TextButton resetNetworkButton = new TextButton("CANCEL", plainButtonStyle);
		resetNetworkButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				na.reset();
				networkStatusDialog.hide();
			}
		});
		
		//This text area shows the current network status. It also shows failure reasons.
		final TextArea networkMessage = new TextArea("Connecting...", plainMessageStyle);
		networkMessage.setTouchable(Touchable.disabled);
		networkMessage.addAction(new Action() {
			private AdapterState oldStatus;

			@Override
			public boolean act(float delta) {					
				AdapterState newStatus = na.getState();

				//The status changed, update text
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
						GameScreen gameScreen = new GameScreen(na.getData().split("\\s")[1], gameRef, true); 
						gameRef.setScreen(new LoadingScreen(gameScreen, gameRef, gameScreen));
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

		//This dialog shows the network adapter status and allows to abort operations.
		networkStatusDialog = new Dialog("", dialogStyle);
		networkStatusDialog.getContentTable().add(networkMessage).center().size(800, 150);
		networkStatusDialog.button(resetNetworkButton);

		//This button switches to the LevelSelecter with multiplayer mode set.
		registerButton = new TextButton("REGISTER", tbStyle);
		registerButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				multiplayerLevelSelecter.refresh();
				initial.setUI(multiplayerLevelSelecter);
			}
		});
		mainTable.add(registerButton).expandX();
		
		//This button invokes the NetworkAdapter.connect(String) method using data from the connectToField
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
		hostListTable.clear();
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
