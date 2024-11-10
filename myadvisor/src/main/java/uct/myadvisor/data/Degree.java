package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Degree definition class, one faculty has many degrees
@Entity
@Table(name = "degrees")
public class Degree extends AbstractEntity {

    private String name;
    private String code;
    private String requirements;
    private Integer minCredits;
    private Integer maxCredits;
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    // Default Constructor
    public Degree () {

    }

    // New Degree
    public Degree (Faculty faculty, String name, String code, String requirements, Integer minCredits, Integer maxCredits) {
        this.faculty = faculty;
        this.name = name;
        this.code = code;
        this.requirements = requirements;
        this.minCredits = minCredits;
        this.maxCredits = maxCredits;
    }

    // Existing Degree
    public Degree (Long id, Faculty faculty, String name, String code, String requirements, Integer minCredits, Integer maxCredits) {
        super.setId(id);
        this.faculty = faculty;
        this.name = name;
        this.code = code;
        this.requirements = requirements;
        this.minCredits = minCredits;
        this.maxCredits = maxCredits;
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
    public String getRequirements() {
        return requirements;
    }
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
    public Integer getMinCredits() {
        return minCredits;
    }
    public void setMinCredits(Integer minCredits) {
        this.minCredits = minCredits;
    }
    public Integer getMaxCredits() {
        return maxCredits;
    }
    public void setMaxCredits(Integer maxCredits) {
        this.maxCredits = maxCredits;
    }
    public Faculty getFaculty() {
        return faculty;
    }
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }
    public String getFacultyName() {
        return faculty.getName();
    }
}
