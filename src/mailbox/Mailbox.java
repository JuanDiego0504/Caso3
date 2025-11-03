package mailbox;

import model.Message;

public interface Mailbox {
    int size();
    // Productores usan put/tryPut (según el tipo de buzón)
}
