package uct.myadvisor.views.semesters;

import uct.myadvisor.data.Semester;

import uct.myadvisor.services.SemesterService;
import uct.myadvisor.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;

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
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

@PageTitle("Semesters")
@Route(value = "semesters", layout = MainLayout.class)
@RolesAllowed({ "USER", "ADMIN" })

// Admin add semesters
public class SemestersView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final SemesterService semesterService;

    private TextField searchField = new TextField();

    private Grid<Semester> grid;
    private GridListDataView<Semester> gridListDataView;

    private List<Semester> semesters;

    public SemestersView(AuthenticationContext authContext, SemesterService semesterService) {
        this.authContext = authContext;
        this.semesterService = semesterService;

        setSizeFull();
        createGrid();
        add(grid);
    }

    // Fetch updated data from the database and store it in the grid
    public void semesterDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            semesters = semesterService.list(pageable).getContent();
            gridListDataView = grid.setItems(semesters);
                    // search filter
            gridListDataView.addFilter(object -> {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesObject = matchesTerm(object.getName() + object.getCode(), searchTerm);

                return matchesObject;
            });
            gridListDataView.refreshAll();
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
        grid = new Grid<>(Semester.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");

        // Grid Data Set
        semesterDataSourceUpdate();

        // Grid Columns
        grid.addColumn(Semester::getName)
                .setHeader("Semester Name")
                .setFlexGrow(1);
        grid.addColumn(Semester::getCode)
                .setHeader("Semester Code")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(semester -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            editButton.addClickListener(event -> {
                openAddEditDialog(semester);
            });
            deleteButton.addClickListener(event -> {
                openDeleteDialog(semester);
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

        Button addSemester = new Button("Add a Semester");
        addSemester.setIcon(VaadinIcon.PLUS.create());
        addSemester.addClickListener(e -> {
            openAddEditDialog(new Semester("", ""));
            gridListDataView.refreshAll();
        });

        HorizontalLayout searchLayout;

        // add components to view
        searchLayout = new HorizontalLayout(searchField, addSemester);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidth("100%");
        add(searchLayout);
    }

    // Add/Edit Semester Dialogue
    private void openAddEditDialog(Semester semester) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Semester");
        if (semester.getId() == null) {
            dialog.setHeaderTitle("Add Semester");
        }

        FormLayout layout = new FormLayout();

        // dialog fields
        TextField name = new TextField("Name", "");
        name.setValue(semester.getName());
        TextField code = new TextField("Code", "");
        code.setValue(semester.getCode());

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save");

        Binder<Semester> binder = new Binder<>(Semester.class); // Data Validation

        binder.forField(name)
            .asRequired("Name cannot be empty") // Makes sure field isn't empty
            .bind(Semester::getName, Semester::setName);
        binder.forField(code)
            .asRequired("Code cannot be empty") // Makes sure field isn't empty
            .bind(Semester::getCode, Semester::setCode);

        // save: update or add
        saveButton.addClickListener(event -> {
            semester.setName(name.getValue());
            
            try {
                binder.writeBean(semester);
                if (semester.getId() == null) {
                    semesterService.update(semester);
                    Notification.show("Added Successfully", 3000, Position.TOP_END);
                } else {
                    semesterService.update(semester);
                    Notification.show("Changed Successfully", 3000, Position.TOP_END);
                }
                semesterDataSourceUpdate();
                dialog.close();
            } catch (ValidationException e) {
                Notification.show("Fields required", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END); 
            }
        });

        // add components to view
        layout.add(name, code);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Delete Semester Dialogue
    private void openDeleteDialog(Semester semester) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Semester");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + semester.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        // delete execute
        Button confirmButton = new Button("Delete", e -> {
            try {
                semesterService.delete(semester.getId());
                Notification.show("Deleted Successfully", 3000, Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Position.TOP_END); 
            }
            semesterDataSourceUpdate();
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
