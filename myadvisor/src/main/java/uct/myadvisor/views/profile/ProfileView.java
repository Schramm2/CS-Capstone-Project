package uct.myadvisor.views.profile;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;
import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Major;
import uct.myadvisor.data.Student;
import uct.myadvisor.data.User;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.DegreeService;
import uct.myadvisor.services.FacultyService;
import uct.myadvisor.services.MajorService;
import uct.myadvisor.services.SmartTutorCourseService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@PageTitle("Profile")
@Route(value = "profile", layout = MainLayout.class)
@RolesAllowed({"STUDENT", "USER"})
public class ProfileView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final SmartTutorCourseService smartTutorCourseService;

    private final DegreeService degreeService;
    private List<Degree> degrees;

    private final MajorService majorService;
    private List<Major> majors;

    // Binder for the User entity
    private Binder<Student> binder = new Binder<>(Student.class);

    public ProfileView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser, UserService userService, DegreeService degreeService, MajorService majorService, SmartTutorCourseService smartTutorCourseService) {
        this.authContext = authContext;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.degreeService = degreeService;
        this.majorService = majorService;
        this.smartTutorCourseService = smartTutorCourseService;

        Optional<Student> student = userService.getStudent(authenticatedUser.get().get().getId());
        student.ifPresent(this::createProfileForm);
    }

    private void createProfileForm(Student student) {
        FormLayout formLayout = new FormLayout();

        // Fields for user profile
        TextField fullNameField = new TextField("Full Name");
        fullNameField.setRequired(true);

        TextField psField = new TextField("PeopleSoft Number");
        psField.setRequired(true);

        TextField snoField = new TextField("Student Number");
        snoField.setRequired(true);
        // ValueChangeListener to capitalise input
        snoField.addValueChangeListener(event -> {
            String capitalizedValue = event.getValue().toUpperCase();
            snoField.setValue(capitalizedValue); // Set the capitalized value back into the field
        });

        TextField emailField = new TextField("Email");
        emailField.setRequired(true);

        // Fetch all degrees from the database
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            degrees = degreeService.list(pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END); 
        }

        ComboBox<Degree> degreeComboBox = new ComboBox<>("Select a Degree");

        degreeComboBox.setItems(degrees);
        degreeComboBox.setItemLabelGenerator(degree -> degree.getFacultyName() + " > " + degree.getName());
        degreeComboBox.setPlaceholder("Choose a Degree");
        degreeComboBox.setClearButtonVisible(true);

        // Fetch all majors from the database
        try {
            majors = majorService.list(pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END); 
        }

        MultiSelectComboBox<Major> majorMultiComboBox = new MultiSelectComboBox<>("Select your Majors");
        majorMultiComboBox.setItems(majors);
        majorMultiComboBox.setItemLabelGenerator(major -> major.getDegreeName() + " > " + major.getName());
        majorMultiComboBox.setValue(student.getMajors());
        majorMultiComboBox.setClearButtonVisible(true);
        majorMultiComboBox.setHelperText("Modifying your majors will erase the Smart Tutor!");

        Set<Major> currentMajors = new HashSet<>(student.getMajors());
        Set<Major> newMajors = new HashSet<>(currentMajors);

        majorMultiComboBox.addValueChangeListener(event -> {
            newMajors.clear();
            newMajors.addAll(event.getValue());
        });

        // Use Binder to bind the fields to the Student
        binder.forField(fullNameField).asRequired("Enter your full name").bind(Student::getName, Student::setName);
        binder.forField(psField).withValidator(psNo -> psNo.length() == 7, "PeopleSoft Number is 7 characters long").asRequired("Enter your PeopleSoft Number").bind(Student::getPsNumber, Student::setPsNumber);
        binder.forField(snoField).withValidator(stuNum -> stuNum.length() == 9, "Student Number is 9 characters long").asRequired("Enter your Student Number").bind(Student::getStudentNumber, Student::setStudentNumber); 
        binder.forField(emailField).withValidator(email -> email.endsWith("@myuct.ac.za"),"Only @myuct.ac.za email addresses are allowed").asRequired("Enter your UCT Email").bind(Student::getUsername, Student::setUsername);
        binder.forField(degreeComboBox).asRequired("Select your Degree").bind(Student::getDegree, Student::setDegree);
        binder.forField(majorMultiComboBox).asRequired("Select your Majors").withValidator(majors -> majors.size() <= 3, "You can select a maximum of 3 majors").bind(Student::getMajors, Student::setMajors);

        // Set initial value in the fields using binder
        binder.readBean(student);

        Button cancelButton = new Button("Cancel", e -> {
            this.getUI().ifPresent(ui -> ui.navigate(""));
        });

        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // Make cancel button invisible if first time user logged in to ensure details entered
        if (authenticatedUser.get().get().getVersion() == 0) {
            cancelButton.setVisible(false);
        }

        Button saveButton = new Button("Save", e -> {
            // Write the user input back to the User object and save it
            if (binder.writeBeanIfValid(student)) {
                userService.updateStudent(student);
                // If majors have changed erase smart tutor data
                if (!currentMajors.equals(newMajors)) {
                    smartTutorCourseService.deleteAllSmartTutorCoursesForUser(student);
                }

                Notification.show("Profile updated successfully", 3000, Notification.Position.TOP_END);
                
                // Navigate to the home page after entering details
                this.getUI().ifPresent(ui -> {
                    ui.navigate(""); // Redirect to the home page
                    // Add a small delay and then refresh the page to update the MainLayout (footer doesnt update automatically)
                    ui.getPage().executeJs("setTimeout(function() { window.location.reload(); }, 100);");
                });
            } else {
                Notification.show("Please enter all information", 3000, Notification.Position.TOP_END);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setWidthFull(); 
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        formLayout.add(fullNameField, psField, snoField, emailField, degreeComboBox, majorMultiComboBox);

        // Form
        VerticalLayout layout = new VerticalLayout(formLayout);
        layout.setAlignItems(Alignment.CENTER);

        add(layout, buttonLayout);
    }
}
