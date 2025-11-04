package actors;

import mailbox.*;
import model.Message;
import model.Type;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FiltroSpam extends Thread {
    private final InboxMailbox inbox;                
    private final QuarantineMailbox quarantine;       
    private final DeliveryMailbox delivery;           
    private final int totalClients;
    private final int nServers;

    private final AtomicInteger startsSeen;
    private final AtomicInteger endsSeen;
    private final AtomicBoolean endSentToDelivery;   

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
            Message m = inbox.take(); 
            if (m == null) return;

            if (m.type == Type.START) {
                startsSeen.incrementAndGet();
               
            } else if (m.type == Type.MAIL) {
                if (m.spam) {
                    m.quarantineTicks = 10000 + rnd.nextInt(10001); 
                    quarantine.put(m); 
                } else {
                    delivery.tryPutOrRetry(m); 
                }
            } else if (m.type == Type.END) {
                endsSeen.incrementAndGet();
            }


            boolean condition;
            synchronized (inbox) { 
                condition = (inbox.size() == 0) && (quarantine.size() == 0)
                        && (endsSeen.get() >= totalClients);
            }
            if (condition) {
                if (endSentToDelivery.compareAndSet(false, true)) {
                    delivery.enqueueEndForServers(nServers); 
                    quarantine.putEnd();                     
                    System.out.printf("[%s] envió FIN a delivery y quarantine\n", getName());
                }
             
                if (endSentToDelivery.get()) break;
            }
        }
        System.out.printf("[%s] terminó.\n", getName());
    }
}
