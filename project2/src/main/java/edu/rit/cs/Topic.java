package edu.rit.cs;

import java.util.List;
import java.io.Serializable;

public class Topic implements Serializable{
	private int id;
	private List<String> keywords;
	private String name;

	public Topic(String name, List<String> keywords) {
		this.name = name;
		this.keywords = keywords;
	}

	public void setID(int newID){
		id = newID;
	}

	public int getID() {
		return id;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "Topic{" +
				"id=" + id +
				", keywords=" + keywords +
				", name='" + name + '\'' +
				'}';
	}

}
