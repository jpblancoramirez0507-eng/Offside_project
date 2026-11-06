package DataStructures;

public class Data_Manager {

    private static Data_Manager manager;
    private final List_Double_User list_user;

    public Data_Manager() {
        this.list_user = new List_Double_User();
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
}
