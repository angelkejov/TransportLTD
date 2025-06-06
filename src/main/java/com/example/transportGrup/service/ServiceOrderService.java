package com.example.transportGrup.service;

import com.example.transportGrup.dto.ServiceOrderRequest;
import com.example.transportGrup.entity.OrderService;
import com.example.transportGrup.entity.OrderStatus;
import com.example.transportGrup.entity.User;
import com.example.transportGrup.repository.ServiceOrderRepository;
import com.example.transportGrup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

@Transactional
public class ServiceOrderService {

    private final ServiceOrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService createOrder(ServiceOrderRequest request, User user) {
        System.out.println(">>> Creating order for user: " + user.getId() + " / " + user.getEmail());

        OrderService order = new OrderService();
        order.setPickupLocation(request.getPickupLocation());
        order.setDropoffLocation(request.getDropoffLocation());
        order.setPickupTime(request.getPickupTime());
        order.setSpecialRequest(request.getSpecialRequest());
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public List<OrderService> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public OrderService getOrderById(Long id, String username) {
        OrderService order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getUser() == null || !order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access");
        }

        return order;
    }
}