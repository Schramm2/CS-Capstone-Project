package uct.myadvisor.views.login;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import uct.myadvisor.security.AuthenticatedUser;

@PageTitle("Login")
@Route(value = "login")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("MyAdvisor");
        i18n.getHeader().setDescription("Login your UCT email and password");
        i18n.setAdditionalInformation(null);
        i18n.getForm().setUsername("Email");

        VerticalLayout registerLayout = new VerticalLayout();
        Paragraph registerText = new Paragraph("New to MyAdvisor?");
        Anchor registerLink = new Anchor("/register", "Register Now!");

        // add components to view
        registerLayout.add(registerText, registerLink);
        registerLayout.setSpacing(false);
        registerLayout.getThemeList().add("spacing-xs");
        registerLayout.setAlignItems(Alignment.CENTER);

        setI18n(i18n);

        getFooter().add(registerLayout);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
