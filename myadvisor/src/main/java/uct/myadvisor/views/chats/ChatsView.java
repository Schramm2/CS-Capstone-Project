package uct.myadvisor.views.chats;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;
import reactor.core.Disposable;
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Student;
import uct.myadvisor.data.User;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.ChatService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Chats")
@Route(value = "chats", layout = MainLayout.class)
@RolesAllowed({"STUDENT", "ADVISOR"})
public class ChatsView extends VerticalLayout {
    
    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final ChatService chatService;

    private List<Advisor> advisors;
    private List<Student> students;
    private Advisor advisor;
    private Student student;

    // Keep track if subscribed to receive messages
    private Disposable messageSubscription;

    // list of advisors for student or advisor
    public void advisorDataSourceUpdate(String userRole, Advisor advisor, Student student) {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (userRole == "ADVISOR") {
                List<Advisor> newAdvisors = new ArrayList<>();
                advisors = userService.findFacultyAdvisors(advisor.getFaculty(), pageable).getContent();
                for (Advisor a : advisors) {
                    if (!a.getId().equals(advisor.getId())) {
                        newAdvisors.add(a);
                    }
                }
                advisors = newAdvisors;
            } else if (userRole == "STUDENT") {
                advisors = userService.findStudentsAdvisors(student, pageable).getContent();
            }
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    // list of students for advisor
    public void studentDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            students = userService.findAdvisorsStudents(advisor, pageable).getContent();

        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }
    
    public ChatsView(AuthenticationContext authContext, UserService userService, AuthenticatedUser authenticatedUser, ChatService chatService) {
        this.authContext = authContext;
        this.userService = userService;
        this.chatService = chatService;
        this.authenticatedUser = authenticatedUser;

        // get logged in user
        Optional<User> opUser = userService.get(authenticatedUser.get().get().getId());
        User sender = opUser.orElseThrow(() -> new IllegalArgumentException("Sender User is required"));

        Optional<Advisor> opAdvisor = userService.getAdvisor(authenticatedUser.get().get().getId());
        advisor = opAdvisor.orElse(null);

        Optional<Student> opStudent = userService.getStudent(authenticatedUser.get().get().getId());
        student = opStudent.orElse(null);

        setSizeFull();

        // user select fields
        ComboBox<Advisor> advisorCombo = new ComboBox<>("Select Advisor");
        // Remove the currently logged-in advisor from the list
        advisorCombo.setItemLabelGenerator(advisor -> advisor.getName() + " (" + advisor.getEmail() + ")");
        advisorCombo.setWidthFull();
        advisorCombo.setClearButtonVisible(true);
        ComboBox<Student> studentCombo = new ComboBox<>("Select Student");
        studentCombo.setItemLabelGenerator(student -> student.getName() + " (" + student.getEmail() + ")");
        studentCombo.setWidthFull();
        studentCombo.setClearButtonVisible(true);

        // show combos to select users based on user role
        if (authContext.hasRole("ADVISOR")) {
            advisorDataSourceUpdate("ADVISOR", advisor, null);
            studentDataSourceUpdate();
            studentCombo.setItems(students);
            add(advisorCombo, studentCombo);
        } else if (authContext.hasRole("STUDENT")) {
            advisorDataSourceUpdate("STUDENT", null, student);
            add(advisorCombo);
        }

        advisorCombo.setItems(advisors);

        var messageList = new MessageList();
        messageList.setWidthFull();
        var textInput = new MessageInput();

        setSizeFull();
        
        textInput.setWidthFull();

        // refresh chats for selected advisor
        advisorCombo.addValueChangeListener(event -> {
            studentCombo.setEnabled(false);
            clearMessages(messageList);
            if (advisorCombo.getValue() != null) {
                loadMessages(sender, advisorCombo.getValue(), messageList);
                add(messageList, textInput);
                expand(messageList);
            }
            else if (studentCombo.isEmpty()) {
                messageSubscription.dispose();
                studentCombo.setEnabled(true);
            }
        });
    
        // refresh chats for selected student
        studentCombo.addValueChangeListener(event -> {
            advisorCombo.setEnabled(false);
            clearMessages(messageList);
            if (studentCombo.getValue() != null) {
                loadMessages(sender, studentCombo.getValue(), messageList);
                add(messageList, textInput);
                expand(messageList);
            }
            else if (studentCombo.isEmpty()) {
                messageSubscription.dispose();
                advisorCombo.setEnabled(true);
            }
        });

        // message send
        textInput.addSubmitListener(event -> {
            if (authContext.hasRole("ADVISOR")) {
                // If selecting a student
                if (advisorCombo.isEmpty()) {
                    chatService.add(sender, studentCombo.getValue(), event.getValue());
                }
                // If selecting an advisor
                else if (studentCombo.isEmpty()) {
                    chatService.add(sender, advisorCombo.getValue(), event.getValue());
                }
            }
            else if (authContext.hasRole("STUDENT")) {
                chatService.add(sender, advisorCombo.getValue(), event.getValue());
            }
        });
        
    }

    // Method to clear messages from view 
    private void clearMessages(MessageList messageList) {
        messageList.setItems(new ArrayList<>());
    }
    
    private void loadMessages(User sender, User receiver, MessageList messageList) {
        // Clear existing subscription if any
        if (messageSubscription != null && !messageSubscription.isDisposed()) {
            messageSubscription.dispose();
        }

        // Clear existing messages
        clearMessages(messageList);

        var newMessageList = new ArrayList<>(messageList.getItems());
        
        // Subscribe to new messages
        messageSubscription = chatService.join(sender, receiver).subscribe(message -> {

            MessageListItem messageItem = new MessageListItem(
                message.getText(),
                message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant(),
                message.getSender().getName()
            );

            if (message.getSender().equals(sender)) {
                messageItem.setUserColorIndex(1); // Differentiate sender
            } else {
                messageItem.setUserColorIndex(2); // Differentiate receiver
            }

            newMessageList.add(messageItem);
            
            getUI().ifPresent(ui -> ui.access((Command) () -> messageList.setItems(newMessageList)));
        });
    }

}