package edu.rit.cs;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KademliaPeer implements Serializable {
    enum STATUS {
        ONLINE, OFFLINE;
    }

    private int nodeID;
    private String ipAddress;
    private String port;
    private boolean isAnchor;
    private LinkedHashMap<String, String> routeTable;
    private LinkedHashMap<String, String> fileDB;

    private int requestID = -1;
    static final long serialVersionUID = 1L;

    public KademliaPeer(int nodeID, String ipAddress, String port, boolean isAnchor) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        this.isAnchor = isAnchor;
        this.routeTable = new LinkedHashMap<String, String>();
        this.fileDB = new LinkedHashMap<String, String>();
    }

    public boolean startPeer() {
        return tellMiniServer(STATUS.ONLINE);
    }

    public boolean stopPeer() {
        return tellMiniServer(STATUS.OFFLINE);
    }

    private boolean tellMiniServer(STATUS status) {
        // Creating a new session to a JSON-RPC 2.0 web service at a specified URL
        // The JSON-RPC 2.0 server URL
        URL serverURL = null;

        try {
            serverURL = new URL("http://" + Config.SERVER_IP + ":" + Config.SERVER_PORT);
        } catch (MalformedURLException e) {
            // handle exception...
            e.printStackTrace();
        }

        // Create new JSON-RPC 2.0 client session
        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

        // Construct new request
        JSONRPC2Request request;
        requestID++;
        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("id", this.nodeID);
        switch (status) {
            case ONLINE:
                request = new JSONRPC2Request("online", requestID);
                myParams.put("ip", this.ipAddress);
                myParams.put("port", this.port);
                myParams.put("isAnchor", this.isAnchor);
                break;
            case OFFLINE:
            default:
                request = new JSONRPC2Request("offline", requestID);
                break;
        }
        request.setNamedParams(myParams);

        // Send request
        JSONRPC2Response response = null;
        try {
            response = mySession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
            // handle exception...
        }

        // Print response result / error
        if (response.indicatesSuccess()) {
            boolean isSuccess = (Boolean) response.getResult();
            return isSuccess;
        } else {
            System.out.println(response.getError().getMessage());
            return false;
        }

    }

    public int getNodeID() {
        return this.nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public String getIP(){
        return this.ipAddress;
    }

    public String getPORT(){
        return this.port;
    }

    public boolean getAnchorState() {
        return isAnchor;
    }

    public LinkedHashMap<String, String> getFileDB() {
        return this.fileDB;
    }

    public void insert(String hashCode, String fileData) {
        if (this.getFileDB().containsKey(hashCode)){  // file already exists.
            return;
        }
        this.getFileDB().put(hashCode, fileData);
    }

    public String lookup(String hashCode) {
        return this.getFileDB().get(hashCode);
    }

    private String getBits(Integer number, Integer binSize) {
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < binSize; i++) {
            sBuilder.append(number & 1);
            number = number >>> 1;
        }
        return sBuilder.reverse().toString();
    }

    public LinkedHashMap<String, String> getRouteTable() {
        return this.routeTable;
    }

    public void updateRouteTable(HashSet<Integer> onlineNodes, Integer binSize) {
        // create or update Routing Table, by default, binSize=4 (=log16)
        LinkedHashMap<String, String> routeTable = new LinkedHashMap<>();
        String nodeBins = getBits(this.nodeID, binSize); // e.g., this.nodeID=2, nodeBins=0010
        routeTable.put(nodeBins, String.valueOf(this.nodeID)); // self node
        // add forwarding nodes by XOR distance
        String pattern = "";
        String suffix = "";
        for (int i = binSize; i > 0; i--) {
            String prefix = nodeBins.substring(0, i - 1); // 001, 00, 0, ""
            String last_bit = nodeBins.substring(i - 1, i); // 0, 1, 0, 0
            String bit_reverse = String.valueOf(1 - Integer.valueOf(last_bit)); // 1, 0, 1, 1
            int num_base = Integer.parseUnsignedInt(prefix + bit_reverse, 2) << (binSize - i); // 3, 0, 4, 8
            suffix = suffix + "x"; // x, xx, xxx, xxxx
            pattern = prefix + suffix; // 001x, 00xx, 0xxx, xxxx
            // [3,3], [0,1], [4,7], [8,15]
            int maxNode = (int) (num_base + Math.pow(2, binSize - i) - 1);
            int minNode = num_base;
            // find online node
            List<Integer> viewedNodes = new ArrayList<>();
            Iterator<Integer> iterator = onlineNodes.iterator();
            while (iterator.hasNext()) {
                int cur_nodeID = iterator.next();
                if (cur_nodeID >= minNode && cur_nodeID <= maxNode) {
                    viewedNodes.add(cur_nodeID);
                }
            }

            if (viewedNodes.size() > 0) {
                // use XOR distance to find the nearest viewed node
                int votedNode = viewedNodes.get(0);
                double minDist = Math.pow(2, binSize);
                for (int j = 0; j < viewedNodes.size(); j++) {
                    // compute XOR distance
                    double dist = this.nodeID ^ viewedNodes.get(j);
                    if (dist < minDist) {
                        minDist = dist;
                        votedNode = viewedNodes.get(j);
                    }
                }
                routeTable.put(pattern, String.valueOf(votedNode));
            } else {
                routeTable.put(pattern, "__");
            }

        }
        this.routeTable = routeTable;
    }

}
