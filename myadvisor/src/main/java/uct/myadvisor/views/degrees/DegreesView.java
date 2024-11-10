package uct.myadvisor.views.degrees;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import uct.myadvisor.data.Admin;
import uct.myadvisor.data.Degree;
import uct.myadvisor.data.Faculty;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.DegreeService;
import uct.myadvisor.services.FacultyService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Degrees")
@Route(value = "degrees", layout = MainLayout.class)
@RolesAllowed({"FACULTY_ADMIN", "USER"})

// Faculty admin -> (table) view of all degrees + CRUD component for editing rules -> (dialog) table code, course etc. 
// - Do we need both (major + degree)
public class DegreesView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final DegreeService degreeService;
    private final FacultyService facultyService;
    private final UserService userService;

    private TextField searchField = new TextField();

    private Grid<Degree> grid;
    private GridListDataView<Degree> gridListDataView;

    private List<Degree> degrees;
    private List<Faculty> faculties;
    private Admin admin;

    public DegreesView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser, DegreeService degreeService, FacultyService facultyService, UserService userService) {
        this.authContext = authContext;
        this.degreeService = degreeService;
        this.facultyService = facultyService;
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        Optional<Admin> opUser = userService.getAdmin(authenticatedUser.get().get().getId());
        admin = opUser.orElse(null);

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void degreeDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (admin != null) {
                degrees = degreeService.findFacultyDegrees(admin.getFaculty(), pageable).getContent();
            } else {
                degrees = degreeService.list(pageable).getContent();
            }
            gridListDataView = grid.setItems(degrees);
                    // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(object.getName() + object.getCode() + object.getFacultyName() + object.getMinCredits() + object.getMaxCredits(), searchTerm);

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
        grid = new Grid<>(Degree.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        degreeDataSourceUpdate();

        // Grid Columns
        grid.addColumn(createClientRenderer())
                .setHeader("Degree Name")
                .setFlexGrow(3);

        grid.addColumn(Degree::getFacultyName)
                .setHeader("Faculty Name")
                .setFlexGrow(3);

        grid.addColumn(Degree::getMinCredits)
                .setHeader("Min Credits")
                .setFlexGrow(0);
        grid.addColumn(Degree::getMaxCredits)
                .setHeader("Max Credits")
                .setFlexGrow(0);

        grid.addComponentColumn(degree -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(degree);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(degree);
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

        Button addButton = new Button("Add a Degree");
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> {
            openAddEditDialog(new Degree(null, "", "", "", 0, 0));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Degree Dialog
    private void openAddEditDialog(Degree degree) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Degree");
        if (degree.getId() == null) {
            dialog.setHeaderTitle("Add Degree");
        }

        FormLayout layout = new FormLayout();

        // form fields
        TextField name = new TextField("Name");
        name.setValue(degree.getName());
        TextField code = new TextField("Code");
        code.setValue(degree.getCode());
        TextField requirements = new TextField("Requirements");
        requirements.setValue(degree.getRequirements());
        IntegerField minCredits = new IntegerField("Min Credits");
        minCredits.setValue(degree.getMinCredits());
        IntegerField maxCredits = new IntegerField("Max Credits");
        maxCredits.setValue(degree.getMaxCredits());
        ComboBox<Faculty> facultyCombo = new ComboBox<>("Faculty");
        facultyCombo.setItemLabelGenerator(Faculty::getName);
        facultyCombo.setClearButtonVisible(true);

        // pre set the faculty if the user is only allowed to add to their faculty
        if (admin != null) {
            if (admin.getFaculty() != null) {
                faculties = Collections.singletonList(admin.getFaculty());
                facultyCombo.setItems(faculties);
                if (degree.getId() == null) {
                    facultyCombo.setValue(admin.getFaculty());
                } else {
                    facultyCombo.setValue(degree.getFaculty());
                }
            }
        } else {
            facultyDataSourceUpdate();
            facultyCombo.setItems(faculties);
            facultyCombo.setValue(degree.getFaculty());
        }

        if (admin.getFaculty() != null && degree.getId() == null) {
            facultyCombo.setValue(admin.getFaculty());
        }

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Degree> binder = new Binder<>(Degree.class); // Data Validation

        binder.forField(name)
            .asRequired("Name cannot be empty") // Makes sure field isn't empty
            .bind(Degree::getName, Degree::setName);
        binder.forField(code)
            .asRequired("Code cannot be empty") // Makes sure field isn't empty
            .bind(Degree::getCode, Degree::setCode);
        binder.forField(minCredits)
            .asRequired("Min Credits cannot be empty") // Makes sure field isn't empty
            .bind(Degree::getMinCredits, Degree::setMinCredits);
        binder.forField(maxCredits)
            .asRequired("Max Credits cannot be empty") // Makes sure field isn't empty
            .bind(Degree::getMaxCredits, Degree::setMaxCredits);
        binder.forField(facultyCombo)
            .asRequired("Faculty cannot be empty") // Makes sure field isn't empty
            .bind(Degree::getFaculty, Degree::setFaculty);

        // save: update or add
        saveButton.addClickListener(event -> {
            degree.setName(name.getValue());
            degree.setCode(code.getValue());
            degree.setRequirements(requirements.getValue());
            degree.setMinCredits(minCredits.getValue());
            degree.setMaxCredits(maxCredits.getValue());
            degree.setFaculty(facultyCombo.getValue());

            try {
                binder.writeBean(degree);
                if (degree.getId() == null) {
                    degreeService.update(degree);
                    Notification.show("Added Successfully", 3000, Notification.Position.TOP_END);
                } else {
                    degreeService.update(degree);
                    Notification.show("Changed Successfully", 3000, Notification.Position.TOP_END);
                }
                degreeDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // add component to view
        layout.add(name, code, facultyCombo, requirements, minCredits, maxCredits);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Degree Dialog
    private void openDeleteDialog(Degree degree) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Degree");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + degree.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                degreeService.delete(degree.getId().intValue());
                Notification.show("Deleted Successfully", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
            degreeDataSourceUpdate();
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

    // custom renderer for degree name and code
    private static Renderer<Degree> createClientRenderer() {
        return LitRenderer.<Degree>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                    "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.degreeName} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">" +
                            "${item.degreeCode}" +
                        "</span>" +
                    "</vaadin-vertical-layout>" +
                "</vaadin-horizontal-layout>")
                .withProperty("degreeName", Degree::getName)
                .withProperty("degreeCode", Degree::getCode);
    }
    
}
