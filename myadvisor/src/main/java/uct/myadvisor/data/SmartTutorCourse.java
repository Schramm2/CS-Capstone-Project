package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Smart tutor course definition class, each student has many smart tutor courses and each smart
// tutor course is associated with a course and holds additional info about how/when the course is taken
@Entity
@Table(name = "smart_tutor_courses")
public class SmartTutorCourse extends AbstractEntity {

    private Boolean passed;
    private Boolean required;
    private Integer year;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    
    // Default Constructor
    public SmartTutorCourse(){
        
    }
    
    // New Faculty
    public SmartTutorCourse (Boolean passed, Integer year, Course course, Semester semester) {
        this.passed = passed;
        this.year = year;
        this.course = course;
        this.semester = semester;
    }

    // Existing Faculty
    public SmartTutorCourse (Long id, Boolean passed, Integer year, Course course, Semester semester) {
        super.setId(id);
        this.passed = passed;
        this.year = year;
        this.course = course;
        this.semester = semester;
    }

    // get and set methods
    public Boolean getPassed() {
        return passed;
    }
    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
    public String getPassedText() {
        return passed ? "Yes" : "No";
    }
    public Boolean getRequired() {
        return required;
    }
    public void setRequired(Boolean required) {
        this.required = required;
    }
    public Integer getYear() {
        return year;
    }
    public void setYear(Integer year) {
        this.year = year;
    }
    public Course getCourse() {
        return course;
    }
    public Integer getCourseCredits() {
        return course != null ? course.getCredits() : 0;
    }
    public void setCourse(Course course) {
        this.course = course;
    }
    public String getCourseCode() {
        return course.getCode();
    }
    public String getCourseName() {
        return course.getName();
    }
    public Semester getSemester() {
        return semester;
    }
    public void setSemester(Semester semester) {
        this.semester = semester;
    }
    public String getSemesterCode() {
        return semester != null ? semester.getCode() : "";
    }
    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
}
