package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Department definition class, one faculty has many departments
@Entity
@Table(name = "departments")
public class Department extends AbstractEntity {

    private String name;
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    public Department(){

    }
    
    // New Department
    public Department (Faculty faculty, String name) {
        this.faculty = faculty;
        this.name = name;
    }

    // Existing Department
    public Department (Long id, Faculty faculty, String name) {
        super.setId(id);
        this.faculty = faculty;
        this.name = name;
    }

    // get and set methods
    public String getName() {
        return name != null ? name : "";
    }
    public void setName(String name) {
        this.name = name;
    }
    public Faculty getFaculty(){
        return faculty;
    }
    public String getFacultyName(){
        return faculty.getName() != null ? faculty.getName() : "";
    }
    public void setFaculty(Faculty faculty){
        this.faculty = faculty;
    }
}
