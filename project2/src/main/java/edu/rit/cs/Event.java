package edu.rit.cs;

import java.util.LinkedHashSet;
import java.util.Collection;
import java.io.Serializable;
import java.util.Iterator;

public class Event implements Serializable {
	private int id = 0;
	private Topic topic;
	private String title;
	private String content;
	private LinkedHashSet<Integer> toBeNotified;

	public Event(Topic topic, String title, String content) {
		this.topic = topic;
		this.title = title;
		this.content = content;
		toBeNotified = new LinkedHashSet<>();
	}

	public int getID() {
		return id;
	}

	public Topic getTopic() {
		return topic;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String toString() {
		return "Event{" +
				"id=" + id +
				", topic=" + topic.getName() +
				", title='" + title + '\'' +
				", content='" + content + '\'' +
				'}';
	}

	public Event setID(int id) {
		this.id = id;
		return this;
	}

	public synchronized boolean addSubscriberList(Collection<Integer> c) {
		if (c != null)
			return toBeNotified.addAll(c);
		return false;
	}

	public synchronized Iterator<Integer> iterator() {
		return toBeNotified.iterator();
	}

	public synchronized int notifySize() {
		return toBeNotified.size();
	}
}
