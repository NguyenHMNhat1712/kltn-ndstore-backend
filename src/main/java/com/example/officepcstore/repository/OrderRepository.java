package com.example.officepcstore.repository;

import com.example.officepcstore.models.enity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
   Optional<Order> findOrderByUser_IdAndState(ObjectId userId, String state);
//  Optional<Order> findOrderByIdAndUser_Id(String orderId, ObjectId userId);
    Optional<Order> findOrderByPaymentInformation_PaymentTokenAndState(String token, String state);
   Optional<Order> findOrderByIdAndState(String orderId, String state);
    Page<Order> findAllByState(String state, Pageable pageable);
    Page<Order> findOrderByUser_Id(ObjectId userId, Pageable pageable);
  Page<Order> findAllByInvoiceDateBetweenAndState(LocalDateTime from, LocalDateTime to, String state, Pageable pageable);
//    @Aggregation("{ $group: { _id : $state, count: { $sum: 1 } } }")
//    List<StateCountAggregate> countAllByState();

    @Query(value=" {state: {'$nin': ['cart']}}")
    Page<Order> findAllByStateNoCart( Pageable pageable);
}
