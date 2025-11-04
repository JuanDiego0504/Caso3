package actors;

import mailbox.DeliveryMailbox;
import model.Message;
import model.Type;

import java.util.Random;

public class DeliveryServer extends Thread {
    private final DeliveryMailbox delivery;
    private final Random rnd;

    public DeliveryServer(int id, DeliveryMailbox delivery, Random rnd) {
        super("Server-" + id);
        this.delivery = delivery;
        this.rnd = rnd;
    }

    @Override public void run() {
        while (true) {
            Message m = delivery.tryTake(); 
            if (m == null) {
                Thread.yield();             
                try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                continue;
            }
            if (m.type == Type.END) break;

            // simular entrega
            try { Thread.sleep(10 + rnd.nextInt(40)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            System.out.printf("[%s] entregó %s\n", getName(), m);
        }
        System.out.printf("[%s] terminó.\n", getName());
    }
}
