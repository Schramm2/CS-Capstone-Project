package uct.myadvisor.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Meeting definition class
@Entity
@Table(name = "meetings")
public class Meeting extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    private LocalDateTime start;
    private LocalDateTime end;
    private String notes;
    private String name;
    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private Advisor advisor;
    private String status;
    private String description;
    @ManyToOne
    @JoinColumn(name = "shared_id")
    private Advisor shared;

    public Meeting(){
        
    }

    // Meeting Slot
    public Meeting(Advisor advisor, LocalDateTime start, LocalDateTime end, String status) {
        this.advisor = advisor;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    // New Meeting
    public Meeting(Student student, Advisor advisor, String name, String description, LocalDateTime start,
            LocalDateTime end, String notes) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.student = student;
        this.advisor = advisor;
        this.description = description;
        this.notes = notes;
    }

    // Existing Meeting
    public Meeting(Long id, String name, LocalDateTime start, LocalDateTime end, String notes) {
        super.setId(id);
        this.name = name;
        this.start = start;
        this.end = end;

        this.notes = notes;
    }

    // get and set methods
    public String getDescription() {
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public void setstart(LocalDateTime start) {
        this.start = start;
    }
    public LocalDateTime getEnd() {
        return end;
    }
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student){
        this.student = student;
    }
    public Advisor getAdvisor() {
        return advisor;
    }
    public String getAdvisorName(){
        return advisor.getName();
    }
    public void setAdvisor(Advisor advisor){
        this.advisor = advisor;
    }
    public Advisor getShareAdvisor() {
        return shared;
    }
    public String getSharedAdvisorName(){
        return shared.getName();
    }
    public void setSharedAdvisor(Advisor shared){
        this.shared = shared;
    }

    // date time formatter
    public String toString(){
        DateTimeFormatter formatterFull = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

        return start.format(formatterFull)+ " - "+end.format(formatterTime);
    }
}
