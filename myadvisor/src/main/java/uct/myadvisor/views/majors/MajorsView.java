package uct.myadvisor.views.majors;

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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
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
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Course;
import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Major;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.CourseService;
import uct.myadvisor.services.DegreeService;
import uct.myadvisor.services.MajorService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Majors")
@Route(value = "majors", layout = MainLayout.class)
@RolesAllowed({ "SENIOR_ADVISOR", "ADVISOR", "USER" })

// List of majors and rules (required + elective courses) - Do we need both
// (major + degree)
public class MajorsView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final DegreeService degreeService;
    private final CourseService courseService;
    private final MajorService majorService;
    private final UserService userService;

    private TextField searchField = new TextField();

    private Grid<Major> grid;
    private GridListDataView<Major> gridListDataView;

    private List<Major> majors;
    private List<Course> requiredCourses;
    private List<Course> electiveCourses;
    private List<Degree> degrees;
    private Advisor advisor;

    public MajorsView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser,
            DegreeService degreeService, MajorService majorService, CourseService courseService,
            UserService userService) {
        this.authContext = authContext;
        this.degreeService = degreeService;
        this.majorService = majorService;
        this.courseService = courseService;
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        Optional<Advisor> opUser = userService.getAdvisor(authenticatedUser.get().get().getId());
        advisor = opUser.orElse(null);

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void majorDataSourceUpdate() {
        Pageable pageable = PageRequest.ofSize(900000000);
        try {
            if (advisor != null) {
                majors = majorService.findAllFacultyMajors(advisor.getFaculty(), pageable).getContent();
            } else {
                majors = majorService.list(pageable).getContent();
            }
            gridListDataView = grid.setItems(majors);
                    // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(
                        object.getName() + object.getCode() + object.getDegreeName() + object.getCredits(), searchTerm);

                return matchesObject;
            });
            gridListDataView.refreshAll();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    // degree data
    public void degreeDataSourceUpdate() {
        Pageable pageable = PageRequest.ofSize(900000000);
        try {
            if (advisor != null) {
                degrees = degreeService.findFacultyDegrees(advisor.getFaculty(), pageable).getContent();
            } else {
                degrees = degreeService.list(pageable).getContent();
            }
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    // course data
    public void courseDataSourceUpdate() {
        Pageable pageable = PageRequest.ofSize(900000000);
        try {
            requiredCourses = courseService.list(pageable).getContent();
            electiveCourses = courseService.list(pageable).getContent();
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
        grid = new Grid<>(Major.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        majorDataSourceUpdate();

        // Grid Columns
        grid.addColumn(createClientRenderer())
                .setHeader("Major Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Major::getDegreeName)
                .setHeader("Degree Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(Major::getCredits)
                .setHeader("Credits")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(major -> {
            if (authContext.hasRole("SENIOR_ADVISOR")) {
                HorizontalLayout buttons = new HorizontalLayout();
                Button editButton = new Button("Edit");
                Button deleteButton = new Button("Delete");
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                editButton.addClickListener(event -> {
                    openAddEditDialog(major);
                });
                deleteButton.addClickListener(event -> {
                    openDeleteDialog(major);
                });
                buttons.add(editButton, deleteButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            } else {
                HorizontalLayout buttons = new HorizontalLayout();
                Button editButton = new Button("Edit");
                
                editButton.addClickListener(event -> {
                    openAddEditDialog(major);
                });
                
                buttons.add(editButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            }

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

        Button addButton = new Button("Add a Major");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Major("", "", 0, null));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Major Dialogue
    private void openAddEditDialog(Major major) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Major");
        if (major.getId() == null) {
            dialog.setHeaderTitle("Add Major");
        }

        FormLayout layout = new FormLayout();

        degreeDataSourceUpdate();
        courseDataSourceUpdate();

        // dialog fields
        TextField name = new TextField("Name");
        name.setValue(major.getName());
        TextField code = new TextField("Code");
        code.setValue(major.getCode());
        IntegerField credits = new IntegerField("Credits");
        credits.setValue(major.getCredits());
        ComboBox<Degree> degreeCombo = new ComboBox<>("Degree");
        degreeCombo.setItems(degrees);
        degreeCombo.setItemLabelGenerator(degree -> degree.getFacultyName() + " > " + degree.getName());
        degreeCombo.setRequired(true);
        degreeCombo.setValue(major.getDegree());
        degreeCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Course> requiredCourseMultiCombo = new MultiSelectComboBox<>("Required Courses");
        requiredCourseMultiCombo.setItems(requiredCourses);
        requiredCourseMultiCombo.setItemLabelGenerator(Course::getCode);
        requiredCourseMultiCombo.setValue(major.getRequired());
        requiredCourseMultiCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Course> ElectiveMultiCombo = new MultiSelectComboBox<>("Elective Courses");
        ElectiveMultiCombo.setItems(electiveCourses);
        ElectiveMultiCombo.setItemLabelGenerator(Course::getCode);
        ElectiveMultiCombo.setValue(major.getElectives());
        ElectiveMultiCombo.setClearButtonVisible(true);

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Major> binder = new Binder<>(Major.class); // Data Validation

        binder.forField(name)
                .asRequired("Name cannot be empty") // Makes sure field isn't empty
                .bind(Major::getName, Major::setName);
        binder.forField(code)
                .asRequired("Code cannot be empty") // Makes sure field isn't empty
                .bind(Major::getCode, Major::setCode);
        binder.forField(credits)
                .asRequired("Credits cannot be empty") // Makes sure field isn't empty
                .bind(Major::getCredits, Major::setCredits);
        binder.forField(degreeCombo)
                .asRequired("Faculty cannot be empty") // Makes sure field isn't empty
                .bind(Major::getDegree, Major::setDegree);

        // save: update or add
        saveButton.addClickListener(event -> {
            major.setName(name.getValue());
            major.setCode(code.getValue());
            major.setCredits(credits.getValue());
            major.setDegree(degreeCombo.getValue());
            major.setRequired(requiredCourseMultiCombo.getValue());
            major.setElectives(ElectiveMultiCombo.getValue());

            try {
                binder.writeBean(major);
                if (major.getId() == null) {
                    majorService.update(major);
                    Notification.show("Added Successfully", 3000, Position.TOP_END);
                } else {
                    majorService.update(major);
                    Notification.show("Changed Successfully", 3000, Position.TOP_END);
                }
                majorDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add fields to dialog
        layout.add(name, code, credits, degreeCombo, requiredCourseMultiCombo, ElectiveMultiCombo);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Major Dialogue
    private void openDeleteDialog(Major major) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Major");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + major.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                majorService.delete(major.getId().intValue());
                Notification.show("Deleted Successfully", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
            degreeDataSourceUpdate();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    // custom renderer for major name and code
    private static Renderer<Major> createClientRenderer() {
        return LitRenderer.<Major>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                        "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.majorName} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        +
                        "${item.majorCode}" +
                        "</span>" +
                        "</vaadin-vertical-layout>" +
                        "</vaadin-horizontal-layout>")
                .withProperty("majorName", Major::getName)
                .withProperty("majorCode", Major::getCode);
    }

}
