package mailbox;

import model.Message;
import java.util.ArrayDeque;
import java.util.Queue;

public class QuarantineMailbox implements Mailbox {
    private final Queue<Message> q = new ArrayDeque<>();
    private volatile boolean endReceived = false;

    public synchronized void put(Message m) { 
        q.add(m);
        notifyAll();
    }

    public Message tryTake() { 
        synchronized (this) {
            return q.poll();
        }
    }

    public synchronized void putEnd() {

        if (!endReceived) {
            endReceived = true;
            q.add(new Message(model.Type.END, -1, -1, false));
            notifyAll();
        }
    }

    @Override public synchronized int size() { return q.size(); }
}
