package com.example.officepcstore.map;

import com.example.officepcstore.models.enity.Order;
import com.example.officepcstore.payload.response.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class OrderMap {
    //toOrderRes
    public OrderResponse getOrderRes (Order order) {

        return new OrderResponse(order.getId(), order.getUser().getId(), order.getUser().getName(),
                order.getTotalProduct(), order.getTotalPrice(), order.getState(), order.getInvoiceDate(),order.getLastUpdateStateDate());
    }
//toOrderDetailRes
    public OrderResponse getOrderDetailRes (Order order) {
        OrderResponse orderRes =  new OrderResponse(order.getId(), order.getUser().getId(), order.getUser().getName(),
                order.getTotalProduct(), order.getTotalPrice(), order.getState(), order.getInvoiceDate(),order.getLastUpdateStateDate());
        orderRes.setItems(order.getOrderedProducts().stream().map(CartMap::toCartProductRes).collect(Collectors.toList()));
        orderRes.setPaymentType(order.getPaymentInformation().getPaymentType());
        orderRes.setPaymentInfo(order.getPaymentInformation().getPayDetails());
        orderRes.setShippingDetail(order.getShippingDetail());
        return orderRes;
    }
}
