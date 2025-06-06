package com.example.transportGrup.controller;

import com.example.transportGrup.dto.ServiceOrderRequest;
import com.example.transportGrup.entity.OrderService;
import com.example.transportGrup.entity.User;
import com.example.transportGrup.service.ServiceOrderService;
import com.example.transportGrup.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService orderService;

    private final UserService userService;

    @GetMapping("/new")
    public String showOrderForm(Model model) {
        model.addAttribute("orderRequest", new ServiceOrderRequest());
        return "order-form";
    }

    @PostMapping("/submit")
    public String submitOrder(@ModelAttribute("orderRequest") ServiceOrderRequest request,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to place an order.");
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());
        System.out.println(">>> Principal username: " + user.getUsername());

        System.out.println(">>> Found user ID: " + user.getId());

        orderService.createOrder(request, user);

        redirectAttributes.addFlashAttribute("success", "Service order placed successfully!");
        return "redirect:/orders/my";
    }

    @GetMapping("/my")
    public String viewMyOrders(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to place an order.");
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("orders", orderService.getOrdersByUser(user));
        return "my-orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to place an order.");
            return "redirect:/login-form";
        }
        OrderService order = orderService.getOrderById(id, principal.getName());
        model.addAttribute("order", order);
        return "order-details";
    }
}
