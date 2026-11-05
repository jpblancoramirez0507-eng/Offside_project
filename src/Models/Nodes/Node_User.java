package Models.Nodes;

import Models.User;

public class Node_User {

    private User user;
    private Node_User next;
    private Node_User former;

    public Node_User(User user) {
        this.user = user;
        this.next = null;
        this.former = null;
    }

    public Node_User getNext() {
        return next;
    }

    public void setNext(Node_User next) {
        this.next = next;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Node_User getFormer() {
        return former;
    }

    public void setFormer(Node_User former) {
        this.former = former;
    }
}
