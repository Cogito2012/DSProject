package edu.rit.cs;

//The JSON-RPC 2.0 Base classes that define the 
//JSON-RPC 2.0 protocol messages

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MiniServer {
	private static Map<Integer, KademliaPeer> onlinePeers = new HashMap<Integer, KademliaPeer>();

	public static void addPeer(int peerID, String ipAddres, String port, boolean isAnchor) {
		KademliaPeer peer = new KademliaPeer(peerID, ipAddres, port, isAnchor);
		onlinePeers.put(peerID, peer);
		// update routing tables for all peers
		updateRouteTables(Config.BIN_SIZE);
		// // update file storage
		updataFileStorage(peer, "add");
	}

	public static void removePeer(int peerID) {
		KademliaPeer peer = onlinePeers.get(peerID);
		onlinePeers.remove(peerID);
		// update routing tables for all peers
		updateRouteTables(Config.BIN_SIZE);
		// // update file storage
		updataFileStorage(peer, "remove");
	}

	public static boolean isPeerOnline(int peerID) {
		return onlinePeers.containsKey(peerID);
	}

	public static String getOnlinePeers(){
		String onlinePeerIDs = "";
		Iterator<Map.Entry<Integer, KademliaPeer>> entries = onlinePeers.entrySet().iterator();
		while (entries.hasNext()){
			int peerID = entries.next().getKey();
			onlinePeerIDs += "," + String.valueOf(peerID);
		}
		onlinePeerIDs = onlinePeerIDs.substring(1, onlinePeerIDs.length());
		return onlinePeerIDs;
	}

	public static boolean isAnchorPeer(int peerID){
		return onlinePeers.get(peerID).getAnchorState();
	}

	public static String getRouteTable(int peerID){
		LinkedHashMap<String, String> routeTable = onlinePeers.get(peerID).getRouteTable();
		String tableStr = "";
		for(Map.Entry<String, String> entry : routeTable.entrySet()){
			tableStr += "," + entry.getKey() + ":" + entry.getValue();
		}
		tableStr = tableStr.substring(1, tableStr.length());
		return tableStr;
	}

	private static void updateRouteTables(int binSize){
		// get all online peer IDs
		HashSet<Integer> onlineNodeSet = new HashSet<>();
		Iterator<Integer> iterKey = onlinePeers.keySet().iterator();
		while (iterKey.hasNext()){
			int nodeID = Integer.valueOf(iterKey.next());
			onlineNodeSet.add(nodeID);
		}
		// update routing table for each peer
		Iterator<Map.Entry<Integer, KademliaPeer>> entries = onlinePeers.entrySet().iterator();
		while (entries.hasNext()){
			entries.next().getValue().updateRouteTable(onlineNodeSet, binSize);
		}
	}

	private static Integer pickAnchorNode(){
		int anchorID = -1;
		Iterator<Map.Entry<Integer, KademliaPeer>> entries = onlinePeers.entrySet().iterator();
		while (entries.hasNext()){
			Map.Entry<Integer, KademliaPeer> entry = entries.next(); 
			if (entry.getValue().getAnchorState()){
				anchorID = entry.getValue().getNodeID();
				break;
			}
		}
		if (anchorID < 0){
			System.out.println("No anchor node!");
			return null;
		}
		return anchorID;
	}

	private static void updataFileStorage(KademliaPeer peer, String op){
		if (op.equals("remove")){
			// need to re-insert all files in this peer to other nodes
			LinkedHashMap<String, String> fileDB = peer.getFileDB();
			for (Map.Entry<String, String> db : fileDB.entrySet()){
				String fileHash = db.getKey();
				String fileData = db.getValue();
				// find the destination node
				String keyHex = fileHash.substring(31,32);  // filehash mod 16
				int keyID = Integer.parseInt(keyHex, 16);   // target Key nodeID
				int updatedNode = KadDHT(pickAnchorNode(), keyID, peer);
				onlinePeers.get(updatedNode).insert(fileHash, fileData);
				System.out.println("File is moved from node " + String.valueOf(peer.getNodeID()) + " to node " + String.valueOf(updatedNode));
			}
		}else if (op.equals("add")){
			// need to check files on each node
			List<LinkedHashMap<String, String>> fileDataTemp = new ArrayList<>();
			List<Integer> nodeListTemp = new ArrayList<>();;
			// gather all file data
			Iterator<Map.Entry<Integer, KademliaPeer>> entries = onlinePeers.entrySet().iterator();
			while (entries.hasNext()){
				Map.Entry<Integer, KademliaPeer> entry = entries.next(); 
				int nodeID = entry.getKey();
				LinkedHashMap<String, String> fileDB = entry.getValue().getFileDB();
				if (!fileDB.entrySet().isEmpty()){
					fileDataTemp.add(fileDB);
					nodeListTemp.add(nodeID);
				}
			}
			for (int i=0; i<fileDataTemp.size(); i++){
				LinkedHashMap<String, String> fileDB = fileDataTemp.get(i);
				int nodeID = nodeListTemp.get(i);
				for (Map.Entry<String, String> db : fileDB.entrySet()){
					String fileHash = db.getKey();
					String fileData = db.getValue();
					// find the destination node
					String keyHex = fileHash.substring(31,32);  // filehash mod 16
					int keyID = Integer.parseInt(keyHex, 16);   // target Key nodeID
					int updatedNode = KadDHT(pickAnchorNode(), keyID, null);
					onlinePeers.get(updatedNode).insert(fileHash, fileData);
					System.out.println("File is moved from node " + String.valueOf(nodeID) + " to node " + String.valueOf(updatedNode));
				}
			}
		}
	}


	private static double compAddress(String clientAddress, String peerAddress){
		// parse the client address
		String ip_client = clientAddress.split(":")[0];
		if (ip_client.equals("localhost")){
			ip_client = "127.0.0.1";
		}
		String[] ipList_client = ip_client.split("\\.");
		int port_client = Integer.parseInt(clientAddress.split(":")[1]);
		// System.out.println("client ip: " + ip_client + ", port: " + String.valueOf(port_client));

		// parse the peer address
		String ip_peer = peerAddress.split(":")[0];
		if (ip_peer.equals("localhost")){
			ip_peer = "127.0.0.1";
		}
		String[] ipList_peer = ip_peer.split("\\.");
		int port_peer = Integer.parseInt(peerAddress.split(":")[1]);
		// System.out.println("peer ip: " + ip_peer + ", port: " + String.valueOf(port_peer));

		// compute the distance bwteen IPs and Ports
		double dist = 0.0;
		for (int i=0; i<4; i++){
			dist += Integer.valueOf(ipList_client[i]) ^ Integer.valueOf(ipList_peer[i]);
		}
		dist += port_client ^ port_peer;
		return dist;
	}


	public static String FindNearestAnchor(String ip, String port){
		String clientAddr = ip + ":" + port;
		// find the nearest Anchor kademlia node (ip, port) of client (ip, port)
		double minDist = 999999;
		Map.Entry<Integer, KademliaPeer> anchorEntry = null;
		Iterator<Map.Entry<Integer, KademliaPeer>> entries = onlinePeers.entrySet().iterator();
		while (entries.hasNext()){
			Map.Entry<Integer, KademliaPeer> peerEntry = entries.next();
			if (peerEntry.getValue().getAnchorState()){
				String peerAddr = peerEntry.getValue().getIP() + ":" + peerEntry.getValue().getPORT();
				double dist = compAddress(clientAddr, peerAddr);
				if (dist < minDist){
					anchorEntry = peerEntry;
					minDist = dist;
				}
			}
		}
		System.out.println("Anchor " + anchorEntry.getKey() + " is the nearest to client (" + ip + ":" + port + ").");
		// response the client to communicate with anchor node
		// info: "anchorID:anchorIP:anchorPORT"
		String destNodeAddr = String.valueOf(anchorEntry.getKey()) + ":" + anchorEntry.getValue().getIP() + ":" + anchorEntry.getValue().getPORT();
		return destNodeAddr;
	}


	private static Integer KadDHT(Integer anchorID, Integer keyID, KademliaPeer peerCandidate){
		int currentID = anchorID;
		while (true){
			KademliaPeer Peer = onlinePeers.get(currentID);
			if (Peer == null){
				Peer = peerCandidate;
			}
			LinkedHashMap<String, String> routeTable = Peer.getRouteTable();
			// search the routing table
			String target = "__";
			int numX = 0;
			for(Map.Entry<String, String> entry : routeTable.entrySet()){
				String nodeBINs = entry.getKey();
				String prefix = nodeBINs.substring(0, nodeBINs.length()-numX);
				if (numX == Config.BIN_SIZE){ // prefix == ""
					target = entry.getValue();
					break;
				}
				int base = Integer.parseUnsignedInt(prefix, 2) << numX;
				int minVal = base + 0;
				int maxVal = (int) (base + Math.pow(2, numX) - 1);
				if (keyID >= minVal && keyID <= maxVal){
					target = entry.getValue();
					break;
				}
				numX ++;
			}
			if (target.equals("__") || target.equals(String.valueOf(keyID))){  // target is not online
				// found the destination
				System.out.println("Found on node " + String.valueOf(currentID));
				break;
			}else{
				// forwarding to the next node (target)
				System.out.println("Forwarding: from " + String.valueOf(currentID) + " to " + String.valueOf(target));
				currentID = Integer.valueOf(target);
			}
		}
		return currentID;
	}

	public static String ForwardFileData(Integer anchorID, String filedata){

		String fileHash = HashCodeUtil.getFileMD5(filedata);
		System.out.println("File content: \r\n" + filedata + "\r\n" + "filehash=" + fileHash);
		// get the keyID from fileHash
		String keyHex = fileHash.substring(31,32);  // filehash mod 16
		int keyID = Integer.parseInt(keyHex, 16);   // target Key nodeID

		// search for the destination node
		System.out.println("Route message to node " + String.valueOf(keyID) + " from anchor node " + String.valueOf(anchorID));
		System.out.println("----------------------");
		int destNode = KadDHT(anchorID, keyID, null);
		System.out.println("----------------------");

		// insert file data into destination node
		onlinePeers.get(destNode).insert(fileHash, filedata);
		System.out.println("File is inserted into node: " + String.valueOf(destNode));
		return String.valueOf(destNode) + ":" + fileHash;
	}


	public static String SearchFileData(Integer anchorID, String fileHash){

		// get the keyID from fileHash
		String keyHex = fileHash.substring(31,32);  // filehash mod 16
		int keyID = Integer.parseInt(keyHex, 16);   // target Key nodeID

		// search for the destination node
		System.out.println("Lookup key=" + String.valueOf(keyID) + " from anchor node " + String.valueOf(anchorID));
		System.out.println("----------------------");
		int destNode = KadDHT(anchorID, keyID, null);
		System.out.println("----------------------");
		
		// lookup file data on destination node
		String fileData = onlinePeers.get(destNode).lookup(fileHash);
		System.out.println("File is found on node: " + String.valueOf(destNode));
		return fileData;
	}

	/**
	 * The port that the server listens on.
	 */
	private static final int PORT = Integer.parseInt(Config.SERVER_PORT);

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for a dealing with a single client
	 * and broadcasting its messages.
	 */
	private static class Handler extends Thread {
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private Dispatcher dispatcher;

		/**
		 * Constructs a handler thread, squirreling away the socket.
		 * All the interesting work is done in the run method.
		 */
		public Handler(Socket socket) {
			this.socket = socket;

			// Create a new JSON-RPC 2.0 request dispatcher
			this.dispatcher =  new Dispatcher();

			// Register the "echo", "getDate" and "getTime" handlers with it
			dispatcher.register(new JsonHandler.MiniServerHandler());
		}

		/**
		 * Services this thread's client by repeatedly requesting a
		 * screen name until a unique one has been submitted, then
		 * acknowledges the name and registers the output stream for
		 * the client in a global set, then repeatedly gets inputs and
		 * broadcasts them.
		 */
		public void run() {
			try {
				// Create character streams for the socket.
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				// read request
				String line;
				line = in.readLine();
				//System.out.println(line);
				StringBuilder raw = new StringBuilder();
				raw.append("" + line);
				boolean isPost = line.startsWith("POST");
				int contentLength = 0;
				while (!(line = in.readLine()).equals("")) {
					//System.out.println(line);
					raw.append('\n' + line);
					if (isPost) {
						final String contentHeader = "Content-Length: ";
						if (line.startsWith(contentHeader)) {
							contentLength = Integer.parseInt(line.substring(contentHeader.length()));
						}
					}
				}
				StringBuilder body = new StringBuilder();
				if (isPost) {
					int c = 0;
					for (int i = 0; i < contentLength; i++) {
						c = in.read();
						body.append((char) c);
					}
				}
				
				JSONRPC2Request request = JSONRPC2Request.parse(body.toString());
				JSONRPC2Response resp = dispatcher.process(request, null);
				// print the online node
				if (request.getMethod().equals("online")){
					Map<String, Object> myParams = request.getNamedParams();
					Object id = myParams.get("id");
					Object ip = myParams.get("ip");
					Object port = myParams.get("port");
					Object isAnchor = myParams.get("isAnchor");
					String msg = "Peer " + String.valueOf(id) + " is online, IP=" + String.valueOf(ip) + ", PORT=" + String.valueOf(port);
					if ((boolean)isAnchor){
						msg += ", (anchor).";
					}
					System.out.println(msg);
				}else if (request.getMethod().equals("offline")){
					Map<String, Object> myParams = request.getNamedParams();
					Object id = myParams.get("id");
					Object ip = myParams.get("ip");
					Object port = myParams.get("port");
					System.out.println("Peer " + String.valueOf(id) + " is offline, IP=" + String.valueOf(ip) + ", PORT=" + String.valueOf(port));
				}

				// send response
				out.write("HTTP/1.1 200 OK\r\n");
				out.write("Content-Type: application/json\r\n");
				out.write("\r\n");
				out.write(resp.toJSONString());
				// do not in.close();
				out.flush();
				out.close();
				socket.close();
			} catch (IOException e) {
				System.out.println(e);
			} catch (JSONRPC2ParseException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

	
	public static void main(String[] args) throws Exception {

		System.out.println("The server is running.");
		ServerSocket listener = new ServerSocket(PORT);
		try {
			while (true) {
				new Handler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}
	
	
}