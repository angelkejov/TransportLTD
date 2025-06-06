package com.example.transportGrup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_service")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderService {

    @Id
    @GeneratedValue
    private Long id;

    private String pickupLocation;
    private String dropoffLocation;
    private LocalDateTime pickupTime;


    //private String vehicleType;
    private String specialRequest;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
