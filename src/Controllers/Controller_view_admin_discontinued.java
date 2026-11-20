package Controllers;

import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Product;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class Controller_view_admin_discontinued implements Initializable {

    @FXML
    private VBox vbProductosEliminados;
    @FXML
    private Hyperlink lnkVolverInventario;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private Label lblMensaje;
    @FXML
    private VBox vbTablaEliminados;
    @FXML
    private ScrollPane spListaEliminados;
    @FXML
    private VBox vbListaEliminados;
    @FXML
    private Label lblTotal;

    private final Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();
    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

    // Se ejecuta al volver al inventario: true si se restauró algún producto
    private Consumer<Boolean> alVolver;
    private boolean huboCambios = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mostrarProductos();
    }

    public void setAlVolver(Consumer<Boolean> alVolver) {
        this.alVolver = alVolver;
    }

    private void mostrarProductos() {
        List<Product> eliminados = catalogo.loadDiscontinuedProducts();

        vbListaEliminados.getChildren().clear();
        for (Product producto : eliminados) {
            vbListaEliminados.getChildren().add(crearFila(producto));
        }

        if (eliminados.isEmpty()) {
            Label vacio = new Label("No hay productos eliminados.\nCuando quites una camiseta del catálogo aparecerá aquí para poder restaurarla.");
            vacio.getStyleClass().add("empty-state");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vbListaEliminados.getChildren().add(vacio);
        }

        lblTotal.setText(eliminados.size() + (eliminados.size() == 1 ? " producto eliminado" : " productos eliminados"));
    }

    private HBox crearFila(Product producto) {
        SVGPath generica = new SVGPath();
        generica.setContent("M9.5,0 Q15,3.6 20.5,0 L30,5 L27,11.5 L22.5,9.5 L22.5,30 L7.5,30 L7.5,9.5 L3,11.5 L0,5 Z");
        generica.getStyleClass().add("product-thumb-placeholder");
        ImageView imagen = new ImageView();
        imagen.setFitWidth(34);
        imagen.setFitHeight(34);
        imagen.setPreserveRatio(true);
        Image cargada = primeraImagen(producto.getIdProduct());
        if (cargada != null) {
            imagen.setImage(cargada);
            generica.setVisible(false);
        }
        StackPane miniatura = new StackPane(generica, imagen);
        miniatura.getStyleClass().add("product-thumb");
        miniatura.setMinSize(38, 38);
        miniatura.setMaxSize(38, 38);

        miniatura.setOpacity(0.6);

        Label lblNombre = new Label(producto.getName());
        lblNombre.getStyleClass().add("cell-title");
        Label lblDetalle = new Label(producto.getBrand() + " · Ref. " + String.format("%04d", producto.getIdProduct()));
        lblDetalle.getStyleClass().add("cell-muted");
        VBox vbInfo = new VBox(1, lblNombre, lblDetalle);

        HBox hbProducto = new HBox(12, miniatura, vbInfo);
        hbProducto.setAlignment(Pos.CENTER_LEFT);
        hbProducto.setMinWidth(0);
        hbProducto.setPrefWidth(260);
        HBox.setHgrow(hbProducto, Priority.ALWAYS);

        Label lblLiga = celda(producto.getLeague(), 140, "cell-text");
        Label lblStock = celda(producto.getStock() + " und.", 80, "cell-text");
        Label lblPrecio = celda("$ " + formato.format(producto.getPrice()), 120, "cell-amount");

        Button btnRestaurar = new Button("Restaurar");
        btnRestaurar.getStyleClass().addAll("row-button", "row-button-add");
        btnRestaurar.setPrefWidth(90);
        btnRestaurar.setOnAction(e -> restaurar(producto));
        HBox hbAccion = new HBox(btnRestaurar);
        hbAccion.setAlignment(Pos.CENTER_LEFT);
        hbAccion.setMinWidth(110);
        hbAccion.setPrefWidth(110);

        HBox fila = new HBox(hbProducto, lblLiga, lblStock, lblPrecio, hbAccion);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("section-row");
        return fila;
    }

    private void restaurar(Product producto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Volver a poner a la venta \"" + producto.getName() + "\"?\n\nAparecerá de nuevo en el catálogo con " + producto.getStock() + " unidades.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Restaurar producto");
        confirmacion.setHeaderText(null);

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (!respuesta.isPresent() || respuesta.get() != ButtonType.OK) {
            return;
        }

        if (catalogo.restoreProduct(producto.getIdProduct())) {
            huboCambios = true;
            mostrarMensaje("\"" + producto.getName() + "\" volvió al catálogo.", true);
        } else {
            mostrarMensaje("No se pudo actualizar el archivo del producto.", false);
        }
        mostrarProductos();
    }

    // Primera imagen del producto ("0001 - Equipo - 1.png" o .jpg)
    private Image primeraImagen(int idProducto) {
        File carpeta = new File(System.getProperty("user.dir"), "src/Images/ProductImages");
        String prefijo = String.format("%04d", idProducto) + " - ";
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith(prefijo) && nombre.toLowerCase().matches(".* - 1\\.(png|jpg|jpeg)"));
        if (archivos == null || archivos.length == 0) {
            return null;
        }
        Image imagen = new Image(archivos[0].toURI().toString(), 68, 68, true, true);
        return imagen.isError() ? null : imagen;
    }

    private Label celda(String texto, double ancho, String clase) {
        Label label = new Label(texto);
        label.getStyleClass().add(clase);
        label.setMinWidth(ancho);
        label.setPrefWidth(ancho);
        return label;
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().setAll("label", exito ? "section-message-success" : "section-message-error");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    @FXML
    private void onVolver(ActionEvent event) {
        if (alVolver != null) {
            alVolver.accept(huboCambios);
        }
    }
}
