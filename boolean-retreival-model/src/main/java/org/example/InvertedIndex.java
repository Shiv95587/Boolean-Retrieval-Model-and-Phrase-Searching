package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InvertedIndex {
    HashMap<String, LinkedList> index;
    public InvertedIndex(){
        index = new HashMap<>();
    }

    public void insertDocument(ArrayList<String> fileTokens, long docID) {
        for(String token : fileTokens) {
            if(this.index.containsKey(token)) {
                this.index.get(token).insert(docID);
            }
            else {
                LinkedList list = new LinkedList();
                list.insert(docID);
                this.index.put(token,list);
            }
        }
    }


}
