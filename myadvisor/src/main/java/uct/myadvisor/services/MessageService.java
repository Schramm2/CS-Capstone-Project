package uct.myadvisor.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uct.myadvisor.data.Message;
import uct.myadvisor.data.MessageRepository;
import uct.myadvisor.data.User;

@Service
public class MessageService {

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    // basic repository CRUD methods
    public Optional<Message> get(Long id) {
        return repository.findById(id);
    }

    public Message update(Message entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Message> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Message> list(Pageable pageable, Specification<Message> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    // Get all messages between users
    public Page<Message> getAllMessagesBetweenUsers(User user1, User user2, Pageable pageable) {
        return repository.findAllMessagesBetweenUsers(user1.getId(), user2.getId(), pageable);
    }

}
