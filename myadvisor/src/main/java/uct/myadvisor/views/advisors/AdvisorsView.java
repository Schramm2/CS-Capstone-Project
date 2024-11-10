package uct.myadvisor.views.advisors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import uct.myadvisor.data.Admin;
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Department;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Major;
import uct.myadvisor.data.Role;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.DepartmentService;
import uct.myadvisor.services.FacultyService;
import uct.myadvisor.services.MajorService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

// List of Advisors and their details
// Details of cluster for senior advisor: Name, Peoplesoft Number, Chat Button, Files Button
// Details of advisor: Name, Peoplesoft Number, Edit Button (will be able to make an advisor senior), Delete Button

@PageTitle("Advisors")
@Route(value = "advisors", layout = MainLayout.class)
@RolesAllowed({ "SENIOR_ADVISOR", "FACULTY_ADMIN" })
public class AdvisorsView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final FacultyService facultyService;
    private final DepartmentService departmentService;
    private final MajorService majorService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private TextField searchField = new TextField();

    private Grid<Advisor> grid;
    private GridListDataView<Advisor> gridListDataView;

    private List<Advisor> advisors;
    private List<Faculty> facultys;
    private List<Department> departments;
    private List<Major> majors;
    private Advisor authAdvisor;
    private Admin authAdmin;

    public AdvisorsView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser,
            FacultyService facultyService, DepartmentService departmentService, MajorService majorService,
            UserService userService, PasswordEncoder passwordEncoder) {
        this.authContext = authContext;
        this.facultyService = facultyService;
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUser = authenticatedUser;

        Optional<Advisor> opAdvisor = userService.getAdvisor(authenticatedUser.get().get().getId());
        authAdvisor = opAdvisor.orElse(null);

        Optional<Admin> opAdmin = userService.getAdmin(authenticatedUser.get().get().getId());
        authAdmin = opAdmin.orElse(null);

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void advisorDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (authAdvisor != null) {
                advisors = userService.findFacultyAdvisors(authAdvisor.getFaculty(), pageable).getContent();
            } else if (authAdmin != null) {
                advisors = userService.findFacultyAdvisors(authAdmin.getFaculty(), pageable).getContent();
            } else {
                advisors = userService.listAdvisors(pageable).getContent();
            }
            gridListDataView = grid.setItems(advisors);
            // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(
                        object.getName() + object.getEmail() + object.getPsNumber() + object.getFacultyName(),
                        searchTerm);

                return matchesObject;
            });
            gridListDataView.refreshAll();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    // faculties combo data source
    public void facultyDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (authAdvisor != null) {
                facultys = Collections.singletonList(authAdvisor.getFaculty());
            } else if (authAdmin != null) {
                facultys = Collections.singletonList(authAdmin.getFaculty());
            } else {
                facultys = facultyService.list(pageable).getContent();
            }
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    public void departmentDataSourceUpdate(Faculty faculty) {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            departments = departmentService.findAllFacultyDepartments(faculty, pageable).getContent();
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
        }
    }

    public void majorDataSourceUpdate(Faculty faculty) {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            majors = majorService.findAllFacultyMajors(faculty, pageable).getContent();
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
        grid = new Grid<>(Advisor.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        advisorDataSourceUpdate();

        // Grid Columns
        grid.addColumn(createClientRenderer())
                .setHeader("Advisor Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Advisor::getPsNumber)
                .setHeader("Peoplesoft Number")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(Advisor::getFacultyName)
                .setHeader("Faculty Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(gridAdvisor -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(gridAdvisor);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(gridAdvisor);
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

        Button addButton = new Button("Add a Advisor");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Advisor(null, null, null, null));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Advisor Dialog
    private void openAddEditDialog(Advisor thisAdvisor) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Advisor");
        if (thisAdvisor.getId() == null) {
            dialog.setHeaderTitle("Add Advisor");
        }

        FormLayout layout = new FormLayout();

        // fields for dialog
        TextField name = new TextField("Name");
        name.setValue(thisAdvisor.getName());
        TextField username = new TextField("Username");
        username.setValue(thisAdvisor.getUsername());
        TextField psNumber = new TextField("Peoplesoft Number");
        psNumber.setValue(thisAdvisor.getPsNumber());
        TextField email = new TextField("Email");
        email.setValue(thisAdvisor.getEmail());
        TextField phone = new TextField("Phone");
        phone.setValue(thisAdvisor.getPhone());
        PasswordField password = new PasswordField("New Password");
        ComboBox<Faculty> facultyCombo = new ComboBox<>("Faculty");
        facultyCombo.setItemLabelGenerator(Faculty::getName);
        facultyCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Department> departmentMultiCombo = new MultiSelectComboBox<>("Departments");
        departmentMultiCombo
                .setItemLabelGenerator(department -> department.getFacultyName() + " > " + department.getName());
        departmentMultiCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Major> majorMultiCombo = new MultiSelectComboBox<>("Majors");
        majorMultiCombo.setItemLabelGenerator(major -> major.getDegree().getFacultyName() + " > " + major.getName());
        majorMultiCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Role> roleMultiCombo = new MultiSelectComboBox<>("Roles:");
        roleMultiCombo.setItems(Role.ADVISOR, Role.SENIOR_ADVISOR);
        roleMultiCombo.setValue(thisAdvisor.getRoles());
        roleMultiCombo.setClearButtonVisible(true);
        roleMultiCombo.setItemLabelGenerator(role -> {
            switch (role) {
                case ADVISOR:
                    return "Advisor";
                case SENIOR_ADVISOR:
                    return "Senior Advisor";
                default:
                    return role.name();
            }
        });

        // set combo data values and list items from database
        facultyDataSourceUpdate();

        facultyCombo.setItems(facultys);
        if (authAdvisor != null && thisAdvisor.getId() == null) {
            facultyCombo.setValue(authAdvisor.getFaculty());
        } else if (authAdmin != null && thisAdvisor.getId() == null) {
            facultyCombo.setValue(authAdmin.getFaculty());
        } else {
            facultyCombo.setValue(thisAdvisor.getFaculty());
        }

        if (facultyCombo != null) {
            majorDataSourceUpdate(facultyCombo.getValue());
            departmentDataSourceUpdate(facultyCombo.getValue());

            majorMultiCombo.setItems(majors);
            majorMultiCombo.setValue(thisAdvisor.getMajors());
            departmentMultiCombo.setItems(departments);
            departmentMultiCombo.setValue(thisAdvisor.getDepartments());
        }

        facultyCombo.addValueChangeListener(event -> {
            majorMultiCombo.clear();
            departmentMultiCombo.clear();
            if (!facultyCombo.isEmpty()) {
                majorDataSourceUpdate(facultyCombo.getValue());
                majorMultiCombo.setItems(majors);
                departmentDataSourceUpdate(facultyCombo.getValue());
                departmentMultiCombo.setItems(departments);
            }
        });

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Advisor> binder = new Binder<>(Advisor.class); // Data Validation

        binder.forField(name)
                .asRequired("Name cannot be empty") // Makes sure field isn't empty
                .bind(Advisor::getName, Advisor::setName);
        binder.forField(username)
                .asRequired("User cannot be empty") // Makes sure field isn't empty
                .bind(Advisor::getUsername, Advisor::setUsername);
        binder.forField(psNumber)
                .asRequired("Peoplesoft Number cannot be empty") // Makes sure field isn't empty
                .bind(Advisor::getPsNumber, Advisor::setPsNumber);
        binder.forField(email)
                .asRequired("Email cannot be empty") // Makes sure field isn't empty
                .bind(Advisor::getEmail, Advisor::setEmail);
        binder.forField(facultyCombo)
                .asRequired("Faculty cannot be empty") // Makes sure field isn't empty
                .bind(Advisor::getFaculty, Advisor::setFaculty);
        binder.forField(roleMultiCombo)
                .asRequired("Roles cannot be empty") // Makes sure field isn't empty
                .bind(Advisor::getRoles, Advisor::setRoles);
        if (thisAdvisor.getId() == null) {
            binder.forField(password)
                    .asRequired("Password must be set for new user")
                    .withConverter(
                            plainPassword -> passwordEncoder.encode(plainPassword),
                            encodedPassword -> "")
                    .bind(Advisor::getHashedPassword, Advisor::setHashedPassword);
        }

        // save: update or create
        saveButton.addClickListener(event -> {
            thisAdvisor.setName(name.getValue());
            thisAdvisor.setPsNumber(psNumber.getValue());
            thisAdvisor.setEmail(email.getValue());
            thisAdvisor.setPhone(phone.getValue());
            thisAdvisor.setFaculty(facultyCombo.getValue());
            thisAdvisor.setDepartments(departmentMultiCombo.getValue());
            thisAdvisor.setMajors(majorMultiCombo.getValue());
            thisAdvisor.setRoles(roleMultiCombo.getSelectedItems());
            if (password.getValue() != "") {
                thisAdvisor.setHashedPassword(passwordEncoder.encode(password.getValue()));

            }

            try {
                binder.writeBean(thisAdvisor);
                if (thisAdvisor.getId() == null) {
                    userService.updateAdvisor(thisAdvisor);
                    Notification.show("Added Successfully", 3000, Position.TOP_END);
                } else {
                    userService.updateAdvisor(thisAdvisor);
                    Notification.show("Changed Successfully", 3000, Position.TOP_END);
                }
                advisorDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add fields to dialog
        layout.add(name, username, psNumber, roleMultiCombo, email, phone, facultyCombo, password, departmentMultiCombo,
                majorMultiCombo);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Advisor Dialog
    private void openDeleteDialog(Advisor thisAdvisor) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Advisor");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + thisAdvisor.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                userService.deleteAdvisor(thisAdvisor.getId());
                Notification.show("Deleted Successfully", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
            advisorDataSourceUpdate();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // add items to dialog
        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    // custom renderer for name and avatar
    private static Renderer<Advisor> createClientRenderer() {
        return LitRenderer.<Advisor>of(
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
                .withProperty("avatar", Advisor::getAvatar)
                .withProperty("name", Advisor::getName)
                .withProperty("email", Advisor::getEmail);
    }

}
