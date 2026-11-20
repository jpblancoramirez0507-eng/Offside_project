package Controllers;

import Controllers.ComponentsControllers.Favorite_Item_ComponentController;
import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Nodes.Node_User;
import Models.Product;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class Controller_view_favorites implements Initializable {

    @FXML
    private ScrollPane spFavoritos;
    @FXML
    private VBox vbFavoritos;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblCantidad;
    @FXML
    private Button btnAnadirTodo;
    @FXML
    private FlowPane fpFavoritos;
    @FXML
    private VBox vbSinFavoritos;
    @FXML
    private Button btnIrCatalogo;

    private final Stack_Of_Product productos = Data_Manager.getManager().getStacks_products();
    private Catalog_Navigator navegador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mostrarFavoritos();
    }

    public void setNavegador(Catalog_Navigator navegador) {
        this.navegador = navegador;
    }

    private String correo() {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        return sesion == null ? null : sesion.getUser().getEmail();
    }

    /**
     * Favoritos del cliente con los datos actuales del catálogo (precio y
     * stock). Los productos que ya no están a la venta no se muestran.
     */
    private List<Product> favoritosActuales() {
        List<Product> actuales = new ArrayList<>();
        for (Product favorito : productos.getFavoriteItems(correo())) {
            Product actual = productos.loadProductFromCatalog(favorito.getIdProduct());
            if (actual != null && actual.isActive()) {
                actuales.add(actual);
            }
        }
        return actuales;
    }

    private void mostrarFavoritos() {
        List<Product> favoritos = favoritosActuales();

        fpFavoritos.getChildren().clear();
        for (Product producto : favoritos) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Favorite_Item_Component.fxml"));
                Parent card = loader.load();
                Favorite_Item_ComponentController controlador = loader.getController();
                controlador.setProducto(producto, this::agregarAlCarrito, this::quitar, this::abrir);
                fpFavoritos.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int cantidad = favoritos.size();
        lblCantidad.setText(cantidad + (cantidad == 1 ? " camiseta guardada" : " camisetas guardadas"));
        btnAnadirTodo.setDisable(favoritos.stream().noneMatch(p -> p.getStock() > 0));

        vbSinFavoritos.setVisible(favoritos.isEmpty());
        vbSinFavoritos.setManaged(favoritos.isEmpty());
        btnAnadirTodo.setVisible(!favoritos.isEmpty());
    }

    private void agregarAlCarrito(Product producto) {
        if (navegador != null) {
            navegador.agregarAlCarrito(producto, null, 1);
        }
    }

    private void quitar(Product producto) {
        productos.toggleFavorite(producto, correo());
        mostrarFavoritos();
        if (navegador != null) {
            navegador.mostrarAviso("Se quitó " + producto.getTeam() + " de tu lista de deseos.");
        }
    }

    private void abrir(Product producto) {
        if (navegador != null) {
            navegador.abrirProducto(producto);
        }
    }

    /**
     * Agrega una unidad de cada favorito con stock (en talla M o la primera
     * disponible).
     */
    @FXML
    private void onAnadirTodo(ActionEvent event) {
        int agregadas = 0;
        int sinStock = 0;
        for (Product producto : favoritosActuales()) {
            int resultado = productos.addItemToCart(producto, correo(), tallaPorDefecto(producto), 1);
            if (resultado > 0) {
                agregadas++;
            } else {
                sinStock++;
            }
        }

        if (navegador != null) {
            navegador.actualizarContadorCarrito();
            String mensaje = agregadas == 0 ? "No hay stock disponible para tus favoritos."
                    : "Se agregaron " + agregadas + (agregadas == 1 ? " camiseta" : " camisetas") + " al carrito."
                    + (sinStock > 0 ? " " + sinStock + " sin stock disponible." : "");
            navegador.mostrarAviso(mensaje);
        }
    }

    // Talla M si existe; si no, la primera
    private String tallaPorDefecto(Product producto) {
        if (producto.getSizes() == null || producto.getSizes().length == 0) {
            return null;
        }
        for (String talla : producto.getSizes()) {
            if (talla.equalsIgnoreCase("M")) {
                return talla;
            }
        }
        return producto.getSizes()[0];
    }

    @FXML
    private void onIrCatalogo(ActionEvent event) {
        if (navegador != null) {
            navegador.volverAlCatalogo();
        }
    }
}
