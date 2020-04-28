package edu.rit.cs.cs_word_count;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Client{
//    public static final String URL = "localhost";
//    public static final int PORT = 8888;
    // public static final String AMAZON_FINE_FOOD_REVIEWS_file="amazon-fine-food-reviews/Reviews.csv";

    /**
     * Read and parse all reviews
     * @param dataset_file
     * @return list of reviews
     */
    public static List<AmazonFineFoodReview> read_reviews(String dataset_file) {
        List<AmazonFineFoodReview> allReviews = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dataset_file))){
            String reviewLine = null;
            // read the header line
            reviewLine = br.readLine();

            //read the subsequent lines
            while ((reviewLine = br.readLine()) != null) {
                allReviews.add(new AmazonFineFoodReview(reviewLine));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return allReviews;
    }

    
    /**
     * Print the list of words and their counts
     * @param wordcount
     */
    public static void print_word_count( Map<String, Integer> wordcount, String result_file){
        FileWriter fileWriter = null;
        try{
            fileWriter = new FileWriter(result_file);
            for(String word : wordcount.keySet()){
                fileWriter.write(word + " : " + wordcount.get(word) + "\r\n");
            }
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    public static void main(String args[]){
        System.out.println("Starting client...");
        Socket socket = null;
        try{
            //parse the IP and PORT
            String ip_port = args[0];
            String URL = ip_port.split(":")[0];
            int PORT = Integer.parseInt(ip_port.split(":")[1]);
            socket = new Socket(URL, PORT);
            // parse the input data file
            String inputFileStr = args[1];
            List<AmazonFineFoodReview> allReviews = read_reviews(inputFileStr);
            System.out.println("The number of reviews: " + allReviews.size());

            // Serialize the data for socket communication
            ListReview listReview = new ListReview();
            listReview.setList(allReviews);
            // construct output stream (send data to server)
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(listReview);
            outputStream.flush();
            
            // get the results from server
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Map<String, Integer> results = (Map<String, Integer>)inputStream.readObject();

            //save the results
            String result_file = inputFileStr.split("/")[0] + "/output.txt";
            System.out.println("Result of word counting from server are saved in: " + result_file);
            print_word_count(results, result_file);
            
            outputStream.close();
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if (socket != null){
                try{
                    socket.close();
                }catch (Exception e){
                    socket = null;
                    e.printStackTrace();
                }
            }
        }
            
    }
}
