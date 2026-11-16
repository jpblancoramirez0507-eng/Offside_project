package Controllers;

import Controllers.ComponentsControllers.Product_ComponentController;
import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Product;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class Controller_view_admin_register_product implements Initializable {

    // Equipos principales de cada liga (lo que se puede elegir en el formulario)
    private static final Map<String, List<String>> EQUIPOS_POR_LIGA = new LinkedHashMap<>();

    static {
        EQUIPOS_POR_LIGA.put("Premier League", Arrays.asList("Arsenal", "Aston Villa", "Chelsea", "Liverpool", "Manchester City", "Manchester United", "Newcastle United", "Tottenham Hotspur"));
        EQUIPOS_POR_LIGA.put("La Liga", Arrays.asList("Athletic Club", "Atletico De Madrid", "Barcelona", "Real Betis", "Real Madrid", "Real Sociedad", "Sevilla", "Valencia", "Villarreal"));
        EQUIPOS_POR_LIGA.put("Serie A", Arrays.asList("Atalanta", "Fiorentina", "Inter De Milan", "Juventus", "Lazio", "Milan", "Napoli", "Roma"));
        EQUIPOS_POR_LIGA.put("Bundesliga", Arrays.asList("Bayer Leverkusen", "Bayern Munich", "Borussia Dortmund", "Eintracht Frankfurt", "RB Leipzig", "VfB Stuttgart"));
        EQUIPOS_POR_LIGA.put("Ligue 1", Arrays.asList("AS Monaco", "Lille", "Olympique de Lyon", "Olympique de Marsella", "OGC Nice", "Paris Saint-Germain"));
    }

    private static final String[] TALLAS = {"XS", "S", "M", "L", "XL", "XXL"};
    private static final long TAMANO_MAXIMO_IMAGEN = 5L * 1024 * 1024;
    private static final int MAXIMO_IMAGENES = 6;

    @FXML
    private VBox vbRegistrarProducto;
    @FXML
    private Hyperlink lnkVolverInventario;
    @FXML
    private Label lblTitulo;

    // Formulario
    @FXML
    private VBox vbFormulario;
    @FXML
    private TextField txtNombreProducto;
    @FXML
    private ComboBox<String> cbLiga;
    @FXML
    private ComboBox<String> cbEquipo;
    @FXML
    private ComboBox<String> cbVersion;
    @FXML
    private ComboBox<String> cbMarca;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStockXS;
    @FXML
    private TextField txtStockS;
    @FXML
    private TextField txtStockM;
    @FXML
    private TextField txtStockL;
    @FXML
    private TextField txtStockXL;
    @FXML
    private TextField txtStockXXL;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private StackPane paneZonaImagen;
    @FXML
    private Label lblImagenesProducto;
    @FXML
    private Label lblZonaImagenTitulo;
    @FXML
    private Label lblZonaImagenDetalle;
    @FXML
    private FlowPane fpImagenes;
    @FXML
    private Label lblAyudaImagenes;
    @FXML
    private Label lblErrorFormulario;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardarProducto;

    // Vista previa
    @FXML
    private VBox vbVistaPrevia;
    @FXML
    private StackPane paneVistaPrevia;
    @FXML
    private Label lblStockTotal;

    private TextField[] camposStock;
    private Product_ComponentController vistaPrevia;
    // Imágenes elegidas, en orden: la primera es la principal
    private final List<ImagenProducto> imagenes = new ArrayList<>();
    private Image imagenVistaPrevia;
    private String ultimoNombreAutomatico = "";

    // Se ejecuta al salir del formulario: true si se guardó un producto
    private Consumer<Boolean> alVolver;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        camposStock = new TextField[]{txtStockXS, txtStockS, txtStockM, txtStockL, txtStockXL, txtStockXXL};

        cbLiga.getItems().addAll(EQUIPOS_POR_LIGA.keySet());
        cbVersion.getItems().addAll("Local 2026/27", "Visitante 2026/27", "Tercera 2026/27");
        cbVersion.getSelectionModel().selectFirst();
        cbMarca.getItems().addAll("Adidas", "Nike", "Puma", "New Balance", "Kappa", "Castore", "Macron", "Umbro");

        // Al cambiar la liga se cargan sus equipos
        cbLiga.valueProperty().addListener((obs, anterior, liga) -> {
            cbEquipo.setValue(null);
            cbEquipo.getItems().setAll(liga == null ? new ArrayList<>() : EQUIPOS_POR_LIGA.get(liga));
            cbEquipo.setPromptText(liga == null ? "Primero elige la liga" : "Selecciona el equipo");
            actualizarVistaPrevia();
        });
        cbEquipo.valueProperty().addListener((obs, anterior, nuevo) -> sugerirNombre());
        cbVersion.valueProperty().addListener((obs, anterior, nuevo) -> sugerirNombre());
        cbMarca.getEditor().textProperty().addListener((obs, anterior, nuevo) -> actualizarVistaPrevia());
        txtNombreProducto.textProperty().addListener((obs, anterior, nuevo) -> actualizarVistaPrevia());

        // Precio y stock solo aceptan números (el precio también acepta puntos)
        txtPrecio.setTextFormatter(new TextFormatter<>(cambio -> cambio.getControlNewText().matches("[\\d.]*") ? cambio : null));
        txtPrecio.textProperty().addListener((obs, anterior, nuevo) -> actualizarVistaPrevia());
        for (TextField campo : camposStock) {
            campo.setTextFormatter(new TextFormatter<>(cambio -> cambio.getControlNewText().matches("\\d{0,5}") ? cambio : null));
            campo.textProperty().addListener((obs, anterior, nuevo) -> actualizarVistaPrevia());
        }

        configurarZonaImagen();
        cargarVistaPrevia();
        actualizarVistaPrevia();
    }

    public void setAlVolver(Consumer<Boolean> alVolver) {
        this.alVolver = alVolver;
    }

    // ---------------------------------------------------------------------
    // Vista previa
    // ---------------------------------------------------------------------

    private void cargarVistaPrevia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Product_Component.fxml"));
            Parent card = loader.load();
            vistaPrevia = loader.getController();
            paneVistaPrevia.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Si el nombre está vacío (o sigue siendo el sugerido), se arma con la
     * versión y el equipo: "Camiseta Local Real Madrid 2026".
     */
    private void sugerirNombre() {
        String nombreActual = txtNombreProducto.getText().trim();
        if (cbEquipo.getValue() != null && (nombreActual.isEmpty() || nombreActual.equals(ultimoNombreAutomatico))) {
            ultimoNombreAutomatico = "Camiseta " + tipoVersion() + " " + cbEquipo.getValue() + " 2026";
            txtNombreProducto.setText(ultimoNombreAutomatico);
        }
        actualizarVistaPrevia();
    }

    // "Visitante 2026/27" -> "Visitante"
    private String tipoVersion() {
        String version = cbVersion.getValue() == null ? "Local" : cbVersion.getValue();
        return version.split(" ")[0];
    }

    private void actualizarVistaPrevia() {
        int stockTotal = calcularStockTotal();
        lblStockTotal.setText("Stock total: " + stockTotal + (stockTotal == 1 ? " unidad" : " unidades"));

        if (vistaPrevia == null) {
            return;
        }

        String nombre = txtNombreProducto.getText().trim().isEmpty() ? "Camiseta " + tipoVersion() : txtNombreProducto.getText().trim();
        String equipo = cbEquipo.getValue() == null ? "Equipo" : cbEquipo.getValue();
        String liga = cbLiga.getValue() == null ? "Liga" : cbLiga.getValue();

        Product producto = new Product(0, nombre, textoMarca(), equipo, liga, tallasConStock(), leerPrecio(), "");
        producto.setStock(stockTotal);

        vistaPrevia.setProducto(producto);
        vistaPrevia.mostrarImagen(imagenVistaPrevia);
    }

    // ---------------------------------------------------------------------
    // Imagen
    // ---------------------------------------------------------------------

    private void configurarZonaImagen() {
        paneZonaImagen.setOnDragOver((DragEvent evento) -> {
            if (evento.getDragboard().hasFiles()) {
                evento.acceptTransferModes(TransferMode.COPY);
                if (!paneZonaImagen.getStyleClass().contains("image-drop-zone-active")) {
                    paneZonaImagen.getStyleClass().add("image-drop-zone-active");
                }
            }
            evento.consume();
        });
        paneZonaImagen.setOnDragExited(evento -> paneZonaImagen.getStyleClass().remove("image-drop-zone-active"));
        paneZonaImagen.setOnDragDropped((DragEvent evento) -> {
            boolean ok = false;
            if (evento.getDragboard().hasFiles()) {
                agregarImagenes(evento.getDragboard().getFiles());
                ok = true;
            }
            evento.setDropCompleted(ok);
            evento.consume();
        });
    }

    @FXML
    private void onElegirImagen(MouseEvent event) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Elegir imágenes del producto");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes PNG o JPG", "*.png", "*.jpg", "*.jpeg"));

        List<File> archivos = selector.showOpenMultipleDialog(paneZonaImagen.getScene().getWindow());
        if (archivos != null) {
            agregarImagenes(archivos);
        }
    }

    /**
     * Agrega las imágenes válidas (PNG/JPG reales, máximo 5 MB, sin repetir y
     * hasta MAXIMO_IMAGENES). Las que no se pueden agregar se informan en el
     * mensaje de error.
     */
    private void agregarImagenes(List<File> archivos) {
        List<String> problemas = new ArrayList<>();

        for (File archivo : archivos) {
            if (imagenes.size() >= MAXIMO_IMAGENES) {
                problemas.add("Solo se pueden agregar " + MAXIMO_IMAGENES + " imágenes.");
                break;
            }

            String nombre = archivo.getName().toLowerCase();
            if (!nombre.endsWith(".png") && !nombre.endsWith(".jpg") && !nombre.endsWith(".jpeg")) {
                problemas.add(archivo.getName() + ": debe ser PNG o JPG.");
                continue;
            }
            if (archivo.length() > TAMANO_MAXIMO_IMAGEN) {
                problemas.add(archivo.getName() + ": pesa más de 5 MB.");
                continue;
            }
            if (yaAgregada(archivo)) {
                problemas.add(archivo.getName() + ": ya está agregada.");
                continue;
            }

            Image imagen = new Image(archivo.toURI().toString(), 300, 300, true, true);
            if (imagen.isError()) {
                problemas.add(archivo.getName() + ": no se pudo leer (puede que no sea un PNG o JPG real, por ejemplo un WebP con extensión .png).");
                continue;
            }

            imagenes.add(new ImagenProducto(archivo, imagen));
        }

        if (problemas.isEmpty()) {
            ocultarError();
        } else {
            mostrarError(String.join("\n", problemas));
        }
        actualizarImagenes();
    }

    private boolean yaAgregada(File archivo) {
        for (ImagenProducto imagen : imagenes) {
            if (imagen.archivo.getAbsoluteFile().equals(archivo.getAbsoluteFile())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vuelve a dibujar las miniaturas. La primera es la principal; al hacer
     * clic en otra pasa a ser la principal y la "×" la quita.
     */
    private void actualizarImagenes() {
        fpImagenes.getChildren().clear();

        for (int i = 0; i < imagenes.size(); i++) {
            ImagenProducto imagen = imagenes.get(i);

            ImageView miniatura = new ImageView(imagen.imagen);
            miniatura.setFitWidth(70);
            miniatura.setFitHeight(70);
            miniatura.setPreserveRatio(true);

            Button btnQuitar = new Button("×");
            btnQuitar.getStyleClass().add("image-thumb-remove");
            btnQuitar.setOnAction(e -> {
                imagenes.remove(imagen);
                ocultarError();
                actualizarImagenes();
            });
            StackPane.setAlignment(btnQuitar, Pos.TOP_RIGHT);
            StackPane.setMargin(btnQuitar, new Insets(4));

            StackPane tarjeta = new StackPane(miniatura, btnQuitar);
            tarjeta.getStyleClass().add("image-thumb");
            tarjeta.setPrefSize(84, 84);
            tarjeta.setMaxSize(84, 84);

            if (i == 0) {
                Label etiqueta = new Label("Principal");
                etiqueta.getStyleClass().add("image-thumb-badge");
                StackPane.setAlignment(etiqueta, Pos.BOTTOM_LEFT);
                StackPane.setMargin(etiqueta, new Insets(4));
                tarjeta.getChildren().add(etiqueta);
                tarjeta.getStyleClass().add("image-thumb-main");
            } else {
                tarjeta.setOnMouseClicked(e -> {
                    imagenes.remove(imagen);
                    imagenes.add(0, imagen);
                    actualizarImagenes();
                });
            }

            Tooltip.install(tarjeta, new Tooltip(imagen.archivo.getName()));
            fpImagenes.getChildren().add(tarjeta);
        }

        boolean hayImagenes = !imagenes.isEmpty();
        fpImagenes.setVisible(hayImagenes);
        fpImagenes.setManaged(hayImagenes);
        lblAyudaImagenes.setVisible(imagenes.size() > 1);
        lblAyudaImagenes.setManaged(imagenes.size() > 1);

        lblImagenesProducto.setText(hayImagenes ? "Imágenes del producto (" + imagenes.size() + " de " + MAXIMO_IMAGENES + ")" : "Imágenes del producto");
        lblZonaImagenTitulo.setText(hayImagenes ? "Arrastra más imágenes o haz clic para agregarlas" : "Arrastra imágenes o haz clic para subirlas");

        imagenVistaPrevia = hayImagenes ? imagenes.get(0).imagen : null;
        actualizarVistaPrevia();
    }

    // Imagen elegida en el formulario: el archivo original y su versión cargada
    private static class ImagenProducto {

        private final File archivo;
        private final Image imagen;

        private ImagenProducto(File archivo, Image imagen) {
            this.archivo = archivo;
            this.imagen = imagen;
        }
    }

    // ---------------------------------------------------------------------
    // Guardar
    // ---------------------------------------------------------------------

    @FXML
    private void onGuardarProducto(ActionEvent event) {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();

        String nombre = txtNombreProducto.getText().trim();
        String marca = textoMarca();
        float precio = leerPrecio();
        int stockTotal = calcularStockTotal();

        if (nombre.isEmpty()) {
            mostrarError("Escribe el nombre del producto.");
            return;
        }
        if (nombre.contains(",")) {
            mostrarError("El nombre no puede tener comas.");
            return;
        }
        if (cbLiga.getValue() == null || cbEquipo.getValue() == null) {
            mostrarError("Elige la liga y el equipo.");
            return;
        }
        if (marca.isEmpty()) {
            mostrarError("Escribe o elige la marca.");
            return;
        }
        if (precio <= 0) {
            mostrarError("Escribe un precio mayor que 0.");
            return;
        }
        if (stockTotal <= 0) {
            mostrarError("Agrega stock en al menos una talla.");
            return;
        }
        if (catalogo.existsProductWithName(nombre)) {
            mostrarError("Ya existe un producto con el nombre \"" + nombre + "\".");
            return;
        }

        int id = catalogo.getNextProductId();
        String descripcion = "Camiseta " + cbEquipo.getValue() + " – Temporada 2026/2027";

        Product producto = new Product(id, nombre, marca, cbEquipo.getValue(), cbLiga.getValue(), tallasConStock(), precio, descripcion);
        producto.setStock(stockTotal);

        if (!catalogo.saveProductToCatalog(producto, txtDescripcion.getText())) {
            mostrarError("No se pudo guardar el archivo del producto.");
            return;
        }

        // Se guardan como "NNNN - Equipo - 1", "- 2", ...; la 1 es la principal
        int imagenesFallidas = 0;
        for (int i = 0; i < imagenes.size(); i++) {
            if (!catalogo.saveProductImage(id, cbEquipo.getValue(), imagenes.get(i).archivo, i + 1)) {
                imagenesFallidas++;
            }
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Producto registrado");
        alerta.setHeaderText(null);
        alerta.setContentText("Se registró \"" + nombre + "\" (Ref. " + String.format("%04d", id) + ") con " + stockTotal + " unidades"
                + " y " + (imagenes.size() - imagenesFallidas) + (imagenes.size() - imagenesFallidas == 1 ? " imagen." : " imágenes.")
                + (imagenesFallidas == 0 ? "" : "\nNo se pudieron copiar " + imagenesFallidas + " imágenes."));
        alerta.showAndWait();

        if (alVolver != null) {
            alVolver.accept(true);
        }
    }

    @FXML
    private void onVolver(ActionEvent event) {
        if (alVolver != null) {
            alVolver.accept(false);
        }
    }

    // ---------------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------------

    private String textoMarca() {
        String marca = cbMarca.getEditor().getText();
        return marca == null ? "" : marca.trim();
    }

    // "$ 189.900" / "189900" -> 189900
    private float leerPrecio() {
        String soloNumeros = txtPrecio.getText().replaceAll("\\D", "");
        if (soloNumeros.isEmpty() || soloNumeros.length() > 9) {
            return 0;
        }
        return Float.parseFloat(soloNumeros);
    }

    private int leerStock(TextField campo) {
        return campo.getText().isEmpty() ? 0 : Integer.parseInt(campo.getText());
    }

    private int calcularStockTotal() {
        int total = 0;
        for (TextField campo : camposStock) {
            total += leerStock(campo);
        }
        return total;
    }

    // Tallas que tienen al menos una unidad, en orden XS..XXL
    private String[] tallasConStock() {
        List<String> tallas = new ArrayList<>();
        for (int i = 0; i < camposStock.length; i++) {
            if (leerStock(camposStock[i]) > 0) {
                tallas.add(TALLAS[i]);
            }
        }
        return tallas.toArray(new String[0]);
    }

    private void mostrarError(String mensaje) {
        lblErrorFormulario.setText(mensaje);
        lblErrorFormulario.setVisible(true);
        lblErrorFormulario.setManaged(true);
    }

    private void ocultarError() {
        lblErrorFormulario.setVisible(false);
        lblErrorFormulario.setManaged(false);
    }

}
