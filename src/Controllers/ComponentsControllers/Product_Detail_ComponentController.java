package Controllers.ComponentsControllers;

import DataStructures.Data_Manager;
import Models.Product;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class Product_Detail_ComponentController implements Initializable {

    @FXML
    private ScrollPane spDetalleProducto;
    @FXML
    private VBox vbDetalleProducto;

    // Navegación
    @FXML
    private HBox hbNavegacion;
    @FXML
    private Hyperlink lnkVolverCatalogo;
    @FXML
    private Hyperlink lnkRutaInicio;
    @FXML
    private Label lblRutaLiga;
    @FXML
    private Label lblRutaEquipo;

    // Galería
    @FXML
    private VBox vbGaleria;
    @FXML
    private StackPane paneImagenPrincipal;
    @FXML
    private SVGPath svgCamisetaGenerica;
    @FXML
    private ImageView imgPrincipal;
    @FXML
    private HBox hbMiniaturas;

    // Información
    @FXML
    private VBox vbInformacion;
    @FXML
    private Label lblVersion;
    @FXML
    private Label lblNombre;
    @FXML
    private HBox hbLiga;
    @FXML
    private Circle cirLiga;
    @FXML
    private Label lblLiga;
    @FXML
    private Label lblMarca;
    @FXML
    private Label lblPrecio;
    @FXML
    private Label lblEnvio;

    // Talla
    @FXML
    private HBox hbTallas;
    @FXML
    private ToggleGroup tgTallas;
    @FXML
    private ToggleButton tbTallaXS;
    @FXML
    private ToggleButton tbTallaS;
    @FXML
    private ToggleButton tbTallaM;
    @FXML
    private ToggleButton tbTallaL;
    @FXML
    private ToggleButton tbTallaXL;
    @FXML
    private ToggleButton tbTallaXXL;

    // Cantidad
    @FXML
    private HBox hbCantidad;
    @FXML
    private Button btnRestarCantidad;
    @FXML
    private Label lblCantidad;
    @FXML
    private Button btnSumarCantidad;
    @FXML
    private Label lblDisponibles;

    // Acciones
    @FXML
    private HBox hbAcciones;
    @FXML
    private Button btnAgregarCarrito;
    @FXML
    private Button btnComprarAhora;
    @FXML
    private Button btnAnadirFavoritos;
    @FXML
    private VBox vbGarantias;

    // Descripción
    @FXML
    private VBox vbDescripcion;
    @FXML
    private Label lblTituloDescripcion;
    @FXML
    private Label lblSubtituloDescripcion;
    @FXML
    private Label lblDescripcion;

    private Product producto;
    private int cantidad = 1;
    private Runnable alVolver;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Siempre debe quedar una talla elegida (no se puede deseleccionar)
        tgTallas.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null && anterior != null) {
                anterior.setSelected(true);
            }
        });
    }

    public void setAlVolver(Runnable alVolver) {
        this.alVolver = alVolver;
    }

    public Product getProducto() {
        return producto;
    }

    /**
     * Llena el detalle con los datos del producto.
     */
    public void setProducto(Product producto) {
        this.producto = producto;

        String version = tipoCamiseta(producto.getName()) + " 2026/27";
        lblVersion.setText(version);
        lblNombre.setText(producto.getName());
        lblRutaLiga.setText(producto.getLeague());
        lblRutaEquipo.setText(producto.getTeam());

        lblLiga.setText(producto.getLeague());
        cirLiga.getStyleClass().removeIf(clase -> clase.startsWith("league-dot-"));
        cirLiga.getStyleClass().add(claseColorLiga(producto.getLeague()));
        lblMarca.setText("·  " + producto.getBrand());

        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
        lblPrecio.setText("$ " + formato.format(producto.getPrice()));

        configurarTallas();
        cantidad = producto.getStock() > 0 ? 1 : 0;
        actualizarCantidad();

        cargarDescripcion();
        cargarImagenes();
        spDetalleProducto.setVvalue(0);
    }

    // ---------------------------------------------------------------------
    // Tallas y cantidad
    // ---------------------------------------------------------------------

    /**
     * Habilita solo las tallas que tiene el producto y deja elegida la M (o la
     * primera disponible).
     */
    private void configurarTallas() {
        List<String> disponibles = new ArrayList<>();
        if (producto.getSizes() != null) {
            for (String talla : producto.getSizes()) {
                disponibles.add(talla.trim().toUpperCase());
            }
        }

        ToggleButton porDefecto = null;
        for (Toggle toggle : tgTallas.getToggles()) {
            ToggleButton boton = (ToggleButton) toggle;
            boolean disponible = disponibles.contains((String) boton.getUserData());
            boton.setSelected(false);
            boton.setDisable(!disponible || producto.getStock() == 0);

            if (disponible && (porDefecto == null || "M".equals(boton.getUserData()))) {
                porDefecto = boton;
            }
        }

        if (porDefecto != null && producto.getStock() > 0) {
            porDefecto.setSelected(true);
        }
    }

    public String getTallaSeleccionada() {
        Toggle seleccionada = tgTallas.getSelectedToggle();
        return seleccionada == null ? null : (String) seleccionada.getUserData();
    }

    public int getCantidad() {
        return cantidad;
    }

    @FXML
    private void onRestarCantidad(ActionEvent event) {
        if (cantidad > 1) {
            cantidad--;
            actualizarCantidad();
        }
    }

    @FXML
    private void onSumarCantidad(ActionEvent event) {
        if (cantidad < producto.getStock()) {
            cantidad++;
            actualizarCantidad();
        }
    }

    private void actualizarCantidad() {
        int stock = producto.getStock();
        boolean agotado = stock == 0;

        lblCantidad.setText(String.valueOf(cantidad));
        btnRestarCantidad.setDisable(agotado || cantidad <= 1);
        btnSumarCantidad.setDisable(agotado || cantidad >= stock);

        lblDisponibles.getStyleClass().remove("detail-available-none");
        if (agotado) {
            lblDisponibles.setText("Agotado");
            lblDisponibles.getStyleClass().add("detail-available-none");
        } else {
            lblDisponibles.setText(stock + (stock == 1 ? " unidad disponible" : " unidades disponibles"));
        }

        btnAgregarCarrito.setDisable(agotado);
        btnComprarAhora.setDisable(agotado);
    }

    // ---------------------------------------------------------------------
    // Descripción e imágenes
    // ---------------------------------------------------------------------

    private void cargarDescripcion() {
        String parrafo = Data_Manager.getManager().getStacks_products().loadLongDescriptionFromCatalog(producto.getIdProduct());
        String encabezado = producto.getDescription() == null ? "" : producto.getDescription().trim();

        lblSubtituloDescripcion.setText(encabezado);
        lblSubtituloDescripcion.setVisible(!encabezado.isEmpty());
        lblSubtituloDescripcion.setManaged(!encabezado.isEmpty());

        lblDescripcion.setText(parrafo.isEmpty() ? "Este producto aún no tiene una descripción detallada." : parrafo);
    }

    /**
     * Busca todas las imágenes del producto ("0001 - Equipo - 1.png",
     * "- 2.png", ...) en orden. La 1 es la principal; si hay más de una se
     * muestran miniaturas para cambiarla.
     */
    private void cargarImagenes() {
        File carpeta = new File(System.getProperty("user.dir"), "src/Images/ProductImages");
        String prefijo = String.format("%04d", producto.getIdProduct()) + " - ";

        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith(prefijo) && nombre.toLowerCase().matches(".* - \\d+\\.(png|jpg|jpeg)"));
        List<Image> imagenes = new ArrayList<>();

        if (archivos != null) {
            Arrays.sort(archivos, Comparator.comparingInt(this::numeroImagen));
            for (File archivo : archivos) {
                Image imagen = new Image(archivo.toURI().toString(), 700, 700, true, true);
                if (!imagen.isError()) {
                    imagenes.add(imagen);
                }
            }
        }

        hbMiniaturas.getChildren().clear();
        mostrarImagenPrincipal(imagenes.isEmpty() ? null : imagenes.get(0));

        if (imagenes.size() > 1) {
            for (Image imagen : imagenes) {
                ImageView vista = new ImageView(imagen);
                vista.setFitWidth(62);
                vista.setFitHeight(62);
                vista.setPreserveRatio(true);

                StackPane miniatura = new StackPane(vista);
                miniatura.getStyleClass().add("detail-thumb");
                miniatura.setPrefSize(74, 74);
                miniatura.setMaxSize(74, 74);
                miniatura.setOnMouseClicked(e -> {
                    mostrarImagenPrincipal(imagen);
                    marcarMiniatura(miniatura);
                });
                hbMiniaturas.getChildren().add(miniatura);
            }
            marcarMiniatura((StackPane) hbMiniaturas.getChildren().get(0));
        }

        hbMiniaturas.setVisible(imagenes.size() > 1);
        hbMiniaturas.setManaged(imagenes.size() > 1);
    }

    // "0001 - Equipo - 2.png" -> 2
    private int numeroImagen(File archivo) {
        String nombre = archivo.getName();
        String sinExtension = nombre.substring(0, nombre.lastIndexOf('.'));
        try {
            return Integer.parseInt(sinExtension.substring(sinExtension.lastIndexOf('-') + 1).trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private void mostrarImagenPrincipal(Image imagen) {
        imgPrincipal.setImage(imagen);
        svgCamisetaGenerica.setVisible(imagen == null);
    }

    private void marcarMiniatura(StackPane seleccionada) {
        hbMiniaturas.getChildren().forEach(nodo -> nodo.getStyleClass().remove("detail-thumb-selected"));
        seleccionada.getStyleClass().add("detail-thumb-selected");
    }

    // ---------------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------------

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

    // ---------------------------------------------------------------------
    // Acciones
    // ---------------------------------------------------------------------

    @FXML
    private void onVolver(ActionEvent event) {
        if (alVolver != null) {
            alVolver.run();
        }
    }

    @FXML
    private void onAgregarCarrito(ActionEvent event) {
        // TODO: agregar al carrito (getTallaSeleccionada() y getCantidad())
    }

    @FXML
    private void onComprarAhora(ActionEvent event) {
        // TODO: comprar ahora
    }

    @FXML
    private void onAnadirFavoritos(ActionEvent event) {
        // TODO: agregar a favoritos
    }

}
