package model;

public class Message {
    public final Type type;
    public final int clientId;    // id del cliente (o -1 si no aplica)
    public final int seq;         // secuencial por cliente (o -1 si no aplica)
    public final boolean spam;    // sólo para MAIL
    public int quarantineTicks;   // sólo para MAIL->quarantine (10k..20k)

    public Message(Type type, int clientId, int seq, boolean spam) {
        this.type = type;
        this.clientId = clientId;
        this.seq = seq;
        this.spam = spam;
    }

    public static Message start(int clientId) { return new Message(Type.START, clientId, -1, false); }
    public static Message end(int clientId)   { return new Message(Type.END,   clientId, -1, false); }
    public static Message mail(int clientId, int seq, boolean spam) { return new Message(Type.MAIL, clientId, seq, spam); }

    @Override public String toString() {
        if (type == Type.MAIL) return "MAIL{c=" + clientId + ", seq=" + seq + ", spam=" + spam + "}";
        if (type == Type.START) return "START{c=" + clientId + "}";
        return "END{c=" + clientId + "}";
    }
}
