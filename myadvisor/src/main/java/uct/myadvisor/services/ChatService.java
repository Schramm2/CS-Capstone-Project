package uct.myadvisor.services;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import uct.myadvisor.data.Message;
import uct.myadvisor.security.AuthenticatedUser;

import uct.myadvisor.data.User;

@Service
public class ChatService {

    private final Sinks.Many<Message> messages = Sinks.many().multicast().directBestEffort();

    private final Flux<Message> messagesFlux = messages.asFlux();

    private final MessageService messageService;

    public ChatService(MessageService messageService) {
        this.messageService = messageService;
    }

    // custom repository get methods
    public Flux<Message> join(User currentUser, User recipient) {
        return Flux.concat(
            loadPreviousMessages(currentUser, recipient),
            this.messagesFlux.filter(message -> 
                (message.getReceiver().equals(currentUser) && message.getSender().equals(recipient)) ||
                (message.getSender().equals(currentUser) && message.getReceiver().equals(recipient))
            )
        );
    }

    // add new message with sender and receiver
    public void add(User sender, User receiver, String message) {
        Message newMessage = new Message(sender, receiver, message);
        messageService.update(newMessage); // Save message to database
        this.messages.tryEmitNext(newMessage);
    }

    // get all messages for two users that have both users in each message
    private Flux<Message> loadPreviousMessages(User currentUser, User recipient) {
        Pageable pageable = PageRequest.of(0, 900000000);
        return Flux.fromIterable(messageService.getAllMessagesBetweenUsers(currentUser, recipient, pageable).getContent());
    }

}