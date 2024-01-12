package com.onlineshopping.orderservice.service;

import com.onlineshopping.orderservice.dto.OrderLineItemsDto;
import com.onlineshopping.orderservice.dto.OrderRequest;
import com.onlineshopping.orderservice.model.Order;
import com.onlineshopping.orderservice.model.OrderLineItems;
import com.onlineshopping.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItems);

//        call inventory service, and place order if product is in stock
        Boolean result = webClient.get()
                .uri("http://localhost:8002/api/inventory")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if(result){
            orderRepository.save(order);
        }
        throw new IllegalArgumentException("Product is not in stock, Please try again");
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
