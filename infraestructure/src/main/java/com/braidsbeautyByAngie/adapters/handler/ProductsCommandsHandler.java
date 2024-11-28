package com.braidsbeautyByAngie.adapters.handler;

import com.braidsbeautyByAngie.ports.in.ItemProductServiceIn;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.commands.CancelProductAndServiceReservationCommand;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.commands.ProductAndServiceReservationCancelledEvent;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.commands.ReserveProductCommand;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservationFailedEvent;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProductsCommandsHandler {

    private final ItemProductServiceIn itemProductServiceIn;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        try {
            log.info("Received ReserveProductCommand: {}", command);

            List<Product> desireProduct = command.getRequestProductsEventList().stream().map(requestProductEvent ->
             Product.builder()
                        .productId(requestProductEvent.getProductId())
                        .quantity(requestProductEvent.getQuantity())
                        .build()
            ).toList();

            List<Product> productList =  itemProductServiceIn.reserveProductIn(command.getShopOrderId(), desireProduct);

            ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                    .productList(productList)
                    .shopOrderId(command.getShopOrderId())
                    .reservationId(command.getReservationId())
                    .build();

            kafkaTemplate.send(productsEventsTopicName, productReservedEvent);
        } catch (Exception e) {
            log.error("Error in ProductsCommandsHandler.handleCommand: {}", e.getMessage());
            ProductReservationFailedEvent productReservationFailedEvent = ProductReservationFailedEvent.builder()
                    .requestProductsEventList(command.getRequestProductsEventList())
                    .shopOrderId(command.getShopOrderId())
                    .build();
            kafkaTemplate.send(productsEventsTopicName, productReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelProductAndServiceReservationCommand command) {
        log.info("Received CancelProductAndServiceReservationCommand: {}", command);
        List<Product> productsToCancel = command.getProductList();
        itemProductServiceIn.cancelProductReservationIn(command.getShopOrderId(), productsToCancel);
        Long[] productIds = productsToCancel.stream( ).map(Product::getProductId).toArray(Long[]::new);

        ProductAndServiceReservationCancelledEvent productAndServiceReservationCancelledEvent = ProductAndServiceReservationCancelledEvent.builder()
                .productIds(productIds)
                .shopOrderId(command.getShopOrderId())
                .reservationId(command.getReservationId())
                .build();
        try {
            kafkaTemplate.send(productsEventsTopicName, productAndServiceReservationCancelledEvent);
            log.info("ProductAndServiceReservationCancelledEvent sent: {}", productAndServiceReservationCancelledEvent);
        } catch (Exception e) {
            log.error("Error in ProductsCommandsHandler.handleCommand: {}", e.getMessage());
        }


    }
}
