package uct.myadvisor.data;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

// Student user child class type
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    private String student_number;
    @ManyToOne
    @JoinColumn(name = "degree_id")
    private Degree degree;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_major",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "major_id"))
    private Set<Major> majors = new HashSet<>();

    // Default Constructor
    public Student() {

    }

    // Register Student Basics
    public Student(String username, String hashedPassword) {
        super(username, hashedPassword);
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(Role.STUDENT);
        setRoles(roleSet);
    }

    // New Student
    public Student (Degree degree, String ps_number, String username, String name, String email, String phone, String student_number) {
        super();
        this.student_number = student_number;
        this.degree = degree;
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(Role.STUDENT);
        setRoles(roleSet);
    }

    // Existing Student
    public Student (Long id, Degree degree, String ps_number, String username, String name, String email, String phone, String student_number) {
        super();
        super.setId(id);
        this.student_number = student_number;
        this.degree = degree;
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(Role.STUDENT);
        setRoles(roleSet);
    }

    // get and set methods
    public void setStudentNumber(String student_number) {
        this.student_number = student_number;
    }
    public String getStudentNumber() {
        return student_number != null ? student_number : "";
    }
    public Degree getDegree(){
        return degree;
    }
    public void setDegree(Degree degree){
        this.degree = degree;
    }
    public String getDegreeName() {
        return degree != null ? degree.getName() : "";
    }
    public Set<Major> getMajors() {
        return majors;
    }
    public String getMajorsString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (Major major : majors) {
            joiner.add(major.getName());
        }
        return joiner.toString();
    }
    public void setMajors(Set<Major> majors) {
        this.majors = majors;
    }
}