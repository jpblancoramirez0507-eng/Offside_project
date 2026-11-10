package Controllers.ComponentsControllers;

import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Product;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class Admin_Product_ComponentController implements Initializable {

    public static final int STOCK_BAJO = 20;

    @FXML
    private HBox paneFilaProducto;

    // Columna: producto
    @FXML
    private HBox hbProducto;
    @FXML
    private StackPane paneImagenProducto;
    @FXML
    private SVGPath svgCamisetaGenerica;
    @FXML
    private ImageView imgProducto;
    @FXML
    private VBox vbInfoProducto;
    @FXML
    private Label lblNombreProducto;
    @FXML
    private Label lblDetalleProducto;

    // Columna: liga
    @FXML
    private HBox hbLiga;
    @FXML
    private Circle cirLiga;
    @FXML
    private Label lblLiga;

    // Columna: stock
    @FXML
    private Label lblStock;

    // Columna: acciones
    @FXML
    private HBox hbAcciones;
    @FXML
    private Button btnAnadirStock;
    @FXML
    private Button btnQuitarStock;
    @FXML
    private Button btnEliminarProducto;

    private Product producto;
    private Runnable alCambiarStock;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setProducto(Product producto, int stock) {
        this.producto = producto;

        lblNombreProducto.setText(producto.getName());
        lblDetalleProducto.setText(producto.getBrand() + " · Ref. " + String.format("%04d", producto.getIdProduct()));

        lblLiga.setText(producto.getLeague());
        cirLiga.getStyleClass().add(claseColorLiga(producto.getLeague()));

        setStock(stock);
        cargarImagen(producto.getIdProduct());
    }

    public void setStock(int stock) {
        lblStock.setText(String.valueOf(stock));
        lblStock.getStyleClass().remove("product-stock-low");
        if (stock < STOCK_BAJO) {
            lblStock.getStyleClass().add("product-stock-low");
        }
    }

    public Product getProducto() {
        return producto;
    }

    /**
     * Busca la primera imagen del producto en src/Images/ProductImages ("0001 -
     * Equipo - 1.png"). Si no existe o no se puede leer, se deja la camiseta
     * genérica.
     */
    private void cargarImagen(int idProduct) {
        File carpeta = new File(System.getProperty("user.dir"), "src/Images/ProductImages");
        String prefijo = String.format("%04d", idProduct) + " - ";

        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith(prefijo) && nombre.endsWith(" - 1.png"));
        if (archivos == null || archivos.length == 0) {
            return;
        }

        Image imagen = new Image(archivos[0].toURI().toString(), 68, 68, true, true);
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

    /**
     * Acción que el panel del administrador ejecuta cuando cambia el stock de
     * esta fila (para recalcular el resumen).
     *
     * @param alCambiarStock
     */
    public void setAlCambiarStock(Runnable alCambiarStock) {
        this.alCambiarStock = alCambiarStock;
    }

    @FXML
    private void onAnadirStock(ActionEvent event) {
        Integer cantidad = pedirCantidad("Añadir stock", "¿Cuántas unidades quieres añadir?");
        if (cantidad == null) {
            return;
        }
        guardarStock(producto.getStock() + cantidad);
    }

    @FXML
    private void onQuitarStock(ActionEvent event) {
        if (producto.getStock() == 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Quitar stock", "Este producto no tiene unidades en stock.");
            return;
        }

        Integer cantidad = pedirCantidad("Quitar stock", "¿Cuántas unidades quieres quitar? (máximo " + producto.getStock() + ")");
        if (cantidad == null) {
            return;
        }

        if (cantidad > producto.getStock()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Quitar stock", "No puedes quitar más unidades de las que hay en stock (" + producto.getStock() + ").");
            return;
        }
        guardarStock(producto.getStock() - cantidad);
    }

    /**
     * Muestra un diálogo para escribir la cantidad. Retorna null si se cancela
     * o si el valor no es un entero mayor que 0.
     */
    private Integer pedirCantidad(String titulo, String mensaje) {
        TextInputDialog dialogo = new TextInputDialog("1");
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(producto.getName() + "  ·  Stock actual: " + producto.getStock());
        dialogo.setContentText(mensaje);

        Optional<String> respuesta = dialogo.showAndWait();
        if (!respuesta.isPresent()) {
            return null;
        }

        try {
            int cantidad = Integer.parseInt(respuesta.get().trim());
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
            return cantidad;
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, titulo, "Escribe un número entero mayor que 0.");
            return null;
        }
    }

    /**
     * Guarda el nuevo stock en el archivo del producto y, si se pudo, actualiza
     * la fila y el resumen del panel.
     */
    private void guardarStock(int nuevoStock) {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();

        if (!catalogo.updateStockInCatalog(producto.getIdProduct(), nuevoStock)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el stock en el archivo del producto.");
            return;
        }

        producto.setStock(nuevoStock);
        setStock(nuevoStock);

        if (alCambiarStock != null) {
            alCambiarStock.run();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void onEliminarProducto(ActionEvent event) {
        // TODO
    }

}
