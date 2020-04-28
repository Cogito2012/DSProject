package edu.rit.cs;

import java.util.LinkedHashSet;

public class TopicsDB{

    private Topic topic;
    // we need to store subscribers' IDs who have already subscribed this topic
    private LinkedHashSet<Integer> subscribers;


    public TopicsDB(Topic topic) {
        this.topic = topic;
        subscribers = new LinkedHashSet<Integer>();
    }

    public Topic getTopic(){
        return topic;
    }

    public synchronized LinkedHashSet<Integer> getSubscribers() {
        return subscribers;
    }

    public synchronized boolean addSubscriber(Integer subID) {
        return subscribers.add(subID);
    }

    public synchronized boolean removeSubscriber(Integer subID) {
        return subscribers.remove(subID);
    }
}