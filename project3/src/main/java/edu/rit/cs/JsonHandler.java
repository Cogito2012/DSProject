package edu.rit.cs;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

import java.util.Map;

public class JsonHandler {

	 public static class MiniServerHandler implements RequestHandler {
	     // Reports the method names of the handled requests
		public String[] handledRequests() {
		    return new String[]{"online", "offline", "ispeeronline", "onlinePeers","isAnchorPeer", "getRouteTable", "transRequest", "uploadFileData", "downloadFileData"};
		}

		// Processes the requests
		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
			Map<String, Object> myParams = req.getNamedParams();
		    if (req.getMethod().equals("online")) {
				// handle peers online
				long id = ((Number) myParams.get("id")).longValue();
				Object ip = myParams.get("ip");
				Object port = myParams.get("port");
				Object isAnchor = myParams.get("isAnchor");
				MiniServer.addPeer((int)id, ip.toString(), port.toString(), (boolean)isAnchor);
				return new JSONRPC2Response(true, req.getID());
	        } else if (req.getMethod().equals("offline")) {
				// handle peers offline
				long id = ((Number) myParams.get("id")).longValue();
	         	MiniServer.removePeer((int)id);
				return new JSONRPC2Response(true, req.getID());
	        } else if (req.getMethod().equals("ispeeronline")) {
				// find if a peer is online or not
				long id = ((Number) myParams.get("id")).longValue();
				return new JSONRPC2Response(MiniServer.isPeerOnline((int)id), req.getID());
			} else if (req.getMethod().equals("onlinePeers")){
				// count the number of online peers
				String onlinePeerIDs = MiniServer.getOnlinePeers();
				return new JSONRPC2Response(onlinePeerIDs, req.getID());
			} else if (req.getMethod().equals("isAnchorPeer")){
				// if the peer is anchor or not
				long id = ((Number) myParams.get("id")).longValue();
				boolean isAnchor = MiniServer.isAnchorPeer((int)id);
				return new JSONRPC2Response(isAnchor, req.getID());
			} else if (req.getMethod().equals("getRouteTable")){
				// get the routing table for given peerID
				long id = ((Number) myParams.get("id")).longValue();
				String table = MiniServer.getRouteTable((int)id);
				return new JSONRPC2Response(table, req.getID());
			} else if (req.getMethod().equals("transRequest")){
				// handle the request to upload file
				Object ip = myParams.get("ip");
				Object port = myParams.get("port");
				String destNodeAddr = MiniServer.FindNearestAnchor(ip.toString(), port.toString());
				return new JSONRPC2Response(destNodeAddr, req.getID());
			} else if (req.getMethod().equals("uploadFileData")){
				// handle the uploading
				long anchorID = ((Number) myParams.get("anchor")).longValue();
				Object filedata = myParams.get("file");
				String fileAddress = MiniServer.ForwardFileData((int)anchorID, filedata.toString());
				return new JSONRPC2Response(fileAddress, req.getID());
			} else if (req.getMethod().equals("downloadFileData")){
				// handle file downloading
				long anchorID = ((Number) myParams.get("anchor")).longValue();
				Object fileHash = myParams.get("fileHash");
				String fileData = MiniServer.SearchFileData((int)anchorID, fileHash.toString());
				return new JSONRPC2Response(fileData, req.getID());
			} else {
				// Method name not supported
				return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
	         }
	     }
	 }
}
