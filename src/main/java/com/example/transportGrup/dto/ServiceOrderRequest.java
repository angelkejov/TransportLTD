package com.example.transportGrup.dto;

import com.example.transportGrup.entity.OrderStatus;
import com.example.transportGrup.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderRequest {
    private Long id;
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDateTime pickupTime;
    private String vehicleType;
    private String specialRequest;
    private OrderStatus status;
    private User user;
    private LocalDateTime createdAt;
}
