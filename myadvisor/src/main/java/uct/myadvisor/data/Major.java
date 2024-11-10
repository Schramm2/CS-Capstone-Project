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

// Major definition class, one degree has many majors
@Entity
@Table(name = "majors")
public class Major extends AbstractEntity {

    private String name;
    private String code;
    private Integer credits;
    @ManyToOne
    @JoinColumn(name = "degree_id")
    private Degree degree;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "major_requiredcourse",
        joinColumns = @JoinColumn(name = "major_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> required = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "major_electivecourse",
        joinColumns = @JoinColumn(name = "major_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> electives = new HashSet<>();

    // Default Constructor
    public Major () {
        
    }
    
    // New Major
    public Major (String name, String code, Integer credits, Degree degree) {
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.degree = degree;
    }

    // Existing Major
    public Major (Long id, String name, String code, Integer credits, Degree degree) {
        super.setId(id);
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.degree = degree;
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
    public Degree getDegree() {
        return degree;
    }
    public void setDegree(Degree degree) {
        this.degree = degree;
    }
    public String getDegreeName() {
        return degree.getName();
    }
    public Set<Course> getRequired() {
        return required;
    }
    public void setRequired(Set<Course> required) {
        this.required = required;
    }
    public Set<Course> getElectives() {
        return electives;
    }
    public void setElectives(Set<Course> electives) {
        this.electives = electives;
    }
}
