package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fine")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String username;

    private double amount;
    private int daysOverdue;

    @Column(nullable = false)
    private String status; // "UNPAID" or "PAID"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private int borrowId;

    public Fine() {}

    public Fine(String username, double amount, int daysOverdue, int borrowId) {
        this.username = username;
        this.amount = amount;
        this.daysOverdue = daysOverdue;
        this.borrowId = borrowId;
        this.status = "UNPAID";
        this.createdAt = LocalDateTime.now();
    }

    // ✅ Complete Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(int daysOverdue) { this.daysOverdue = daysOverdue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getBorrowId() { return borrowId; }
    public void setBorrowId(int borrowId) { this.borrowId = borrowId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}