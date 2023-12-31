package com.example.officepcstore.models.enity.product;


import com.example.officepcstore.models.enity.Brand;
import com.example.officepcstore.models.enity.Category;
import com.example.officepcstore.models.enity.CommentProduct;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.TextScore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class  Product {
    @Id
    private String id;
    private String name;
    private String description;
    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @Field(targetType = DECIMAL128)
    private BigDecimal reducedPrice;
    private int discount = 0;
    private long stock;
    private long sold = 0;
    @DocumentReference(lazy = true)
    @Indexed
    private Category category;
    @DocumentReference(lazy = true)
    @Indexed
    private Brand brand;
    private double rate = 0;
    @ReadOnlyProperty
    @DocumentReference(lookup="{'product':?#{#self._id} }", lazy = true)
    @Indexed
    private List<CommentProduct> commentProducts;
    @Indexed
    private String state;
    private List<ProductImage> productImageList = new ArrayList<>();

    @Indexed
    private List<Map<String, String>> productConfiguration = new ArrayList<>();

    @CreatedDate
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime createdDate;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @LastModifiedDate
    LocalDateTime lastModifiedDate;
    @TextScore
    Float score;

//    public Product(String name, String description, BigDecimal price, Category category, Brand brand, String state, int discount) {
//        this.name = name;
//        this.description = description;
//        this.price = price;
//        this.category = category;
//        this.brand = brand;
//        this.state = state;
//        this.discount = discount;
//    }


    public Product(String name, String description, BigDecimal price, int discount, long stock, Category category, Brand brand, List<Map<String, String>> productConfiguration, String state) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.stock = stock;
        this.category = category;
        this.brand = brand;
        this.productConfiguration = productConfiguration;
        this.state = state;
    }




    @Transient
    public int getAllCommentRate() {
        try {
            return commentProducts.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
