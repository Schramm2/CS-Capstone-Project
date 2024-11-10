# MyAdvisor

User Roles:
  Admin - can add/edit/delete students, admins, faculties, department and semesters
  Faculty Advisor - can add/edit/delete their faculties advisors, department and degrees
  Senior Advisor - can add/edit/delete their faculties advisors/majors and add/edit/delete their departments courses
  Advisor - can add/edit their faculties majors, add/edit/delete their departments courses; manage their meetings/requests/slots; view and upload to their majors students files; access to their majors students smart tutor; and chat with their majors students and all faculty advisors
  Student - can schedule meetings, chat with their majors advisors, upload files, and use the smart tutor

Test users:
  Commerce faculty:
    - Faculty admin:
      Username: facultycom 
      Password: admin
    - Senior advisor and advisor:
      Username: senioradvisorcom 
      Password: admin
    - Senior advisor:
      Username: seniorcom 
      Password: admin
    - Advisor:
      Username: advisorcom 
      Password: admin
    - Senior advisor:
      Username: seniorcom 
      Password: admin
    - Student (BCom - Computer Science and Information Systems):
      Username: studentcom 
      Password: admin
  Science faculty:
    - Faculty admin:
      Username: facultysci
      Password: admin
    - Senior advisor and advisor:
      Username: senioradvisorsci 
      Password: admin
    - Senior advisor:
      Username: seniorsci
      Password: admin
    - Advisor:
      Username: advisorsci 
      Password: admin
    - Senior advisor:
      Username: seniorsci 
      Password: admin
    - Student (BSc - Computer Science and Business Computing):
      Username: studentsci 
      Password: admin

## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/my-advisor-1.0-SNAPSHOT.jar`

## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/docs/components/app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `src/main/frontend` contains the client-side JavaScript views of your application.
- `themes` folder in `src/main/frontend` contains the custom CSS styles.

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorial at [vaadin.com/docs/latest/tutorial/overview](https://vaadin.com/docs/latest/tutorial/overview).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/docs/latest/components](https://vaadin.com/docs/latest/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Build any UI without custom CSS by discovering Vaadin's set of [CSS utility classes](https://vaadin.com/docs/styling/lumo/utility-classes). 
- Find a collection of solutions to common use cases at [cookbook.vaadin.com](https://cookbook.vaadin.com/).
- Find add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin).
