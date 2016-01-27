package com.gff.spacenauts.desktop.net;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.gff.spacenauts.Globals;
import com.gff.spacenauts.net.NetworkAdapter;

public class InetAdapter implements NetworkAdapter {

	private enum Agent {
		HOST, GUEST
	}

	private static final int POOL_SIZE = 10;

	private static final String FAIL_UNKNOWN = "Unknown";
	private static final String FAIL_CANCELLED = "Task was cancelled";
	private static final String FAIL_INVALID_RESPONSE = "Server error: ";

	private static final String ANS_OK = "OK";
	private static final String ANS_WAITING = "WAITING";
	private static final String ANS_MATCHED = "MATCHED";

	//Executors related stuff
	private ExecutorService executor;
	private Connect connectRequest;
	private StatusCheck statusCheckRequest;
	private Finalize finalizeRequest;
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

	private void checkUpdateResult() {
		if (updateResult != null) {
			if (updateResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} else if (updateResult.isDone()) {
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

	private void checkConnectionResult() {
		String serverAnswer = null;
		if (connectionResult != null) {
			if (connectionResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} else 	if (connectionResult.isDone()) {
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
					if (agent == Agent.HOST) {
						String[] splitAnswer = serverAnswer.split("\\s", 2);
						if (splitAnswer.length < 2) {
							fail(FAIL_INVALID_RESPONSE + serverAnswer);
							return;
						} else {
							cookie = splitAnswer[1];
						}
					}
					
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
			} else if (statusCheckResult.isDone()) {
				try {
					serverAnswer = statusCheckResult.get();
				} catch (ExecutionException e) {
					fail(e.getCause().getMessage());
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				if (serverAnswer.equals(ANS_WAITING)) {
					resetStatusCheckRequest();
					return;
				} else if (serverAnswer.equals(ANS_MATCHED)) {
					state = AdapterState.FINALIZING;
					finalizeRequest = new Finalize(statusCheckRequest.getSocket());
					finalizeResult = executor.submit(finalizeRequest);
				} else {
					fail(FAIL_INVALID_RESPONSE + serverAnswer);
					return;
				}
			}
		} else {
			//We're waiting but there's no request, let's invoke a new one
			statusCheckTimer += delta;
			if (statusTimerExpired()) submitStatusCheckRequest();
		}
	}

	private void submitStatusCheckRequest() {
		if (agent == Agent.HOST) {
			statusCheckRequest = new StatusCheck(cookie);
		} else if (agent == Agent.GUEST) {
			statusCheckRequest = new StatusCheck(connectRequest.getSocket());
		}
		statusCheckResult = executor.submit(statusCheckRequest);
	}


	private void resetStatusCheckRequest() {
		statusCheckResult = null;
		statusCheckTimer = 0;
	}

	private boolean statusTimerExpired () {
		float expire = (agent == Agent.HOST) ? Globals.expireCheck : 5;	//TODO tuning;
		return statusCheckTimer > expire;
	}

	private void checkFinalizingResult () {
		String serverAnswer = null;
		if (finalizeResult != null) {
			if (finalizeResult.isCancelled()) {
				fail(FAIL_CANCELLED);
				return;
			} else {
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
					} else {
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

		state = AdapterState.IDLE;
		agent = null;
		cookie = null;
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