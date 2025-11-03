package config;

import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    public final int clients, mailsPerClient, filters, servers;
    public final int inboxCapacity, deliveryCapacity;
    public final long seed;

    public Config(String path) {
        try {
            Properties p = new Properties();
            p.load(new FileInputStream(path));
            clients = Integer.parseInt(p.getProperty("clients"));
            mailsPerClient = Integer.parseInt(p.getProperty("mailsPerClient"));
            filters = Integer.parseInt(p.getProperty("filters"));
            servers = Integer.parseInt(p.getProperty("servers"));
            inboxCapacity = Integer.parseInt(p.getProperty("inbox.capacity"));
            deliveryCapacity = Integer.parseInt(p.getProperty("delivery.capacity"));
            seed = Long.parseLong(p.getProperty("seed", "0"));
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo config: " + e.getMessage(), e);
        }
    }
}
