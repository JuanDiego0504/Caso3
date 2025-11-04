package app;

import actors.*;
import config.Config;
import mailbox.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
  public static void main(String[] args) throws Exception {
    String cfgPath = (args.length > 0) ? args[0] : "case3.properties";
    Config cfg = new Config(cfgPath);
    System.out.printf(
        "Config: clients=%d, mailsPerClient=%d, filters=%d, servers=%d, inboxCap=%d, deliveryCap=%d, seed=%d%n",
        cfg.clients, cfg.mailsPerClient, cfg.filters, cfg.servers, cfg.inboxCapacity, cfg.deliveryCapacity, cfg.seed);

    Random rnd = (cfg.seed == 0) ? new Random() : new Random(cfg.seed);

    InboxMailbox inbox = new InboxMailbox(cfg.inboxCapacity);
    QuarantineMailbox quarantine = new QuarantineMailbox();
    DeliveryMailbox delivery = new DeliveryMailbox(cfg.deliveryCapacity);

    List<Thread> all = new ArrayList<>();

    for (int c = 1; c <= cfg.clients; c++) {
      all.add(new ClienteEmisor(c, cfg.mailsPerClient, inbox, new Random(rnd.nextLong())));
    }

    AtomicInteger startsSeen = new AtomicInteger(0);
    AtomicInteger endsSeen = new AtomicInteger(0);
    AtomicBoolean endSentToDelivery = new AtomicBoolean(false);

    for (int f = 1; f <= cfg.filters; f++) {
      all.add(new FiltroSpam(
          f, inbox, quarantine, delivery,
          cfg.clients, cfg.servers,
          startsSeen, endsSeen, endSentToDelivery,
          new Random(rnd.nextLong())
      ));
    }

    all.add(new QuarantineManager(quarantine, delivery, new Random(rnd.nextLong())));

    for (int s = 1; s <= cfg.servers; s++) {
      all.add(new DeliveryServer(s, delivery, new Random(rnd.nextLong())));
    }

    all.forEach(Thread::start);
    for (Thread t : all) t.join();

    System.out.printf("FIN: inbox.size=%d, quarantine.size=%d, delivery.size=%d%n",
        inbox.size(), quarantine.size(), delivery.size());
    System.out.println("Sistema terminó correctamente.");
  }
}
