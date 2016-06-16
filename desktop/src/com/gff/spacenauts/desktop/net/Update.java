package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;
import com.gff.spacenauts.net.NetworkAdapter;
import com.gff.spacenauts.net.NetworkAdapter.Host;

/**
 * Java Callable that handles the host list UPDATE from a SGPP server.
 * It will return the ArrayList of {@link NetworkAdapter.Host}. 
 * 
 * @author Alessio
 *
 */
public class Update implements Callable<ArrayList<Host>> {

	private static final String CMD_LIST = "LIST";
	private static final String ANS_BEGIN = "LIST_BEGIN";
	private static final String ANS_END = "LIST_END";
	private static final int TIMEOUT = 10000;

	private ArrayList<Host> hostList;
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket socket;

	/**
	 * An exception caused by invalid host data.
	 * 
	 * @author Alessio
	 *
	 */
	private static class InvalidHostException extends Exception {

		private static final long serialVersionUID = 5515205344158630387L;
		private static final String MESSAGE = "Host data was invalid: ";

		public InvalidHostException(String hostData) {
			super(MESSAGE + hostData);
		}		
	}

	/**
	 * An exception caused by an invalid server response.
	 * 
	 * @author Alessio
	 */
	private static class InvalidResponseException extends Exception {

		private static final long serialVersionUID = -2762707638242698152L;
		private static final String MESSAGE = "Unknown server response: ";

		public InvalidResponseException (String response) {
			super(MESSAGE + response);
		}
	}

	@Override
	public ArrayList<Host> call() throws Exception {
		try {
			socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);
			socket.setSoTimeout(TIMEOUT);
			
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

			writer.println(CMD_LIST);
			String ans = reader.readLine();
			
			if (ANS_BEGIN.equals(ans)) {
				
				//The host list has been given. Extract all hosts info from it.
				hostList = new ArrayList<Host>(5);
				Host host = null;
				while ((host = getHostFromString(reader.readLine())) != null) 
					hostList.add(host);
				
			} else {
				throw new InvalidResponseException(ans);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (socket != null) socket.close();
		}
		
		return hostList;
	}

	/**
	 * Extracts host data from a SGPP Host list line. This string is usually formatted as:<br>
	 * NICKNAME CONN_COOKIE DATA.
	 * 
	 * @param hostString
	 * @return
	 * @throws InvalidHostException
	 */
	private Host getHostFromString(String hostString) throws InvalidHostException {
		if (hostString == null || ANS_END.equals(hostString)) return null;
		String[] values = hostString.split("\\s", 3);
		if (values.length < 3) throw new InvalidHostException(hostString);
		return new Host(values[0], values[1], values[2]);
	}

}
