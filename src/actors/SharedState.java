package actors;


public class SharedState {
    private int startsSeen = 0;          
    private int endsSeen   = 0;            
    private boolean endSentToDelivery = false; 

    public synchronized void incStarts() { startsSeen++; }
    public synchronized void incEnds()   { endsSeen++;   }

    public synchronized int getStarts() { return startsSeen; }
    public synchronized int getEnds()   { return endsSeen;   }


    public synchronized boolean tryMarkEndSent() {
        if (endSentToDelivery) return false;
        endSentToDelivery = true;
        return true;
    }

    public synchronized boolean isEndSent() { return endSentToDelivery; }
}
