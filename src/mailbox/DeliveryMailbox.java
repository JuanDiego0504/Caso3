package mailbox;

import model.Message;
import model.Type;

import java.util.ArrayDeque;
import java.util.Queue;

public class DeliveryMailbox implements Mailbox {
    private final int capacity;
    private final Queue<Message> q = new ArrayDeque<>();

    public DeliveryMailbox(int capacity) { 
        this.capacity = capacity; 
    }

 
    public void tryPutOrRetry(Message m) {
        for (;;) {
            synchronized (this) {
                if (q.size() < capacity) {
                    q.add(m);
                    notifyAll();
                    return;
                }
            }
            try { 
                Thread.sleep(10); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
                return; 
            }
        }
    }

 
    public synchronized boolean tryPut(Message m) {
        if (q.size() >= capacity) return false;
        q.add(m);
        notifyAll();
        return true;
    }

    public synchronized Message tryTake() {
        return q.poll();
    }

    
    public synchronized void enqueueEndForServers(int nServers) {
        for (int i = 0; i < nServers; i++) {
            
            q.add(new Message(Type.END, -1, -1, false));
        }
        notifyAll();
    }

    @Override 
    public synchronized int size() { 
        return q.size(); 
    }
}
