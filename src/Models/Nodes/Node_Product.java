package Models.Nodes;

import Models.Product;

public class Node_Product {

    private Product product;
    private Node_Product next;
    private Node_Product former;

    public Node_Product(Product product) {
        this.product = product;
        this.next = null;
        this.former = null;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Node_Product getNext() {
        return next;
    }

    public void setNext(Node_Product next) {
        this.next = next;
    }

    public Node_Product getFormer() {
        return former;
    }

    public void setFormer(Node_Product former) {
        this.former = former;
    }
}
