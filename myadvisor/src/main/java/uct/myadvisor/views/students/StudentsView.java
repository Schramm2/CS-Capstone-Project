package uct.myadvisor.views.students;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Major;
import uct.myadvisor.data.Student;
import uct.myadvisor.services.DegreeService;
import uct.myadvisor.services.MajorService;
import uct.myadvisor.services.SmartTutorCourseService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

// Change roles to Peoplesoft Number, need to remember to add peoplesoft number values to clients.
// Remember to change edit dialog role combo box
// Add degree to edit student

@PageTitle("Students")
@Route(value = "students", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "USER" })

public class StudentsView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final DegreeService degreeService;
    private final MajorService majorService;
    private final UserService userService;
    private final SmartTutorCourseService smartTutorCourseService;
    private final PasswordEncoder passwordEncoder;

    private TextField searchField = new TextField();

    private Grid<Student> grid;
    private GridListDataView<Student> gridListDataView;

    private List<Student> students;
    private List<Degree> degrees;
    private List<Major> majors;

    public StudentsView(AuthenticationContext authContext, DegreeService degreeService, MajorService majorService,
            UserService userService, SmartTutorCourseService smartTutorCourseService, PasswordEncoder passwordEncoder) {
        this.authContext = authContext;
        this.degreeService = degreeService;
        this.majorService = majorService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.smartTutorCourseService = smartTutorCourseService;

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void studentDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            students = userService.listStudents(pageable).getContent();
            gridListDataView = grid.setItems(students);
            // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(
                        object.getName() + object.getEmail() + object.getPsNumber() + object.getDegreeName(),
                        searchTerm);

                return matchesObject;
            });
            gridListDataView.refreshAll();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    // degrees combo data source
    public void degreeDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            degrees = degreeService.list(pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    // majors combo data source
    public void majorDataSourceUpdate(Degree degree) {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            majors = majorService.findAllDegreeMajors(degree, pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    // List Search Function
    private boolean matchesTerm(String value, String searchTerm) {
        return StringUtils.containsIgnoreCase(value, searchTerm);
    }

    // Main List View with Search and Add Button
    private void createGrid() {
        grid = new Grid<>(Student.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        studentDataSourceUpdate();

        // Grid Columns
        grid.addColumn(createClientRenderer())
                .setHeader("Student Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Student::getPsNumber)
                .setHeader("Peoplesoft Number")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(Student::getDegreeName)
                .setHeader("Degree Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(student -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(student);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(student);
            });
            buttons.add(editButton, deleteButton);
            buttons.setAlignItems(Alignment.END);
            return buttons;
        })
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setSuffixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> gridListDataView.refreshAll());

        Button addButton = new Button("Add a Student");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Student(null, "", "", "", "", "", ""));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Student Dialog
    private void openAddEditDialog(Student student) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Student");
        if (student.getId() == null) {
            dialog.setHeaderTitle("Add Student");
        }

        FormLayout layout = new FormLayout();

        degreeDataSourceUpdate();

        // fields for dialog
        TextField name = new TextField("Name");
        name.setValue(student.getName());
        TextField username = new TextField("Username");
        username.setValue(student.getUsername());
        TextField psNumber = new TextField("Peoplesoft Number");
        psNumber.setValue(student.getPsNumber());
        TextField studentNumber = new TextField("Student Number");
        studentNumber.setValue(student.getStudentNumber());
        TextField email = new TextField("Email");
        email.setValue(student.getEmail());
        TextField phone = new TextField("Phone");
        phone.setValue(student.getPhone());
        PasswordField password = new PasswordField("New Password");
        ComboBox<Degree> degreeCombo = new ComboBox<>("Degree");
        degreeCombo.setItems(degrees);
        degreeCombo.setItemLabelGenerator(degree -> degree.getFacultyName() + " > " + degree.getName());
        degreeCombo.setValue(student.getDegree());
        degreeCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Major> majorMultiCombo = new MultiSelectComboBox<>("Majors");
        majorMultiCombo.setItemLabelGenerator(major -> major.getDegreeName() + " > " + major.getName());
        majorMultiCombo.setClearButtonVisible(true);
        majorMultiCombo.setHelperText("Modifying your majors will erase the Smart Tutor!");

        Set<Major> currentMajors = new HashSet<>(student.getMajors());
        Set<Major> newMajors = new HashSet<>(currentMajors);

        majorMultiCombo.addValueChangeListener(event -> {
            newMajors.clear();
            newMajors.addAll(event.getValue());
        });

        // manage degree combo change
        if (degreeCombo.getValue() != null) {
            majorDataSourceUpdate(degreeCombo.getValue());
            majorMultiCombo.setItems(majors);
            majorMultiCombo.setValue(student.getMajors());
        }

        degreeCombo.addValueChangeListener(event -> {
            majorMultiCombo.clear();
            if (!degreeCombo.isEmpty()) {
                majorDataSourceUpdate(degreeCombo.getValue());
                majorMultiCombo.setItems(majors);
            }
        });

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Student> binder = new Binder<>(Student.class); // Data Validation

        binder.forField(name)
                .asRequired("Name cannot be empty") // Makes sure field isn't empty
                .bind(Student::getName, Student::setName);
        binder.forField(username)
                .asRequired("User cannot be empty") // Makes sure field isn't empty
                .bind(Student::getUsername, Student::setUsername);
        binder.forField(psNumber)
                .asRequired("Peoplesoft Number cannot be empty") // Makes sure field isn't empty
                .bind(Student::getPsNumber, Student::setPsNumber);
        binder.forField(studentNumber)
                .asRequired("Student Number cannot be empty") // Makes sure field isn't empty
                .bind(Student::getStudentNumber, Student::setStudentNumber);
        binder.forField(email)
                .asRequired("Email cannot be empty") // Makes sure field isn't empty
                .bind(Student::getEmail, Student::setEmail);
        binder.forField(degreeCombo)
                .asRequired("Degree cannot be empty") // Makes sure field isn't empty
                .bind(Student::getDegree, Student::setDegree);
        if (student.getId() == null) {
            binder.forField(password)
                    .asRequired("Password must be set for new user")
                    .withConverter(
                            plainPassword -> passwordEncoder.encode(plainPassword),
                            encodedPassword -> "")
                    .bind(Student::getHashedPassword, Student::setHashedPassword);
        }

        // save: update or add
        saveButton.addClickListener(event -> {
            student.setName(name.getValue());
            student.setPsNumber(psNumber.getValue());
            student.setStudentNumber(studentNumber.getValue());
            student.setEmail(email.getValue());
            student.setPhone(phone.getValue());
            student.setMajors(majorMultiCombo.getValue());
            student.setDegree(degreeCombo.getValue());
            if (password.getValue() != "") {
                student.setHashedPassword(passwordEncoder.encode(password.getValue()));

            }

            try {
                binder.writeBean(student);
                if (student.getId() == null) {
                    userService.updateStudent(student);
                    // If majors have changed erase smart tutor data
                    if (!currentMajors.equals(newMajors)) {
                        smartTutorCourseService.deleteAllSmartTutorCoursesForUser(student);
                    }
                    Notification.show("Added Successfully", 3000, Position.TOP_END);
                } else {
                    userService.updateStudent(student);
                    Notification.show("Changed Successfully", 3000, Position.TOP_END);
                }
                studentDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add fields to layout
        layout.add(name, username, psNumber, studentNumber, email, phone, degreeCombo, majorMultiCombo, password);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Student Dialog
    private void openDeleteDialog(Student student) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Student");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + student.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                userService.deleteStudent(student.getId());
                Notification.show("Deleted Successfully", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
            studentDataSourceUpdate();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    // custom renderer for name and avatar
    private static Renderer<Student> createClientRenderer() {
        return LitRenderer.<Student>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                        "<vaadin-avatar img=\"${item.avatar}\" name=\"${item.name}\" alt=\"User avatar\"></vaadin-avatar>"
                        +
                        "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.name} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        +
                        "${item.email}" +
                        "</span>" +
                        "</vaadin-vertical-layout>" +
                        "</vaadin-horizontal-layout>")
                .withProperty("avatar", Student::getAvatar)
                .withProperty("name", Student::getName)
                .withProperty("email", Student::getEmail);
    }

}
