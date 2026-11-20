package Controllers.ComponentsControllers;

import Models.Product;
import Utilities.Product_Images;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class Favorite_Item_ComponentController implements Initializable {

    @FXML
    private VBox paneCardFavorito;
    @FXML
    private StackPane paneImagen;
    @FXML
    private SVGPath svgCamisetaGenerica;
    @FXML
    private ImageView imgProducto;
    @FXML
    private Label lblEtiqueta;
    @FXML
    private Button btnQuitarFavorito;
    @FXML
    private VBox vbInfo;
    @FXML
    private Label lblNombreEquipo;
    @FXML
    private Label lblVersion;
    @FXML
    private Circle cirLiga;
    @FXML
    private Label lblLiga;
    @FXML
    private Label lblPrecio;
    @FXML
    private Button btnAgregarCarrito;

    private Product producto;
    private Consumer<Product> alAgregar;
    private Consumer<Product> alQuitar;
    private Consumer<Product> alAbrir;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Un clic en el card (fuera de sus botones) abre el detalle del producto
        paneCardFavorito.setOnMouseClicked(evento -> {
            if (alAbrir != null && producto != null && !vieneDeUnBoton(evento.getPickResult().getIntersectedNode())) {
                alAbrir.accept(producto);
            }
        });
    }

    /**
     * Llena el card con el producto (datos actuales del catálogo) y las
     * acciones que ejecuta la lista de deseos.
     */
    public void setProducto(Product producto, Consumer<Product> alAgregar, Consumer<Product> alQuitar, Consumer<Product> alAbrir) {
        this.producto = producto;
        this.alAgregar = alAgregar;
        this.alQuitar = alQuitar;
        this.alAbrir = alAbrir;

        lblNombreEquipo.setText(producto.getTeam());
        lblVersion.setText(Product_Images.versionText(producto.getName()));
        lblLiga.setText(producto.getLeague());
        cirLiga.getStyleClass().add(Product_Images.leagueColorClass(producto.getLeague()));
        lblPrecio.setText("$ " + NumberFormat.getIntegerInstance(new Locale("es", "CO")).format(producto.getPrice()));

        boolean agotado = producto.getStock() <= 0;
        lblEtiqueta.setVisible(agotado);
        lblEtiqueta.setManaged(agotado);
        btnAgregarCarrito.setDisable(agotado);

        Image imagen = Product_Images.firstImage(producto.getIdProduct(), 300);
        imgProducto.setImage(imagen);
        svgCamisetaGenerica.setVisible(imagen == null);
    }

    public Product getProducto() {
        return producto;
    }

    private boolean vieneDeUnBoton(Node nodo) {
        while (nodo != null && nodo != paneCardFavorito) {
            if (nodo instanceof ButtonBase) {
                return true;
            }
            nodo = nodo.getParent();
        }
        return false;
    }

    @FXML
    private void onAgregarCarrito(ActionEvent event) {
        if (alAgregar != null) {
            alAgregar.accept(producto);
        }
    }

    @FXML
    private void onQuitarFavorito(ActionEvent event) {
        if (alQuitar != null) {
            alQuitar.accept(producto);
        }
    }
}
