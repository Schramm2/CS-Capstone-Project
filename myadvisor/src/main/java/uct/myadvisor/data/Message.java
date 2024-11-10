package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Message definition class, one user has many messages (sent or received)
@Entity
@Table(name = "messages")
public class Message extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
    private String text;
    private LocalDateTime timestamp;

    public Message() {}

    // New Message
    public Message (User sender, User receiver, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    // Existing Message
    public Message (Long id, User sender, User receiver, String text, LocalDateTime timestamp) {
        super.setId(id);
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
    }

    // get and set methods
    public User getSender() {
        return sender;
    }
    public User getReceiver() {
        return receiver;
    }
    public String getText() {
        return text;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
