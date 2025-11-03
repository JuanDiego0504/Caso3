package actors;

import mailbox.InboxMailbox;
import model.Message;
import java.util.Random;

public class ClienteEmisor extends Thread {
    private final int clientId;
    private final int mails;
    private final InboxMailbox inbox;
    private final Random rnd;

    public ClienteEmisor(int clientId, int mails, InboxMailbox inbox, Random rnd) {
        super("Cliente-" + clientId);
        this.clientId = clientId;
        this.mails = mails;
        this.inbox = inbox;
        this.rnd = rnd;
    }

    @Override public void run() {
        inbox.put(Message.start(clientId));
        for (int i = 1; i <= mails; i++) {
            boolean spam = rnd.nextBoolean();
            inbox.put(Message.mail(clientId, i, spam));
        }
        inbox.put(Message.end(clientId));
        System.out.printf("[%s] terminó. (enviados=%d)\n", getName(), mails);
    }
}
