package Controllers.ComponentsControllers;

import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Product;
import Utilities.Product_Images;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class Cart_Item_ComponentController implements Initializable {

    @FXML
    private HBox paneItemCarrito;
    @FXML
    private StackPane paneImagen;
    @FXML
    private SVGPath svgCamisetaGenerica;
    @FXML
    private ImageView imgProducto;
    @FXML
    private VBox vbInfo;
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblVersionLiga;
    @FXML
    private Label lblTalla;
    @FXML
    private HBox hbAcciones;
    @FXML
    private Button btnRestar;
    @FXML
    private Label lblCantidad;
    @FXML
    private Button btnSumar;
    @FXML
    private Hyperlink lnkMoverFavoritos;
    @FXML
    private VBox vbPrecio;
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblPrecioUnitario;
    @FXML
    private Hyperlink lnkEliminar;

    private final Stack_Of_Product productos = Data_Manager.getManager().getStacks_products();
    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

    private Product item;
    private String correo;
    // Se ejecuta cuando la línea cambia o se elimina (la vista del carrito recalcula el resumen)
    private Runnable alCambiar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    /**
     * Llena la línea con un producto del carrito del cliente.
     */
    public void setItem(Product item, String correo, Runnable alCambiar) {
        this.item = item;
        this.correo = correo;
        this.alCambiar = alCambiar;

        lblNombre.setText(item.getName());
        lblVersionLiga.setText(Product_Images.versionText(item.getName()) + " · " + item.getLeague());
        lblTalla.setText("Talla: " + (item.getSelectedSize() == null ? "única" : item.getSelectedSize()));

        Image imagen = Product_Images.firstImage(item.getIdProduct(), 140);
        imgProducto.setImage(imagen);
        svgCamisetaGenerica.setVisible(imagen == null);

        actualizarCantidad();
    }

    public Product getItem() {
        return item;
    }

    private void actualizarCantidad() {
        // Stock que queda contando lo que hay de este producto en otras tallas
        Product actual = productos.loadProductFromCatalog(item.getIdProduct());
        int stock = actual == null ? 0 : actual.getStock();
        int otrasTallas = productos.getUnitsInCart(item.getIdProduct(), correo) - item.getQuantity();

        lblCantidad.setText(String.valueOf(item.getQuantity()));
        btnRestar.setDisable(item.getQuantity() <= 1);
        btnSumar.setDisable(item.getQuantity() >= stock - otrasTallas);

        lblSubtotal.setText("$ " + formato.format((double) item.getPrice() * item.getQuantity()));
        lblPrecioUnitario.setText("$ " + formato.format(item.getPrice()) + " c/u");
    }

    private void cambiarCantidad(int nuevaCantidad) {
        int cantidad = productos.updateCartItemQuantity(item.getIdProduct(), correo, item.getSelectedSize(), nuevaCantidad);
        item.setQuantity(cantidad);
        actualizarCantidad();
        avisarCambio();
    }

    private void avisarCambio() {
        if (alCambiar != null) {
            alCambiar.run();
        }
    }

    @FXML
    private void onRestar(ActionEvent event) {
        cambiarCantidad(item.getQuantity() - 1);
    }

    @FXML
    private void onSumar(ActionEvent event) {
        cambiarCantidad(item.getQuantity() + 1);
    }

    @FXML
    private void onEliminar(ActionEvent event) {
        productos.removeCartItem(item.getIdProduct(), correo, item.getSelectedSize());
        avisarCambio();
    }

    /**
     * Quita la línea del carrito y deja el producto en favoritos.
     */
    @FXML
    private void onMoverAFavoritos(ActionEvent event) {
        if (!productos.isFavorite(item.getIdProduct(), correo)) {
            productos.toggleFavorite(item, correo);
        }
        productos.removeCartItem(item.getIdProduct(), correo, item.getSelectedSize());
        avisarCambio();
    }
}
