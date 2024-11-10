package uct.myadvisor.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// File definition class, related to user table
@Entity
@Table(name = "files")
public class File extends AbstractEntity {

    private String name;
    private String path;
    @ManyToOne
    @JoinColumn(name = "uploaded_id")
    private User uploadedBy;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    private LocalDateTime uploadedAt;

    public File() {}

    // New File
    public File (String name, User uploadedBy, Student student, LocalDateTime uploadedAt) {
        this.name = name;
        this.path = determinePath(uploadedBy);
        this.uploadedBy = uploadedBy;
        this.student = student;
        this.uploadedAt = uploadedAt;
    }

    // Existing File
    public File (Long id, String name, User uploadedBy, Student student, LocalDateTime uploadedAt) {
        super.setId(id);
        this.name = name;
        this.uploadedBy = uploadedBy;
        this.student = student;
        this.uploadedAt = uploadedAt;
    }

    // get and set methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public User getUploadedBy() {
        return uploadedBy;
    }
    public String getUploadedByName() {
        return uploadedBy != null ? uploadedBy.getName() : "";
    }
    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    public Student getStudent() {
        return student;
    }
    public String getStudentName() {
        return student != null ? student.getName() : "";
    }
    public void setStudent(Student student) {
        this.student = student;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // file storage location
    private String determinePath(User uploadedBy) {
        String basePath = System.getProperty("user.dir") + "/target/files/";
        String userPath = basePath + uploadedBy.getId();
        Path path = Paths.get(userPath);
        
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path); // Create the directory if it doesn't exist
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle any IOExceptions (like permission issues)
        }
        
        return userPath;
    }

    // Method to delete the file from the filesystem
    public boolean deleteFileFromSystem() {
        Path path = Paths.get(this.getPath(), this.getName());
        try {
            if (Files.exists(path)) {
                Files.delete(path); // Delete the file from the filesystem
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle any IOExceptions
        }
        return false;
    }
}
