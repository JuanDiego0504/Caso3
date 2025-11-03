package actors;

import mailbox.*;
import model.Message;
import model.Type;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FiltroSpam extends Thread {
    private final InboxMailbox inbox;                 // pasiva
    private final QuarantineMailbox quarantine;       // semiactiva
    private final DeliveryMailbox delivery;           // semiactiva
    private final int totalClients;
    private final int nServers;

    private final AtomicInteger startsSeen;
    private final AtomicInteger endsSeen;
    private final AtomicBoolean endSentToDelivery;    // sólo uno envía FIN a delivery

    private final Random rnd;

    public FiltroSpam(
            int id,
            InboxMailbox inbox,
            QuarantineMailbox quarantine,
            DeliveryMailbox delivery,
            int totalClients,
            int nServers,
            AtomicInteger startsSeen,
            AtomicInteger endsSeen,
            AtomicBoolean endSentToDelivery,
            Random rnd) {

        super("Filtro-" + id);
        this.inbox = inbox;
        this.quarantine = quarantine;
        this.delivery = delivery;
        this.totalClients = totalClients;
        this.nServers = nServers;
        this.startsSeen = startsSeen;
        this.endsSeen = endsSeen;
        this.endSentToDelivery = endSentToDelivery;
        this.rnd = rnd;
    }

    @Override public void run() {
        while (true) {
            Message m = inbox.take(); // PASIVA
            if (m == null) return;

            if (m.type == Type.START) {
                startsSeen.incrementAndGet();
                // Los START no se reenvían
            } else if (m.type == Type.MAIL) {
                if (m.spam) {
                    m.quarantineTicks = 10000 + rnd.nextInt(10001); // [10000..20000]
                    quarantine.put(m); // SEMIACTIVA (productor)
                } else {
                    delivery.tryPutOrRetry(m); // SEMIACTIVA (productor)
                }
            } else if (m.type == Type.END) {
                endsSeen.incrementAndGet();
            }

            // ¿Condición para FIN?
            // inbox vacío + quarantine vacía + recibidos todos los END
            boolean condition;
            synchronized (inbox) { // leer tam. inbox de forma segura
                condition = (inbox.size() == 0) && (quarantine.size() == 0)
                        && (endsSeen.get() >= totalClients);
            }
            if (condition) {
                if (endSentToDelivery.compareAndSet(false, true)) {
                    delivery.enqueueEndForServers(nServers); // broadcast a servers
                    quarantine.putEnd();                     // para que termine el manager
                    System.out.printf("[%s] envió FIN a delivery y quarantine\n", getName());
                }
                // Los filtros terminan cuando ya se garantizó lo anterior
                if (endSentToDelivery.get()) break;
            }
        }
        System.out.printf("[%s] terminó.\n", getName());
    }
}
