package uct.myadvisor.views.meetings;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;
import uct.myadvisor.data.Advisor;
import uct.myadvisor.data.Meeting;
import uct.myadvisor.data.Student;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.services.MeetingService;
import uct.myadvisor.services.UserService;
import uct.myadvisor.views.MainLayout;

// Student should be able to request a meeting for the advisor
// Possily redo the requests dialog to include open slots and custom requested meetings

@PageTitle("Meetings")
@Route(value = "meetings", layout = MainLayout.class)
@RolesAllowed({ "STUDENT", "ADVISOR", "USER" })
public class MeetingsView extends VerticalLayout {

    private final transient AuthenticationContext authContext;
    private final AuthenticatedUser authenticatedUser;
    private final MeetingService meetingService;
    private final UserService userService;

    private Tabs tabs;
    private Tab bookedTab, requestsTab, slotsTab, sharedTab;
    private Grid<Meeting> bookedGrid, requestsGrid, slotsGrid, sharedGrid;

    private List<Meeting> bookedMeetings;
    private List<Meeting> meetingSlots;
    private List<Meeting> requestedMeetingSlots;
    private List<Meeting> sharedNotesMeetings;

    private List<Advisor> advisors;

    private Student authStudent;
    private Advisor authAdvisor;

    public MeetingsView(AuthenticationContext authContext, MeetingService meetingService, UserService userService,
            AuthenticatedUser authenticatedUser) {
        this.authContext = authContext;
        this.meetingService = meetingService;
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        // Student should only see the Meetings Tab and Meeting Requests Tab
        if (authContext.hasRole("STUDENT")) {

            Optional<Student> opStudent = userService.getStudent(authenticatedUser.get().get().getId());
            authStudent = opStudent.orElse(null);
            addClassName("meetings-view");
            setSizeFull();
            createTabs();
            createGrids();

            HorizontalLayout tablayout = new HorizontalLayout(bookedGrid, requestsGrid);
            tablayout.setSizeFull();
            add(tabs, tablayout);
            bookedGrid.setVisible(true);
            requestsGrid.setVisible(false);

            updateTabContent();
            // Advisor should see all tabs being the Meeting Requests tab, Requests Tab and
            // Slots Tab
        } else if (authContext.hasRole("ADVISOR") || authContext.hasRole("SENIOR_ADVISOR")) {

            Optional<Advisor> opAdvisor = userService.getAdvisor(authenticatedUser.get().get().getId());
            authAdvisor = opAdvisor.orElse(null);
            addClassName("meetings-view");
            setSizeFull();
            createTabs();
            createGrids();

            HorizontalLayout tablayout = new HorizontalLayout(bookedGrid, requestsGrid, slotsGrid, sharedGrid);
            tablayout.setSizeFull();
            add(tabs, tablayout);
            bookedGrid.setVisible(true);
            requestsGrid.setVisible(false);
            slotsGrid.setVisible(false);
            sharedGrid.setVisible(false);

            updateTabContent();
        }

    }

