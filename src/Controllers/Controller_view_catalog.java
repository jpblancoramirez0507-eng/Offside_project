package Controllers;

import Controllers.ComponentsControllers.Product_ComponentController;
import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Product;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class Controller_view_catalog implements Initializable {

    // Contenedor raíz
    @FXML
    private BorderPane paneRaiz;

    // Barra superior
    @FXML
    private HBox hbBarraSuperior;
    @FXML
    private HBox hbLogo;
    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnMisCompras;
    @FXML
    private Button btnFavoritos;
    @FXML
    private MenuButton mbMiCuenta;
    @FXML
    private MenuItem miCerrarSesion;
    @FXML
    private HBox hbCarrito;
    @FXML
    private Button btnCarrito;
    @FXML
    private Label lblContadorCarrito;

    // Filtros: liga
    @FXML
    private VBox vbFiltros;
    @FXML
    private VBox vbLigas;
    @FXML
    private ToggleGroup tgLigas;
    @FXML
    private ToggleButton tbTodasLigas;
    @FXML
    private ToggleButton tbPremierLeague;
    @FXML
    private ToggleButton tbLaLiga;
    @FXML
    private ToggleButton tbSerieA;
    @FXML
    private ToggleButton tbBundesliga;
    @FXML
    private ToggleButton tbLigue1;
    @FXML
    private Label lblCantidadTodas;
    @FXML
    private Label lblCantidadPremier;
    @FXML
    private Label lblCantidadLaLiga;
    @FXML
    private Label lblCantidadSerieA;
    @FXML
    private Label lblCantidadBundesliga;
    @FXML
    private Label lblCantidadLigue1;

    // Filtros: precio
    @FXML
    private Label lblRangoPrecio;
    @FXML
    private Slider sldPrecioMaximo;

    // Filtros: talla
    @FXML
    private TilePane tpTallas;
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
    @FXML
    private Button btnLimpiarFiltros;

    // Contenido
    @FXML
    private ScrollPane spContenido;
    @FXML
    private VBox vbContenido;
    @FXML
    private HBox hbBanner;
    @FXML
    private Button btnVerTodasBanner;
    @FXML
    private Label lblTituloCatalogo;
    @FXML
    private Hyperlink lnkVerTodas;
    @FXML
    private FlowPane fpProductos;
    @FXML
    private Label lblSinResultados;

    // Cards cargados (controlador + nodo), en el mismo orden del catálogo
    private final List<Product_ComponentController> cards = new ArrayList<>();
    private final List<Node> nodosCards = new ArrayList<>();

    private final NumberFormat formatoPrecio = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
    private double precioMinimo = 0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarProductos();
        configurarFiltroPrecio();
        actualizarCantidadesPorLiga();

        // La liga siempre tiene una opción elegida (no se puede deseleccionar)
        tgLigas.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                aplicarFiltros();
            }
        });
        tgTallas.selectedToggleProperty().addListener((obs, anterior, nuevo) -> aplicarFiltros());
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> aplicarFiltros());
        sldPrecioMaximo.valueProperty().addListener((obs, anterior, nuevo) -> {
            actualizarTextoPrecio();
            aplicarFiltros();
        });

        aplicarFiltros();
    }

    /**
     * Lee los productos de src/Files/ProductsBase y crea un card
     * (Product_Component) por cada uno.
     */
    private void cargarProductos() {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();
        File carpetaProductos = new File(System.getProperty("user.dir"), "src/Files/ProductsBase");

        int id = 1;
        while (new File(carpetaProductos, String.format("%04d", id) + " - Product Data.txt").exists()) {
            Product producto = catalogo.loadProductFromCatalog(id);
            id++;

            if (producto == null) {
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Product_Component.fxml"));
                Parent card = loader.load();
                Product_ComponentController controladorCard = loader.getController();
                controladorCard.setProducto(producto);

                cards.add(controladorCard);
                nodosCards.add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * El slider va del precio más bajo al más alto del catálogo (redondeados a
     * miles de 10.000) y empieza en el máximo.
     */
    private void configurarFiltroPrecio() {
        double minimo = Double.MAX_VALUE;
        double maximo = 0;
        for (Product_ComponentController card : cards) {
            minimo = Math.min(minimo, card.getProducto().getPrice());
            maximo = Math.max(maximo, card.getProducto().getPrice());
        }
        if (cards.isEmpty()) {
            minimo = 0;
        }

        precioMinimo = Math.floor(minimo / 10000) * 10000;
        double precioMaximo = Math.ceil(maximo / 10000) * 10000;

        sldPrecioMaximo.setMin(precioMinimo);
        sldPrecioMaximo.setMax(Math.max(precioMaximo, precioMinimo));
        sldPrecioMaximo.setValue(sldPrecioMaximo.getMax());
        sldPrecioMaximo.setBlockIncrement(10000);
        actualizarTextoPrecio();
    }

    private void actualizarTextoPrecio() {
        lblRangoPrecio.setText("$ " + formatoPrecio.format(precioMinimo) + "  –  $ " + formatoPrecio.format(sldPrecioMaximo.getValue()));
    }

    private void actualizarCantidadesPorLiga() {
        lblCantidadTodas.setText(String.valueOf(cards.size()));
        lblCantidadPremier.setText(String.valueOf(contarLiga("premierleague")));
        lblCantidadLaLiga.setText(String.valueOf(contarLiga("laliga")));
        lblCantidadSerieA.setText(String.valueOf(contarLiga("seriea")));
        lblCantidadBundesliga.setText(String.valueOf(contarLiga("bundesliga")));
        lblCantidadLigue1.setText(String.valueOf(contarLiga("ligue1")));
    }

    private int contarLiga(String clave) {
        int cantidad = 0;
        for (Product_ComponentController card : cards) {
            if (claveLiga(card.getProducto().getLeague()).equals(clave)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // "La Liga" / "Laliga" -> "laliga"
    private String claveLiga(String liga) {
        return liga == null ? "" : liga.toLowerCase().replace(" ", "");
    }

    /**
     * Muestra solo los cards que cumplen la liga, el texto de búsqueda, el
     * precio máximo y la talla elegidos.
     */
    private void aplicarFiltros() {
        String liga = tgLigas.getSelectedToggle() == null ? "" : (String) tgLigas.getSelectedToggle().getUserData();
        String talla = tgTallas.getSelectedToggle() == null ? null : (String) tgTallas.getSelectedToggle().getUserData();
        String busqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        double precioMaximo = sldPrecioMaximo.getValue();

        List<Node> visibles = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Product producto = cards.get(i).getProducto();

            boolean cumpleLiga = liga.isEmpty() || claveLiga(producto.getLeague()).equals(liga);
            boolean cumplePrecio = producto.getPrice() <= precioMaximo;
            boolean cumpleTalla = talla == null || tieneTalla(producto, talla);
            boolean cumpleBusqueda = busqueda.isEmpty()
                    || producto.getName().toLowerCase().contains(busqueda)
                    || producto.getTeam().toLowerCase().contains(busqueda)
                    || producto.getLeague().toLowerCase().contains(busqueda);

            if (cumpleLiga && cumplePrecio && cumpleTalla && cumpleBusqueda) {
                visibles.add(nodosCards.get(i));
            }
        }

        fpProductos.getChildren().setAll(visibles);

        boolean hayFiltros = !liga.isEmpty() || talla != null || !busqueda.isEmpty() || precioMaximo < sldPrecioMaximo.getMax();
        lblTituloCatalogo.setText(hayFiltros ? visibles.size() + " camisetas encontradas" : "Camisetas destacadas");

        lblSinResultados.setVisible(visibles.isEmpty());
        lblSinResultados.setManaged(visibles.isEmpty());
    }

    private boolean tieneTalla(Product producto, String talla) {
        if (producto.getSizes() == null) {
            return false;
        }
        for (String disponible : producto.getSizes()) {
            if (disponible.equalsIgnoreCase(talla)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Quita todos los filtros y muestra el catálogo completo.
     */
    @FXML
    private void onVerTodas(ActionEvent event) {
        tbTodasLigas.setSelected(true);
        if (tgTallas.getSelectedToggle() != null) {
            tgTallas.getSelectedToggle().setSelected(false);
        }
        txtBuscar.clear();
        sldPrecioMaximo.setValue(sldPrecioMaximo.getMax());
        aplicarFiltros();
        spContenido.setVvalue(0);
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        // Regresa al inicio de sesión reutilizando la misma ventana
        try {
            Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Views/View_main.fxml"));
            paneRaiz.getScene().setRoot(vistaLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
