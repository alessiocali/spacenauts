package com.gff.spacenauts.android.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.gff.spacenauts.Globals;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;
import com.gff.spacenauts.android.net.InThread.InThreadStatus;
import com.gff.spacenauts.android.net.OutThread.OutThreadStatus;
import com.gff.spacenauts.net.NetworkAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

/**
 * <p>
 * A network adapter that establishes connection using Android's WifiP2P APIs for service discovery.
 * This allows for fully P2P, offline local multiplayer. It is the approach used for the Android project since
 * in a mobile context there is usually no flat connection, and the online approach using client-server remote matchmaking
 * consumes data. WiFi direct has been preferred to Bluetooth due to reliability, performance gain and an overall broader compatibility,
 * at the price of requiring a minimum Android API level of 16 (Jellybean), which is pretty common anyway.
 * </p>
 * 
 * <h1>How this works:</h1>
 * 
 * <p>
 * The adapter is based off a FSM (Finite State Machine) not unlike that defined for SGMP. While no connection is
 * in act, the adapter is {@link AdapterState#IDLE IDLE}. When the user registers {@link #register(String, int, String)} is invoked
 * and the adapter switches to {@link AdapterState#CONNECTING CONNECTING}. The user will now be visible through Android's WiFi P2P API.
 * The other player shall scan for nearby hosts by clicking the update button, thus invoking {@link #updateHosts()} and switching to 
 * {@link AdapterState#CONNECTING CONNECTING}. Selecting a host and then clicking the connect button will invoke 
 * {@link #connect(com.gff.spacenauts.net.NetworkAdapter.Host) connect(Host)}. This will initiate the connection. Once the P2P connection is established
 * by the framework it will call back {@link WifiDirectBroadcastReceiver} methods to request connection info. Once these are available
 * the {@link #connectionListener} callbacks will be invoked (yes, WiFi Direct on Android is rather convoluted), the Adapter will
 * switch to {@link AdapterState#FINALIZING FINALIZING} and start a {@link ConnectionThread} to perform the handshake and instantiate the IO threads.
 * The {@link #updateState(float)} notices a successfully established connection and switch to {@link AdapterState#GAME GAME}. On a upper level
 * the UI should notice this and start the game properly.
 * </p>
 * 
 * <p>
 * Any error will return a {@link #failureReason} and switch the state to {@link AdapterState#FAILURE FAILURE}. The adapter state
 * can be reset by invoking {@link #reset()}. This will close any open connection and reset the state of the P2P framework
 * to start anew.
 * </p>
 * 
 * @author Alessio Cali'
 *
 */
public class WifiP2PNetworkAdapter implements NetworkAdapter {

	private static final String TAG = "WifiP2PNetworkAdapter";
	public static final int ENABLE_WIFI = 1234;

	//Network informations
	private static final UUID CONNECTION_COOKIE = UUID.randomUUID();
	private static final String SERVICE_NAME = "SPACENAUTS_SERVICE";
	private static final String PROTOCOL = "_sgmp._tcp";

	//Failure messages
	private static final String FAIL_WIFI_OFF ="WiFi must be turned on to play multiplayer mode";
	private static final String FAIL_CHANNEL_DISCONNECTED = "WiFi connection lost";
	private static final String FAIL_INCOHERENT_TRANSITION = "Internal error (INCOHERENT_TRANSITION)";
	private static final String FAIL_NO_HOST = "Host invalid or host timed out";
	private static final String FAIL_UNKNOWN = "Unknwon error";
	private static final String FAIL_FRIEND_CLOSED = "Closed connection with fellow player";
	private static final String FAIL_IO = "Network IO error";
	private static final String FAIL_IO_NOT_READY = "Network not ready";

	//Record keys
	private static final String KEY_SERVICE_UUID = "SERVICE_ID";
	private static final String KEY_NICKNAME = "NICKNAME";
	private static final String KEY_TIMEOUT = "TIMEOUT";
	private static final String KEY_DATA = "DATA";
	private static final String KEY_COOKIE = "COOKIE";

	//Record values
	private static final String VALUE_COOKIE = CONNECTION_COOKIE.toString();

	//Default values
	private static final String DEFAULT_NICKNAME = "UNKNWON";
	private static final Long DEFAULT_TIMEOUT = 1l;
	private static final String DEFAULT_DATA = "";
	private static final String DEFAULT_COOKIE = "";

	//Action listeners
	private final FailureActionListener addServiceRequestListener = new FailureActionListener(Request.ADD_SERVICE_REQUEST);
	private final FailureActionListener discoveryListener = new FailureActionListener(Request.DISCOVERY);
	private final FailureActionListener addLocalServiceListener = new FailureActionListener(Request.REGISTRATION);
	private final FailureActionListener connectionRequestListener = new FailureActionListener(Request.CONNECTION);

