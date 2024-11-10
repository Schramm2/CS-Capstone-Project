package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// Faculty definition class
@Entity
@Table(name = "faculties")
public class Faculty extends AbstractEntity {

    private String name;
    
    // Default Constructor
    public Faculty(){
        
    }
    
    // New Faculty
    public Faculty (String name) {
        this.name = name;
    }

    // Existing Faculty
    public Faculty (Long id, String name) {
        super.setId(id);
        this.name = name;
    }

    // get and set methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
