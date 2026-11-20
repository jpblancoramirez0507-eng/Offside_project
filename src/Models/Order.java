package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int number;
    private final String email_customer;
    private final LocalDateTime date_purchase;
    private final List<Product> products = new ArrayList<>();
    private double discount;
    private double shipping;
    private String discountCode;

    public Order(String email_customer, LocalDateTime date_purchase) {
        this.email_customer = email_customer;
        this.date_purchase = date_purchase;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getEmail_customer() {
        return email_customer;
    }

    public LocalDateTime getDate_purchase() {
        return date_purchase;
    }

    public List<Product> getProducts() {
        return products;
    }

    public double getProductsTotal() {
        double total = 0;
        for (Product product : products) {
            total += (double) product.getPrice() * product.getQuantity();
        }
        return total;
    }

    public double getTotal() {
        return getProductsTotal() - discount + shipping;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getShipping() {
        return shipping;
    }

    public void setShipping(double shipping) {
        this.shipping = shipping;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public int getUnits() {
        int units = 0;
        for (Product product : products) {
            units += product.getQuantity();
        }
        return units;
    }
}
