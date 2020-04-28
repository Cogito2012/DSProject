package edu.rit.cs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class ServerAgent {

    JSONRPC2Session serverSession;

    private int requestID = -1;
    public ServerAgent() {
        URL serverURL = null;
        try {
            serverURL = new URL("http://" + Config.SERVER_IP + ":" + Config.SERVER_PORT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.serverSession = new JSONRPC2Session(serverURL);
    }

    private static void printRouteTable(Map<String, String> routeTable, Integer nodeID){
        System.out.println("---- Routing table (node=" + String.valueOf(nodeID) + ")----");
        routeTable.forEach((k, v) -> System.out.println(k + " --> " + v));
        System.out.println("--------------------------------------");
    }

    private boolean tellMiniServer(String method, Integer nodeID, String ipAddress, String port, boolean isAnchor){
        
        // Construct new request
        JSONRPC2Request request;
        requestID++;
        if (method.equals("online")){
            request = new JSONRPC2Request("online", requestID);
        }else if (method.equals("offline")){
            request = new JSONRPC2Request("offline", requestID);
        }else{
            request = new JSONRPC2Request("offline", requestID);
        }
        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("id", nodeID);
        myParams.put("ip", ipAddress);
        myParams.put("port", port);
        myParams.put("isAnchor", isAnchor);
        request.setNamedParams(myParams);
        // Send request
        JSONRPC2Response response = null;
        try {
            response = this.serverSession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }
        // Print response result / error
        try{
            if (response.indicatesSuccess()){
                return (boolean)response.getResult();
            }
        }catch (NullPointerException e){
            System.out.println("Mini-server is not working well!");
            System.out.println(response.getError().getMessage());
        }
        return false;
    }

    private String makeIPAddress(Integer peerID){
        String ipAddress = "";
        String[] IPs = Config.DEFAULT_PEER_IP.split("\\.");
        for (int i = 0; i < IPs.length - 1; i++) {
            ipAddress += "." + IPs[i];
        }
        ipAddress = ipAddress.substring(1, ipAddress.length());
        String last_section = String.valueOf(Integer.valueOf(IPs[IPs.length - 1]) + peerID);
        ipAddress += "." + last_section;
        return ipAddress;
    }

    private String getOnlinePeers(){
        // Construct new request
        JSONRPC2Request request;
        requestID++;
        request = new JSONRPC2Request("onlinePeers", requestID);
        // Send request
        JSONRPC2Response response = null;
        try {
            response = this.serverSession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }
        // Print response result / error
        try{
            if (response.indicatesSuccess()){
                return (String)response.getResult();
            }
        }catch (NullPointerException e){
            System.out.println("Mini-server is not working well!");
            System.out.println(response.getError().getMessage());
        }
        return null;
    }

    private boolean isAnchorPeer(int peerID){
        // Construct new request
        JSONRPC2Request request;
        requestID++;
        request = new JSONRPC2Request("isAnchorPeer", requestID);
        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("id", peerID);
        request.setNamedParams(myParams);
        // Send request
        JSONRPC2Response response = null;
        try {
            response = this.serverSession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }
        // Print response result / error
        try{
            if (response.indicatesSuccess()){
                return (boolean)response.getResult();
            }
        }catch (NullPointerException e){
            System.out.println("Mini-server is not working well!");
            System.out.println(response.getError().getMessage());
        }
        return false;
    }

    private LinkedHashMap<String, String> getRouteTable(int peerID){
        // Construct new request
        JSONRPC2Request request;
        requestID++;
        request = new JSONRPC2Request("getRouteTable", requestID);
        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("id", peerID);
        request.setNamedParams(myParams);
        // Send request
        JSONRPC2Response response = null;
        try {
            response = this.serverSession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }
        // Print response result / error
        try{
            if (response.indicatesSuccess()){
                String tableStr = (String)response.getResult();
                String[] tableItems = tableStr.split(",");
                LinkedHashMap<String, String> routeTable = new LinkedHashMap<>();
                for (int i=0; i<tableItems.length; i++){
                    routeTable.put(tableItems[i].split(":")[0], tableItems[i].split(":")[1]);
                }
                return routeTable;
            }
        }catch (NullPointerException e){
            System.out.println("Mini-server is not working well!");
            System.out.println(response.getError().getMessage());
        }
        return null;
    }

    private void createP2PNet(Scanner scanner){

        System.out.println("Enter Kademlia node IDs: (Use space as seperator)");
        String[] nodeIDs = scanner.nextLine().trim().split("\\s+");
        System.out.println("Specify Anchor node IDs: (Use space as seperator)");
        List<String> anchorIDs = Arrays.asList(scanner.nextLine().trim().split("\\s+"));

        // check if the anchors are valid
        List<String> nodeList = Arrays.asList(nodeIDs);
        for (int i=0; i<anchorIDs.size(); i++){
            if (!nodeList.contains(anchorIDs.get(i))){
                System.out.println("Invalid anchor node IDs!");
                return;
            }
        }

        boolean isSuccess = true;
        for (int i=0; i<nodeIDs.length; i++){
            // if the current node is anchor
            boolean isAnchor = false;
            if (anchorIDs.contains(nodeIDs[i])){
                isAnchor = true;
            }
            // Tell server to set online nodes
            int peerID = Integer.valueOf(nodeIDs[i]);
            // here we use psudo IP and port for each peer
            String ipAddress = makeIPAddress(peerID);
            String port = String.valueOf(Integer.valueOf(Config.DEFAULT_PORT_PREFIX) + peerID);
            boolean response = tellMiniServer("online", peerID, ipAddress, port, isAnchor);
            if (!response){
                isSuccess = false;
            }
        }
        if (isSuccess){
            System.out.println("Online Kademlia nodes: ");
            String onlinePeerIDs = getOnlinePeers();
            if (onlinePeerIDs==null){
                System.out.println("No online peers!");
                return;
            }
            System.out.println(onlinePeerIDs);
            // get the Route Table for each current online node
            List<String> strList = Arrays.asList(onlinePeerIDs.split(","));
            for (int i=0; i<strList.size(); i++){
                int peerID = Integer.valueOf(strList.get(i));
                LinkedHashMap<String, String> routeTable = getRouteTable(peerID);
                if (routeTable != null){
                    printRouteTable(routeTable, peerID);
                }else{
                    System.out.println("Failed to get routing table of peer " + String.valueOf(peerID));
                }
            }
        } 
    }

    private void addNode(Scanner scanner){

        // aquire online nodes from MiniServer
        String onlinePeerIDs = getOnlinePeers();
        if (onlinePeerIDs==null){
            System.out.println("No existing online nodes! Choose to create a P2P network first!");
            return;
        }
        // parse the online node IDs
        List<Integer> existingNodes = new ArrayList<>();
        List<String> strList = Arrays.asList(onlinePeerIDs.split(","));
        for (int i=0; i<strList.size(); i++){
            existingNodes.add(Integer.valueOf(strList.get(i)));
        }

        System.out.println("Enter a new node ID:");
        int node = Integer.valueOf(scanner.nextLine().trim());
        // process add Node
        if (existingNodes.contains(node)){
            System.out.println("Node " + String.valueOf(node) + " is already online.");
            return;
        }else{
            String ipAddress = makeIPAddress(node);
            String port = String.valueOf(Integer.valueOf(Config.DEFAULT_PORT_PREFIX) + node);
            boolean isSuccess = tellMiniServer("online", node, ipAddress, port, false);
            if (isSuccess){
                // print message 
                System.out.println("Online Kademlia nodes: ");
                onlinePeerIDs = getOnlinePeers();
                if (onlinePeerIDs==null){
                    System.out.println("No online peers!");
                    return;
                }
                System.out.println(onlinePeerIDs);
                // get the Route Table for each current online node
                strList = Arrays.asList(onlinePeerIDs.split(","));
                for (int i=0; i<strList.size(); i++){
                    int peerID = Integer.valueOf(strList.get(i));
                    LinkedHashMap<String, String> routeTable = getRouteTable(peerID);
                    if (routeTable != null){
                        printRouteTable(routeTable, peerID);
                    }else{
                        System.out.println("Failed to get routing table of peer " + String.valueOf(peerID));
                    }
                }
            }else{
                System.out.println("Failed to add the node " + String.valueOf(node));
            }
        }
    }

    private void removeNode(Scanner scanner){

        // aquire online nodes from MiniServer
        String onlinePeerIDs = getOnlinePeers();
        if (onlinePeerIDs==null){
            System.out.println("No existing online nodes! Choose to create a P2P network first!");
            return;
        }
        // parse the online node IDs
        List<Integer> existingNodes = new ArrayList<>();
        List<String> strList = Arrays.asList(onlinePeerIDs.split(","));
        for (int i=0; i<strList.size(); i++){
            existingNodes.add(Integer.valueOf(strList.get(i)));
        }

        System.out.println("Existing online nodes: ");
        System.out.println(onlinePeerIDs);
        
        System.out.println("Enter an existing node ID:");
        int node = Integer.valueOf(scanner.nextLine().trim());
        // remove
        if (!existingNodes.contains(node)){
            System.out.println("Node " + String.valueOf(node) + " is already offline.");
            return;
        }else{
            if (isAnchorPeer(node)){
                System.out.println("Node " + String.valueOf(node) + " is an anchor, cannnot be removed!");
                return;
            }
            String ipAddress = makeIPAddress(node);
            String port = String.valueOf(Integer.valueOf(Config.DEFAULT_PORT_PREFIX) + node);
            boolean isSuccess = tellMiniServer("offline", node, ipAddress, port, false);
            if (isSuccess){
                // print message 
                System.out.println("Online Kademlia nodes: ");
                onlinePeerIDs = getOnlinePeers();
                if (onlinePeerIDs==null){
                    System.out.println("No online peers!");
                    return;
                }
                System.out.println(onlinePeerIDs);
                // get the Route Table for each current online node
                strList = Arrays.asList(onlinePeerIDs.split(","));
                for (int i=0; i<strList.size(); i++){
                    int peerID = Integer.valueOf(strList.get(i));
                    LinkedHashMap<String, String> routeTable = getRouteTable(peerID);
                    if (routeTable != null){
                        printRouteTable(routeTable, peerID);
                    }else{
                        System.out.println("Failed to get routing table of peer " + String.valueOf(peerID));
                    }
                }
            }else{
                System.out.println("Failed to remove the node " + String.valueOf(node));
            }
        }
        
        
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean endop = false;

        ServerAgent agent = new ServerAgent();
        do {
            System.out.println("================ Please choose operation [ID] =====================");
            System.out.println("[0]: Create a P2P network.");
            System.out.println("[1]: Add a new node.");
            System.out.println("[2]: Remove an existing node.");
            System.out.println("[3]: Exit.");
            int ID = -1;
            try{
                ID = scanner.nextInt();
                scanner.nextLine();
            }catch(Exception e){
                scanner.nextLine();
            }
            switch(ID){
                case 0: agent.createP2PNet(scanner); break;
                case 1: agent.addNode(scanner); break;
                case 2: agent.removeNode(scanner); break;
                case 3: endop = true; break;
                default: System.out.println("Invalid choice! Please choose digits from 0 to 2.");
            }
        } while(!endop);

    }
}