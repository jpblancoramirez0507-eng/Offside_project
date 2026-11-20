package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int number;
    private final String email_customer;
    private final LocalDateTime date_purchase;
    private final List<Product> products = new ArrayList<>();

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

    public double getTotal() {
        double total = 0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }
}
