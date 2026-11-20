package DataStructures;

import Models.Nodes.Node_User;

public class Data_Manager {

    private static Data_Manager manager;
    private final List_Double_User list_user;
    private final Stack_Of_Product stacks_products;
    private final App_Settings settings;
    private final Discount_Codes discount_codes;

    private Node_User current_user;

    public Data_Manager() {
        this.list_user = new List_Double_User();
        this.stacks_products = new Stack_Of_Product();
        this.settings = new App_Settings();
        this.discount_codes = new Discount_Codes();
    }

    public static Data_Manager getManager() {
        if (manager == null) {
            manager = new Data_Manager();
        }
        return manager;
    }

    public List_Double_User getList_user() {
        return list_user;
    }

    public Stack_Of_Product getStacks_products() {
        return stacks_products;
    }

    public Discount_Codes getDiscount_codes() {
        return discount_codes;
    }

    public App_Settings getSettings() {
        return settings;
    }

    public Node_User getCurrent_user() {
        return current_user;
    }

    public void setCurrent_user(Node_User current_user) {
        this.current_user = current_user;
    }
}
