package edu.rit.cs;


import java.io.File;

public interface Peer {

    /**
     * Given a file, a peer will generate a hashCode for this file and place
     * it on a node according to the underlying DHT algorithm
     * @param file
     * @return the hashCode of a file
     */
    public String insert(File file);

    /**
     * Given the hashCode of a file, a peer should return the file.
     * @param hashCode
     * @return the file corresponding to the hashCode
     */
    public File lookup(String hashCode);

}
