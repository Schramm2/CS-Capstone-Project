package uct.myadvisor.data;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

// Advisor user child class type
@Entity
@DiscriminatorValue("ADVISOR")
public class Advisor extends User {

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    Faculty faculty;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "advisor_department",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "department_id"))
    private Set<Department> departments = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_major",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "major_id"))
    private Set<Major> majors = new HashSet<>();

    // Default Constructor
    public Advisor () {

    }

    // New Advisor
    public Advisor (Set<Role> roles, Faculty faculty, Set<Department> departments, Set<Major> majors) {
        super();
        setRoles(roles);
        this.departments = departments;
        this.majors = majors;
    }

    // Existing Advisor
    public Advisor (Long id, Set<Role> roles, Faculty faculty, Set<Department> departments, Set<Major> majors) {
        super();
        super.setId(id);
        setRoles(roles);
        this.departments = departments;
        this.majors = majors;
    }

    // get and set methods
    public Faculty getFaculty(){
        return faculty;
    }
    public void setFaculty(Faculty faculty){
        this.faculty = faculty;
    }
    public String getFacultyName(){
        return faculty != null ? faculty.getName() : "";
    }
    public Set<Department> getDepartments() {
        return departments;
    }
    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }
    public Set<Major> getMajors() {
        return majors;
    }
    public void setMajors(Set<Major> majors) {
        this.majors = majors;
    }
}