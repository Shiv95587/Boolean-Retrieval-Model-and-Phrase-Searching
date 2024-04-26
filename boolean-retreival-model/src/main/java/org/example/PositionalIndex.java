package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class PositionalIndex {
    //    Positional index
    public final Map<String, Map<Long, List<Integer>>> index;
    public PositionalIndex() {
        this.index = new HashMap<>();
    }

    //    Method that adds a file tokens into positional index
    public void addDocument(ArrayList<String> tokens, Long documentId) {

        for(int i = 0; i < tokens.size(); ++i)
        {
            String term = tokens.get(i);
            Map<Long, List<Integer>> docPositions = index.get(term);
            if (docPositions == null) {
                docPositions = new HashMap<>();
                index.put(term, docPositions);
            }
            List<Integer> positionsList = docPositions.get(documentId);
            if (positionsList == null) {
                positionsList = new ArrayList<>();
                docPositions.put(documentId, positionsList);
            }
            positionsList.add(i);
        }
    }

    // displays positional index on console
    public void display()
    {
        for(Map.Entry<String, Map<Long, List<Integer>>> entry : index.entrySet()) {
            String term = entry.getKey();
            Map<Long, List<Integer>> docs = entry.getValue();
            System.out.println(term + "-> [");
            for(Map.Entry<Long, List<Integer>> entry1 : docs.entrySet())
            {
                Long docId = entry1.getKey();
                System.out.println(docId + ": [");
                List<Integer> positions = entry1.getValue();
                for(int pos : positions)
                {
                    System.out.println(pos + ", ");
                }
                System.out.println("]");
            }
            System.out.println("]");
        }
    }
    public Map<Long, List<Integer>> getPositions(String term) {
        return index.get(term);
    }

    public ArrayList<Long> positionalIntersect(Map<Long, List<Integer>> positions1, Map<Long, List<Integer>> positions2, int k)
    {
        ArrayList<Long> result = new ArrayList<>();;
        // Traverse positions1 and positions2
        for (Map.Entry<Long, List<Integer>> entry1 : positions1.entrySet()) {
            long docId = entry1.getKey();
            List<Integer> list1 = entry1.getValue();
            if (positions2.containsKey(docId)) {

                List<Integer> list2 = positions2.get(docId);
                int i = 0;
                int j = 0;

                // Traverse both lists
                while (i < list1.size() && j < list2.size()) {
                    int pos1 = list1.get(i);
                    int pos2 = list2.get(j);

                    // If positions are within k distance, add to result
                    if (Math.abs(pos1 - pos2) - 1  <= k) {
                        result.add(docId);
                        i++;
                        j++;
                        break;
                    }
                    // Move to the next position in the list with smaller position value
                    else if (pos1 < pos2) {
                        i++;
                    } else {
                        j++;
                    }
                }
            }
        }

        return result;
    }

}
