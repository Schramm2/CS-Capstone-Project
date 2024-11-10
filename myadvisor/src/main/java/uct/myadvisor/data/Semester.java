package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// Semester definition class, each course has one or many semesters
@Entity
@Table(name = "semesters")
public class Semester extends AbstractEntity {

    private String name;
    private String code;
    
    // Default Constructor
    public Semester(){
        
    }
    
    // New Semester
    public Semester (String name, String code) {
        this.name = name;
        this.code = code;
    }

    // Existing Semester
    public Semester (Long id, String name, String code) {
        super.setId(id);
        this.name = name;
        this.code = code;
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
}