	//Android and WP2P fields
	private Activity activity;
	private Intent turnOnWifiIntent;
	private WifiManager wifiManager;
	private WifiP2pManager p2pManager;
	private WifiP2pConfig config;
	private Channel channel;
	private IntentFilter filter;
	private BroadcastReceiver broadcastReceiver;
	private ChannelListener channelListener;
	private ConnectionInfoListener connectionListener;
	private HashMap<String, String> serviceRecord;
	private WifiP2pDnsSdServiceRequest serviceRequest;
	private WifiP2pDnsSdServiceInfo serviceInfo;
	private DnsSdServiceResponseListener serviceListener;
	private DnsSdTxtRecordListener recordListener;

	//Interface related fields
	private AdapterState state = AdapterState.IDLE;
	private String failureReason = FAIL_UNKNOWN;
	private ArrayList<Host> hostList;
	private HashMap<String, DeviceInfo> deviceMap;	//cookie -> DeviceInfo map
	private HashMap<String, Map<String, String>> recordMap;	//MAC -> Service record map
	private ConnectionThread connectionThread;
	private String localData;
	private InThread inThread;
	private OutThread outThread;

	/**
	 * This broadcast receiver handles any relevant change in WiFi state. If WiFi has been turned off, it will reset the interface.
	 * If another user is trying to connect, it will request the connection info.
	 * 
	 * @author Alessio Cali'
	 *
	 */
	private class WifiDirectBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context c, Intent intent) {

			String action = intent.getAction();

			if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

				if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) 
					reset();

			} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

				if (networkInfo.isConnected()) 
					p2pManager.requestConnectionInfo(channel, connectionListener);
			}
		}
	}

	/**
	 * A data structure holding data for device management.
	 * 
	 * @author Alessio Cali'
	 *
	 */
	private class DeviceInfo {
		public final WifiP2pDevice device;
		public final long start;	//When the device has first been discovered
		public final long timeout;	//The device's publicized timeout.

		public DeviceInfo(WifiP2pDevice device, long start, long end) {
			this.device = device;
			this.start = start;
			this.timeout = end;
		}
	}

	private enum Request {
		ADD_SERVICE_REQUEST, DISCOVERY, REGISTRATION, CONNECTION
	}

	/**
	 * This listener is used to inform the interface of any system related failure.
	 * 
	 * @author Alessio Cali'
	 *
	 */
	private class FailureActionListener implements ActionListener {		

		private static final String FAIL_BUSY = "Android P2P: BUSY";
		private static final String FAIL_ERROR = "Android P2P: ERROR";
		private static final String FAIL_NO_SERVICE_REQUEST = "Android P2P: NO_SERVICE_REQUEST";
		private static final String FAIL_P2P_UNSUPPORTED = "Android P2P: P2P_UNSUPPORTED";

		private Request request;	//The type of request this listener is used for.

		private FailureActionListener (Request request) {
			this.request = request;
		}

		@Override
		public void onFailure(int reason) {
			String reasonString;

			switch (reason) {
			case WifiP2pManager.BUSY:
				reasonString = FAIL_BUSY;
				break;
			case WifiP2pManager.ERROR:
				reasonString = FAIL_ERROR;
				break;
			case WifiP2pManager.NO_SERVICE_REQUESTS:
				reasonString = FAIL_NO_SERVICE_REQUEST;
				break;
			case WifiP2pManager.P2P_UNSUPPORTED:
				reasonString = FAIL_P2P_UNSUPPORTED;
				break;
			default:
				reasonString = FAIL_UNKNOWN;
				break;
			}

			Logger.log(LogLevel.WARNING, TAG, request + ": FAIL. Reson was: " + reasonString);
			fail(reasonString);
		}

		@Override
		public void onSuccess() {
			Logger.log(LogLevel.UPDATE, TAG, request + ": SUCCESS");
		}

	}

	public WifiP2PNetworkAdapter (Activity activity) {
		//General initialization
		this.activity = activity;
		turnOnWifiIntent = new Intent(activity, WifiDialog.class);
		wifiManager = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
		p2pManager = (WifiP2pManager) activity.getSystemService(Activity.WIFI_P2P_SERVICE);
		broadcastReceiver = new WifiDirectBroadcastReceiver();
		initChannel();

		//Broadcast receiver filter
		filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		//Service record init
		serviceRecord = new HashMap<String, String>();
		serviceRecord.put(KEY_SERVICE_UUID, Globals.SERVICE_UUID);
		serviceRecord.put(KEY_COOKIE, VALUE_COOKIE);

		serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, PROTOCOL, serviceRecord);

		config = new WifiP2pConfig();
		config.wps.setup = WpsInfo.PBC;

		channelListener = new ChannelListener () {
			@Override
			public void onChannelDisconnected() {
				fail(FAIL_CHANNEL_DISCONNECTED);
				initChannel();
			}
		};

		/*
		 * This listener is informed whenever a DNS SD Record is found. It stores its data in the recordMap.
		 * Android always calls this before the service listener.
		 */
		recordListener = new DnsSdTxtRecordListener () {

			@Override
			public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice device) {

				String recordUuid = record.get(KEY_SERVICE_UUID);

				if (recordUuid != null) {
					if (recordUuid.equals(Globals.SERVICE_UUID.toString())) {
						recordMap.put(device.deviceAddress, record);
						Logger.log(LogLevel.UPDATE, TAG, "A service record was found on: " + device.deviceName);
					} else Logger.log(LogLevel.UPDATE, TAG, "A DNS record was found, but the service UUID did not match");
				} else Logger.log(LogLevel.UPDATE, TAG, "A DNS record was found, but the service UUID did not match");

			}

		};

		/*
		 * This listener is called whenever a DNS SD service is available. It looks for an entry in the record map,
		 * and if it finds one it adds a new host with the corresponding info. This is always called after the record
		 * listener so there is no risk of a false miss.
		 */
		serviceListener = new DnsSdServiceResponseListener() {

			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

				String deviceAddress = srcDevice.deviceAddress;

				if (recordMap.containsKey(deviceAddress)) {
					Map<String, String> record = recordMap.get(deviceAddress);
					addHost(record, srcDevice);
					recordMap.remove(deviceAddress);
					Logger.log(LogLevel.UPDATE, TAG, "A service was found");
				} else {
					Logger.log(LogLevel.UPDATE, TAG, "A service was found, but it was not registered");
				}
			}

		};

		p2pManager.setDnsSdResponseListeners(channel, serviceListener, recordListener);

		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

		/*
		 * This listener reacts to the BroadcastListener connectionInfo request. It's called once
		 * the hosts are connected and it starts a connection thread (server-side or client-side depending on the group owner).
		 */
		connectionListener = new ConnectionInfoListener() {

			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				if (info.groupFormed) {

					if (state == AdapterState.CONNECTING) {
						state = AdapterState.FINALIZING;

						if (info.isGroupOwner) {
							startServerThread();
						} else {
							startClientThread(info.groupOwnerAddress);
						}
					} else {
						//Connection must only happen from CONNECTING 
						fail(FAIL_INCOHERENT_TRANSITION);
					}
				}
			}
		};


		hostList = new ArrayList<Host>();
		deviceMap = new HashMap<String, DeviceInfo>();
		recordMap = new HashMap<String, Map<String,String>>();
	}

	/**
	 * Called from the current Activity when the user refuses to turn on WiFi.
	 */
	public void wifiRefused () {
		fail(FAIL_WIFI_OFF);
	}

	/**
	 * Invokes a {@link WifiDialog} to turn WiFi on.
	 */
	private void initWifi () {
		activity.startActivityForResult(turnOnWifiIntent, ENABLE_WIFI);
	}

	/**
	 * Initializes the P2P manager to get a new channel.
	 */
	private void initChannel () {
		channel = p2pManager.initialize(activity, activity.getMainLooper(), channelListener);
	}

	/**
	 * Ensures that WiFi is ON and that the channel is valid. It is called before most network related operations.
	 * Since WiFi activation is not guaranteed one must check WiFi status after the call. 
	 */
	private void connectionCheck() {
		if (!wifiManager.isWifiEnabled()) initWifi();
		if (channel == null) initChannel();
	}

	/**
	 * Asks the OS to scan for services matching the given request. Used to scan for hosting players.
	 * The given request is blank. Any check that the received service is a Spacenauts multiplayer
	 * service is done within the recordListener, by looking for the game's UUID: {@link Globals#SERVICE_UUID}.
	 * See {@link WifiP2PNetworkAdapter#WifiP2PNetworkAdapter(Activity) WifiP2PNetworkAdapter(Activity)}.
	 */
	private void addServiceRequest() {
		p2pManager.discoverPeers(channel, null);
		p2pManager.addServiceRequest(channel, serviceRequest, addServiceRequestListener);
		p2pManager.discoverServices(channel, discoveryListener);
	}

	/**
	 * Notifies the OS to stop looking for game services nearby.
	 */
	private void removeServiceRequest() {
		p2pManager.removeServiceRequest(channel, serviceRequest, null);
	}

	/**
	 * Adds and publicizes a new game service. Any nearby player will be able to discover the user
	 * and accept to play with him / her.
	 */
	private void addLocalService() {
		serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, PROTOCOL, serviceRecord);
		p2pManager.addLocalService(channel, serviceInfo, addLocalServiceListener);
	}

	/**
	 * Stops publicizing the game service.
	 */
	private void removeLocalService() {
		p2pManager.removeLocalService(channel, serviceInfo, null);
	}

	/**
	 * Adds a new host to the list of players willing to play. If any datum is missing,
	 * a default value is put instead. 
	 *  
	 * @param record the host's DNS record.
	 * @param wifiDevice the host's WifiP2pDevice.
	 */
	private void addHost (Map<String, String> record, WifiP2pDevice wifiDevice) {
		String cookie = record.get(KEY_COOKIE) != null ? record.get(KEY_COOKIE) : DEFAULT_COOKIE;
		String nickname = record.get(KEY_NICKNAME) != null ? record.get(KEY_NICKNAME) : DEFAULT_NICKNAME;
		String data = record.get(KEY_DATA) != null ? record.get(KEY_DATA) : DEFAULT_DATA;
		Long timeout = record.get(KEY_TIMEOUT) != null ? Long.valueOf(record.get(KEY_TIMEOUT)) : DEFAULT_TIMEOUT;

		Host host = new Host(nickname, cookie, data);

		hostList.add(host);
		deviceMap.put(cookie, new DeviceInfo(wifiDevice, System.currentTimeMillis(), timeout * 1000));
	}

	/**
	 * Removes any saved host.
	 */
	private void clearAll () {
		hostList.clear();
		deviceMap.clear();
	}

	/**
	 * Cleans any timed out host from the list.
	 */
	private void removeTimedOut () {
		Host currentHost;
		DeviceInfo currentDevice;
		long currentTime = System.currentTimeMillis();

		for (Iterator<Host> i = hostList.iterator(); i.hasNext() ; ) {
			currentHost = i.next();
			currentDevice = deviceMap.get(currentHost.connectionCookie);

			if (currentDevice != null) {
				if (currentTime - currentDevice.start > currentDevice.timeout) {
					Logger.log(LogLevel.UPDATE, TAG, "Host timed out: " + currentHost.nickname);
					deviceMap.remove(currentHost.connectionCookie);
					i.remove();
				}
			} else {
				//Outstanding reference, remove.
				i.remove();
			}
		}
	}

	/**
	 * Starts a new ConnectionThread server-side
	 * 
	 * @see {@link ConnectionThread}
	 */
	private void startServerThread () {
		if (connectionThread != null) {
			connectionThread.interrupt();
		}

		connectionThread = new ConnectionThread();
		connectionThread.start();
	}

	/**
	 * Starts a new ConnectionThread to connect to the group owner, client-side.
	 * 
	 * @param groupOwnerAddress
	 * @see {@link ConnectionThread}
	 */
	private void startClientThread (InetAddress groupOwnerAddress) {
		if (connectionThread != null) {
			connectionThread.interrupt();
		}

		connectionThread = new ConnectionThread(groupOwnerAddress);
		connectionThread.start();
	}

	/**
	 * Shortcut for setting the state to FAILURE and setting the failure reason.
	 * 
	 * @param reason
	 */
	private void fail(String reason) {
		state = AdapterState.FAILURE;
		failureReason = reason;
	}

	@Override
	public void updateState(float delta) {
		switch (state) {
		case IDLE:			//All these cases are dealt by Android callbacks.
		case FAILURE:
		case UPDATING:
		case CONNECTING:
		case WAITING:
			return;

		case FINALIZING:
			if (connectionThread == null) return;
			switch (connectionThread.getConnectionState()) {	//Check the ConnectionThread state. If it succeeded, start the game.
			case RUNNING:
				return;
			case SUCCESS:
				inThread = connectionThread.getInput();
				outThread = connectionThread.getOutput();
				inThread.start();
				outThread.start();
				state = AdapterState.GAME;
				return;
			case FAIL:
				fail(connectionThread.getFailureReason());
				return;
			}
			return;

		case GAME:
			if (outThread == null || inThread == null) {	//Handles any gametime connection error.
				fail(FAIL_IO_NOT_READY);
				return;
			} else {
				switch (outThread.getStatus()) {
				case RUNNING:
					break;
				case CLOSED:
					state = AdapterState.IDLE;
					return;
				case FAIL:
					fail(FAIL_IO);
					return;
				}

				switch (inThread.getStatus()) {
				case RUNNING:
					break;
				case CLOSED:
					state = AdapterState.IDLE;
					return;
				case FAIL:
					fail(FAIL_IO);
					return;
				}
			}
		}
	}

	@Override
	public void updateHosts() {
		if (state != AdapterState.IDLE) return;	//Cannot update if not IDLE.
		else {
			clearAll();
			connectionCheck();
			addServiceRequest();
			state = AdapterState.UPDATING;
		}
	}

	@Override
	public void stopHostUpdate() {
		removeServiceRequest();

		if (state == AdapterState.UPDATING) 
			state = AdapterState.IDLE;
		else return;
	}

	@Override
	public void getHosts(ArrayList<Host> destination) {
		removeTimedOut();
		destination.clear();
		destination.addAll(hostList);
	}

	@Override
	public void register(String nickname, int timeout, String data) {
		reset();
		connectionCheck();

		if (channel == null || !wifiManager.isWifiEnabled()) {
			fail(FAIL_WIFI_OFF);
			return;
		}

		state = AdapterState.CONNECTING;

		serviceRecord.put(KEY_NICKNAME, nickname);
		serviceRecord.put(KEY_TIMEOUT, String.valueOf(timeout));
		serviceRecord.put(KEY_DATA, data);
		localData = data;
		addLocalService();
		addServiceRequest();		//Although weird, user must scan for nearby services to be visible. It's an Android related stuff.
	}

	@Override
	public void connect(Host host) {
		
		if (!deviceMap.containsKey(host.connectionCookie)) {
			fail(FAIL_NO_HOST);
			return;
		} else {
			DeviceInfo deviceInfo = deviceMap.get(host.connectionCookie);
			reset();
			connectionCheck();

			if (channel == null || !wifiManager.isWifiEnabled()) {
				fail(FAIL_WIFI_OFF);
				return;
			}

			state = AdapterState.CONNECTING;

			localData = host.data;
			config.deviceAddress = deviceInfo.device.deviceAddress;

			p2pManager.connect(channel, config, connectionRequestListener);
		}
	}

	@Override
	public void reset() {
		//Stop all threads. Stopping also clears any pending resource.
		if (connectionThread != null) {
			if (connectionThread.isAlive()) connectionThread.interrupt();
			connectionThread = null;
		}
		if (inThread != null) {
			if (inThread.isAlive()) inThread.interrupt();
			inThread = null;
		}
		if (outThread != null) {
			if (outThread.isAlive()) outThread.interrupt();
			outThread = null;
		}

		stopHostUpdate();
		clearAll();
		removeServiceRequest();
		removeLocalService();
		p2pManager.cancelConnect(channel, null);
		p2pManager.removeGroup(channel, null);

		state = AdapterState.IDLE;
	}

	@Override
	public void send(String message) {
		if (state != AdapterState.GAME) {
			Logger.log(LogLevel.ERROR, TAG, "Can only send in GAME status");
			return;
		}

		if (outThread != null) {
			OutThreadStatus outStatus = outThread.getStatus();

			switch (outStatus) {
			case RUNNING:
				outThread.putMessage(message);
				break;
			case CLOSED:
				fail(FAIL_FRIEND_CLOSED);
				break;
			case FAIL:
				fail(FAIL_IO);
				break;
			}

			return;
		} else {
			fail(FAIL_IO_NOT_READY);
			return;
		}
	}

	@Override
	public String receive() {

		if (state != AdapterState.GAME) {
			Logger.log(LogLevel.ERROR, TAG, "Can only send in GAME status");
			return null;
		}


		if (inThread != null) {
			InThreadStatus inStatus = inThread.getStatus();

			switch (inStatus) {
			case RUNNING:
				return inThread.getMessage();
			case CLOSED:
			default:
				fail(FAIL_FRIEND_CLOSED);
				return null;
			}
		} else {
			fail(FAIL_IO_NOT_READY);
			return null;
		}
	}

	@Override
	public AdapterState getState() {
		return state;
	}

	@Override
	public String getData() {
		return localData;
	}

	@Override
	public String getFailureReason() {
		return failureReason;
	}

	/**
	 * Called from the current Activity during onResume. Used to register the broadcast receiver.
	 */
	public void registerReceiver() {
		activity.registerReceiver(broadcastReceiver, filter);
	}

	/**
	 * Called from the current Activity during onPause. Used to unregister the broadcast receiver.
	 */
	public void unregisterReceiver() {
		activity.unregisterReceiver(broadcastReceiver);
	}
}