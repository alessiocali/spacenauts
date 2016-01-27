package com.gff.spacenauts.android.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;

/**
 * A thread that sends messages to another player through network.
 * Access to the message queue is thread-safe.
 * 
 * @author Alessio Cali'
 *
 */
public class OutThread extends Thread {
	
	public enum OutThreadStatus {
		RUNNING, CLOSED, FAIL
	}
	
	private final static String TAG = "OutThread";
	
	private final static String MSG_SERVER_CLOSE = "SERVER CLOSE";
	
	private static final int QUEUE_SIZE = 1000;
	private Socket socket;
	private PrintWriter writer;
	private Array<String> messageQueue;
	private OutThreadStatus status = OutThreadStatus.RUNNING;

	public OutThread (Socket socket) {
		if (socket == null) throw new GdxRuntimeException(new IllegalArgumentException("Socket can't be null"));
		this.socket = socket;
		messageQueue = new Array<String>(QUEUE_SIZE);
	}

	@Override
	public void run() {
		try {
			writer = new PrintWriter(socket.getOutputStream(), true);

			while (status == OutThreadStatus.RUNNING) {
				String msg;
				synchronized(messageQueue) {
					while (messageQueue.size == 0) {
						messageQueue.wait();
					}
					msg = messageQueue.first();
					messageQueue.removeIndex(0);
				}

				writer.println(msg);
				if (msg.equals(MSG_SERVER_CLOSE)) status = OutThreadStatus.CLOSED;
			}
		} catch (InterruptedException ine) {
			Logger.log(LogLevel.WARNING, TAG, "Interrupted");
			status = OutThreadStatus.CLOSED;
		} catch (IOException e) {
			status = OutThreadStatus.FAIL;
			e.printStackTrace();
		}
	}

	/**
	 * Queues a message to be sent to the other player. Access is thread safe.
	 * 
	 * @param msg the message to send.
	 * @return true if the message could be queued. False if the buffer is full.
	 */
	public boolean putMessage(String msg) {
		synchronized (messageQueue) {
			if (messageQueue.size == QUEUE_SIZE) {
				Logger.log(LogLevel.WARNING, TAG, "Full buffer!");
				return false;
			} else {
				messageQueue.add(msg);
				messageQueue.notifyAll();
				return true;
			}
		}
	}

	/**
	 * Stops thread. Warning: socket must be disposed appropriately.
	 */
	public void close () {
		status = OutThreadStatus.CLOSED;
	}

	public OutThreadStatus getStatus() {
		return status;
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		try {
			if (!socket.isClosed()) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}