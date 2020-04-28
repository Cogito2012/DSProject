package edu.rit.cs.cs_word_count;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server{

    private ServerSocket serverSocket;

    public Server(int port){
        try{
            serverSocket = new ServerSocket(port);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start() throws Exception{
        try {
            while (true){
                Socket client  = serverSocket.accept();
                new HandlerThread(client);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Print the list of words and their counts
     * @param wordcount
     */
    public static void print_word_count( Map<String, Integer> wordcount){
        for(String word : wordcount.keySet()){
            System.out.println(word + " : " + wordcount.get(word));
        }
    }

    /**
     * Emit 1 for every word and store this as a <key, value> pair
     * @param allReviews
     * @return
     */
    public static List<KV<String, Integer>> map(List<AmazonFineFoodReview> allReviews) {
        List<KV<String, Integer>> kv_pairs = new ArrayList<KV<String, Integer>>();

        for(AmazonFineFoodReview review : allReviews) {
            Pattern pattern = Pattern.compile("([a-zA-Z]+)");
            Matcher matcher = pattern.matcher(review.get_Summary());

            while(matcher.find())
                kv_pairs.add(new KV(matcher.group().toLowerCase(), 1));
        }
        return kv_pairs;
    }


    /**
     * count the frequency of each unique word
     * @param kv_pairs
     * @return a list of words with their count
     */
    public static Map<String, Integer> reduce(List<KV<String, Integer>> kv_pairs) {
        Map<String, Integer> results = new HashMap<>();

        for(KV<String, Integer> kv : kv_pairs) {
            if(!results.containsKey(kv.getKey())) {
                results.put(kv.getKey(), kv.getValue());
            } else{
                int init_value = results.get(kv.getKey());
                results.replace(kv.getKey(), init_value, init_value+kv.getValue());
            }
        }
        return results;
    }




    private class HandlerThread implements Runnable {
        private Socket socket;
        public HandlerThread(Socket client){
            socket = client;
            new Thread(this).start();
        }

        public void run(){
            try{
                // receive data from the client
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ListReview listReview = (ListReview)inputStream.readObject();
                // parse the data into AmazonFineFoodReview list
                List<AmazonFineFoodReview> allReviews = listReview.getList();
                System.out.println("File data received.");
                
                // use MapReduce method to count words
                System.out.println("Start to counting...");
                MyTimer myMapTimer = new MyTimer("map operation");
                myMapTimer.start_timer();
                List<KV<String, Integer>> kv_pairs = map(allReviews);
                myMapTimer.stop_timer();

                MyTimer myReduceTimer = new MyTimer("reduce operation");
                myReduceTimer.start_timer();
                Map<String, Integer> results = reduce(kv_pairs);
                myReduceTimer.stop_timer();
                myReduceTimer.print_elapsed_time();
 
                // print_word_count(results);
                System.out.println("Finished counting.");
                myMapTimer.print_elapsed_time();
                myReduceTimer.print_elapsed_time();

                ObjectOutputStream outputStream  = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(results);

                outputStream.close();
                inputStream.close();
            }catch (Exception e){
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


    public static void main(String[] args){

        Server server = new Server(Integer.parseInt(args[0]));
        try {
            System.out.println("Starting the server...");
            server.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
