package edu.rit.cs;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class SubClient{

    private static int status = 0; // indicates whether this user login

    public static User getLoginInfo(Scanner scanner){
        System.out.println("Enter your name:");
        String name = scanner.nextLine().trim();
        System.out.println("Enter your password:");
        String password = scanner.nextLine().trim();
        User user = new User(name, password, "subscriber");
        return user;
    }

    private static Topic getTopicInput(Scanner scanner, PubSubAgent agent){
        System.out.println("Enter the name of topic:");
        String inputTopic = scanner.nextLine().trim();
        // get topic object from server
        Topic topic = agent.retrieveTopic(inputTopic);
        return topic;
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
            System.out.println("[1]: Subscribe to a keyword.");
            System.out.println("[2]: Subscribe to a topic.");
            System.out.println("[3]: Unsubsribe from a topic.");
            System.out.println("[4]: Unsubsribe from all.");
            System.out.println("[5]: Exit.");
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
                case 0: agent.login(getLoginInfo(scanner)); status = 1; break;
                case 1:
                    System.out.println("Enter keyword to subscribe to:");
                    agent.subscribe(scanner.nextLine().trim()); break;
                case 2: agent.subscribe(getTopicInput(scanner, agent)); break;
                case 3: agent.unsubscribe(getTopicInput(scanner, agent)); break;
                case 4: agent.unsubscribe(); break;
                case 5: endop = true; break;
                default: System.out.println("Invalid choice! Please choose digits from 0 to 5.");
            }
        } while(!endop);
    }
}