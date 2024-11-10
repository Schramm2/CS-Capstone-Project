package uct.myadvisor.views.admins;

import java.util.List;
import java.util.Set;

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
import uct.myadvisor.data.Faculty;
import uct.myadvisor.data.Role;
import uct.myadvisor.services.FacultyService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Admins")
@Route(value = "admins", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "USER" })
public class AdminView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final FacultyService facultyService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private TextField searchField = new TextField();

    private Grid<Admin> grid;
    private GridListDataView<Admin> gridListDataView;

    private List<Admin> admins;
    private List<Faculty> facultys;

    public AdminView(AuthenticationContext authContext, FacultyService facultyService, UserService userService,
            PasswordEncoder passwordEncoder) {
        this.authContext = authContext;
        this.facultyService = facultyService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void adminDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            admins = userService.listAdmins(pageable).getContent();
            gridListDataView = grid.setItems(admins);
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

    // fetch faculties
    public void facultyDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            facultys = facultyService.list(pageable).getContent();
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
        grid = new Grid<>(Admin.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        adminDataSourceUpdate();

        // Grid Columns
        grid.addColumn(createClientRenderer())
                .setHeader("Admin Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Admin::getPsNumber)
                .setHeader("Peoplesoft Number")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(Admin::getFacultyName)
                .setHeader("Faculty Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(admin -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(admin);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(admin);
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

        Button addButton = new Button("Add a Admin");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Admin(null, null));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Admin Dialog
    private void openAddEditDialog(Admin admin) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Admin");
        if (admin.getId() == null) {
            dialog.setHeaderTitle("Add Admin");
        }

        FormLayout layout = new FormLayout();

        facultyDataSourceUpdate();

        // fields for dialog
        TextField name = new TextField("Name");
        name.setValue(admin.getName());
        TextField username = new TextField("Username");
        username.setValue(admin.getUsername());
        TextField psNumber = new TextField("Peoplesoft Number");
        psNumber.setValue(admin.getPsNumber());
        TextField email = new TextField("Email");
        email.setValue(admin.getEmail());
        TextField phone = new TextField("Phone");
        phone.setValue(admin.getPhone());
        PasswordField password = new PasswordField("New Password");
        ComboBox<Faculty> facultyCombo = new ComboBox<>("Faculty");
        facultyCombo.setItems(facultys);
        facultyCombo.setItemLabelGenerator(Faculty::getName);
        facultyCombo.setValue(admin.getFaculty());
        facultyCombo.setClearButtonVisible(true);
        MultiSelectComboBox<Role> roleMultiCombo = new MultiSelectComboBox<>("Role:");
        roleMultiCombo.setItems(Role.ADMIN, Role.FACULTY_ADMIN);
        roleMultiCombo.setValue(admin.getRoles());
        facultyCombo.setClearButtonVisible(true);
        roleMultiCombo.setItemLabelGenerator(role -> {
            switch (role) {
                case ADMIN:
                    return "Admin";
                case FACULTY_ADMIN:
                    return "Faculty Admin";
                default:
                    return role.name();
            }
        });

        // limit the number of roles to 1
        roleMultiCombo.addValueChangeListener(event -> {
            Set<Role> selectedItems = event.getValue();
            if (selectedItems.size() > 1) {
                Notification.show("You can only select 1 role.", 3000, Position.TOP_END);
                roleMultiCombo.setValue(event.getOldValue());
            }
        });

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Admin> binder = new Binder<>(Admin.class); // Data Validation

        binder.forField(name)
                .asRequired("Name cannot be empty") // Makes sure field isn't empty
                .bind(Admin::getName, Admin::setName);
        binder.forField(username)
                .asRequired("User cannot be empty") // Makes sure field isn't empty
                .bind(Admin::getUsername, Admin::setUsername);
        binder.forField(psNumber)
                .asRequired("Peoplesoft Number cannot be empty") // Makes sure field isn't empty
                .bind(Admin::getPsNumber, Admin::setPsNumber);
        binder.forField(email)
                .asRequired("Email cannot be empty") // Makes sure field isn't empty
                .bind(Admin::getEmail, Admin::setEmail);
        binder.forField(facultyCombo)
                .asRequired("Faculty cannot be empty") // Makes sure field isn't empty
                .bind(Admin::getFaculty, Admin::setFaculty);
        binder.forField(roleMultiCombo)
                .asRequired("Role cannot be empty") // Makes sure field isn't empty
                .bind(Admin::getRoles, Admin::setRoles);

        // Role required for faculty admin but must be empty for admin
        roleMultiCombo.addValueChangeListener(event -> {
            Set<Role> selectedRoles = event.getValue();

            if (!selectedRoles.isEmpty()) {
                Role firstRole = selectedRoles.iterator().next();

                if (firstRole == Role.FACULTY_ADMIN) {
                    binder.forField(facultyCombo)
                            .asRequired("Roles cannot be empty")
                            .bind(Admin::getFaculty, Admin::setFaculty);
                } else if (firstRole == Role.ADMIN) {
                    binder.forField(facultyCombo)
                            .withValidator(faculty -> faculty == null, "Faculty must be empty for Admin")
                            .bind(Admin::getFaculty, Admin::setFaculty);
                }
            }
        });

        if (admin.getId() == null) {
            binder.forField(password)
                    .asRequired("Password must be set for new user")
                    .withConverter(
                            plainPassword -> passwordEncoder.encode(plainPassword),
                            encodedPassword -> "")
                    .bind(Admin::getHashedPassword, Admin::setHashedPassword);
        }

        // save: update or create
        saveButton.addClickListener(event -> {
            admin.setName(name.getValue());
            admin.setPsNumber(psNumber.getValue());
            admin.setEmail(email.getValue());
            admin.setPhone(phone.getValue());
            admin.setFaculty(facultyCombo.getValue());
            admin.setRoles(roleMultiCombo.getSelectedItems());
            if (password.getValue() != "") {
                admin.setHashedPassword(passwordEncoder.encode(password.getValue()));

            }

            try {
                binder.writeBean(admin);
                if (admin.getId() == null) {
                    userService.updateAdmin(admin);
                    Notification.show("Added Successfully", 3000, Position.TOP_END);
                } else {
                    userService.updateAdmin(admin);
                    Notification.show("Changed Successfully", 3000, Position.TOP_END);
                }
                adminDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add fields to dialog
        layout.add(name, username, psNumber, roleMultiCombo, email, phone, facultyCombo, password);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Admin Dialog
    private void openDeleteDialog(Admin admin) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Admin");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + admin.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                userService.deleteAdmin(admin.getId());
                Notification.show("Deleted Successfully", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END);
            }
            adminDataSourceUpdate();
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
    private static Renderer<Admin> createClientRenderer() {
        return LitRenderer.<Admin>of(
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
                .withProperty("avatar", Admin::getAvatar)
                .withProperty("name", Admin::getName)
                .withProperty("email", Admin::getEmail);
    }

}
