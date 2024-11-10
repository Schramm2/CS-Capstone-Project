package uct.myadvisor.views.home;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import jakarta.annotation.security.PermitAll;
import uct.myadvisor.data.Role;
import uct.myadvisor.security.AuthenticatedUser;
import uct.myadvisor.views.MainLayout;
import uct.myadvisor.views.chats.ChatsView;
import uct.myadvisor.views.meetings.MeetingsView;
import uct.myadvisor.views.profile.ProfileView;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
@RouteAlias("home")
@PermitAll
public class HomeView extends Composite<VerticalLayout> {

    private final AuthenticatedUser authenticatedUser;
    
    public HomeView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        String name = authenticatedUser.get().get().getName();
        
        // Welcome Message
        H1 h1 = new H1();
        h1.setWidthFull();
        h1.getStyle().set("text-align", "center");
        h1.getStyle().set("margin-bottom", "15px");

        // Static text
        Span welcomeText = new Span("Welcome to MyAdvisor, ");

        // User's name with gradient style
        Span nameText = new Span(name + "!");
        nameText.getStyle().set("background-image", "linear-gradient(45deg, #1e3a8a, #3b82f6)");
        nameText.getStyle().set("-webkit-background-clip", "text");
        nameText.getStyle().set("-webkit-text-fill-color", "transparent");
        nameText.getStyle().set("-moz-background-clip", "text");
        nameText.getStyle().set("-moz-text-fill-color", "transparent");

        // Add both spans to the H1
        h1.add(welcomeText, nameText);

        // Introduction Text
        // Paragraph textMedium = new Paragraph("Learn some of the basics below!");
        // textMedium.setWidth("100%");
        // textMedium.getStyle().set("font-size", "var(--lumo-font-size-m)");

        // Create Tip Cards
        Div tipsContainer = new Div();
        tipsContainer.getStyle().set("display", "flex");
        tipsContainer.getStyle().set("flex-wrap", "wrap");
        tipsContainer.setWidthFull();
        //tipsContainer.setPadding(true);
        Image logo = new Image("/icons/logo.png", "Logo");

        logo.setHeight("auto");
        logo.setWidth("500px");
        if (authenticatedUser.get().get().getRoles().contains(Role.STUDENT)) {
            tipsContainer.add(createTipCard("Navigation", "Use the dashboard to view your tasks, appointments, and messages.", LineAwesomeIcon.HAND_POINTER.create(), HomeView.class));
            tipsContainer.add(createTipCard("Messaging", "Use the chat feature to communicate with your advisor or fellow students.", LineAwesomeIcon.COMMENT_DOTS.create(), ChatsView.class));
            tipsContainer.add(createTipCard("Scheduling", "Easily schedule appointments using the calendar tool.", LineAwesomeIcon.CALENDAR_CHECK.create(), MeetingsView.class));
            tipsContainer.add(createTipCard("Profile Settings", "Update your profile information in the settings menu.", LineAwesomeIcon.COG_SOLID.create(), ProfileView.class));
        }else{
            tipsContainer.add(createTipCard("Navigation", "Use the dashboard to view your tasks, appointments, and messages.", LineAwesomeIcon.HAND_POINTER.create(), HomeView.class));

        }
       
        //tipsContainer.add(createTipCard("Profile Settings", "Update your profile information in the settings menu.", LineAwesomeIcon.COG_SOLID.create()));
        //tipsContainer.add(createTipCard("Profile Settings", "Update your profile information in the settings menu.", LineAwesomeIcon.COG_SOLID.create()));

        // Add components to the layout
        getContent().setWidthFull();
        getContent().getStyle().set("flex-grow", "1");
        getContent().getStyle().set("margin-top", "2em");
        getContent().setAlignItems(Alignment.CENTER); // Center content
        getContent().add(h1, tipsContainer, logo);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Bring up profile view for new user to enter details
        if (authenticatedUser.get().isPresent() && authenticatedUser.get().get().getVersion() == 0 && authenticatedUser.get().get().getRoles().contains(Role.STUDENT)) {
            System.out.println(authenticatedUser.get().get().getVersion() + " here " + authenticatedUser.get().get().getRoles());
            getUI().ifPresent(ui -> ui.navigate(ProfileView.class));  // Use `ProfileView.class` for better routing
        }
    }

    private Div createTipCard(String title, String description, Component icon, Class<? extends Component> navigationTarget) {
        Div card = new Div();
        card.getStyle().set("border-radius", "15px");
        card.getStyle().set("box-shadow", "0 4px 8px 0 rgba(0, 0, 0, 0.2)");
        card.getStyle().set("padding", "20px");
        card.getStyle().set("margin", "10px");
        card.getStyle().set("background-color", "white");
        card.getStyle().set("width", "300px");
        //card.getStyle().set("max-width", "300px");
        card.getStyle().set("flex", "1 1 auto"); // Make sure cards are responsive
    
        // Create a HorizontalLayout for the icon and title
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER); // Align icon and text vertically centered
        
        H1 cardTitle = new H1(title);
        cardTitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        cardTitle.getStyle().set("margin", "0");
    
        // Add icon and title to the header layout
        header.add(icon, cardTitle);
    
        Paragraph cardDescription = new Paragraph(description);
        cardDescription.getStyle().set("font-size", "var(--lumo-font-size-s)");
        card.addClickListener(e -> UI.getCurrent().navigate(navigationTarget));
        card.addClickListener(e -> {
            card.getStyle().set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.2)");
        });
        // Add the header layout and description to the card
        card.add(header, cardDescription);
        return card;
    }
}
