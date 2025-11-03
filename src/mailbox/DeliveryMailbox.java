package mailbox;

import model.Message;
import model.Type;

import java.util.ArrayDeque;
import java.util.Queue;

public class DeliveryMailbox implements Mailbox {
    private final int capacity;
    private final Queue<Message> q = new ArrayDeque<>();

    public DeliveryMailbox(int capacity) { this.capacity = capacity; }

    public void tryPutOrRetry(Message m) {
        for (;;) {
            synchronized (this) {
                if (q.size() < capacity) {
                    q.add(m);
                    notifyAll();
                    return;
                }
            }
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }

    public Message tryTake() { // servidores: espera ACTIVA (poll + pausa mínima afuera)
        synchronized (this) {
            return q.poll();
        }
    }

    public void enqueueEndForServers(int nServers) {
        synchronized (this) {
            for (int i = 0; i < nServers; i++) {
                q.add(new Message(Type.END, -1, -1, false));
            }
            notifyAll();
        }
    }

    @Override public synchronized int size() { return q.size(); }
}
