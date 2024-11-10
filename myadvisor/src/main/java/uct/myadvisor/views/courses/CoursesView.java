package uct.myadvisor.views.courses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Course;
import uct.myadvisor.data.Department;
import uct.myadvisor.data.Semester;
import uct.myadvisor.data.SmartTutorCourse;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.CourseService;
import uct.myadvisor.services.DepartmentService;
import uct.myadvisor.services.SemesterService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Courses")
@Route(value = "courses", layout = MainLayout.class)
@RolesAllowed({ "SENIOR_ADVISOR", "ADVISOR", "USER" })

// List of courses + details (prerequisites etc.) + CRUD view to
public class CoursesView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final SemesterService semesterService;
    private final UserService userService;

    private TextField searchField = new TextField();

    private Grid<Course> grid;
    private GridListDataView<Course> gridListDataView;

    private List<Course> courses;
    private List<Course> requisites;
    private List<Department> departments;
    private List<Semester> semesters;
    private Advisor advisor;

    public CoursesView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser,
            DepartmentService departmentService, CourseService courseService, SemesterService semesterService,
            UserService userService) {
        this.authContext = authContext;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.semesterService = semesterService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;

        Optional<Advisor> opUser = userService.getAdvisor(authenticatedUser.get().get().getId());
        advisor = opUser.orElse(null);

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void courseDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (advisor != null) {
                courses = courseService.findAllDepartmentCourses(advisor, pageable).getContent();
            } else {
                courses = courseService.list(pageable).getContent();
            }
            gridListDataView = grid.setItems(courses);
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(object.getName() + object.getCode() + object.getDepartmentName()
                        + object.getCredits() + object.getLevel(), searchTerm);

                return matchesObject;
            });

            gridListDataView.refreshAll();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    // Fetch updated data from the database and store it in the grid
    public void requisitesDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            requisites = courseService.list(pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    public void departmentDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (advisor != null) {
                departments = new ArrayList<>(advisor.getDepartments());
            } else {
                departments = departmentService.list(pageable).getContent();
            }
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    public void semesterDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            semesters = semesterService.list(pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    // List Search Function
    private boolean matchesTerm(String value, String searchTerm) {
        return StringUtils.containsIgnoreCase(value, searchTerm);
    }

    // Main List View with Search and Add Button
    private void createGrid() {
        grid = new Grid<>(Course.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        courseDataSourceUpdate();

        // Grid Columns
        grid.addColumn(createClientRenderer())
                .setHeader("Course")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Course::getDepartmentName)
                .setHeader("Department")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(Course::getCredits)
                .setHeader("Credits")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        Grid.Column<Course> levelColumn = grid.addColumn(Course::getLevel)
                .setHeader("Level")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);
        grid.sort(Arrays.asList(new GridSortOrder<>(levelColumn, SortDirection.ASCENDING)));

        grid.addComponentColumn(course -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(course);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(course);
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

        Button addButton = new Button("Add a Course");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Course("", "", 0, 0, null, null, null, null));
            
            gridListDataView.refreshAll();
        });

        // search filter

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Course Dialog
    private void openAddEditDialog(Course course) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Course");
        if (course.getId() == null) {
            dialog.setHeaderTitle("Add Course");
        }

        FormLayout layout = new FormLayout();

        departmentDataSourceUpdate();
        requisitesDataSourceUpdate();
        semesterDataSourceUpdate();

        // dialogue fields
        TextField name = new TextField("Name");
        name.setValue(course.getName());
        TextField code = new TextField("Code");
        code.setValue(course.getCode());
        IntegerField level = new IntegerField("Level");
        level.setValue(course.getLevel());
        IntegerField credits = new IntegerField("Credits");
        credits.setValue(course.getCredits());
        ComboBox<Department> departmentCombo = new ComboBox<>("Department");
        departmentCombo.setItems(departments);
        departmentCombo.setItemLabelGenerator(department -> department.getFacultyName() + " > " + department.getName());
        departmentCombo.setValue(course.getDepartment());
        departmentCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Semester> semesterMultiCombo = new MultiSelectComboBox<>("Semesters");
        semesterMultiCombo.setItems(semesters);
        semesterMultiCombo.setItemLabelGenerator(Semester::getName);
        semesterMultiCombo.setValue(course.getSemesters());
        semesterMultiCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Course> prerequisiteMultiCombo = new MultiSelectComboBox<>("Prerequisites");
        prerequisiteMultiCombo.setItems(requisites);
        prerequisiteMultiCombo.setItemLabelGenerator(Course::getCode);
        prerequisiteMultiCombo.setValue(course.getPrerequisites());
        prerequisiteMultiCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Course> corequisiteMultiCombo = new MultiSelectComboBox<>("Corequisites");
        corequisiteMultiCombo.setItems(requisites);
        corequisiteMultiCombo.setItemLabelGenerator(Course::getCode);
        corequisiteMultiCombo.setValue(course.getCorequisites());
        corequisiteMultiCombo.setClearButtonVisible(true);

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Course> binder = new Binder<>(Course.class); // Data Validation

        binder.forField(name)
                .asRequired("Name cannot be empty") // Makes sure field isn't empty
                .bind(Course::getName, Course::setName);
        binder.forField(code)
                .asRequired("Code cannot be empty") // Makes sure field isn't empty
                .bind(Course::getCode, Course::setCode);
        binder.forField(level)
                .asRequired("Level cannot be empty") // Makes sure field isn't empty
                .bind(Course::getLevel, Course::setLevel);
        binder.forField(credits)
                .asRequired("Credits cannot be empty") // Makes sure field isn't empty
                .bind(Course::getCredits, Course::setCredits);
        binder.forField(departmentCombo)
                .asRequired("Faculty cannot be empty") // Makes sure field isn't empty
                .bind(Course::getDepartment, Course::setDepartment);
        binder.forField(semesterMultiCombo)
                .asRequired("Semesters cannot be empty") // Makes sure field isn't empty
                .bind(Course::getSemesters, Course::setSemesters);

        // save: add or update
        saveButton.addClickListener(event -> {
            course.setName(name.getValue());
            course.setCode(code.getValue());
            course.setCredits(credits.getValue());
            course.setLevel(level.getValue());
            course.setDepartment(departmentCombo.getValue());
            course.setSemesters(semesterMultiCombo.getValue());
            course.setPrerequisites(prerequisiteMultiCombo.getValue());
            course.setCorequisites(corequisiteMultiCombo.getValue());

            try {
                binder.writeBean(course);
                if (course.getId() == null) {
                    courseService.update(course);
                    Notification.show("Added Successfully", 3000, Notification.Position.TOP_END);
                } else {
                    courseService.update(course);
                    Notification.show("Changed Successfully", 3000, Notification.Position.TOP_END);
                }
                courseDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add fields to dialog
        layout.add(name, code, credits, level, departmentCombo, semesterMultiCombo, prerequisiteMultiCombo,
                corequisiteMultiCombo);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Course Dialog
    private void openDeleteDialog(Course course) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Course");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + course.getCode() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                courseService.delete(course.getId());
                Notification.show("Deleted Successfully", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
            }
            courseDataSourceUpdate();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // add components to view
        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    // custom renderer for course name and code
    private static Renderer<Course> createClientRenderer() {
        return LitRenderer.<Course>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                        "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.courseCode} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        +
                        "${item.courseName}" +
                        "</span>" +
                        "</vaadin-vertical-layout>" +
                        "</vaadin-horizontal-layout>")
                .withProperty("courseCode", Course::getCode)
                .withProperty("courseName", Course::getName);
    }

}
