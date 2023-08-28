package com.example.officepcstore.service;

import com.example.officepcstore.config.Constant;
import com.example.officepcstore.excep.AppException;
import com.example.officepcstore.excep.NotFoundException;
import com.example.officepcstore.map.CartMap;
import com.example.officepcstore.models.enity.Order;
import com.example.officepcstore.models.enity.OrderProduct;
import com.example.officepcstore.models.enity.User;
import com.example.officepcstore.models.enity.product.Product;
import com.example.officepcstore.payload.ResponseObjectData;
import com.example.officepcstore.payload.request.CartReq;
import com.example.officepcstore.payload.response.CartProductResponse;
import com.example.officepcstore.payload.response.CartResponse;
import com.example.officepcstore.repository.OrderProductRepository;
import com.example.officepcstore.repository.OrderRepository;
import com.example.officepcstore.repository.ProductRepository;
import com.example.officepcstore.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final CartMap cartMap;
  //  private final RecommendCheckUtils recommendCheckUtils;
    private final TaskScheduler taskScheduler;

    public ResponseEntity<?> getProductFromCart(String userId) {
            Optional<User> user = userRepository.findUserByIdAndState(userId, Constant.USER_ACTIVE);
        if (user.isPresent()) {
            Optional<Order> order = orderRepository.findOrderByUser_IdAndState(new ObjectId(userId), Constant.ORDER_STATE_ENABLE);
            if (order.isPresent()) {
                CartResponse res = cartMap.getProductCartRes(order.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObjectData(true, "Get cart complete", res));
            } throw new NotFoundException("Not found cart  userid: "+userId);
        } throw new NotFoundException("Not found user with id: "+userId);
    }
//addAndUpdateProductToCart
    @Transactional
    public ResponseEntity<?> createAndPutProductToCart(String userId, CartReq req) {
        Optional<User> user = userRepository.findUserByIdAndState(userId, Constant.USER_ACTIVE);
        if (user.isPresent()) {
            Optional<Order> order = orderRepository.findOrderByUser_IdAndState(new ObjectId(userId), Constant.ORDER_STATE_ENABLE);
            if (order.isPresent()) {
                Optional<OrderProduct> products = order.get().getItems().stream().filter(
                        p -> p.getItem().getId().equals(req.getProductId())).findFirst();
                if (products.isPresent())
                    return countinueUpdateProductInCart(products.get(), req);
                else
                    return addProductToCartAvailable(order.get(), req);
            } else
                return createOrderByCart(user.get(), req);
        }
        throw new NotFoundException("Not found user with id: "+userId);
    }
//processAddProductToOrder
    @Transactional
    @Synchronized
    ResponseEntity<?> createOrderByCart(User user, CartReq req) {
        if (req.getQuantity() <= 0) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid quantity");
        Optional<Product> product = productRepository.findById(req.getProductId());
        if (product.isPresent()) {
            checkProductQuantityAndStock(product.get(), req);
            Order order = new Order(user, Constant.ORDER_STATE_ENABLE);
            orderRepository.insert(order);
            OrderProduct orderProduct = new OrderProduct(product.get(), req.getQuantity(), order);
            orderProductRepository.insert(orderProduct );
            CartProductResponse res = CartMap.toCartProductRes(orderProduct );
//            addScoreToRecommendation(productOption.get().getProduct().getCategory().getId(),
//                    productOption.get().getProduct().getBrand().getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObjectData(true, "Product have add to cart first time complete", res));
        } else throw new NotFoundException("Not found product with id: "+req.getProductId());
    }
//processAddProductToExistOrder
    private ResponseEntity<?> addProductToCartAvailable(Order order, CartReq req) {
//        if (req.getQuantity() <= 0)
//            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid quantity");
        Optional<Product> product = productRepository.findById(req.getProductId());
        if (product.isPresent()) {
            checkProductQuantityAndStock(product.get(), req);
            OrderProduct orderProduct = new OrderProduct(product.get(), req.getQuantity(), order);
            orderProductRepository.insert(orderProduct);
            CartProductResponse res = CartMap.toCartProductRes(orderProduct);
//            addScoreToRecommendation(productOption.get().getProduct().getCategory().getId(),
//                    productOption.get().getProduct().getBrand().getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObjectData(true, "Add product to cart complete", res));
        } else throw new NotFoundException("Not found product  id: "+req.getProductId());
    }

//    private void addScoreToRecommendation(String catId, String brandId, String userId) {
//        recommendCheckUtils.setCatId(catId);
//        recommendCheckUtils.setBrandId(brandId);
//        recommendCheckUtils.setType(Constants.CART_TYPE);
//        recommendCheckUtils.setUserId(userId);
//        recommendCheckUtils.setUserRepository(userRepository);
//        taskScheduler.schedule(recommendCheckUtils, new Date(System.currentTimeMillis()));
//    }

    private void checkProductQuantityAndStock(Product product, CartReq req) {

            if (product.getStock() < req.getQuantity()) {
                throw new AppException(HttpStatus.CONFLICT.value(), "Quantity exceeds stock: "+req.getProductId());
            }
    }

    private ResponseEntity<?> countinueUpdateProductInCart(OrderProduct orderProduct, CartReq req) {
        if (orderProduct.getQuantity() + req.getQuantity() == 0) {
            orderProductRepository.deleteById(orderProduct.getId());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObjectData(true, "Remove item "+orderProduct.getId()+" in cart success", ""));
        }
                long quantity = orderProduct.getQuantity() + req.getQuantity();
                if (orderProduct.getItem().getStock() >= quantity && quantity > 0) {
                    orderProduct.setQuantity(quantity);
                    orderProductRepository.save(orderProduct);
                } else throw new AppException(HttpStatus.CONFLICT.value(), "Quantity exceeds stock this product: "+req.getProductId());

        CartProductResponse res = CartMap.toCartProductRes(orderProduct);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObjectData(true, "Update product "+req.getProductId()+" complete", res));
    }


    public ResponseEntity<?> removeProductFromCart(String userId, String orderProductId) {
        Optional<User> user = userRepository.findUserByIdAndState(userId, Constant.USER_ACTIVE);
        if (user.isPresent()) {
            Optional<OrderProduct> orderProduct = orderProductRepository.findById(orderProductId);
            if (orderProduct.isPresent() && orderProduct.get().getOrder().getUser().getId().equals(userId)){
                orderProductRepository.deleteById(orderProductId);
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObjectData(true, "Remove item "+orderProductId+" in cart complete", ""));
            }
            else throw new AppException(HttpStatus.NOT_FOUND.value(), "Not found product in cart");
        } throw new NotFoundException("Not found user with id: "+userId);
    }
}
