package uct.myadvisor.views.files;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.File;
import uct.myadvisor.data.Student;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.FileService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

@PageTitle("Files")
@Route(value = "files", layout = MainLayout.class)
@RolesAllowed({ "STUDENT", "ADVISOR", "USER" })
public class FilesView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;

    private final FileService fileService;
    private final UserService userService;

    private TextField searchField = new TextField();

    private Grid<File> grid;
    private GridListDataView<File> gridListDataView;

    private List<File> files;

    private List<Student> students;
    private Student selectedStudent;

    public FilesView(AuthenticationContext authContext, AuthenticatedUser authenticatedUser, FileService fileService,
            UserService userService) {
        this.authContext = authContext;
        this.authenticatedUser = authenticatedUser;
        this.fileService = fileService;
        this.userService = userService;

        setSizeFull();

        if (authContext.hasRole("ADVISOR")) {
            // Combobox for advisor to select student to view the files they've uploaded
            ComboBox<Student> studentSelect = new ComboBox<>();
            studentSelect.setWidthFull();
            studentSelect.setAllowCustomValue(false);
            add(studentSelect);

            studentSelect.setLabel("Select Student:");
            studentSelect.setItemLabelGenerator(student -> student.getName() + " (" + student.getEmail() + ")");
            Advisor advisor = (Advisor) authenticatedUser.get().orElse(null);

            Pageable pageable = PageRequest.of(0, 900000000);
            if (advisor != null) {
                try {
                    students = userService.findAdvisorsStudents(advisor, pageable).getContent();
                    studentSelect.setItems(students);
                } catch (Exception er) {
                    Notification.show("Error: " + er.getMessage());
                }
            } else {
                students = userService.listStudents(pageable).getContent();
                studentSelect.setItems(students);
            }

            studentSelect.addValueChangeListener(e -> {
                selectedStudent = e.getValue();
                if (selectedStudent != null && advisor != null) {
                    if (grid != null) {
                        fileDataSourceUpdate();
                    } else {
                        createGrid();
                    }
                }
            });

        } else if (authContext.hasRole("STUDENT")) {
            selectedStudent = (Student) authenticatedUser.get().orElse(null);
            createGrid();
        }
    }

    // Fetch updated data from the database and store it in the grid
    public void fileDataSourceUpdate() {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (authContext.hasRole("ADVISOR")) {
                Advisor advisor = (Advisor) authenticatedUser.get().orElse(null);
                // Gets files that advisor has uploaded to student and files student has
                // uploaded to advisor
                files = fileService.getFilesByAdvisorAndStudent(advisor.getId(), selectedStudent.getId(), pageable)
                        .getContent();
            } else if (authContext.hasRole("STUDENT")) {
                Student student = (Student) authenticatedUser.get().orElse(null);
                // Gets files that student has uploaded and files advisor(s) have uploaded to
                // student
                files = fileService.getFilesByStudentAndAdvisors(student.getId(), pageable).getContent();
            }
            gridListDataView = grid.setItems(files);
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

    private boolean matchesTerm(String value, String searchTerm) {
        return StringUtils.containsIgnoreCase(value, searchTerm);
    }

    private void createGrid() {
        grid = new Grid<>(File.class, false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeight("100%");

        // Grid Data Set
        fileDataSourceUpdate();

        grid.addColumn(File::getName)
                .setHeader("File Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(File::getUploadedByName)
                .setHeader("Uploaded By")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(file -> {
            LocalDateTime dateTime = file.getUploadedAt();
            DateTimePicker dateTimePicker = new DateTimePicker();
            dateTimePicker.setValue(dateTime);
            dateTimePicker.setReadOnly(true);
            dateTimePicker.setWidth("250px");
            return dateTimePicker;
        })
                .setHeader("Uploaded At")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(file -> {
            HorizontalLayout buttons = new HorizontalLayout();

            // Need to wrap download button in anchor
            Button downloadButton = new Button("Download");
            Anchor downloadAnchor = new Anchor(createResource(file), "");
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.add(downloadButton);

            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            // Disable delete button if current user is not owner of file
            if (!authenticatedUser.get().orElse(null).getId().equals(file.getUploadedBy().getId())) {
                deleteButton.setEnabled(false);
            }

            deleteButton.addClickListener(event -> {
                openDeleteDialog(file);
            });

            buttons.add(downloadAnchor, deleteButton);
            buttons.setAlignItems(Alignment.END);
            return buttons;
        })
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        
        searchField.setWidth("30%");
        searchField.setPlaceholder("Search");
        searchField.setSuffixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> gridListDataView.refreshAll());

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf", ".pdf", "image/png", ".png");

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            try {

                // Create a new File entity and save it to the database
                File fileEntity = new File(fileName, authenticatedUser.get().orElse(null), selectedStudent,
                        LocalDateTime.now());
                fileService.update(fileEntity);

                // Create a File object for the destination
                java.io.File destinationFile = new java.io.File(fileEntity.getPath(), fileName);

                // Get the InputStream from the buffer
                InputStream inputStream = buffer.getInputStream(fileName);

                // Use try-with-resources to ensure streams are closed properly
                try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
                    inputStream.transferTo(outputStream);
                }

                // Refresh the grid
                fileDataSourceUpdate();

                Notification.show("File uploaded successfully: " + fileName, 3000, Notification.Position.TOP_END);
            } catch (Exception e) {
                Notification.show("Error uploading file: " + e.getMessage(), 3000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 3000, Notification.Position.TOP_END);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // add components to grid
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.add(searchField, upload);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        searchLayout.setAlignItems(Alignment.CENTER);

        add(searchLayout, grid);
    }

    // Delete File Dialogue
    private void openDeleteDialog(File file) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete File");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to delete: " + file.getName() + "?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });

        Button confirmButton = new Button("Delete", e -> {
            try {
                fileService.delete(file.getId());
                Notification.show(file.getName() + " Deleted Successfully", 3000, Notification.Position.TOP_END);
            } catch (Exception er) {
                Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
            }
            fileDataSourceUpdate();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }

    private StreamResource createResource(File file) {
        return new StreamResource(file.getName(), () -> {
            try {
                java.io.File sourceFile = new java.io.File(file.getPath(), file.getName());
                return new FileInputStream(sourceFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
