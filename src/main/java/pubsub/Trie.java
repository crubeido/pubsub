package pubsub;

import java.util.HashMap;
import java.util.Map;

public class Trie {
    private class Node {
        private Map<Character, Node> children;
        boolean end;

        public Node() {
            children = new HashMap<>();
            end = false;
        }
    }

    public Node root;

    public Trie() {
        this.root = new Node();
    }

    public void insert(String word) {
        Node current = this.root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Node node = current.children.get(c);
            if (node == null) {
                node = new Node();
                current.children.put(c, node);
            }
            current = node;
        }
        // Set end to true
        current.end = true;
    }

    public boolean delete(String word, int i, Node current) {
        if(i == word.length()) {
            if(!current.end) {
                return false;
            } else {
                current.end = false;
                return current.children.size() == 0;
            }
        }
        if(current.children.get(word.charAt(i)) == null) {
            return false;
        }
        boolean shouldDelete = this.delete(word,i+1,current.children.get(word.charAt(i)));
        if(shouldDelete) {
            current.children.remove(word.charAt(i));
            return current.children.size() == 0;
        }
        return false;
    }
    public boolean search(String word) {
        Node current = this.root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Node node = current.children.get(c);
            if (node == null)
                return false;
            current = node;
        }
        return current.end;
    }
}