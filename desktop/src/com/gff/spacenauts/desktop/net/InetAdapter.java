package com.gff.spacenauts.desktop.net;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.gff.spacenauts.Globals;
import com.gff.spacenauts.net.NetworkAdapter;

/**
 * <p>
 * A {@link NetworkAdapter} that connects to a remote server to perform matchmaking.
 * Transactions are done through the SGMP protocol to instantiate a virtual P2P connection
 * between players using the server as a relay. More details about SGMP are detailed
 * in a separate document. In short, a Host player contacts the server declaring the 
 * parameters for his match. On the other side a Guest players fetches the Host list from
 * the server, then performs a connection request to the server providing that Host's ID (cookie).
 * After the server has acknowledged both the host and guest for the pairing they send an ack back
 * and can then start communicating. 
 * </p>
 * 
 * <p>
 * As for the implementation, most of the work is done through the use of Java {@link Executors}
 * for one-shot tasks that will later return a result. This way one must not worry too much about
 * concurrency between threads as access to data is always performed thread-safely.
 * </p>
 * 
 * <p>
 * While this could of course work on Android too it was built with PCs in mind, who usually
 * have a flat connection available. On mobile devices the use of local ad hoc connections
 * (WiFi Direct or Bluetooth) are more appealing, both performance and cost wise.
 * </p> 
 * 
 * @author Alessio
 *
 */
public class InetAdapter implements NetworkAdapter {

	private enum Agent {
		HOST, GUEST
	}

	private static final int POOL_SIZE = 10;

	//Error messages
	private static final String FAIL_UNKNOWN = "Unknown";
	private static final String FAIL_CANCELLED = "Task was cancelled";
	private static final String FAIL_INVALID_RESPONSE = "Server error: ";

	//Answers
	private static final String ANS_OK = "OK";
	private static final String ANS_WAITING = "WAITING";
	private static final String ANS_MATCHED = "MATCHED";

	//Executors related stuff
	private ExecutorService executor;

	//Executors requests
	private Connect connectRequest;
	private StatusCheck statusCheckRequest;
	private Finalize finalizeRequest;
	
	//Executors results
	private Future<ArrayList<Host>> updateResult;
	private Future<String> connectionResult;
	private Future<String> statusCheckResult;
	private Future<String> finalizeResult;
	
	//Adapter related stuff
	private AdapterState state = AdapterState.IDLE;
	private Agent agent;
	private String failureReason = FAIL_UNKNOWN;
	private float statusCheckTimer = 0;
	private String localData;
	private String cookie;		//This is either the privateCookie (HOST) or publicCookie (GUEST)
	private ArrayList<Host> hostList;
	private InThread inThread;
	private OutThread outThread;

	public InetAdapter () {
		executor = Executors.newFixedThreadPool(POOL_SIZE);
	}

	/**
	 * Dispatches every state to its handler.
	 */
	@Override
	public void updateState(float delta) {
		switch (state) {
		case IDLE:
			return;
		case UPDATING:
			checkUpdateResult();
			break;
		case CONNECTING:
			checkConnectionResult();
			break;
		case WAITING:
			checkStatusResult(delta);
			break;
		case FINALIZING:
			checkFinalizingResult();
			break;
		default:
			return;
		}
	}

