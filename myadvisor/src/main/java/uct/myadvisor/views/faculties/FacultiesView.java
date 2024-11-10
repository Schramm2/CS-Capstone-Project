package uct.myadvisor.views.faculties;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import uct.myadvisor.data.Faculty;
import uct.myadvisor.services.FacultyService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Faculties")
@Route(value = "faculties", layout = MainLayout.class)
@RolesAllowed({ "USER", "ADMIN" })

// Admin add faculties -> if faculty admin only show the faculty they apart of
// -> if admin dont allow editing of departments
// Admin -> (Table) list of faculties and be able to add new ones and delete
// Table on GPT
public class FacultiesView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final FacultyService facultyService;

    private TextField searchField = new TextField();

    private Grid<Faculty> grid;
    private GridListDataView<Faculty> gridListDataView;

    private List<Faculty> faculties;

    public FacultiesView(AuthenticationContext authContext, FacultyService facultyService) {
        this.authContext = authContext;
        this.facultyService = facultyService;

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void facultyDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            faculties = facultyService.list(pageable).getContent();
            gridListDataView = grid.setItems(faculties);
            // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(object.getName(), searchTerm);

                return matchesObject;
            });
            gridListDataView.refreshAll();
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
        grid = new Grid<>(Faculty.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        facultyDataSourceUpdate();

        // Grid Columns
        grid.addColumn(Faculty::getName)
                .setHeader("Faculty Name")
                .setFlexGrow(1);

        grid.addComponentColumn(faculty -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(faculty);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(faculty);
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

        Button addFaculty = new Button("Add a Faculty");
        addFaculty.setIcon(VaadinIcon.PLUS.create());
        addFaculty.addClickListener(e -> {
            openAddEditDialog(new Faculty(""));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addFaculty);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Faculty Dialogue
    private void openAddEditDialog(Faculty faculty) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Faculty");
        if (faculty.getId() == null) {
            dialog.setHeaderTitle("Add Faculty");
        }

        FormLayout layout = new FormLayout();

        // dialog fields
        TextField name = new TextField("Name", "");
        name.setValue(faculty.getName());

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Faculty> binder = new Binder<>(Faculty.class); // Data Validation

        binder.forField(name)
            .asRequired("Name cannot be empty") // Makes sure field isn't empty
            .bind(Faculty::getName, Faculty::setName);

        // save: update or add
        saveButton.addClickListener(event -> {
            faculty.setName(name.getValue());
            
            try {
                binder.writeBean(faculty);
                if (faculty.getId() == null) {
                    facultyService.update(faculty);
                    Notification.show("Added Successfully", 3000, Notification.Position.TOP_END);
                } else {
                    facultyService.update(faculty);
                    Notification.show("Changed Successfully", 3000, Notification.Position.TOP_END);
                }
                facultyDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
        });

        // add fields to dialog
        layout.add(name);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Faculty Dialogue
    private void openDeleteDialog(Faculty faculty) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Faculty");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + faculty.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                facultyService.delete(faculty.getId().intValue());
                Notification.show("Deleted Successfully", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END); 
            }
            facultyDataSourceUpdate();
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
