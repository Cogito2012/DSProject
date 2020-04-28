package edu.rit.cs;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class PubClient{

    private static int status = 0; // indicates whether this user login

    public static User getLoginInfo(Scanner scanner){
        System.out.println("Enter your name:");
        String name = scanner.nextLine().trim();
        System.out.println("Enter your password:");
        String password = scanner.nextLine().trim();
        User user = new User(name, password, "publisher");
        return user;
    }

    public static Topic generateTopic(Scanner scanner) {
        System.out.println("Enter the topic content: ");
        String topicName = scanner.nextLine().trim();
        System.out.println("Enter the keywords for this topic. Keywords should be a single line separated by whitespace:");
        String[] keywords_arr = scanner.nextLine().trim().split("\\s+");
        List<String> keywords = Arrays.asList(keywords_arr);

        Topic topic = new Topic(topicName, keywords);
        return topic;
    }

    public static Event generateEvent(Scanner scanner, PubSubAgent agent){
        System.out.println("Enter the name your event topic:");
        String inputTopic = scanner.nextLine().trim();
        // get topic object from server
        Topic topic = agent.retrieveTopic(inputTopic);
        if (topic!= null) {
            System.out.println("Enter event title:");
            String title = scanner.nextLine().trim();
            System.out.println("Enter content for this event. Leave a blank line to finish:");
            String content="", next;
            while ( !((next = scanner.nextLine()).isEmpty()) )
                content += next;
            Event event = new Event(topic, title, content);
            return event;
        }else{
            System.out.println("Topic not found in server database.");
        }
        return null;
    }

    public static void main(String args[]){

        String ip_port = args[0];
        String URL = ip_port.split(":")[0];
        int PORT = Integer.parseInt(ip_port.split(":")[1]);
        PubSubAgent agent = new PubSubAgent(URL, PORT);

        Scanner scanner = new Scanner(System.in);
        boolean endop = false;
        do {
            System.out.println("================ Please choose [ID] of each following operations =====================");
            System.out.println("[0]: Login/Register my account.");
            System.out.println("[1]: Advertise a topic.");
            System.out.println("[2]: Publish an event.");
//            System.out.println("[3]: Lookup all historic topics.");
//            System.out.println("[4]: Lookup all historic events.");
            System.out.println("[3]: Exit.");
            System.out.print("> ");
            int ID = -1;
            try{
                ID = scanner.nextInt();
                scanner.nextLine();
            }catch(Exception e){
                scanner.nextLine();
            }
            if ((ID != 0) && status == 0){
                // not login
                System.out.println("Please login first!");
                continue;
            }
            switch(ID){
                case 0: agent.login(getLoginInfo(scanner)); status=1; break;
                case 1: agent.advertise(generateTopic(scanner)); break;
                case 2: agent.publish(generateEvent(scanner, agent)); break;
//                case 3: agent.lookupTopics(); break;
//                case 4: agent.lookupEvents(); break;
                case 3: endop = true; break;
                default: System.out.println("Invalid choice! Please choose digits from 0 to 3.");
            }
        } while(!endop);
    }


}
