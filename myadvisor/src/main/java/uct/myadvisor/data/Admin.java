package uct.myadvisor.data;

import java.util.Set;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Admin user child class type
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    // Default Constructor
    public Admin () {

    }

    // New Admin
    public Admin (Set<Role> roles, Faculty faculty) {
        super();
        setRoles(roles);
    }

    // Existing Admin
    public Admin (Long id, Set<Role> roles, Faculty faculty) {
        super();
        super.setId(id);
        setRoles(roles);
    }

    //get and set methods
    public Faculty getFaculty () {
        return faculty;
    }
    public void setFaculty (Faculty faculty) {
        this.faculty = faculty;
    }
    public String getFacultyName(){
        return faculty != null ? faculty.getName() : "";
    }
}