	/**
	 * Verifies the update status and, if successful, updates the host 
	 * list and returns to {@link AdapterState#IDLE}.
	 */
	private void checkUpdateResult() {

		if (updateResult != null) {
			
			if (updateResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} 
			
			else if (updateResult.isDone()) {
				
				try {
					hostList = updateResult.get();
				} catch (ExecutionException e) {
					fail(e.getCause().getMessage());
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				state = AdapterState.IDLE;
			}
		}
	}

	/**
	 * Verifies whether the server successfully acked a connection request
	 * (either by Host or Guest) and acts accordingly.
	 */
	private void checkConnectionResult() {
		String serverAnswer = null;
		
		if (connectionResult != null) {
			
			if (connectionResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} 
			
			else if (connectionResult.isDone()) {
				
				try {
					serverAnswer = connectionResult.get();
				} catch (ExecutionException e) {
					fail(e.getCause().getMessage());
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}

				if (serverAnswer.startsWith(ANS_OK)) {
					
					//Connected as Host. Setting the private cookie given by the server.
					if (agent == Agent.HOST) {
						String[] splitAnswer = serverAnswer.split("\\s", 2);
						if (splitAnswer.length < 2) {
							fail(FAIL_INVALID_RESPONSE + serverAnswer);
							return;
						} else {
							cookie = splitAnswer[1];
						}
					}
					
					//The server acked back, so we need to keep checking the matchmaking status.
					submitStatusCheckRequest();
					state = AdapterState.WAITING;
					
				} else {
					fail(FAIL_INVALID_RESPONSE + serverAnswer);
				}
			}
		}
	}
	
	private void checkStatusResult(float delta) {
		String serverAnswer = null;
		
		if (statusCheckResult != null) {
			
			if (statusCheckResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} 
			
			else if (statusCheckResult.isDone()) {
				
				try {
					serverAnswer = statusCheckResult.get();
				} catch (ExecutionException e) {
					fail(e.getCause().getMessage());
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				//The server said no match yet. Oh well, better try again.				
				if (serverAnswer.equals(ANS_WAITING)) {
					resetStatusCheckRequest();
					return;
				} 
				
				//We have a match! Go to FINALIZING
				else if (serverAnswer.equals(ANS_MATCHED)) {
					state = AdapterState.FINALIZING;
					finalizeRequest = new Finalize(statusCheckRequest.getSocket());
					finalizeResult = executor.submit(finalizeRequest);
				} 
				
				//Something went wrong. Fail away.
				else {
					fail(FAIL_INVALID_RESPONSE + serverAnswer);
					return;
				}
			}
			
		} else {
			//We're waiting but there's no request, let's invoke a new one...
			statusCheckTimer += delta;
			//...provided enough time has passed. We don't want to DoS our server do we?
			if (statusTimerExpired()) submitStatusCheckRequest();
		}
	}

	/**
	 * Submits a {@link StatusCheck} to the executor. If the agent is a Host
	 * it will provide the private cookie, otherwise being a guest it will
	 * provide the current connection established from the {@link Connect}
	 * request.
	 */
	private void submitStatusCheckRequest() {
		
		if (agent == Agent.HOST) 
			statusCheckRequest = new StatusCheck(cookie);
		
		else if (agent == Agent.GUEST) 
			statusCheckRequest = new StatusCheck(connectRequest.getSocket());
		
		statusCheckResult = executor.submit(statusCheckRequest);
	}

	/**
	 * Voids any previous result and resets the timer for StatusCheck requests.
	 */
	private void resetStatusCheckRequest() {
		statusCheckResult = null;
		statusCheckTimer = 0;
	}

	/**
	 * <p>
	 * Tests the statusCheckTimer against a certain interval of time.
	 * If the agent is a Guest it is fixed to 5 seconds. Otherwise, as
	 * a Host, it is a function of the host timeout (that is, the max time
	 * the Host is expected to wait). This value is a smooth exponential
	 * curve that goes from 1 to 10 seconds asymptotically when the Host
	 * timeout varies from 0 to 300 seconds. Something like this:
	 * </p>
	 * 
	 * <pre>
	 *   ex
	 * 10 |           xxxxxxxxxxxxx
	 *    |         xx 
	 *  5 |        x 
	 *    |      xx
	 *  1 |xxxxxx
	 *    |------------------------ to
	 *    0        50           300
	 * </pre>
	 * 
	 * <p>
	 * To check the exact formula refer to {@link Globals#updateExpire()}.
	 * </p>
	 * 
	 * @return
	 */
	private boolean statusTimerExpired () {		
		//Maybe some tuning should be done against these values?
		float expired = (agent == Agent.HOST) ? Globals.expireCheck : 5;
		return statusCheckTimer > expired;
	}

	/**
	 * Checks the result of the FINALIZING handshake. If the server acks
	 * for a GAME_READY then we start the IO threads and switch to
	 * {@link AdapterState#GAME GAME}.
	 */
	private void checkFinalizingResult () {
		String serverAnswer = null;
		
		if (finalizeResult != null) {
			
			if (finalizeResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} 
			
			else {
				
				if (finalizeResult.isDone()) {
					
					try {
						serverAnswer = finalizeResult.get();
					} catch (ExecutionException e) {
						fail(e.getCause().getMessage());
						return;
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
					
					if (serverAnswer.equals("GAME_READY")) {
						inThread = new InThread(finalizeRequest.getSocket());
						outThread = new OutThread(finalizeRequest.getSocket());
						inThread.start();
						outThread.start();
						state = AdapterState.GAME;
					} 
					
					else {
						fail(FAIL_INVALID_RESPONSE + serverAnswer);
						return;
					}
				
				}
			
			}
			
		}	
	}

	@Override
	public void updateHosts() {
		if (state == AdapterState.IDLE) {
			state = AdapterState.UPDATING;
			updateResult = executor.submit(new Update());
		}
	}

	@Override
	public void stopHostUpdate() {
		if (state == AdapterState.UPDATING) {
			state = AdapterState.IDLE;
			updateResult.cancel(false);
			hostList = null;
		}
	}

	@Override
	public void getHosts(ArrayList<Host> destination) {
		if (hostList != null) 
			destination.addAll(hostList);
	}

	@Override
	public void register(String nickname, int timeout, String data) {
		if (state == AdapterState.IDLE) {
			agent = Agent.HOST;
			state = AdapterState.CONNECTING;
			connectionResult = executor.submit(new Register(nickname, timeout * 1000, data));
			localData = data;
		}
	}

	@Override
	public void connect(Host host) {
		if (state == AdapterState.IDLE) {
			agent = Agent.GUEST;
			state = AdapterState.CONNECTING;
			cookie = host.connectionCookie;
			connectRequest = new Connect(host.connectionCookie);
			connectionResult = executor.submit(connectRequest);
			localData = host.data;
		}
	}

	@Override
	public void reset() {
		if (state == AdapterState.WAITING && agent == Agent.HOST) 
			executor.submit(new Close(cookie));

		if (updateResult != null) {
			updateResult.cancel(false);
			updateResult = null;
		}
		
		if (connectionResult != null) {
			connectionResult.cancel(false);
			connectionResult = null;
		}
		
		if (statusCheckResult != null) {
			statusCheckResult.cancel(false);
			statusCheckResult = null;
		}
		
		if (finalizeResult != null) {
			finalizeResult.cancel(false);
			finalizeResult = null;
		}
		
		if (inThread != null) {
			inThread.interrupt();
			inThread = null;
		}
		
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}

		agent = null;
		cookie = null;
		state = AdapterState.IDLE;
	}

	@Override
	public void send(String message) {
		if (state == AdapterState.GAME) {
			outThread.putMessage(message);
		}
	}

	@Override
	public String receive() {
		if (state == AdapterState.GAME) {
			return inThread.getMessage();
		} else return "";
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

	private void fail(String failureReason) {
		state = AdapterState.FAILURE;
		this.failureReason = failureReason;
	}
}