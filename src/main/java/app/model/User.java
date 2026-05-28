package app.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    // 🌟 ADDED: JsonbTransient breaks serialization loops between users and their loan rows
    @JsonbTransient
    @XmlTransient
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BorrowedBook> borrowings = new ArrayList<>();

    // 🌟 ADDED: JsonbTransient breaks serialization loops between users and their unpaid/paid fines
    @JsonbTransient
    @XmlTransient
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Fine> fines = new ArrayList<>();

    // =========================
    // CONSTRUCTORS
    // =========================

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(int id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<BorrowedBook> getBorrowings() { return borrowings; }
    public void setBorrowings(List<BorrowedBook> borrowings) { this.borrowings = borrowings; }

    public List<Fine> getFines() { return fines; }
    public void setFines(List<Fine> fines) { this.fines = fines; }
}