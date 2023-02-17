package entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @Column(name = "reference", nullable = false)
    private String reference;

    // Define a field to store the name of the product
    private String name;

    // Define a field to store a brief description of the product
    private String description;

    // Define a field to store the URL of the image for the product
    private String Image;

    // Define a field to store the current price of the product. This price may change during a promotion.
    private float productPrice;

    // Define a field to store the price of the product before any discounts are applied
    private float productPriceBeforeDiscount;

    // Define a field to store the price of shipping the product
    private float deliveryPrice;

    // Define a field to store the average rating of the product
    private float rating;

    // Define a field to store the number of ratings that have been given for the product
    private int numberOfRatings;

    // Define a field to store the quantity of the product available for purchase
    private int quantity;

    private String productWeight;

    // Define a field to indicate whether the product is currently available for purchase
    private boolean enabled;

    // Define a field to store any additional delivery instructions provided by the customer
    private String AdditionalDeliveryInstructions;

    // Define a field to store the current status of the product
    private ProductStatus productStatus;
}