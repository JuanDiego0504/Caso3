package mailbox;

import model.Message;
import java.util.ArrayDeque;
import java.util.Queue;

public class InboxMailbox implements Mailbox {
    private final int capacity;
    private final Queue<Message> q = new ArrayDeque<>();

    public InboxMailbox(int capacity) { this.capacity = capacity; }

    public synchronized void put(Message m) {
        while (q.size() == capacity) {
            try { wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
        q.add(m);
        notifyAll(); 
    }

    public synchronized Message take() {
        while (q.isEmpty()) {
            try { wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return null; }
        }
        Message m = q.remove();
        notifyAll(); 
        return m;
    }

    @Override public synchronized int size() { return q.size(); }
}
