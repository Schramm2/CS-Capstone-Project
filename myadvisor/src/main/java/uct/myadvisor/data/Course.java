package uct.myadvisor.data;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

// Course definition class, one department has many courses
@Entity
@Table(name = "courses")
public class Course extends AbstractEntity {

    private String name;
    private String code;
    private Integer credits;
    private Integer level;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "course_semester",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "semester_id")
    )
    private Set<Semester> semesters = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "course_prerequisites",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private Set<Course> prerequisites = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "course_corequisites",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "corequisite_id")
    )
    private Set<Course> corequisites = new HashSet<>();
    
    // Default Constructor
    public Course () {
        
    }
    
    // New Course
    public Course (String name, String code, Integer credits, Integer level, Department department, Set<Semester> semesters, Set<Course> prerequisites, Set<Course> corequisites) {
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.level = level;
        this.department = department;
        this.semesters = semesters;
        this.prerequisites = prerequisites;
        this.corequisites = corequisites;
    }

    // Existing Course
    public Course (Long id, String name, String code, Integer credits, Integer level, Department department, Set<Semester> semesters, Set<Course> prerequisites, Set<Course> corequisites) {
        super.setId(id);
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.level = level;
        this.department = department;
        this.semesters = semesters;
        this.prerequisites = prerequisites;
        this.corequisites = corequisites;
    }

    // get and set methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Integer getCredits() {
        return credits;
    }
    public void setCredits(Integer credits) {
        this.credits = credits;
    }
    public Integer getLevel() {
        return level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public Integer determineYear(Course course) {
        Integer year;
        switch(course.getLevel()) {
            case 5:
                year = 1;
                break;
            case 6:
                year = 2;
                break;
            case 7:
                year = 3;
                break;
            case 8:
                year = 4;
                break;
            default:
                return 0;
        }
        return year;

    }
    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }
    public Set<Semester> getSemesters() {
        return semesters;
    }
    public void setSemesters(Set<Semester> semesters) {
        this.semesters = semesters;
    }
    public Semester getSemester() {
        return semesters.iterator().next();
    }
    public String getDepartmentName() {
        return department.getName();
    }
    public Set<Course> getPrerequisites() {
        return prerequisites;
    }
    public void setPrerequisites(Set<Course> prerequisites) {
        this.prerequisites = prerequisites;
    }
    public Set<Course> getCorequisites() {
        return corequisites;
    }
    public void setCorequisites(Set<Course> corequisites) {
        this.corequisites = corequisites;
    }
}
