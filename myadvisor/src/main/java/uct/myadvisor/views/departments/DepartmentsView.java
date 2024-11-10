package uct.myadvisor.views.departments;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;
import uct.myadvisor.data.Admin;
import uct.myadvisor.data.Department;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.DepartmentService;
import uct.myadvisor.services.FacultyService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Departments")
@Route(value = "departments", layout = MainLayout.class)
@RolesAllowed({ "FACULTY_ADMIN", "ADMIN", "USER" })

public class DepartmentsView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final DepartmentService departmentService;
    private final FacultyService facultyService;
    private final UserService userService;

    private TextField searchField = new TextField();

    private Grid<Department> grid;
    private GridListDataView<Department> gridListDataView;

    private List<Department> departments;
    private List<Faculty> faculties;
    private Admin authAdmin;

    public DepartmentsView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser, DepartmentService departmentService, FacultyService facultyService, UserService userService) {
        this.authContext = authContext;
        this.departmentService = departmentService;
        this.facultyService = facultyService;
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        
        Optional<Admin> opAdmin = userService.getAdmin(authenticatedUser.get().get().getId());
        authAdmin = opAdmin.orElse(null);

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void departmentDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (authAdmin != null && authAdmin.getFaculty() != null) {
                departments = departmentService.findAllFacultyDepartments(authAdmin.getFaculty(), pageable).getContent();
            } else {
                departments = departmentService.list(pageable).getContent();
            }
            gridListDataView = grid.setItems(departments);
                    // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(object.getName() + object.getFacultyName(), searchTerm);

                return matchesObject;
            });
            gridListDataView.refreshAll();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
        }
    }

    // get faculties data
    public void facultyDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            faculties = facultyService.list(pageable).getContent();
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
        grid = new Grid<>(Department.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        departmentDataSourceUpdate();

        // Grid Columns
        grid.addColumn(Department::getName)
                .setHeader("Department Name")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(Department::getFacultyName)
                .setHeader("Faculty Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(department -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(department);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(department);
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

        Button addButton = new Button("Add a Department");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Department(null, ""));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add compoennts to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Department Dialogue
    private void openAddEditDialog(Department department) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Department");
        if (department.getId() == null) {
            dialog.setHeaderTitle("Add Department");
        }

        FormLayout layout = new FormLayout();

        // form fields
        TextField name = new TextField("Name");
        name.setValue(department.getName());
        ComboBox<Faculty> facultyCombo = new ComboBox<>("Faculty");
        facultyCombo.setItemLabelGenerator(Faculty::getName);
        facultyCombo.setClearButtonVisible(true);

        // set faculty if user is only allowed to add to that faculty
        if (authAdmin.getFaculty() != null) {
            if (authAdmin.getFaculty() != null) {
                faculties = Collections.singletonList(authAdmin.getFaculty());
                facultyCombo.setItems(faculties);
                if (department.getId() == null) {
                    facultyCombo.setValue(authAdmin.getFaculty());
                } else {
                    facultyCombo.setValue(department.getFaculty());
                }
            }
        } else {
            facultyDataSourceUpdate();
            facultyCombo.setItems(faculties);
            facultyCombo.setValue(department.getFaculty());
        }
        
        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Department> binder = new Binder<>(Department.class); // Data Validation

        binder.forField(name)
            .asRequired("Name cannot be empty") // Makes sure field isn't empty
            .bind(Department::getName, Department::setName);
        binder.forField(facultyCombo)
            .asRequired("Faculty cannot be empty") // Makes sure field isn't empty
            .bind(Department::getFaculty, Department::setFaculty);

        // save: update or add
        saveButton.addClickListener(event -> {
            department.setName(name.getValue());
            department.setFaculty(facultyCombo.getValue());

            try {
                binder.writeBean(department);
                if (department.getId() == null) {
                    departmentService.update(department);
                    Notification.show("Added Successfully", 3000, Notification.Position.TOP_END);
                } else {
                    departmentService.update(department);
                    Notification.show("Changed Successfully", 3000, Notification.Position.TOP_END);
                }
                departmentDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add fields to dialog
        layout.add(name, facultyCombo);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Department Dialog
    private void openDeleteDialog(Department department) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Department");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + department.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                departmentService.delete(department.getId().intValue());
                Notification.show("Deleted Successfully", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
            departmentDataSourceUpdate();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // add components to dialog
        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

}
