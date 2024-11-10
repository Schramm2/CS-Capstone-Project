package uct.myadvisor.views;

import java.util.Optional;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;

import uct.myadvisor.data.Role;
import uct.myadvisor.data.User;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.views.admins.AdminView;
import uct.myadvisor.views.advisors.AdvisorsView;
import uct.myadvisor.views.chats.ChatsView;
import uct.myadvisor.views.courses.CoursesView;
import uct.myadvisor.views.degrees.DegreesView;
import uct.myadvisor.views.departments.DepartmentsView;
import uct.myadvisor.views.faculties.FacultiesView;
import uct.myadvisor.views.files.FilesView;
import uct.myadvisor.views.home.HomeView;
import uct.myadvisor.views.majors.MajorsView;
import uct.myadvisor.views.meetings.MeetingsView;
import uct.myadvisor.views.profile.ProfileView;
import uct.myadvisor.views.semesters.SemestersView;
import uct.myadvisor.views.smartTutor.SmartTutorView;
import uct.myadvisor.views.students.StudentsView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;
    

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("MyAdvisor");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // Home
        nav.addItem(new SideNavItem("Home", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));

        // Meetings
        if (accessChecker.hasAccess(MeetingsView.class)) {
            nav.addItem(new SideNavItem("Meetings", MeetingsView.class, LineAwesomeIcon.CALENDAR_DAY_SOLID.create()));
        }

        // Chats
        if (accessChecker.hasAccess(ChatsView.class)) {
            nav.addItem(new SideNavItem("Chats", ChatsView.class, LineAwesomeIcon.COMMENTS.create()));
        }

        // Files
        if (accessChecker.hasAccess(FilesView.class)) {
            nav.addItem(new SideNavItem("Files", FilesView.class, LineAwesomeIcon.FILE_ALT_SOLID.create()));
        }

        // Smart Tutor
        if (accessChecker.hasAccess(SmartTutorView.class)) {
            nav.addItem(new SideNavItem("Smart Tutor", SmartTutorView.class, LineAwesomeIcon.ATOM_SOLID.create()));
        }
        

        // Students
        if (accessChecker.hasAccess(StudentsView.class)) {
            nav.addItem(new SideNavItem("Students", StudentsView.class, LineAwesomeIcon.USERS_COG_SOLID.create()));
        }

        // Advisors
        if (accessChecker.hasAccess(AdvisorsView.class)) {
            nav.addItem(new SideNavItem("Advisors", AdvisorsView.class, LineAwesomeIcon.GRADUATION_CAP_SOLID.create()));
        }

        // Admin
        if (accessChecker.hasAccess(AdminView.class)) {
            nav.addItem(new SideNavItem("Admin", AdminView.class, LineAwesomeIcon.CHESS_KING_SOLID.create()));
        }

        // Faculties
        if (accessChecker.hasAccess(FacultiesView.class)) {
            nav.addItem(new SideNavItem("Faculties", FacultiesView.class, LineAwesomeIcon.BUILDING_SOLID.create()));
        }

        // Departments
        if (accessChecker.hasAccess(DepartmentsView.class)) {
            nav.addItem(new SideNavItem("Departments", DepartmentsView.class, LineAwesomeIcon.BOOKMARK_SOLID.create()));
        }

        // Degrees
        if (accessChecker.hasAccess(DegreesView.class)) {
            nav.addItem(new SideNavItem("Degrees", DegreesView.class, LineAwesomeIcon.SCROLL_SOLID.create()));
        }

        // Majors
        if (accessChecker.hasAccess(MajorsView.class)) {
            nav.addItem(new SideNavItem("Majors", MajorsView.class, LineAwesomeIcon.BOOK_SOLID.create()));
        }

        // Courses
        if (accessChecker.hasAccess(CoursesView.class)) {
            nav.addItem(new SideNavItem("Courses", CoursesView.class, LineAwesomeIcon.BOOK_OPEN_SOLID.create()));
        }

        // Semesters
        if (accessChecker.hasAccess(SemestersView.class)) {
            nav.addItem(new SideNavItem("Semesters", SemestersView.class, LineAwesomeIcon.CALENDAR_WEEK_SOLID.create()));
        }

        // Disable sidenav until student enters their details for the first time
        if (authenticatedUser.get().isPresent() && authenticatedUser.get().get().getVersion() == 0 && authenticatedUser.get().get().getRoles().contains(Role.STUDENT)) {
            nav.getChildren().forEach(component -> {
                component.getElement().setEnabled(false);
            });
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            // Sign Out
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });
            // Profile - Only students can edit their profile
            if(user.getRoles().contains(Role.STUDENT)){
                userName.getSubMenu().addItem("Profile", e -> {
                    getUI().ifPresent(ui -> ui.navigate(ProfileView.class));
                });
            }
            

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        // Disable footer until student enters their details for the first time
        if (authenticatedUser.get().isPresent() && authenticatedUser.get().get().getVersion() == 0 && authenticatedUser.get().get().getRoles().contains(Role.STUDENT)) {
            layout.getChildren().forEach(component -> {
                component.getElement().setEnabled(false);
            });
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
