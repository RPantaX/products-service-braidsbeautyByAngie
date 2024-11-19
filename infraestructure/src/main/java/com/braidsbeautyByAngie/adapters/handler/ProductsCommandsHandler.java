package com.braidsbeautyByAngie.adapters.handler;

import com.braidsbeautyByAngie.ports.in.ItemProductServiceIn;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.commands.CancelProductReservationCommand;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.commands.ProductReservationCancelledEvent;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.commands.ReserveProductCommand;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservationFailedEvent;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@KafkaListener(topics = "${products.commands.topic.name}")
@RequiredArgsConstructor
public class ProductsCommandsHandler {

    private final ItemProductServiceIn itemProductServiceIn;
    private static final Logger logger = LoggerFactory.getLogger(ProductsCommandsHandler.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        try {
            List<Product> desireProduct = command.getRequestProductsEventList().stream().map(requestProductEvent -> Product.builder()
                    .productId(requestProductEvent.getProductId())
                    .quantity(requestProductEvent.getQuantity())
                    .build()).toList();

            List<Product> productList =  itemProductServiceIn.reserveProductIn(command.getShopOrderId(), desireProduct);

            ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                    .productList(productList)
                    .shopOrderId(command.getShopOrderId())
                    .build();

            kafkaTemplate.send(productsEventsTopicName, productReservedEvent);
        } catch (Exception e) {
            logger.error("Error in ProductsCommandsHandler.handleCommand: {}", e.getMessage());
            ProductReservationFailedEvent productReservationFailedEvent = ProductReservationFailedEvent.builder()
                    .requestProductsEventList(command.getRequestProductsEventList())
                    .shopOrderId(command.getShopOrderId())
                    .build();
            kafkaTemplate.send(productsEventsTopicName, productReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelProductReservationCommand command) {
        List<Product> productsToCancel = command.getProductList();
        itemProductServiceIn.cancelProductReservationIn(command.getShopOrderId(), productsToCancel);

        Long[] productIds = productsToCancel.stream().map(Product::getProductId).toArray(Long[]::new);

        ProductReservationCancelledEvent productReservationCancelledEvent = ProductReservationCancelledEvent.builder()
                .productIds(productIds)
                .shopOrderId(command.getShopOrderId())
                .build();

        kafkaTemplate.send(productsEventsTopicName, productReservationCancelledEvent);

    }
}
