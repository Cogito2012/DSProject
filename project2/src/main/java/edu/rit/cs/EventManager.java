package edu.rit.cs;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class EventManager{
	private ServerSocket serverSocket;

    protected LinkedHashSet<User> UsersDBList;
	protected LinkedHashSet<TopicsDB> TopicsDBList;
	private static boolean SUCCESS = true;
    private static boolean FAILED = false;
    protected int PORT;

    protected Integer eventID = 0;
    protected LinkedList<Event> pendingEvents;

    public EventManager() {
        UsersDBList = new LinkedHashSet<>();
        TopicsDBList = new LinkedHashSet<>();
        pendingEvents = new LinkedList<>();
    }

    /*
	 * Start the repo service
	 */
	private void startService(String[] args) {
	    // start login/register service
            int port = Integer.parseInt(args[0]);
            this.PORT = port;
            try{
                serverSocket = new ServerSocket(port);
                while (true){
                    Socket client  = serverSocket.accept();
                    new ClientHandler(client);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            
	}



    private class ClientHandler implements Runnable {
        private Socket socket;
        public ClientHandler(Socket client){
            this.socket = client;
            new Thread(this).start();
        }

        public void run(){
            try{
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Object obj = inputStream.readObject();
                if (obj instanceof User){
                    // user login
                    System.out.println("Processing user login...");
                    User user = (User)obj;
                    int roleID = addUser(user);

                    ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(roleID);
                    outputStream.close();
                }else if (obj instanceof Topic) {
                    // publisher advertise topic
                    System.out.println("Processing topics advertising...");
                    Topic topic = (Topic) obj;
                    boolean success = addTopic(topic);
                    System.out.println("Topic data: \t" + topic.toString());

                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(success);
                    outputStream.close();
                }else if (obj instanceof Event) {
                    // publisher publish event
                    System.out.println("Processing events publishing...");
                    Event event = (Event) obj;
                    boolean success = addEvent(event);
                    System.out.println("Event data: \t" + event.toString());

                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(success);
                    outputStream.close();
                }else if (obj instanceof String) {
                    // parse the string data to Hashmap
                    String str = (String)obj;
                    Map<String, String> recData = parseReceived(str);
                    if (recData.containsKey("retrieveTopic")){
                        String input_str = recData.get("retrieveTopic");
                        // Retrieve topics
                        System.out.println("Processing topics retrieving...");
                        ArrayList<Topic> topicList = getTopics();
                        Topic topic = searchTopic(topicList, input_str);

                        ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(topic);
                        outputStream.close();
                    }else if (recData.containsKey("subKeyword")) {
                        // parse the received keywords and roleID
                        String keyword = recData.get("subKeyword");
                        int roleID = Integer.valueOf(recData.get("roleID"));
                        // add subscriber
                        System.out.println("Processing topics subscription by keyword (" + keyword + ")...");
                        String[] subscribedTopics = addSubscriberByKeyword(keyword, roleID);
                        // List current subscribers
                        showSubscribersByKeyword(keyword);

                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(subscribedTopics);
                        outputStream.close();
                    }else if (recData.containsKey("subTopic")) {
                        String topicName = recData.get("subTopic");
                        int roleID = Integer.valueOf(recData.get("roleID"));
                        // add subscriber
                        String userName = recData.get("user");
                        System.out.println("Processing topics subscription by name (" + topicName + ")...");
                        boolean success = addSubscriberByName(topicName, roleID);
                        // List current subscribers
                        showSubscribersByName(topicName);

                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(success);
                        outputStream.close();
                    }else if (recData.containsKey("unsubTopic")){
                        String topicName = recData.get("unsubTopic");
                        int roleID = Integer.valueOf(recData.get("roleID"));
                        // remove the subscriber
                        String userName = recData.get("user");
                        System.out.println("Removing subscriber " + userName + " from topic (" + topicName + ")...");
                        boolean success = removeSubscriber(topicName, roleID);
                        // List current subscribers
                        showSubscribersByName(topicName);

                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(success);
                        outputStream.close();
                    }else if (recData.containsKey("unsubAll")){
                        int roleID = Integer.valueOf(recData.get("roleID"));
                        // remove the subscriber
                        String userName = recData.get("user");
                        System.out.println("Removing subscriber " + userName + " from all topics...");
                        boolean success = removeSubscriber(roleID);
                        // List current subscribers
//                        showSubscribersByName(topicName);

                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(success);
                        outputStream.close();
                    }
                }
                else{
                    ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(true);
                    outputStream.close();
                }
                inputStream.close();
            } catch (Exception e){
                System.out.println("Server is not working.");
                e.printStackTrace();
            }finally{
                if (socket != null){
                    try{
                        socket.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Map<String, String> parseReceived(String str){
	    Map<String, String> result = new HashMap<>();
	    String[] pairs = str.split(",");
	    for (int i=0; i<pairs.length; i++){
	        String kv_str = pairs[i];
	        String[] keyValue = kv_str.split(":");
	        result.put(keyValue[0], keyValue[1]);
        }
	    return result;
    }
	/*
	 * notify all subscribers of new event 
	 */
	private int notifySubscribers(Event event) {
        Iterator<Integer> sub_iter = event.iterator();
        System.out.println("Following subscribers will be notified: ");
        int num = 0;
        while (sub_iter.hasNext()) {
            Integer subID = sub_iter.next();
            // how to notify event to current subscriber subID??
            System.out.println(subID);
            sub_iter.remove();
            num ++;
        }
        if (num == 0){
            System.out.println("None");
        }
        //when this returns 0, we know every subscriber has received the message
        return event.notifySize();
	}

	public boolean addEvent(Event event){
        if (event.getID() != 0) {
            System.err.println("Event has already been published.");
            return false;
        }
        synchronized (TopicsDBList) {
            for (TopicsDB topicsDB : TopicsDBList){
                if (topicsDB.getTopic().getID() == event.getTopic().getID()){
                    event.setID(++eventID).addSubscriberList(topicsDB.getSubscribers());
                    if (notifySubscribers(event) > 0) {
                        // still has some subsribers are not notified
                        // store the event into pendding database
                        synchronized (pendingEvents) {
                            pendingEvents.add(event);
                            pendingEvents.notifyAll();
                        }
                    }
                    return true;
                }
            }
        }
        System.err.println("Event topic is not found.");
        return false;
    }

    public int addUser(User user){
	    synchronized (UsersDBList){
	        for (User u : UsersDBList){
	            if (u.name.equals(user.name) && u.role.equals(user.role)){
	                // user already exists
                    System.out.println("User " + user.name + " as a " + user.role + " (ID=" + u.getRoleID() + ") found.");
                    return u.getRoleID();
                }
            }
	        // user does not exist, need to register
            // get existing ID set
            LinkedHashSet<Integer> IDset = new LinkedHashSet<>();
            for (User u : UsersDBList){
                int roleID = u.getRoleID();
                String role = u.getRole();
                if (roleID != -1 && role.equals(user.getRole())){
                    IDset.add(roleID);
                }
            }
            // add new user
            int newID = 0;
            if (IDset.size() > 0){
                newID = Collections.max(IDset) + 1;
            }
            user.setRoleID(newID);
            UsersDBList.add(user);
            System.out.println("New user (ID=" + newID + ", NAME=" + user.getName() + ") is registered as " + user.getRole() + ".");
	        return newID;
        }
    }
	/*
	 * add new topic when received advertisement of new topic
	 */
	public boolean addTopic(Topic topic){
		synchronized (TopicsDBList) {
		    TopicsDB topicsDB = new TopicsDB(topic);
		    if (TopicsDBList.add(topicsDB)){
		        return SUCCESS;
            }
		    return FAILED;
        }
	}

	public ArrayList<Topic> getTopics(){
	    synchronized (TopicsDBList) {
            ArrayList<Topic> topics = new ArrayList<>();
            for (TopicsDB topicsDB : TopicsDBList)
                topics.add(topicsDB.getTopic());
            return topics;
        }
    }

    public Topic searchTopic(ArrayList<Topic> topicList, String probe){
	    // currently only support searching by topic name
        for (Topic topic : topicList)
            if (topic.getName().equals(probe))
                return topic;
        return null;
    }
	/*
	 * add subscriber to the internal list
	 */
	private String[] addSubscriberByKeyword(String keyword, Integer roleID){
		// subscribe a topic according to the keyword
        List<String> topicNames = new ArrayList<>();
        for (TopicsDB topicsDB : TopicsDBList){
            List<String> keywordsList = topicsDB.getTopic().getKeywords();
            if (keywordsList.contains(keyword)){
                topicsDB.addSubscriber(roleID);
                topicNames.add(topicsDB.getTopic().getName());
            }
        }
        return topicNames.toArray(new String[topicNames.size()]);
	}

	private boolean addSubscriberByName(String topicName, Integer roleID){
	    // subscribe the specified topic by name
        for (TopicsDB topicsDB : TopicsDBList){
            if (topicsDB.getTopic().getName().equals(topicName)){
                topicsDB.addSubscriber(roleID);
                return true;
            }
        }
        return false;
    }

    private boolean removeSubscriber(String topicName, Integer roleID){
	    for (TopicsDB topicsDB : TopicsDBList){
	        if (topicsDB.getTopic().getName().equals(topicName)) {
	            return topicsDB.removeSubscriber(roleID);
            }
        }
	    return false;
    }
	
	/*
	 * remove subscriber from the list
	 */
	private boolean removeSubscriber(Integer roleID){
        for (TopicsDB topicsDB : TopicsDBList){
            if (topicsDB.getSubscribers().contains(roleID)) {
                return topicsDB.removeSubscriber(roleID);
            }
        }
        return false;
	}
    /*
     * show the list of subscriber for a specified keyword of multiple topics
     */
    private void showSubscribersByKeyword(String keyword){
        System.out.println("List subscribers of the each topic with the keyword (" + keyword + "): ");
        for (TopicsDB topicsDB : TopicsDBList) {
            List<String> keywordsList = topicsDB.getTopic().getKeywords();
            if (keywordsList.contains(keyword)){
                String topicName = topicsDB.getTopic().getName();
                Iterator<Integer> iterator = topicsDB.getSubscribers().iterator();
                String sub_ids = "";
                while (iterator.hasNext()){
                    sub_ids += " " + String.valueOf(iterator.next());
                }
                System.out.println("-- Topic: " + topicName + ", subscribers: " + sub_ids);
            }
        }
    }
	/*
	 * show the list of subscriber for a specified topic
	 */
	private void showSubscribersByName(String topicName){
        System.out.println("List subscribers of the topic (" + topicName + "): ");
        int numSub = 0;
        for (TopicsDB topicsDB : TopicsDBList) {
            if (topicsDB.getTopic().getName().equals(topicName)){
                Iterator<Integer> iterator = topicsDB.getSubscribers().iterator();
                String sub_ids = "";
                while (iterator.hasNext()){
                    sub_ids += " " + String.valueOf(iterator.next());
                }
                System.out.println("-- Topic: " + topicName + ", subscribers: " + sub_ids);
                numSub ++;
            }
        }
        if (numSub == 0){
            System.out.println("None.");
        }
	}
	
	
	public static void main(String[] args) {
        // set IP address and PORT number
        System.out.println("Starting the event manager server...");
		new EventManager().startService(args);
	}


}
