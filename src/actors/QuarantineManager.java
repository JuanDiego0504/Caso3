package actors;

import mailbox.DeliveryMailbox;
import mailbox.QuarantineMailbox;
import model.Message;
import model.Type;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class QuarantineManager extends Thread {
    private final QuarantineMailbox quarantine;  
    private final DeliveryMailbox delivery;       
    private final Random rnd;

    public QuarantineManager(QuarantineMailbox quarantine, DeliveryMailbox delivery, Random rnd) {
        super("QuarantineManager");
        this.quarantine = quarantine;
        this.delivery = delivery;
        this.rnd = rnd;
    }

    @Override public void run() {
        boolean endReceived = false;
        List<Message> buffer = new LinkedList<>();

        while (true) {
           
            Message m;
            while ((m = quarantine.tryTake()) != null) {
                if (m.type == Type.END) {
                    endReceived = true; 
                } else {
                    buffer.add(m);
                }
            }

           
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

          
            for (Iterator<Message> it = buffer.iterator(); it.hasNext();) {
                Message msg = it.next();
                msg.quarantineTicks = Math.max(0, msg.quarantineTicks - 1000);
                if (msg.quarantineTicks == 0) {
                    int n = 1 + rnd.nextInt(21);
                    if (n % 7 == 0) {
                        System.out.printf("[QManager] descartó %s (n=%d)\n", msg, n);
                    } else {
                        delivery.tryPutOrRetry(msg);
                        System.out.printf("[QManager] liberó %s a delivery\n", msg);
                    }
                    it.remove();
                }
            }

      
            if (endReceived && buffer.isEmpty() && quarantine.size() == 0) break;
        }
        System.out.println("[QManager] terminó.");
    }
}
