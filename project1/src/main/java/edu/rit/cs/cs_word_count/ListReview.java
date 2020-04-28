package edu.rit.cs.cs_word_count;

import java.io.Serializable;
import java.util.List;

public class ListReview implements Serializable {
    
    private List<AmazonFineFoodReview> list;
    
    public List<AmazonFineFoodReview> getList() {
        return list;
    }

    public void setList(List<AmazonFineFoodReview> list) {
        this.list = list;
    }
}
