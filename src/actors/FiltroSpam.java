package actors;

import mailbox.InboxMailbox;
import mailbox.QuarantineMailbox;
import mailbox.DeliveryMailbox;
import model.Message;
import model.Type;


public class FiltroSpam extends Thread {

    private final InboxMailbox inbox;
    private final QuarantineMailbox quarantine;
    private final DeliveryMailbox delivery;

    private final int totalClients;
    private final int nServers;

    private final SharedState state;      
    private final java.util.Random rnd;

    public FiltroSpam(int id,
                      InboxMailbox inbox,
                      QuarantineMailbox quarantine,
                      DeliveryMailbox delivery,
                      int totalClients,
                      int nServers,
                      SharedState state,
                      java.util.Random rnd) {
        super("Filtro-" + id);
        this.inbox = inbox;
        this.quarantine = quarantine;
        this.delivery = delivery;
        this.totalClients = totalClients;
        this.nServers = nServers;
        this.state = state;
        this.rnd = rnd;
    }

    @Override
    public void run() {
        try {
            while (true) {
              
                Message m = inbox.take();

                if (m.type == Type.START) {
                    state.incStarts();
                    continue;
                }

                if (m.type == Type.END) {
                    state.incEnds();
                   
                } else if (m.type == Type.MAIL) {
                    if (m.spam) {
       
                        m.quarantineTicks = 10000 + rnd.nextInt(10001);
                        quarantine.put(m);  
                    } else {
                      
                        while (!delivery.tryPut(m)) {
                            Thread.sleep(2);
                        }
                    }
                }

            
                boolean condition;
              
                synchronized (inbox) {
                    condition = (inbox.size() == 0)
                             && (quarantine.size() == 0)
                             && (state.getEnds() >= totalClients);
                }

                if (condition) {
                    
                    if (state.tryMarkEndSent()) {
                        delivery.enqueueEndForServers(nServers); 
                        quarantine.putEnd();                  
                        System.out.printf("[%s] envió FIN a delivery y quarantine%n",
                                getName());
                    }
                  
                    if (state.isEndSent()) break;
                }
            }
        } catch (InterruptedException ie) {
            interrupt();
        }
    }
}
