package Controllers.ComponentsControllers;

import DataStructures.Data_Manager;
import Models.Product;
import java.io.File;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class Product_ComponentController implements Initializable {
    
    public static int ultimasUnidades() {
        return Data_Manager.getManager().getSettings().getUltimasUnidades();
    }

    @FXML
    private VBox paneCardProducto;

    // Imagen
    @FXML
    private StackPane paneImagenProducto;
    @FXML
    private SVGPath svgCamisetaGenerica;
    @FXML
    private ImageView imgProducto;
    @FXML
    private Label lblEtiqueta;
    @FXML
    private Button btnFavorito;

    // Información
    @FXML
    private VBox vbInfoProducto;
    @FXML
    private Label lblNombreEquipo;
    @FXML
    private Label lblTipoTemporada;
    @FXML
    private HBox hbLiga;
    @FXML
    private Circle cirLiga;
    @FXML
    private Label lblLiga;
    @FXML
    private HBox hbPrecio;
    @FXML
    private Label lblPrecio;
    @FXML
    private Button btnAgregarCarrito;

    private Product producto;
    private Consumer<Product> alSeleccionar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Un clic en el card (fuera de sus botones) abre el detalle del producto
        paneCardProducto.setOnMouseClicked(evento -> {
            if (alSeleccionar != null && producto != null && !vieneDeUnBoton(evento.getPickResult().getIntersectedNode())) {
                alSeleccionar.accept(producto);
            }
        });
    }

    /**
     * Acción que se ejecuta al hacer clic en el card (el catálogo abre el
     * detalle del producto).
     */
    public void setAlSeleccionar(Consumer<Product> alSeleccionar) {
        this.alSeleccionar = alSeleccionar;
    }

    private boolean vieneDeUnBoton(Node nodo) {
        while (nodo != null && nodo != paneCardProducto) {
            if (nodo instanceof ButtonBase) {
                return true;
            }
            nodo = nodo.getParent();
        }
        return false;
    }

    /**
     * Llena el card con los datos del producto.
     */
    public void setProducto(Product producto) {
        this.producto = producto;

        lblNombreEquipo.setText(producto.getTeam());
        lblTipoTemporada.setText(tipoCamiseta(producto.getName()) + " 2026/27");

        lblLiga.setText(producto.getLeague());
        // Se quita el color anterior por si el card se vuelve a llenar (vista previa)
        cirLiga.getStyleClass().removeIf(clase -> clase.startsWith("league-dot-"));
        cirLiga.getStyleClass().add(claseColorLiga(producto.getLeague()));

        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
        lblPrecio.setText("$ " + formato.format(producto.getPrice()));

        mostrarEtiquetaStock(producto.getStock());
        cargarImagen(producto.getIdProduct());
    }

    /**
     * Muestra una imagen directamente (por ejemplo, la elegida en el registro
     * de un producto). Con null vuelve a la camiseta genérica.
     */
    public void mostrarImagen(Image imagen) {
        imgProducto.setImage(imagen);
        svgCamisetaGenerica.setVisible(imagen == null);
    }

    public Product getProducto() {
        return producto;
    }

    private void mostrarEtiquetaStock(int stock) {
        String texto = null;
        lblEtiqueta.getStyleClass().remove("card-badge-sold-out");
        if (stock == 0) {
            texto = "Agotado";
            lblEtiqueta.getStyleClass().add("card-badge-sold-out");
        } else if (stock < ultimasUnidades()) {
            texto = "Últimas unidades";
        }

        lblEtiqueta.setText(texto);
        lblEtiqueta.setVisible(texto != null);
        lblEtiqueta.setManaged(texto != null);
    }

    // "Camiseta Visitante Bayer Leverkusen 2026" -> "Visitante"
    private String tipoCamiseta(String nombre) {
        String texto = nombre == null ? "" : nombre.toLowerCase();
        if (texto.contains("visitante")) {
            return "Visitante";
        } else if (texto.contains("tercera")) {
            return "Tercera";
        }
        return "Local";
    }

    /**
     * Busca la primera imagen del producto en src/Images/ProductImages
     * ("0001 - Equipo - 1.png"). Si no existe o no se puede leer, se deja la
     * camiseta genérica.
     */
    private void cargarImagen(int idProduct) {
        File carpeta = new File(System.getProperty("user.dir"), "src/Images/ProductImages");
        String prefijo = String.format("%04d", idProduct) + " - ";

        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith(prefijo) && nombre.toLowerCase().matches(".* - 1\\.(png|jpg|jpeg)"));
        if (archivos == null || archivos.length == 0) {
            return;
        }

        Image imagen = new Image(archivos[0].toURI().toString(), 300, 300, true, true);
        if (!imagen.isError()) {
            imgProducto.setImage(imagen);
            svgCamisetaGenerica.setVisible(false);
        }
    }

    private String claseColorLiga(String liga) {
        String clave = liga == null ? "" : liga.toLowerCase().replace(" ", "");
        switch (clave) {
            case "premierleague":
                return "league-dot-premier";
            case "laliga":
                return "league-dot-laliga";
            case "seriea":
                return "league-dot-seriea";
            case "bundesliga":
                return "league-dot-bundesliga";
            case "ligue1":
                return "league-dot-ligue1";
            default:
                return "league-dot";
        }
    }

    @FXML
    private void onFavorito(ActionEvent event) {
        // TODO: agregar a favoritos
    }

    @FXML
    private void onAgregarCarrito(ActionEvent event) {
        // TODO: agregar al carrito
    }

}
