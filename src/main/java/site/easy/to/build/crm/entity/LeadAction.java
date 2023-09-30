package site.easy.to.build.crm.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_action")
public class LeadAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "action")
    private String action;
    @Column(name = "date_time")
    private LocalDateTime timestamp;
    @ManyToOne
    @JoinColumn(name = "lead_id")
    private Lead lead;

    public LeadAction() {
    }

    public LeadAction(String action, LocalDateTime timestamp, Lead lead) {
        this.action = action;
        this.timestamp = timestamp;
        this.lead = lead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }
}
