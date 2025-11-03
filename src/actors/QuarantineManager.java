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
    private final QuarantineMailbox quarantine;   // semiactiva
    private final DeliveryMailbox delivery;       // semiactiva
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
            // 1) intentar drenar algo (semiactivo)
            Message m;
            while ((m = quarantine.tryTake()) != null) {
                if (m.type == Type.END) {
                    endReceived = true; // pero seguimos hasta vaciar cuarentena
                } else {
                    buffer.add(m);
                }
            }

            // 2) tick cada ~1s
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

            // 3) procesar buffer: decrementar ticks y pasar a delivery (o descartar)
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

            // 4) condición de terminación: recibí END y ya no quedan mensajes en buffer ni en cola
            if (endReceived && buffer.isEmpty() && quarantine.size() == 0) break;
        }
        System.out.println("[QManager] terminó.");
    }
}
