package com.gff.spacenauts.net;

import java.util.ArrayList;

/**
 * <p>
 * An interface for implementing multiplayer and matchmaking features.<br>
 * Any implementation must provide the given methods based on their description. This interface was built
 * with SGMP in mind, so an enumeration is also defined which represents all of SGMP possible client states 
 * for both Host and Guest (plus an extra UPDATING status, since the UI needs acting differently on update).
 * Implementing classes need not necessarily make use of all states, as long as each method is properly implemented.
 * </p>
 * <p>
 * A data structure called {@link Host} is also provided. Any information regarding other players should be stored
 * in a Host instance.
 * </p>
 * <p>
 * Example implementations are WifiP2PNetworkAdapter which uses Android's API for WiFi Direct
 * and InetAdapter which uses Java Sockets and the SGMP protocol. See their respective projects for more info.
 * </p>
 * 
 * @author Alessio Cali'
 *
 */
public interface NetworkAdapter {

	/**
	 * The status this interface is currently in. Each state has a precise meaning:
	 * 
	 * <ul>
	 * <li><em>IDLE</em>: the interface is not operating. It is safe to leave the multiplayer menu.</li>
	 * <li><em>UPDATING</em>: the interface is currently fetching the host list. Before performing any operation while in this state you should stop the update process.</li>
	 * <li><em>CONNECTING</em>: the interface is currently trying to establish a connection with the matchmaking framework, either by publicizing itself to start a new game or by asking to join another player who publicized himself first.</li>
	 * <li><em>WAITING</em>: the interface has successfully established a connection with the matchmaking framework.</li>
	 * <li><em>FINALIZING</em>: the matchmaking framework managed to connect the two players and the interface is now confirming its state to the other player.</li>
	 * <li><em>GAME</em>: the two players are connected and playing together. It is now possible to send messages to each other.</li>
	 * <li><em>FAILURE</em>: something has gone wrong during the process. The reason of this failure is stored and can be retrieved and shown to the player by calling {@link NetworkAdapter#getFailureReason() getFailureReason()}.</li>
	 * </ul>
	 * 
	 * <p>
	 * It is <em>not</em> mandatory to implement all different states. Depending on the underlying framework, one could consider CONNECTING, WAITING and FINALIZING as one single state,
	 * or it might wish to not report any error and just go back to IDLE in case of failure. Mind that the UI expects the interface to at least start in the IDLE status and the game's engine
	 * only sends messages if the interface is in GAME status, so one should at least implement these two.
	 * </p>
	 * <p>
	 * Also note that it is NOT needed to start a new game from within the interface. The UI periodically polls the adapter's state and starts a new game
	 * once {@link NetworkAdapter#getState()} returns {@link AdapterState#GAME}.
	 * </p>
	 * 
	 * @author Alessio Cali'
	 *
	 */
	public enum AdapterState {
		IDLE, UPDATING, CONNECTING, WAITING, FINALIZING, GAME, FAILURE
	}

	/**
	 * A data structure that holds any host related data. This includes:
	 * 
	 * <ul style="list-style-type:none;">
	 * <li>nickname: the host's former name, as publicized.</li>
	 * <li>connectionCookie: the host's unique identifier, used to perform the connection.</li>
	 * <li>data: a extra data String provided by the host, to define the match's parameters. It is formatted as "LEVEL_NAME MAP_FILE"</li>
	 * </ul>
	 * 
	 * @author Alessio Cali'
	 *
	 */
	public class Host {
		public final String nickname;
		public final String connectionCookie;
		public final String data;
		
		public Host (String nickname, String connectionCookie, String data) {
			this.nickname = nickname;
			this.connectionCookie = connectionCookie;
			this.data = data;
		}
	}
	
	/**
	 * Called to update the interface's state at each frame. Can be used to make use of asynchronous calls rather
	 * then concurrent methods with threads.
	 * 
	 * @param delta the time elapsed (in seconds) since the last call.
	 * 
	 */
	public void updateState (float delta);

	/**
	 * Updates the hosts list. Implementation should also clear any previously cached data.
	 * It is recommended (but not obligatory) to change to the {@link AdapterState.UPDATING} status.
	 */
	public void updateHosts ();
	
	/**
	 * Stops the update and puts in IDLE status. Should not clear the hosts list.
	 */
	public void stopHostUpdate();
	
	/**
	 * Puts all hosts in destination.
	 * 
	 * @param destination the array where to put the hosts.
	 */
	public void getHosts (ArrayList<Host> destination);
	
	/**
	 * Registers as host. This will transition from IDLE to CONNECTING and set the current data 
	 * from the parameter data.
	 * 
	 * @param nickname
	 * @param timeout 
	 * @param data additional data for gameplay setup.
	 */
	public void register (String nickname, int timeout, String data);
	
	/**
	 * Connects as guest to the given host. This will transition from IDLE to CONNECTING
	 * and set the current data to the host's data.
	 * 
	 * @param connectionCookie
	 */
	public void connect (Host host);
	
	/**
	 * Resets the adapter. The implementation should put it in IDLE state and 
	 * clean up any pending connection or update and the hosts list. 
	 */
	public void reset ();
	
	/**
	 * During game time, sends a message to the other player.
	 * 
	 * @param message
	 */
	public void send (String message);
	
	/**
	 * During game time, receives incoming messages from the other player.
	 * The usual implementation is to pop a FIFO queue, however this can be changed
	 * at the programmer's discretion.
	 * 
	 * @return an incoming message if any, null otherwise.
	 */
	public String receive ();
	
	/**
	 * @return the adapter's current state.
	 */
	public AdapterState getState ();
	
	/**
	 * Get the data string for gameplay setup. It should be in the format "LEVEL_NAME MAP_FILE"
	 * 
	 * @return the setup data.
	 */
	public String getData ();
	
	/**
	 * Informs caller of the failure's reason.
	 * 
	 * @return a message error in case of FAILURE, null otherwise.
	 */
	public String getFailureReason ();
	
}
