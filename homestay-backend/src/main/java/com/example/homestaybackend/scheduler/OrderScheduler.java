package com.example.homestaybackend.scheduler;

import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderScheduler {

    private static final int CHECKOUT_COMPLETE_HOUR = 12;

    @Autowired
    private OrderRepository orderRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void checkOrdersOnStartup() {
        System.out.println("\n=== Application startup: checking expired orders ===");
        completeExpiredOrders();
    }

    /**
     * Runs every day at 12:00 and marks confirmed orders as completed after checkout time.
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void autoCompleteOrders() {
        System.out.println("\n=== Scheduled task: auto-completing checkout orders ===");
        completeExpiredOrders();
    }

    public void completeExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        int currentHour = now.getHour();

        System.out.println("Run time: " + now);
        System.out.println("Current date: " + today);
        System.out.println("Current hour: " + currentHour);

        List<Order> ordersToComplete = orderRepository.findOrdersToComplete(today);

        if (currentHour < CHECKOUT_COMPLETE_HOUR) {
            System.out.println("Before " + CHECKOUT_COMPLETE_HOUR + ":00; skipping today's check-out orders");
            ordersToComplete = ordersToComplete.stream()
                    .filter(order -> order.getCheckOutDate().isBefore(today))
                    .collect(Collectors.toList());
        }

        System.out.println("Found " + ordersToComplete.size() + " orders to complete");

        if (!ordersToComplete.isEmpty()) {
            List<Long> orderIds = ordersToComplete.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            orderRepository.batchUpdateStatusToCompleted(orderIds);

            System.out.println("Marked these orders as completed:");
            ordersToComplete.forEach(order ->
                    System.out.println("- Order no: " + order.getOrderNo()
                            + ", checkout date: " + order.getCheckOutDate()
                            + ", checkout time: " + order.getCheckOutDate() + " " + CHECKOUT_COMPLETE_HOUR + ":00")
            );
        } else {
            System.out.println("No orders need completion");
        }

        System.out.println("=== Order check finished ===\n");
    }
}
