package DataStructures;

public class Data_Manager {

    private static Data_Manager manager;
    private final List_Double_User list_user;
    private final Stack_Of_Product stacks_products;
    
    public Data_Manager() {
        this.list_user = new List_Double_User();
        this.stacks_products = new Stack_Of_Product();
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
}
