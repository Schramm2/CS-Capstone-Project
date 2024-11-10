package uct.myadvisor.views.register;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import uct.myadvisor.data.Student;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.login.LoginView;

@Route(value = "register")
@AnonymousAllowed
public class RegisterView extends LoginOverlay {

    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordField confirmPassword;
    
    public RegisterView(AuthenticatedUser authenticatedUser, UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("MyAdvisor");
        i18n.getHeader().setDescription("Register using your UCT email and password.");
        i18n.setAdditionalInformation(null);
        i18n.getForm().setTitle("Register");
        i18n.getForm().setSubmit("Register");
        i18n.getForm().setUsername("Email");

        confirmPassword = new PasswordField("Confirm Password");
        
        // Password constraints
        confirmPassword.setRequired(true);
        confirmPassword.setHelperText("6 to 12 characters. Only letters A-Z and numbers supported.");

        getCustomFormArea().add(confirmPassword);

        VerticalLayout loginLayout = new VerticalLayout();
        Paragraph loginText = new Paragraph("Already apart of MyAdvisor?");
        Anchor loginLink = new Anchor("/login", "Log in!");

        // add components to view
        loginLayout.add(loginText, loginLink);
        loginLayout.setSpacing(false);
        loginLayout.getThemeList().add("spacing-xs");
        loginLayout.setAlignItems(Alignment.CENTER);

        setI18n(i18n);

        getFooter().add(loginLayout);

        setForgotPasswordButtonVisible(false);
        setOpened(true);

        // To register the user
        addLoginListener(this::handleRegistration);
        
    }  

    private void handleRegistration(LoginEvent event) {
        String username = event.getUsername();
        String rawPassword = event.getPassword();

        // Validate email
        if (!validateEmail(username)) {
            event.getSource().showErrorMessage("Invalid email", "Please use your UCT email (studentnumber@myuct.ac.za)");
            return;  // Stops the registration process
        }
        
        // If passwords do not match, show an error and return early
        if (!validatePasswords(rawPassword, confirmPassword.getValue())) {
            event.getSource().showErrorMessage("Passwords do not match/satisfy constraints", "Please ensure passwords match and satisfy constraints");
            return;  // Stops the registration process
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Attempt to register the user
        Student student = new Student(username, hashedPassword);
        boolean registrationSuccessful = userService.createStudent(student);

        if (registrationSuccessful) {
            // Redirect to profile page after successful registration - needs fixing!!!!
            event.getSource().getUI().get().navigate(LoginView.class);
            Notification.show("Registration successfull, please log in", 3000, Notification.Position.TOP_END);
        } else {
            event.getSource().showErrorMessage("Email already exists", "Check that you have entered your email correctly or log in now");
        }

        // Prevent the default form submission
        event.getSource().setError(true);
    }

    private boolean validateEmail(String email) {
        // Regex pattern to enforce @myuct.ac.za domain
        String emailPattern = "^[a-zA-Z0-9._%+-]+@myuct\\.ac\\.za$";
        return email.matches(emailPattern);
    }

    private boolean validatePasswords(String password, String confirmPassword) {
        // Password constraints
        int minLength = 6;
        int maxLength = 12;
        String allowedCharPattern = "^[A-Za-z0-9]+$";
    
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            return false;
        }
    
        // Check if password length is within limits
        if (password.length() < minLength || password.length() > maxLength) {
            return false;
        }
    
        // Check if password contains only allowed characters
        if (!password.matches(allowedCharPattern)) {
            return false;
        }

        return true;
    }
    

    
}
