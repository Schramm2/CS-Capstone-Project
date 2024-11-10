package uct.myadvisor.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.component.avatar.Avatar;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.util.Set;

// Main user parent class type
@Entity
@Table(name = "application_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role")
@DiscriminatorValue("USER")
public class User extends AbstractEntity {

    private String username;
    private String name;
    private String psNumber;
    private String email;
    private String phone;

    @JsonIgnore
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    private Avatar avatar;

    // Default Constructor
    public User() {

    }

    // Constructor to create a user when registration
    public User(String username, String password) {
        super();
        this.username = username;
        this.hashedPassword = password;
    }

    // get and set methods
    public String getUsername() {
        return username != null ? username : "";
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name != null ? name : "";
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPsNumber() {
        return psNumber != null ? psNumber : "";
    }
    public void setPsNumber(String psNumber) {
        this.psNumber = psNumber;
    }
    public String getEmail() {
        return email != null ? email : "";
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone != null ? phone : "";
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getHashedPassword() {
        return hashedPassword != null ? hashedPassword : "";
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public Avatar getAvatar() {
        return avatar;
    }
    public void setAvatar(String name) {
        this.avatar = new Avatar(name);
    }

}
