package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "authorized_emails")
public class AuthorizedEmail {

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "assigned_role", nullable = false)
    private String assignedRole;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public AuthorizedEmail() {}

    public AuthorizedEmail(String email, String assignedRole) {
        this.email = email;
        this.assignedRole = assignedRole;
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAssignedRole() { return assignedRole; }
    public void setAssignedRole(String assignedRole) { this.assignedRole = assignedRole; }
}