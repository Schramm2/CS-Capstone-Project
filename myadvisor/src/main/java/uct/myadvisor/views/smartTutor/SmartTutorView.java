package uct.myadvisor.views.smartTutor;

import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Course;
import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Major;
import uct.myadvisor.data.Semester;
import uct.myadvisor.data.SmartTutorCourse;
import uct.myadvisor.data.Student;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.CourseService;
import uct.myadvisor.services.DegreeService;
import uct.myadvisor.services.MajorService;
import uct.myadvisor.services.SmartTutorCourseService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiConsumer;

@PageTitle("Smart Tutor")
@Route(value = "smarttutor", layout = MainLayout.class)
@RolesAllowed({"ADVISOR", "STUDENT"})
public class SmartTutorView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;

    private final SmartTutorCourseService smartTutorCourseService;
    private List<SmartTutorCourse> smartTutorCourses;

    private final MajorService majorService;
    private List<Major> majors;
    
    private final DegreeService degreeService;
    private List<Degree> degrees;

    private final CourseService courseService;
    private List<Course> allRequired;
    private List<Course> allElectives;

    private Grid<SmartTutorCourse> grid;
    private GridListDataView<SmartTutorCourse> gridListDataView;

    private final UserService userService;
    private Advisor authAdvisor;
    private Student authStudent;
    private Student selectedStudent;
    private List<Student> students;

    private ComboBox<Course> electiveCombo;

    private TextField searchField = new TextField();

    public SmartTutorView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser, UserService userService, SmartTutorCourseService smartTutorCourseService, MajorService majorService, CourseService courseService, DegreeService degreeService) {
        this.authContext = authContext;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.smartTutorCourseService = smartTutorCourseService;
        this.majorService = majorService;
        this.courseService = courseService;
        this.degreeService = degreeService;

        setSizeFull();

        // Check if user is an advisor or student
        if (authContext.hasRole("ADVISOR")) {
            Optional<Advisor> opAdvisor = userService.getAdvisor(authenticatedUser.get().get().getId());
            authAdvisor = opAdvisor.orElse(null);
            
            // Combobox for advisor to select student to view the files they've uploaded
            ComboBox<Student> studentCombo = new ComboBox<>();
            studentCombo.setWidthFull();
            studentCombo.setAllowCustomValue(false);

            studentCombo.setLabel("Select Student:");
            studentCombo.setItemLabelGenerator(student -> student.getName() + " (" + student.getEmail() + ")");

            studentDataSourceUpdate();
            studentCombo.setItems(students);
            add(studentCombo);

            studentCombo.addValueChangeListener(e -> {
                selectedStudent = e.getValue();
                if (selectedStudent != null && authAdvisor != null) {
                    if (grid != null) {
                        smartTutorCourseDataSourceUpdate();
                    }
                    else {
                        createGrid();
                    }
                }
            });
            
        }
        else if (authContext.hasRole("STUDENT")) {
            selectedStudent = (Student) authenticatedUser.get().orElse(null);
            // If NO smart tutor courses for student show welcome dialogue
            if (smartTutorCourseService.listAllStudentCourses(selectedStudent, PageRequest.of(0, 900000000)).getContent().isEmpty()) {
                openWelcomeDialogue();
            }
            else {
                createGrid();
            }
        }
    }

    // Fetch updated student data from the database used for student combobox
    public void studentDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            students = userService.findAdvisorsStudents(authAdvisor, pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    // Fetch updated smart tutor course data from the database and store it in the grid
    public void smartTutorCourseDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            smartTutorCourses = smartTutorCourseService.listAllStudentCourses(selectedStudent, pageable).getContent();
            // If no courses exist, add courses for student according to majors
            if (smartTutorCourses.isEmpty()) {
                courseService.addCoursesForStudentMajors(selectedStudent, pageable);
            } 
            // If courses exist, create the grid
            else {
                gridListDataView = grid.setItems(smartTutorCourses);
                // search filter
                gridListDataView.addFilter(object -> {
                    String searchTerm = searchField.getValue().trim();

                    if (searchTerm.isEmpty())
                        return true;

                    boolean matchesCode = matchesTerm(object.getCourseCode(), searchTerm);

                    return matchesCode;
                });
                gridListDataView.refreshAll();
            }
            
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 10000, Notification.Position.TOP_END); 
        }
    }

    // Fetch updated degree data from the database used for degree combobox
    public void degreeDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            degrees = degreeService.list(pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END); 
        }
    }

    // Fetch updated major data from the database used for major combobox
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
        grid = new Grid<>(SmartTutorCourse.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        smartTutorCourseDataSourceUpdate();

        VerticalLayout titleLayout = new VerticalLayout();
        H4 title = new H4(selectedStudent.getDegree().getName() + " in " + selectedStudent.getMajorsString());
        titleLayout.add(title);
        titleLayout.setAlignItems(Alignment.CENTER);

        // Grid Columns
        grid.addColumn(createCourseRenderer())
            .setHeader("Course")
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        grid.addColumn(createStatusComponentRenderer())
            .setHeader("Type")
            .setAutoWidth(true)
            .setFlexGrow(1)
            .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(SmartTutorCourse::getCourseCredits)
                .setHeader("Credits")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setKey("courseCredits");

        grid.addColumn(SmartTutorCourse::getPassedText)
                .setHeader("Passed")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(SmartTutorCourse::getSemesterCode)
                .setHeader("Semester Code")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        Grid.Column<SmartTutorCourse> yearColumn = grid.addColumn(SmartTutorCourse::getYear)
                .setHeader("Year Taken")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setSortable(true)
                .setKey("year");
        grid.sort(Arrays.asList(new GridSortOrder<>(yearColumn, SortDirection.ASCENDING)));

        grid.addComponentColumn(smartTutorCourse -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.setEnabled(!smartTutorCourse.getRequired());
            editButton.addClickListener(event -> {
                openEditDialog(smartTutorCourse);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(smartTutorCourse);
                updateTotalCreditsFooter();
            });
            
            buttons.add(editButton, deleteButton);
            buttons.setAlignItems(Alignment.END);
            return buttons;
        })
        .setHeader("Actions")
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setTextAlign(ColumnTextAlign.CENTER);

        searchField.setWidth("100%");
        searchField.setPlaceholder("Search");
        searchField.setSuffixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> gridListDataView.refreshAll());

        electiveCombo = new ComboBox<>();
        electiveCombo.setWidthFull();
        electiveCombo.setPlaceholder("Choose an elective");
        electiveCombo.setAllowCustomValue(false);
        electiveCombo.setItemLabelGenerator(course -> course.getCode() + " - " + course.getName());

        // Update elective combo box
        updateElectiveComboBox();

        Button addElective = new Button("Add Elective");

        electiveCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                addElective.setEnabled(true);
            } else {
                addElective.setEnabled(false);
            }
        });

        addElective.setEnabled(false);

        addElective.setIcon(VaadinIcon.PLUS.create());
        addElective.addClickListener(e -> {
            // Function to add elective to grid
            Course selectedCourse = electiveCombo.getValue();
            if (selectedCourse != null) {
                addElectiveCourse(selectedCourse);
                updateTotalCreditsFooter();
            }
        });

        Button resetButton = new Button("Reset");
        resetButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        resetButton.addClickListener(e -> {
            openResetDialog();
        });


        // Filter the grid based on search term
        gridListDataView.addFilter(object -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            boolean matchesObject = matchesTerm(object.getCourseCode() + object.getCourseCredits() + object.getPassedText() + object.getSemesterCode(), searchTerm);

            return matchesObject;
        });

        HorizontalLayout searchLayout;

        Span liabilityText = new Span("*Smart Tutor may not be 100% correct, please consult with your student advisor");
        liabilityText.getStyle().set("font-size", "small");
        liabilityText.getStyle().set("color", "var(--lumo-error-text-color)");

        searchLayout = new HorizontalLayout(searchField, electiveCombo, addElective, resetButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");

        // Footer Row for total credits
        updateTotalCreditsFooter();

        add(titleLayout, searchLayout, grid, liabilityText);
    }

    // Update the total credits footer for when grid rows are added or removed
    private void updateTotalCreditsFooter() {
        int totalCredits = calculateTotalCredits();
        grid.removeAllFooterRows();
        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(grid.getColumnByKey("courseCredits")).setText("Total Credits: " + totalCredits);
    }
    
    // Calculate the total credits for the student from all the courses in the grid
    private int calculateTotalCredits() {
        return smartTutorCourses.stream()
            .mapToInt(SmartTutorCourse::getCourseCredits)
            .sum();
    }

    // Update the elective combo box with courses that are not in the grid
    private void updateElectiveComboBox() {
        if (selectedStudent != null) {
            allElectives = new ArrayList<>(courseService.findAllStudentElectiveCourses(selectedStudent, PageRequest.of(0, 900000000)).getContent());

            smartTutorCourseDataSourceUpdate();

            // Remove courses that are already in the grid
            allElectives.removeAll(smartTutorCourses.stream()
                .map(SmartTutorCourse::getCourse)
                .collect(Collectors.toList()));
            
            electiveCombo.setItems(allElectives);
        }
    }

    // Add an elective course to the grid
    private void addElectiveCourse(Course course) {
        SmartTutorCourse newElective = new SmartTutorCourse();
        newElective.setCourse(course);
        newElective.setStudent(selectedStudent);
        newElective.setRequired(false); // Elective course
        newElective.setPassed(false); 
        newElective.setYear(course.determineYear(course)); 

        // Set a default semester or the first available semester for the course
        if (!course.getSemesters().isEmpty()) {
            newElective.setSemester(course.getSemesters().iterator().next());
        } else {
            Notification.show("No semesters available for this course", 3000, Notification.Position.TOP_END);
            return;
        }

        try {
            smartTutorCourseService.update(newElective);
            Notification.show("Elective Added Successfully", 3000, Notification.Position.TOP_END);
            smartTutorCourseDataSourceUpdate();
            updateElectiveComboBox(); // Update the combo box to remove the added course from elective list
            updateTotalCreditsFooter();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    // Edit SmartTutorCourse Dialogue
    private void openEditDialog(SmartTutorCourse smartTutorCourse) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit SmartTutorCourse");

        FormLayout layout = new FormLayout();

        ComboBox<Semester> semesterCombo = new ComboBox<>("Semester");
        semesterCombo.setItems(smartTutorCourse.getCourse().getSemesters());
        semesterCombo.setItemLabelGenerator(Semester::getName);
        semesterCombo.setValue(smartTutorCourse.getSemester());
        semesterCombo.setClearButtonVisible(true);
        
        IntegerField year = new IntegerField("Year Taken");
        year.setValue(smartTutorCourse.getYear());
        year.setMin(1);
        year.setStepButtonsVisible(true);
        year.setWidth("50%");

        Checkbox passed = new Checkbox("Passed?");
        passed.setValue(smartTutorCourse.getPassed());

        HorizontalLayout yearPassedLayout = new HorizontalLayout();
        yearPassedLayout.setAlignItems(Alignment.BASELINE);

        yearPassedLayout.add(year, passed);

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<SmartTutorCourse> binder = new Binder<>(SmartTutorCourse.class); // Data Validation

        binder.forField(semesterCombo)
            .asRequired("Semester cannot be empty") // Makes sure field isn't empty
            .bind(SmartTutorCourse::getSemester, SmartTutorCourse::setSemester);
        binder.forField(year)
            .asRequired("Year cannot be empty") // Makes sure field isn't empty
            .bind(SmartTutorCourse::getYear, SmartTutorCourse::setYear);

        // Save Button for updating/adding
        saveButton.addClickListener(event -> {
            smartTutorCourse.setPassed(passed.getValue());
            smartTutorCourse.setSemester(semesterCombo.getValue());
            smartTutorCourse.setYear(year.getValue());
            
            try {
                binder.writeBean(smartTutorCourse);
                smartTutorCourseService.update(smartTutorCourse); // Update the course
                Notification.show("Changed Successfully", 3000, Notification.Position.TOP_END);
                smartTutorCourseDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
        });

        layout.add(semesterCombo, yearPassedLayout);
        layout.setWidth("250px");
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete SmartTutorCourse Dialogue
    private void openDeleteDialog(SmartTutorCourse smartTutorCourse) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Smart Tutor Course");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + smartTutorCourse.getCourseName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        Button confirmButton = new Button("Delete", e -> {
            try {
                smartTutorCourseService.delete(smartTutorCourse.getId()); // Delete the course
                Notification.show("Deleted Successfully", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
            smartTutorCourseDataSourceUpdate();
            updateElectiveComboBox();
            updateTotalCreditsFooter();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    // Reset Smart Tutor Dialogue
    private void openResetDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Reset Smart Tutor");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to reset Smart Tutor?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        Button confirmButton = new Button("Reset", e -> {
            try {
                smartTutorCourseService.deleteAllSmartTutorCoursesForUser(selectedStudent); // Delete all courses
                Notification.show("Reset Successfully", 3000, Notification.Position.TOP_END);  
                UI.getCurrent().getPage().reload();  
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    // Open Welcome Dialogue
    private void openWelcomeDialogue() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Welcome to Smart Tutor!");

        FormLayout welcomeDialogueLayout = new FormLayout();

        ComboBox<Degree> degreeCombo = new ComboBox<>("Select Degree");
        degreeDataSourceUpdate();
        degreeCombo.setItems(degrees);
        degreeCombo.setValue(selectedStudent.getDegree());
        degreeCombo.setItemLabelGenerator(degree -> degree.getFacultyName() + " > " + degree.getName());
        degreeCombo.setClearButtonVisible(true);

        MultiSelectComboBox<Major> majorMultiCombo = new MultiSelectComboBox<>("Select Majors");
        majorMultiCombo.setItemLabelGenerator(major -> major.getDegreeName() + " > " + major.getName());
        majorMultiCombo.setClearButtonVisible(true);

        // Update major combo box based on selected degree
        if (degreeCombo.getValue() != null) {
            majorDataSourceUpdate(degreeCombo.getValue());
            majorMultiCombo.setItems(majors);
            majorMultiCombo.setValue(selectedStudent.getMajors());
        }

        degreeCombo.addValueChangeListener(event -> { 
            majorMultiCombo.clear();
            if (!degreeCombo.isEmpty()) {
                majorDataSourceUpdate(degreeCombo.getValue());
                majorMultiCombo.setItems(majors);
            }
        });

        Button getStartedButton = new Button("Get Started!");
        getStartedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Binder<Student> binder = new Binder<>(Student.class); // Data Validation
        binder.forField(degreeCombo).asRequired("Select your Degree").bind(Student::getDegree, Student::setDegree);
        binder.forField(majorMultiCombo).asRequired("Select your Majors").withValidator(majors -> majors.size() <= 3, "You can select a maximum of 3 majors").bind(Student::getMajors, Student::setMajors);

        getStartedButton.addClickListener(event -> {
            // Write the user input back to the User object and save it
            if (binder.writeBeanIfValid(selectedStudent)) {
                userService.updateStudent(selectedStudent); // Update the student
                Notification.show("Computing Smart Tutor", 3000, Notification.Position.TOP_END);
                createGrid();
                dialog.close();
            } else {
                Notification.show("Please enter all information", 3000, Notification.Position.TOP_END);
            }
        });

        cancelButton.addClickListener(event -> {
            dialog.close();
            // Go back home if student cancels welcome dialogue
            UI.getCurrent().navigate("");
        });

        welcomeDialogueLayout.add(degreeCombo, majorMultiCombo);
        dialog.getFooter().add(cancelButton, getStartedButton);
        dialog.add(welcomeDialogueLayout);
        dialog.open();
    }
    
    // Course Renderer to show course code and name
    private static Renderer<SmartTutorCourse> createCourseRenderer() {
        return LitRenderer.<SmartTutorCourse>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                    "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.courseCode} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">" +
                            "${item.courseName}" +
                        "</span>" +
                    "</vaadin-vertical-layout>" +
                "</vaadin-horizontal-layout>")
                .withProperty("courseCode", SmartTutorCourse::getCourseCode)
                .withProperty("courseName", SmartTutorCourse::getCourseName);
    }

    // Type badges for type of course
    private static final SerializableBiConsumer<Span, SmartTutorCourse> statusComponentUpdater = (span, smartTutorCourse) -> {
        span.setText(smartTutorCourse.getRequired() ? "Required" : "Elective");
        
        if (smartTutorCourse.getRequired()) {
            span.getElement().setAttribute("theme", "badge success " + "Required");
        } else {
            span.getElement().setAttribute("theme", "badge " + "Elective");
        }
     
    };

    // Status Renderer for type of course
    private static ComponentRenderer<Span, SmartTutorCourse> createStatusComponentRenderer() {
        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
    }
}
    

