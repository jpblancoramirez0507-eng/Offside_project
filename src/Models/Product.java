package Models;

import java.time.LocalDateTime;

public class Product {

    private int idProduct;
    private String name;
    private String brand;
    private String team;
    private String league;
    private String[] sizes;
    private float price;
    private String description;
    private String email_customer;
    private LocalDateTime date_purchase;
    private String selectedSize;

    public Product() {
    }

    public Product(int idProduct, String name, String brand, String team, String league, String[] sizes, float price, String description) {
        this.idProduct = idProduct;
        this.name = name;
        this.brand = brand;
        this.team = team;
        this.league = league;
        this.sizes = sizes;
        this.price = price;
        this.description = description;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public String[] getSizes() {
        return sizes;
    }

    public void setSizes(String[] sizes) {
        this.sizes = sizes;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail_customer() {
        return email_customer;
    }

    public void setEmail_customer(String email_customer) {
        this.email_customer = email_customer;
    }

    public LocalDateTime getDate_purchase() {
        return date_purchase;
    }

    public void setDate_purchase(LocalDateTime date_purchase) {
        this.date_purchase = date_purchase;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }
}
