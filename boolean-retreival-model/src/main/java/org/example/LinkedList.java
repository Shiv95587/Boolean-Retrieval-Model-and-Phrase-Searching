package org.example;
import java.util.ArrayList;

class Node {
    long data;
    Node next;

    Node(long data) {
        this.data = data;
        this.next = null;
    }
}

public class LinkedList {
    public Node head;

    public LinkedList() {
        this.head = null;
    }
    public void insert(long data) {
        Node newNode = new Node(data);

        // If the list is empty or new data is smaller than the head
        if(head == null)
        {
            head = newNode;
            return;
        }

//        if(data < head.data)
//        {
//
//        }
        if (data < head.data) {
            newNode.next = head;
            head = newNode;
            return;
        }

        if(data == head.data)
            return;
        Node current = head;
        // Find the position to insert the new node
        while (current.next != null && current.next.data < data) {
            current = current.next;
        }

        if (current.next == null && current.data == data)
            return;

        if (current.next != null && current.next.data == data)
            return;

        // Insert the new node after current
        newNode.next = current.next;
        current.next = newNode;
    }

    public long size() {
        long count = 0;
        Node current = head;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }

    public void display() {
        Node current = head;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.next;
        }
        System.out.println();
    }

    public static LinkedList intersect(LinkedList l1, LinkedList l2) {
        LinkedList answer = new LinkedList();

        if (l1 == null || l2 == null) {
            return null;
        }

        Node n = l1.head;
        Node m = l2.head;
        while (n != null && m != null) {
            if (n.data == m.data) {
                answer.insert(n.data);
                n = n.next;
                m = m.next;
            } else if (n.data < m.data) {
                n = n.next;
            } else {
                m = m.next;
            }
        }
        return answer;
    }

    public static LinkedList operationOR(LinkedList l1, LinkedList l2) {
        LinkedList answer = new LinkedList();
        if (l1 == null && l2 == null)
            return null;
        if (l1 == null) {
            answer = l2;
        } else if (l2 == null) {
            answer = l1;
        } else {
            Node n = l1.head;
            Node m = l2.head;
            while (n != null && m != null) {
                if (n.data < m.data) {
                    answer.insert(n.data);
                    n = n.next;
                } else if (n.data > m.data) {
                    answer.insert(m.data);
                    m = m.next;
                } else {
                    answer.insert(m.data);
                    n = n.next;
                    m = m.next;
                }
            }

            while (n != null) {
                answer.insert(n.data);
                n = n.next;
            }

            while (m != null) {
                answer.insert(m.data);
                m = m.next;
            }
        }
        return answer;
    }

    public static LinkedList operationNOT(LinkedList l, ArrayList<Long> documents) {
        if(l == null)
        {
            LinkedList ans = new LinkedList();
            for(long doc : documents)
            {
                ans.insert(doc);
            }
            return ans;
        }
        ArrayList<Long> temp = new ArrayList<>();
        Node tempHead = l.head;
        while (tempHead != null) {
            temp.add(tempHead.data);
            tempHead = tempHead.next;
        }
        documents.removeAll(temp);
        LinkedList ans = new LinkedList();
        for(Long element : documents)
        {
            ans.insert(element);
        }
        return ans;
    }

    public ArrayList<Long> convertToArrayList()
    {
        ArrayList<Long> ans = new ArrayList<>();
        Node temp = this.head;
        while(temp != null)
        {
            ans.add(temp.data);
            temp = temp.next;
        }

        return ans;
    }
}