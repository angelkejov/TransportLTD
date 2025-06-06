package com.example.transportGrup.repository;

import com.example.transportGrup.entity.OrderService;
import com.example.transportGrup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<OrderService, Long> {
    List<OrderService> findByUser(User user);
}