    // Fetch updated data from the database and store it in the grid
    public void meetingSlotsDataSourceUpdate(Advisor advisor) {

        // Meeting Slots
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            meetingSlots = meetingService.listAdvisorSlots(advisor, pageable).getContent();

        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage());
        }
    }

    // Fetch updated data from the database and store it in the grid
    public void advisorDataSourceUpdate(String userRole, Advisor advisor, Student student) {
        Pageable pageable = PageRequest.of(0, 900000000);
        try {
            if (userRole == "ADVISOR") {
                List<Advisor> newAdvisors = new ArrayList<>();
                advisors = userService.findFacultyAdvisors(advisor.getFaculty(), pageable).getContent();
                for (Advisor a : advisors) {
                    if (!a.getId().equals(authAdvisor.getId())) {
                        newAdvisors.add(a);
                    }
                }
                advisors = newAdvisors;
            } else if (userRole == "STUDENT") {
                advisors = userService.findStudentsAdvisors(student, pageable).getContent();
            }
        } catch (Exception er) {
            Notification.show("Error: " + er.getMessage(), 3000, Notification.Position.TOP_END);
        }
    }

    // Create Tabs for grids
    private void createTabs() {

        if (authContext.hasRole("ADVISOR") || authContext.hasRole("SENIOR_ADVISOR")) {
            bookedTab = new Tab("Booked Meetings");
            requestsTab = new Tab("Meeting Requests");
            slotsTab = new Tab("Slots");
            sharedTab = new Tab("Shared With Me");

            tabs = new Tabs(bookedTab, requestsTab, slotsTab, sharedTab);

            tabs.addSelectedChangeListener(event -> {
                updateTabContent();
            });
        } else {
            bookedTab = new Tab("Booked Meetings");
            requestsTab = new Tab("Meeting Requests");

            tabs = new Tabs(bookedTab, requestsTab);

            tabs.addSelectedChangeListener(event -> {
                updateTabContent();
            });
        }

    }

    // Fetch data from database and update the selected tab with the list
    private void updateTabContent() {

        if (authContext.hasRole("ADVISOR") || authContext.hasRole("SENIOR_ADVISOR")) {
            bookedGrid.setVisible(false);
            requestsGrid.setVisible(false);
            slotsGrid.setVisible(false);
            if (tabs.getSelectedTab().equals(bookedTab)) {
                bookedMeetings = meetingService
                        .listAdvisorBooked(authAdvisor, PageRequest.of(0, 100)).getContent();
                bookedGrid.setItems(bookedMeetings);
                bookedGrid.setVisible(true);
            } else if (tabs.getSelectedTab().equals(requestsTab)) {
                requestedMeetingSlots = meetingService.listAdvisorRequests(authAdvisor, PageRequest.of(0, 100))
                        .getContent();
                requestsGrid.setItems(requestedMeetingSlots);
                requestsGrid.setVisible(true);

            } else if (tabs.getSelectedTab().equals(slotsTab)) {
                meetingSlots = meetingService.listAdvisorSlots(authAdvisor, PageRequest.of(0, 100)).getContent();
                slotsGrid.setItems(meetingSlots);
                slotsGrid.setVisible(true);
            } else if (tabs.getSelectedTab().equals(sharedTab)) {
                sharedNotesMeetings = meetingService.listSharedNotes(authAdvisor, PageRequest.of(0, 100)).getContent();
                sharedGrid.setItems(sharedNotesMeetings);
                sharedGrid.setVisible(true);
            }
        } else if (authContext.hasRole("STUDENT")) {
            bookedGrid.setVisible(false);
            requestsGrid.setVisible(false);

            if (tabs.getSelectedTab().equals(bookedTab)) {
                bookedMeetings = meetingService
                        .listStudentBooked(authStudent, PageRequest.of(0, 100)).getContent();
                bookedGrid.setItems(bookedMeetings);
                bookedGrid.setVisible(true);
            } else if (tabs.getSelectedTab().equals(requestsTab)) {
                requestedMeetingSlots = meetingService.listStudentRequests(authStudent, PageRequest.of(0, 100))
                        .getContent();
                requestsGrid.setItems(requestedMeetingSlots);
                requestsGrid.setVisible(true);

            }
        }

    }

    // Main List View with Add Button
    private void createGrids() {
        // Student Grids
        if (authContext.hasRole("STUDENT")) {

            Button scheduleButton = new Button("Schedule a Meeting");
            scheduleButton.setIcon(VaadinIcon.PLUS.create());
            scheduleButton.addClickListener(e -> {

                openScheduleDialog();
            });
            // Schedule component
            HorizontalLayout searchLayout = new HorizontalLayout(scheduleButton);
            searchLayout.setAlignItems(Alignment.CENTER);
            add(searchLayout);
            // BookedGrid Columns
            bookedGrid = new Grid<>(Meeting.class, false);
            bookedGrid.addColumn(createAdvisorRenderer())
                    .setHeader("Advisor")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            bookedGrid.addColumn(Meeting::getDescription)
                    .setHeader("Description")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            bookedGrid.addColumn(createStatusComponentRenderer())
                    .setHeader("Status")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            bookedGrid.addColumn(new ComponentRenderer<>(meeting -> {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(meeting.getStart().toLocalDate());
                datePicker.setReadOnly(true);
                datePicker.setWidth("145px");

                TimePicker timePicker = new TimePicker();
                timePicker.setValue(meeting.getStart().toLocalTime());
                timePicker.setReadOnly(true);
                timePicker.setWidth("100px");

                layout.add(datePicker, timePicker);
                return layout;
            }))
                    .setHeader("Date and Time")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            bookedGrid.setSizeFull();
            // RequestsGrid Columns
            requestsGrid = new Grid<>(Meeting.class, false);
            requestsGrid.addColumn(createAdvisorRenderer())
                    .setHeader("Advisor")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            requestsGrid.addColumn(Meeting::getDescription)
                    .setHeader("Description")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            requestsGrid.addColumn(createStatusComponentRenderer())
                    .setHeader("Status")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            requestsGrid.addColumn(new ComponentRenderer<>(meeting -> {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(meeting.getStart().toLocalDate());
                datePicker.setReadOnly(true);
                datePicker.setWidth("145px");

                TimePicker timePicker = new TimePicker();
                timePicker.setValue(meeting.getStart().toLocalTime());
                timePicker.setReadOnly(true);
                timePicker.setWidth("100px");

                layout.add(datePicker, timePicker);
                return layout;
            }))
                    .setHeader("Date and Time")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            requestsGrid.addComponentColumn(meeting -> {
                HorizontalLayout buttons = new HorizontalLayout();

                Button cancelButton = new Button("Cancel");
                cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                cancelButton.setEnabled(
                        !meeting.getStatus().equals("Cancelled") && !meeting.getStatus().equals("Rejected"));

                cancelButton.addClickListener(event -> {
                    openCancelRequestDialog(meeting);
                });

                buttons.add(cancelButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            })
                    .setHeader("Actions")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setTextAlign(ColumnTextAlign.CENTER);

            requestsGrid.setSizeFull();

            updateTabContent();
        } else if (authContext.hasRole("ADVISOR") || authContext.hasRole("SENIOR_ADVISOR")) {
            // Advisor Slot list data set
            meetingSlotsDataSourceUpdate(authAdvisor);
            // BookedGrid Columns
            bookedGrid = new Grid<>(Meeting.class, false);
            bookedGrid.addColumn(createStudentRenderer())
                    .setHeader("Student")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            bookedGrid.addColumn(Meeting::getDescription)
                    .setHeader("Description")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            bookedGrid.addColumn(createStatusComponentRenderer())
                    .setHeader("Status")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            bookedGrid.addColumn(new ComponentRenderer<>(meeting -> {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(meeting.getStart().toLocalDate());
                datePicker.setReadOnly(true);
                datePicker.setWidth("145px");

                TimePicker timePicker = new TimePicker();
                timePicker.setValue(meeting.getStart().toLocalTime());
                timePicker.setReadOnly(true);
                timePicker.setWidth("100px");

                layout.add(datePicker, timePicker);
                return layout;
            }))
                    .setHeader("Date and Time")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            bookedGrid.addComponentColumn(meeting -> {
                HorizontalLayout buttons = new HorizontalLayout();
                Button editButton = new Button("Edit");

                editButton.addClickListener(event -> {
                    openNoteDialog(meeting);
                });

                Button cancelButton = new Button("Cancel");
                cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                cancelButton.addClickListener(event -> {
                    openCancelDialog(meeting);
                });

                buttons.add(editButton, cancelButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            })
                    .setHeader("Actions")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setTextAlign(ColumnTextAlign.CENTER);
            bookedGrid.setSizeFull();
            // RequestsGrid Columns
            requestsGrid = new Grid<>(Meeting.class, false);
            requestsGrid.addColumn(createAdvisorRenderer())
                    .setHeader("Student")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            requestsGrid.addColumn(Meeting::getDescription)
                    .setHeader("Description")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            requestsGrid.addColumn(createStatusComponentRenderer())
                    .setHeader("Status")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            requestsGrid.addColumn(new ComponentRenderer<>(meeting -> {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(meeting.getStart().toLocalDate());
                datePicker.setReadOnly(true);
                datePicker.setWidth("145px");

                TimePicker timePicker = new TimePicker();
                timePicker.setValue(meeting.getStart().toLocalTime());
                timePicker.setReadOnly(true);
                timePicker.setWidth("100px");

                layout.add(datePicker, timePicker);
                return layout;
            }))
                    .setHeader("Date and Time")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            requestsGrid.addComponentColumn(meeting -> {
                HorizontalLayout buttons = new HorizontalLayout();
                Button approveButton = new Button("Approve");

                approveButton.addClickListener(event -> {
                    openRequestsDialog(meeting, "Approve");
                });
                approveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                Button rejectButton = new Button("Reject");

                rejectButton.addClickListener(event -> {
                    openRequestsDialog(meeting, "Reject");
                });
                rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                buttons.add(approveButton, rejectButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            })
                    .setHeader("Actions")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setTextAlign(ColumnTextAlign.CENTER);
            requestsGrid.setSizeFull();
            // SlotsGrid columns
            slotsGrid = new Grid<>(Meeting.class, false);
            slotsGrid.addColumn(createAdvisorRenderer())
                    .setHeader("Student")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            slotsGrid.addColumn(Meeting::getDescription)
                    .setHeader("Description")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            slotsGrid.addColumn(createStatusComponentRenderer())
                    .setHeader("Status")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            slotsGrid.addColumn(new ComponentRenderer<>(meeting -> {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(meeting.getStart().toLocalDate());
                datePicker.setReadOnly(true);
                datePicker.setWidth("145px");

                TimePicker timePicker = new TimePicker();
                timePicker.setValue(meeting.getStart().toLocalTime());
                timePicker.setReadOnly(true);
                timePicker.setWidth("100px");

                layout.add(datePicker, timePicker);
                return layout;
            }))
                    .setHeader("Date and Time")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            slotsGrid.addComponentColumn(meeting -> {
                HorizontalLayout buttons = new HorizontalLayout();
                Button notesButton = new Button("Edit");

                notesButton.addClickListener(event -> {
                    openEditSlotDialog(meeting);
                });

                Button cancelButton = new Button("Cancel");

                cancelButton.addClickListener(event -> {
                    openCancelDialog(meeting);
                });
                cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                buttons.add(notesButton, cancelButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            })
                    .setHeader("Actions")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setTextAlign(ColumnTextAlign.CENTER);
            slotsGrid.setSizeFull();

            sharedGrid = new Grid<>(Meeting.class, false);
            sharedGrid.addColumn(createStudentRenderer())
                    .setHeader("Student")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            sharedGrid.addColumn(createAdvisorRenderer())
                    .setHeader("Advisor")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            sharedGrid.addColumn(Meeting::getDescription)
                    .setHeader("Description")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            sharedGrid.addColumn(new ComponentRenderer<>(meeting -> {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(meeting.getStart().toLocalDate());
                datePicker.setReadOnly(true);
                datePicker.setWidth("145px");

                TimePicker timePicker = new TimePicker();
                timePicker.setValue(meeting.getStart().toLocalTime());
                timePicker.setReadOnly(true);
                timePicker.setWidth("100px");

                layout.add(datePicker, timePicker);
                return layout;
            }))
                    .setHeader("Date and Time")
                    .setAutoWidth(true)
                    .setFlexGrow(1);
            sharedGrid.addComponentColumn(meeting -> {
                HorizontalLayout buttons = new HorizontalLayout();
                Button viewButton = new Button("View");

                viewButton.addClickListener(event -> {
                    openSharedNoteDialog(meeting);
                });

                buttons.add(viewButton);
                buttons.setAlignItems(Alignment.END);
                return buttons;
            })
                    .setHeader("Actions")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setTextAlign(ColumnTextAlign.CENTER);
            slotsGrid.setSizeFull();

            // Add slot component
            Button slotButton = new Button("Add Slots");
            slotButton.setIcon(VaadinIcon.PLUS.create());
            slotButton.addClickListener(e -> {
                openSlotDialog();
            });

            HorizontalLayout searchLayout = new HorizontalLayout(slotButton);
            searchLayout.setAlignItems(Alignment.CENTER);
            add(searchLayout);
            // Set Grid Data
            updateTabContent();
        }

    }

    // Edit Advisor Meeting Slot details dialogue
    public void openEditSlotDialog(Meeting meeting) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Meeting");

        FormLayout layout = new FormLayout();

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        DatePicker datePicker = new DatePicker("Meeting Date");
        datePicker.setValue(meeting.getStart().toLocalDate());
        datePicker.setMin(LocalDate.now());

        TimePicker timePickerStart = new TimePicker("Meeting Start");
        timePickerStart.setValue(meeting.getStart().toLocalTime());
        timePickerStart.setStep(Duration.ofMinutes(15));
        timePickerStart.setMin(LocalTime.of(8, 0));
        timePickerStart.setMax(LocalTime.of(16, 0));

        TimePicker timePickerEnd = new TimePicker("Meeting End");
        timePickerEnd.setValue(meeting.getEnd().toLocalTime());
        timePickerEnd.setStep(Duration.ofMinutes(15));
        timePickerEnd.setMin(LocalTime.of(8, 0));
        timePickerEnd.setMax(LocalTime.of(16, 0));

        Button confirmButton = new Button("Confirm", e -> {
            LocalDate meetingDate = datePicker.getValue();
            LocalTime meetingStart = timePickerStart.getValue();
            LocalDateTime startDayTime = meetingDate.atTime(meetingStart);
            LocalTime meetingEnd = timePickerEnd.getValue();
            LocalDateTime endDayTime = meetingDate.atTime(meetingEnd);

            meeting.setstart(startDayTime);
            meeting.setEnd(endDayTime);
            meetingService.update(meeting);
            updateTabContent();
            dialog.close();
        });

        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(datePicker, timePickerStart, timePickerEnd);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();

    }

    public void openCancelRequestDialog(Meeting meeting) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Meeting");

        FormLayout layout = new FormLayout();

        if (meeting.getStatus() == "Cancelled" || meeting.getStatus() == "Custom Request") {
            Paragraph textSmall = new Paragraph();
            textSmall.setText("Are you sure you want to cancel this meeting?");
            Button cancelButton = new Button("Cancel", e -> {
                dialog.close();
            });

            Button confirmButton = new Button("Confirm", e -> {

                meetingService.delete(meeting.getId());
                updateTabContent();
                dialog.close();

            });
            layout.add(textSmall);
            confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            layout.add(textSmall);
            dialog.add(layout);
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(confirmButton);

            dialog.open();
        } else {
            Paragraph textSmall = new Paragraph();
            textSmall.setText("Are you sure you want to cancel this meeting?");
            Button cancelButton = new Button("Cancel", e -> {
                dialog.close();
            });
            Button confirmButton = new Button("Confirm", e -> {

                meeting.setName(null);
                meeting.setStatus("Slot");
                meeting.setDescription(null);

                meeting.setStudent(null);
                meetingService.update(meeting);
                updateTabContent();
                dialog.close();

            });
            layout.add(textSmall);
            confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            layout.add(textSmall);
            dialog.add(layout);
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(confirmButton);

            dialog.open();
        }

    }

    // Cancel / Delete the meeting dialogue
    public void openCancelDialog(Meeting meeting) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Delete Meeting");

        FormLayout layout = new FormLayout();

        Paragraph textSmall = new Paragraph();
        textSmall.setText("Are you sure you want to cancel this meeting?");

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        Button confirmButton = new Button("Confirm", e -> {
            if (meeting.getStudent() != null) {
                meeting.setStatus("Cancelled");
                meetingService.update(meeting);
                updateTabContent();
                dialog.close();
            } else {
                meetingService.delete(meeting.getId());
                updateTabContent();
                dialog.close();
            }

        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        layout.add(textSmall);
        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();

    }

    // Handle Advisor Meeting request Dialogue
    public void openRequestsDialog(Meeting meeting, String action) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        if (action == "Reject") {
            dialog.setHeaderTitle("Reject Meeting");

            dialog.setWidth("500px");
            FormLayout layout = new FormLayout();
            Button cancelButton = new Button("Cancel", e -> {
                dialog.close();
            });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button rejectButton = new Button("Reject");
            rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            Paragraph confirmation = new Paragraph("Are you sure you want to reject this meeting request?");

            Button revertButton = new Button("Revert to slot");
            revertButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            revertButton.addClickListener(event -> {
                meeting.setName(null);
                meeting.setStatus("Slot");
                meeting.setDescription(null);

                meeting.setStudent(null);
                // Update meeting in database
                meetingService.update(meeting);
                updateTabContent();
                dialog.close();

            });

            rejectButton.addClickListener(event -> {
                meeting.setStatus("Rejected");
                // Update meeting in database
                meetingService.update(meeting);
                updateTabContent();
                dialog.close();
                Notification.show("Meeting Rejected", 3000,
                        Position.TOP_END);
            });

            layout.add(confirmation);
            dialog.add(layout);
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(rejectButton);
            dialog.getFooter().add(revertButton);

            dialog.open();

        } else if (action == "Approve") {
            dialog.setHeaderTitle("Approve Meeting");

            dialog.setWidth("500px");
            FormLayout layout = new FormLayout();
            Button cancelButton = new Button("Cancel", e -> {
                dialog.close();
            });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button confirmButton = new Button("Confirm");
            Paragraph confirmation = new Paragraph("Are you sure you want to approve this meeting request?");

            confirmButton.addClickListener(event -> {
                meeting.setStatus("Booked");
                // Update meeting in database
                meetingService.update(meeting);
                updateTabContent();
                Notification.show("Meeting approved", 3000,
                        Position.TOP_END);
                dialog.close();
            });

            layout.add(confirmation);
            dialog.add(layout);
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(confirmButton);

            dialog.open();

        }

    }

    public void openSharedNoteDialog(Meeting meeting) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("View Shared Note");

        Paragraph notes = new Paragraph(meeting.getNotes());
        FormLayout layout = new FormLayout();

        Button closeButton = new Button("Close", e -> {
            dialog.close();
        });
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.getFooter().add(closeButton);
        layout.add(notes);
        dialog.add(layout);
        dialog.open();

    }

    // Edit / View Notes Dialogue
    public void openNoteDialog(Meeting meeting) {
        advisorDataSourceUpdate("ADVISOR", authAdvisor, null);

        ComboBox<Advisor> advisorCombo = new ComboBox<>("Share With Advisor");
        
        advisorCombo.setItems(advisors);
        advisorCombo.setValue(meeting.getShareAdvisor());
        advisorCombo.setItemLabelGenerator(advisor -> advisor.getName() + " (" + advisor.getEmail() + ")");
        advisorCombo.setWidthFull();
        advisorCombo.setClearButtonVisible(true);

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Edit Notes");

        FormLayout layout = new FormLayout();

        TextArea notes = new TextArea("Notes");
        notes.setWidth("600px");
        notes.setHeight("250px");
        if (meeting.getNotes() != null) {
            notes.setValue(meeting.getNotes());
        }

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            meeting.setNotes(notes.getValue());
            meeting.setSharedAdvisor(advisorCombo.getValue());
            // Update meeting in database
            meetingService.update(meeting);
            Notification.show("Meeting Notes Updated");
            updateTabContent();
            dialog.close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(notes, advisorCombo);

        dialog.add(layout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    // Add a Slot dialogue
    public void openSlotDialog() {
        updateTabContent();

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Add a slot");

        FormLayout formLayout = new FormLayout();

        DatePicker datePicker = new DatePicker("Meeting Date");
        datePicker.setMin(LocalDate.now());

        TimePicker timePickerStart = new TimePicker("Meeting Start");
        timePickerStart.setStep(Duration.ofMinutes(15));
        timePickerStart.setMin(LocalTime.of(8, 0));
        timePickerStart.setMax(LocalTime.of(16, 0));

        TimePicker timePickerEnd = new TimePicker("Meeting End");
        timePickerEnd.setStep(Duration.ofMinutes(15));
        timePickerEnd.setMin(LocalTime.of(8, 0));
        timePickerEnd.setMax(LocalTime.of(16, 0));

        formLayout.add(datePicker, timePickerStart, timePickerEnd);

        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Save", event -> {
            if (datePicker.getValue() == null
                    || timePickerStart.getValue() == null || timePickerEnd.getValue() == null) {
                Notification.show("Please make sure all fields have been filled in", 3000, Position.TOP_END);

            } else if (timePickerStart.getValue().equals(timePickerEnd.getValue())
                    || timePickerEnd.getValue().isBefore(timePickerStart.getValue())
                    || timePickerStart.getValue().isAfter(timePickerEnd.getValue())) {
                Notification.show("The Starting time and Ending time of the meeting cannot be the same", 3000,
                        Position.TOP_END);
            } else {
                LocalDate meetingDate = datePicker.getValue();
                LocalTime meetingStart = timePickerStart.getValue();
                LocalDateTime startDayTime = meetingDate.atTime(meetingStart);
                LocalTime meetingEnd = timePickerEnd.getValue();
                LocalDateTime endDayTime = meetingDate.atTime(meetingEnd);

                Optional<Advisor> opAdvisor = userService.getAdvisor(authenticatedUser.get().get().getId());
                Advisor advisor = opAdvisor.orElseThrow(() -> new IllegalArgumentException("Advisor is required"));
                Meeting meetingSlot = new Meeting(advisor, startDayTime, endDayTime, "Slot");
                meetingService.update(meetingSlot);
                updateTabContent();

                Notification.show("Meeting slot added" +
                        " on " + meetingDate + " at " + meetingStart, 3000, Position.TOP_END);
                dialog.close();
            }

        });

        dialog.add(formLayout);
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);
        dialog.open();

    }

    // Schedule a Meeting dialogue
    public void openScheduleDialog() {
        advisorDataSourceUpdate("STUDENT", null, authStudent);

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setHeaderTitle("Schedule a Meeting");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), // Mobile: 1 column
                new FormLayout.ResponsiveStep("600px", 2), // Tablet: 2 columns
                new FormLayout.ResponsiveStep("1024px", 3) // Desktop: 3 columns
        );

        ComboBox<Advisor> advisor = new ComboBox<>("Select Advisor");
        advisor.setWidthFull();

        advisor.setItems(advisors);
        advisor.setItemLabelGenerator(Advisor::getName);

        TextField description = new TextField("Description");
        description.setWidthFull();

        ComboBox<Meeting> advisorSlots = new ComboBox<>("Advisor Slots");
        advisor.addValueChangeListener(event -> {
            if (advisor.getValue() != null) {
                meetingSlotsDataSourceUpdate(advisor.getValue());
                advisorSlots.setItems(meetingSlots);
                if (meetingSlots.isEmpty()) {
                    Notification.show("No Meeting slots for this advisor", 3000, Position.TOP_CENTER);
                }
                advisorSlots.setItemLabelGenerator(Meeting::toString);
            }

        });
        advisorSlots.setWidthFull();

        advisorSlots.addFocusListener(event -> {
            if (advisor.getValue() == null) {
                Notification.show("Please select an advisor first", 3000, Position.TOP_CENTER);

            }
        });

        DatePicker datePicker = new DatePicker("Meeting Date");
        datePicker.setMin(LocalDate.now());
        datePicker.setWidthFull();

        Paragraph infoText = new Paragraph(
                "Select a Date, Start Time and End Time without selecting a slot to request a custom slot with the advisor");
        infoText.getStyle().set("margin-bottom", "15px");

        TimePicker timePickerStart = new TimePicker("Meeting Start");
        timePickerStart.setStep(Duration.ofMinutes(15));
        timePickerStart.setMin(LocalTime.of(8, 0));
        timePickerStart.setMax(LocalTime.of(16, 0));
        timePickerStart.setWidthFull();

        TimePicker timePickerEnd = new TimePicker("Meeting End");
        timePickerEnd.setStep(Duration.ofMinutes(15));
        timePickerEnd.setMin(LocalTime.of(8, 0));
        timePickerEnd.setMax(LocalTime.of(16, 0));
        timePickerEnd.setWidthFull();

        formLayout.add(infoText, 3);
        formLayout.add(advisor, 1);
        formLayout.add(description, 2);
        formLayout.add(advisorSlots, 1);
        formLayout.add(datePicker, 1);
        formLayout.add(timePickerStart, 1);
        formLayout.add(timePickerEnd, 1);

        advisorSlots.addValueChangeListener(event -> {
            if (advisorSlots.getValue() != null) {

                datePicker.setEnabled(true);
                timePickerStart.setEnabled(true);
                timePickerEnd.setEnabled(true);

                datePicker.setValue(advisorSlots.getValue().getStart().toLocalDate());
                timePickerStart.setValue(advisorSlots.getValue().getStart().toLocalTime());
                timePickerEnd.setValue(advisorSlots.getValue().getEnd().toLocalTime());

                datePicker.setEnabled(false);
                timePickerStart.setEnabled(false);
                timePickerEnd.setEnabled(false);
            } else {

                datePicker.setEnabled(true);
                timePickerStart.setEnabled(true);
                timePickerEnd.setEnabled(true);

                datePicker.clear();
                timePickerStart.clear();
                timePickerEnd.clear();
            }
        });
        // Clear all fields in dialogue components
        Button clearButton = new Button("Clear", event -> {
            advisor.clear();
            advisorSlots.clear();
            description.clear();
            datePicker.clear();
            timePickerEnd.clear();
            timePickerStart.clear();

            advisor.setEnabled(true);
            advisorSlots.setEnabled(true);
            description.setEnabled(true);
            datePicker.setEnabled(true);
            timePickerEnd.setEnabled(true);
            timePickerStart.setEnabled(true);
        });

        Button saveButton = new Button("Schedule", event -> {
            if (advisor.getValue() != null
                    && (advisorSlots.getValue() != null || (datePicker.getValue() != null
                            && timePickerStart.getValue() != null && timePickerEnd.getValue() != null))
                    && description.getValue() != null) {
                if (advisorSlots.getValue() == null) {
                    LocalDate meetingDate = datePicker.getValue();
                    LocalTime meetingStart = timePickerStart.getValue();
                    LocalDateTime startDayTime = meetingDate.atTime(meetingStart);
                    LocalTime meetingEnd = timePickerEnd.getValue();
                    LocalDateTime endDayTime = meetingDate.atTime(meetingEnd);
                    Advisor selectedAdvisor = advisor.getValue();
                    Optional<Student> opStudent = userService.getStudent(authenticatedUser.get().get().getId());
                    Student student = opStudent
                            .orElseThrow(() -> new IllegalArgumentException("Student is required"));
                    Meeting newMeeting = new Meeting();
                    newMeeting.setAdvisor(selectedAdvisor);
                    newMeeting.setstart(startDayTime);
                    newMeeting.setEnd(endDayTime);
                    newMeeting.setDescription(description.getValue());
                    newMeeting.setStudent(student);
                    newMeeting.setName(authenticatedUser.get().get().getName());
                    newMeeting.setStatus("Custom Request");

                    // Create meeting in database
                    meetingService.update(newMeeting);

                    updateTabContent();

                    Notification.show("Meeting request with " + selectedAdvisor.getName() +
                            " on " + meetingDate + " at " + meetingStart + " requested", 3000, Position.TOP_END);
                    dialog.close();
                } else {
                    Advisor selectedEmployee = advisor.getValue();

                    LocalDate meetingDate = datePicker.getValue();
                    LocalTime meetingStart = timePickerStart.getValue();
                    // LocalDateTime startDayTime = meetingDate.atTime(meetingStart);
                    // LocalTime meetingEnd = timePickerEnd.getValue();
                    // LocalDateTime endDayTime = meetingDate.atTime(meetingEnd);

                    Optional<Student> opStudent = userService.getStudent(authStudent.getId());
                    Student student = opStudent
                            .orElseThrow(() -> new IllegalArgumentException("Student is required"));
                    Meeting newMeeting = advisorSlots.getValue();
                    newMeeting.setDescription(description.getValue());
                    newMeeting.setStudent(student);
                    newMeeting.setName(authenticatedUser.get().get().getName());
                    newMeeting.setStatus("Awaiting Approval");

                    // Update Meeting in database
                    meetingService.update(newMeeting);

                    updateTabContent();

                    Notification.show("Meeting request with " + selectedEmployee.getName() +
                            " on " + meetingDate + " at " + meetingStart + " requested", 3000, Position.TOP_END);
                    dialog.close();
                }

            } else {
                Notification.show("Please fill in all fields", 3000, Position.TOP_END);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });

        clearButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.add(formLayout);

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(clearButton);
        dialog.getFooter().add(saveButton);

        dialog.open();

    }

    private static Renderer<Meeting> createStudentRenderer() {
        return LitRenderer.<Meeting>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                        "<vaadin-avatar img=\"${item.img}\" name=\"${item.Meeting}\" alt=\"User avatar\"></vaadin-avatar>"
                        +
                        "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.Meeting} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        +
                        "${item.amount}" +
                        "</span>" +
                        "</vaadin-vertical-layout>" +
                        "</vaadin-horizontal-layout>")

                .withProperty("Meeting", Meeting::getName);

    }

    private static Renderer<Meeting> createAdvisorRenderer() {
        return LitRenderer.<Meeting>of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">" +
                        "<vaadin-avatar img=\"${item.img}\" name=\"${item.Meeting}\" alt=\"User avatar\"></vaadin-avatar>"
                        +
                        "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" +
                        "<span> ${item.Meeting} </span>" +
                        "<span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        +
                        "${item.amount}" +
                        "</span>" +
                        "</vaadin-vertical-layout>" +
                        "</vaadin-horizontal-layout>")

                .withProperty("Meeting", Meeting::getAdvisorName);

    }

    private static final SerializableBiConsumer<Span, Meeting> statusComponentUpdater = (span, meeting) -> {
        span.setText(meeting.getStatus());

        switch (meeting.getStatus()) {

            case "Booked":
                span.getElement().setAttribute("theme", "badge success " + meeting.getStatus().toLowerCase());
                break;
            case "Awaiting Approval":
                span.getElement().setAttribute("theme", "badge contrast " + meeting.getStatus().toLowerCase());
                break;
            case "Slot":
                span.getElement().setAttribute("theme", "badge contrast " + meeting.getStatus().toLowerCase());
                break;
            case "Requested":
                span.getElement().setAttribute("theme", "badge contrast " + meeting.getStatus().toLowerCase());
                break;
            case "Custom Request":
                span.getElement().setAttribute("theme", "badge contrast " + meeting.getStatus().toLowerCase());
                break;
            case "Cancelled":
                span.getElement().setAttribute("theme", "badge error " + meeting.getStatus().toLowerCase());
                break;
            case "Rejected":
                span.getElement().setAttribute("theme", "badge error " + meeting.getStatus().toLowerCase());
                break;
        }
    };

    private static ComponentRenderer<Span, Meeting> createStatusComponentRenderer() {
        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
    }

};
