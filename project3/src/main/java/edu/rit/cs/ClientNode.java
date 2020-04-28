package edu.rit.cs;

import java.util.Scanner;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ClientNode {

    private static String requestFileTrans(String ipAddress, String port){
        URL serverURL = null;
        try {
            serverURL = new URL("http://" + Config.SERVER_IP+":"+Config.SERVER_PORT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Create new JSON-RPC 2.0 client session
        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
        // Construct new request
        JSONRPC2Request request;
        // generate a random request ID
        Random rand = new Random();
        rand.setSeed(1234);
        request = new JSONRPC2Request("transRequest", rand.nextInt(10));

        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("ip", ipAddress);
        myParams.put("port", port);
        request.setNamedParams(myParams);

        // Send request
        JSONRPC2Response response = null;
        try {
            response = mySession.send(request);
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


    private static String uploadFileData(String dataFile, int anchorID){
        URL serverURL = null;
        try {
            serverURL = new URL("http://" + Config.SERVER_IP+":"+Config.SERVER_PORT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Create new JSON-RPC 2.0 client session
        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
        // Construct new request
        JSONRPC2Request request;
        // generate a random request ID
        Random rand = new Random();
        rand.setSeed(1234);
        request = new JSONRPC2Request("uploadFileData", rand.nextInt(10));

        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("anchor", anchorID);
        myParams.put("file", dataFile);
        request.setNamedParams(myParams);

        // Send request
        JSONRPC2Response response = null;
        try {
            response = mySession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }

        // Print response result / error
        try{
            if (response.indicatesSuccess()){
                return (String)response.getResult();
            }else{
                return null;
            }
        }catch (NullPointerException e){
            System.out.println("Mini-server is not working well!");
            System.out.println(response.getError().getMessage());
        }
        return null;
    }

    private static void uploadFile(String ipAddress, Integer port, Scanner scanner) {
        // request file upload
        String anchorAddr = requestFileTrans(ipAddress, String.valueOf(port));
        if (anchorAddr!=null && anchorAddr != ""){
            // parse the IP and PORT from the response
            String anchorID = anchorAddr.split(":")[0];
            String anchorIP = anchorAddr.split(":")[1];
            System.out.println("The destination anchor: " + String.valueOf(anchorID) + ", IP=" + anchorIP);
            // input the filepath
            System.out.println("Enter the file path:");
            String filePath = scanner.nextLine().trim();
            File dataFile = new File(filePath);
            if (dataFile.exists()){
                String fileData = readTextFile(dataFile);
                String fileAddress = uploadFileData(fileData, Integer.valueOf(anchorID));
                String destNode = fileAddress.split(":")[0];
                String fileHash = fileAddress.split(":")[1];
                System.out.println("File is stored in peer (node=" + destNode + "), file hashcode: " + fileHash);
            }else{
                System.out.println("File does not exists!");
            }
        }else{
            System.out.println("Failed to request uploading!");
        }
    }

    private static String downloadFileData(Integer anchorID, String fileHash){
        URL serverURL = null;
        try {
            serverURL = new URL("http://" + Config.SERVER_IP+":"+Config.SERVER_PORT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Create new JSON-RPC 2.0 client session
        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
        // Construct new request
        JSONRPC2Request request;
        // generate a random request ID
        Random rand = new Random();
        rand.setSeed(1234);
        request = new JSONRPC2Request("downloadFileData", rand.nextInt(10));

        Map<String, Object> myParams = new HashMap<String, Object>();
        myParams.put("anchor", anchorID);
        myParams.put("fileHash", fileHash);
        request.setNamedParams(myParams);

        // Send request
        JSONRPC2Response response = null;
        try {
            response = mySession.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
        }

        // Print response result / error
        try{
            if (response.indicatesSuccess()){
                return (String)response.getResult();
            }else{
                return null;
            }
        }catch (NullPointerException e){
            System.out.println("Mini-server is not working well!");
            System.out.println(response.getError().getMessage());
        }
        return null;
    }

    private static void downloadFile(String ipAddress, Integer port, Scanner scanner) {
        // request file upload
        String anchorAddr = requestFileTrans(ipAddress, String.valueOf(port));
        if (anchorAddr!=null && anchorAddr != ""){
            // input the filepath
            System.out.println("Enter the file hash:");
            String fileHash = scanner.nextLine().trim();
            // check if the filehash is valid
            boolean isValid = HashCodeUtil.checkHash(fileHash);
            if (!isValid){
                System.out.println("Invalid file hash! Try again!");
                return;
            }
            int anchorID = Integer.valueOf(anchorAddr.split(":")[0]);
            String fileData = downloadFileData(anchorID, fileHash);
            if (fileData != null){
                System.out.println("Download the file successfully! Stored in ./resources/downloaded.txt.");
                writeTextFile("./resources/downloaded.txt", fileData);
            }else{
                System.out.println("Failed to request downloading!");
            }
        } else {
            System.out.println("Failed to request downloading!");
        }
    }

    private static String readTextFile(File file) {
        String filedata = "";
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                filedata += data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filedata;
    }

    public static void writeTextFile(String filePath, String fileData) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(fileData.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String ip_port = args[0];
        String URL = ip_port.split(":")[0];
        int PORT = Integer.parseInt(ip_port.split(":")[1]);

        Scanner scanner = new Scanner(System.in);
        boolean endop = false;
        do {
            System.out.println("================ Please choose [ID] of each following operations =====================");
            System.out.println("[0]: Upload file to P2P network.");
            System.out.println("[1]: Get file from P2P network.");
            System.out.println("[2]: Exit.");
            int ID = -1;
            try{
                ID = scanner.nextInt();
                scanner.nextLine();
            }catch(Exception e){
                scanner.nextLine();
            }
            switch(ID){
                case 0: uploadFile(URL, PORT, scanner); break;
                case 1: downloadFile(URL, PORT, scanner); break;
                case 2: endop = true; break;
                default: System.out.println("Invalid choice! Please choose digits from 0 to 2.");
            }
        } while(!endop);
    }

}