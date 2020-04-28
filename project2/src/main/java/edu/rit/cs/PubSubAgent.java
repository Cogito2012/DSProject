package edu.rit.cs;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.*;
import java.io.*;

public class PubSubAgent implements Publisher, Subscriber{

	// The maximum number of times that a publisher cound repeatedly send data to server
	public static final int MAX_NUM_ADV = 5;
	public static final int MAX_NUM_PUB = 5;
	public static final int MAX_NUM_SUB = 5;
	public static final int MAX_TRIES = 5;
	public static final int ID_CONNECTION_ACTION = 0;
	public static String URL;
	public int PORT;
	public User currentUser;

	public PubSubAgent(String url, int port){
		this.URL = url;
		this.PORT = port;
		this.currentUser = null;
	}

	protected EventManager eventManager;

	@Override
	public void subscribe(Topic topic) {
		if (topic == null){
			return;
		}
		// TODO Auto-generated method stub
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success) {
			new Thread(new Runnable() {
				public void run() {
					int numSubs = 0;
					while (++numSubs < MAX_NUM_SUB) {
						try {
							boolean success = exeSubscribe(topic);
							if (!success) {
								System.err.println("Failed to subscribe topics!");
							}else{
								System.out.println("The topic is successfully subscribed!");
							}
							return;
						}catch (Exception e){
							System.out.println("Try to subscribe to the topic again after one second.");
							try {
								Thread.sleep(1000);
							}catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					System.out.println("Cannot connect with server to subscribe!");
				}
			}).start();
		}else{
			System.out.println("Connection failed!");
		}
	}

	@Override
	public void subscribe(String keyword) {
		// TODO Auto-generated method stub
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success) {
			new Thread(new Runnable() {
				public void run() {
					int numSubs = 0;
					while (++numSubs < MAX_NUM_SUB) {
						try {
							boolean success = exeSubscribe(keyword);
							if (!success) {
								System.err.println("Failed to subscribe topics!");
							}
							return;
						}catch (Exception e){
							System.out.println("Try to subscribe to keyword again after one second.");
							try {
								Thread.sleep(1000);
							}catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					System.out.println("Cannot connect with server to subscribe!");
				}
			}).start();
		}else{
			System.out.println("Connection failed!");
		}
	}

	public boolean exeSubscribe(Topic topic){
		Socket socket = null;
		try{
			socket = new Socket(URL, PORT);
			ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject("subTopic:" + topic.getName() + ",user:" + currentUser.getName() + ",roleID:" + currentUser.getRoleID());

			// get the response from server
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			boolean response = (boolean) inputStream.readObject();

			outputStream.close();
			inputStream.close();
			return response;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean exeUnsubscribe(Topic topic){
		Socket socket = null;
		try{
			socket = new Socket(URL, PORT);
			ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject("unsubTopic:" + topic.getName() + ",user:" + currentUser.getName() + ",roleID:" + currentUser.getRoleID());

			// get the response from server
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			boolean response = (boolean) inputStream.readObject();

			outputStream.close();
			inputStream.close();
			return response;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean exeUnsubscribeAll(){
		Socket socket = null;
		try{
			socket = new Socket(URL, PORT);
			ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject("unsubAll:None" + ",user:" + currentUser.getName() + ",roleID:" + currentUser.getRoleID());

			// get the response from server
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			boolean response = (boolean) inputStream.readObject();

			outputStream.close();
			inputStream.close();
			return response;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean exeSubscribe(String keyword){
		Socket socket = null;
		try{
			socket = new Socket(URL, PORT);
			ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject("subKeyword:" + keyword + ",user:" + currentUser.getName() + ",roleID:" + currentUser.getRoleID());

			// get the response from server
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			String[] response = (String[]) inputStream.readObject();
			System.out.println("Following topics are subscribed!");
			for (int i=0; i<response.length; i++)
				System.out.println(response[i]);

			outputStream.close();
			inputStream.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void unsubscribe(Topic topic) {
		// TODO Auto-generated method stub
		if (topic == null){
			return;
		}
		// TODO Auto-generated method stub
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success) {
			new Thread(new Runnable() {
				public void run() {
					int numSubs = 0;
					while (++numSubs < MAX_NUM_SUB) {
						try {
							boolean success = exeUnsubscribe(topic);
							if (!success) {
								System.err.println("Failed to ubsubscribe topics!");
							}else{
								System.out.println("The topic is successfully unsubscribed!");
							}
							return;
						}catch (Exception e){
							System.out.println("Try to unsubscribe the topic again after one second.");
							try {
								Thread.sleep(1000);
							}catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					System.out.println("Cannot connect with server to subscribe!");
				}
			}).start();
		}else{
			System.out.println("Connection failed!");
		}
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success) {
			new Thread(new Runnable() {
				public void run() {
					int numSubs = 0;
					while (++numSubs < MAX_NUM_SUB) {
						try {
							boolean success = exeUnsubscribeAll();
							if (!success) {
								System.err.println("Failed to ubsubscribe all topics!");
							}else{
								System.out.println("All topics are successfully unsubscribed!");
							}
							return;
						}catch (Exception e){
							System.out.println("Try to unsubscribe all topics again after one second.");
							try {
								Thread.sleep(1000);
							}catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					System.out.println("Cannot connect with server to subscribe!");
				}
			}).start();
		}else{
			System.out.println("Connection failed!");
		}
	}

	@Override
	public void listSubscribedTopics() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(Event event) {
		// TODO Auto-generated method stub
		if (event == null){
			return;
		}
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success) {
			new Thread(new Runnable() {
				public void run() {
					int numPubs = 0;
					while (++numPubs < MAX_NUM_PUB) {
						try {
							// sent event data to server
							boolean success = sendEvent(event);
							if (!success) {
								System.err.println("Failed to publish event!");
							}else{
								System.out.println("Event is successfully stored in server!");
							}
							return;
						}catch (Exception e){
							System.out.println("Try to publish event again after one second.");
							try {
								Thread.sleep(1000);
							}catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					System.out.println("Cannot connect with server to publish the event!");
				}
			}).start();
		}else{
			System.out.println("Connection failed!");
		}
	}

	public boolean sendEvent(Event event){
		Socket socket = null;
		try {
			socket = new Socket(URL, PORT);
			ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject(event);

			// get the response from server
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			boolean response = (boolean) inputStream.readObject();

			outputStream.close();
			inputStream.close();
			return response;
		}catch(Exception e){
			return false;
		}

	}

	public Topic retrieveTopic(String topicStr){
		Topic topic = null;
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success){
			Socket socket = null;
			try{
				// sent topics data to server
				socket = new Socket(URL, PORT);
				ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
				outputStream.writeObject("retrieveTopic:" + topicStr);

				// get results from server
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				topic = (Topic) inputStream.readObject();

				outputStream.close();
				inputStream.close();
				return topic;
			}catch (Exception exception){
				return null;
			}
		}else{
			System.out.println("Connection failed!");
			return null;
		}
	}


	public boolean buildConnection(String url, int port, int action_id){
		int numConnect = 0;
		while (++numConnect < MAX_TRIES) {
			Socket socket = null;
			try {
				socket = new Socket(URL, PORT);
				ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
				outputStream.writeObject(action_id);

				// get the response from server
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				boolean response = (boolean) inputStream.readObject();

				outputStream.close();
				inputStream.close();
				return response;
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		System.out.println("Timeout!");
		return false;
	}

	@Override
	public void advertise(Topic topic) {
		// TODO Auto-generated method stub
		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success){
			new Thread(new Runnable() {
				public void run(){
					int numAds = 0;
					while (++numAds < MAX_NUM_ADV){
						try{
							boolean success = sendTopic(topic);
							if (!success) {
								System.err.println("Topic already exists on database");
							}else{
								System.out.println("Topic is successfully stored in server!");
							}
							return;
						}catch (Exception e){
							System.out.println("Try to advertise again after one second.");
							try {
								Thread.sleep(1000);
							}catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					System.out.println("Cannot connect with server to advertise the topic!");
				}
			}).start();
		}else{
			System.out.println("Connection failed!");
		}
	}

	public boolean sendTopic(Topic topic){
		Socket socket = null;
		try{
			socket = new Socket(URL, PORT);
			ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject(topic);

			// get the response from server
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			boolean response = (boolean) inputStream.readObject();

			outputStream.close();
			inputStream.close();
			return response;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public void login(User user){

		boolean success = buildConnection(URL, PORT, ID_CONNECTION_ACTION);
		if (success){
			System.out.println("Connected!");
			Socket socket = null;
			try{
				socket = new Socket(URL, PORT);
				ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
				outputStream.writeObject(user);

				// get the response from server
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				Integer roleID = (Integer)inputStream.readObject();
				System.out.println("Your ID from server is: " + roleID);
				// update user's roleID
				synchronized (roleID){
					user.setRoleID(roleID);
					this.currentUser = user;
				}

				outputStream.close();
				inputStream.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			System.out.println("Connection failed!");
		}
	}


}
