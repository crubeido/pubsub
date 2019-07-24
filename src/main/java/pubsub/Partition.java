package pubsub;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Partition {
    private Object monitor = new Object();
    private LinkedList<String> queue = new LinkedList<String>();
    private int index;
    private boolean writeDone = false;

    public Partition(int index) {
        this.index = index;
    }

    public boolean getWriteDone() {
        return this.writeDone;
    }

    public void setWriteDone(boolean writeDone) {
        this.writeDone = writeDone;
    }

    public int count() {
        return this.queue.size();
    }

    public String word(int index) {
        return this.queue.get(index);
    }

    public void push(String word) {
        synchronized (monitor) {
            queue.addLast(word);
            monitor.notify();
        }
    }

    public String pop() throws InterruptedException {
        String word = null;
        synchronized (monitor) {
            if (queue.isEmpty()) {
                System.out.println("partition[" + this.index + "]: pop waiting");
                monitor.wait();
            }
            word = queue.removeFirst();
        }
        if (word == null) throw new NoSuchElementException();
        return word;
    }
}
