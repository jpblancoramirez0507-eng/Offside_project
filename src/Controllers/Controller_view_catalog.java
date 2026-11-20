package Controllers;

import Controllers.ComponentsControllers.Product_ComponentController;
import Controllers.ComponentsControllers.Product_Detail_ComponentController;
import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Nodes.Node_User;
import Models.Product;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
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
import javafx.stage.Popup;
import javafx.util.Duration;

public class Controller_view_catalog implements Initializable, Catalog_Navigator {

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
    private MenuItem miMiCuenta;
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

    // Mientras se ve el detalle de un producto se guardan el catálogo y los filtros
    private Node vistaDetalle;
    private Node contenidoCatalogo;
    private Node filtrosCatalogo;

    // Aviso flotante que se está mostrando
    private Popup aviso;

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
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> {
            // Si se busca desde el detalle, se vuelve al catálogo para ver los resultados
            volverAlCatalogo();
            aplicarFiltros();
        });
        sldPrecioMaximo.valueProperty().addListener((obs, anterior, nuevo) -> {
            actualizarTextoPrecio();
            aplicarFiltros();
        });

        aplicarFiltros();

        // Barra superior: carrito, lista de deseos y mis compras
        btnCarrito.setOnAction(e -> abrirCarrito());
        btnFavoritos.setOnAction(e -> abrirFavoritos());
        btnMisCompras.setOnAction(e -> abrirHistorial());
        actualizarContadorCarrito();
        actualizarFavoritosDeCards();
    }

    /**
     * Lee los productos de src/Files/ProductsBase y crea un card
     * (Product_Component) por cada uno.
     */
    private void cargarProductos() {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();

        // Solo los productos a la venta (los descontinuados no se muestran)
        for (Product producto : catalogo.loadActiveCatalog()) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Product_Component.fxml"));
                Parent card = loader.load();
                Product_ComponentController controladorCard = loader.getController();
                controladorCard.setProducto(producto);
                controladorCard.setAlSeleccionar(this::abrirDetalle);
                controladorCard.setAlAgregarCarrito(p -> agregarAlCarrito(p, null, 1));
                controladorCard.setAlAlternarFavorito(this::alternarFavorito);

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

    // ---------------------------------------------------------------------
    // Vistas del cliente: detalle, carrito, lista de deseos y mis compras
    // ---------------------------------------------------------------------

    /**
     * Pone una vista en lugar del catálogo (se ocultan los filtros) y marca en
     * la barra superior la opción correspondiente.
     */
    private void mostrarVista(Node vista, Button opcionActiva) {
        if (vistaDetalle == null) {
            contenidoCatalogo = paneRaiz.getCenter();
            filtrosCatalogo = paneRaiz.getLeft();
        }
        vistaDetalle = vista;
        paneRaiz.setLeft(null);
        paneRaiz.setCenter(vista);
        marcarOpcionBarra(opcionActiva);
    }

    private void marcarOpcionBarra(Button opcionActiva) {
        for (Button boton : new Button[]{btnMisCompras, btnFavoritos, btnCarrito}) {
            boton.getStyleClass().remove("topbar-logout-active");
        }
        mbMiCuenta.getStyleClass().remove("topbar-menu-active");
        if (opcionActiva != null) {
            opcionActiva.getStyleClass().add("topbar-logout-active");
        }
    }

    /**
     * Muestra el detalle del producto (Product_Detail_Component).
     */
    private void abrirDetalle(Product producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Product_Detail_Component.fxml"));
            Parent detalle = loader.load();
            Product_Detail_ComponentController controladorDetalle = loader.getController();
            controladorDetalle.setProducto(producto);
            controladorDetalle.setAlVolver(this::volverAlCatalogo);
            controladorDetalle.setNavegador(this);
            mostrarVista(detalle, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void abrirProducto(Product producto) {
        // Se usa el producto actual del catálogo (stock y precio al día)
        Product actual = Data_Manager.getManager().getStacks_products().loadProductFromCatalog(producto.getIdProduct());
        abrirDetalle(actual != null ? actual : producto);
    }

    @Override
    public void volverAlCatalogo() {
        if (vistaDetalle == null) {
            return;
        }
        paneRaiz.setLeft(filtrosCatalogo);
        paneRaiz.setCenter(contenidoCatalogo);
        vistaDetalle = null;
        marcarOpcionBarra(null);
        // Por si cambió algo (favoritos o stock) mientras se veía otra vista
        actualizarFavoritosDeCards();
    }

    @Override
    public void abrirCarrito() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/View_cart.fxml"));
            Parent vista = loader.load();
            ((Controller_view_cart) loader.getController()).setNavegador(this);
            mostrarVista(vista, btnCarrito);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void abrirFavoritos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/View_favorites.fxml"));
            Parent vista = loader.load();
            ((Controller_view_favorites) loader.getController()).setNavegador(this);
            mostrarVista(vista, btnFavoritos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * "Mi cuenta" del cliente (desde el menú de la barra superior).
     */
    @FXML
    private void onMiCuenta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/View_account.fxml"));
            Parent vista = loader.load();
            ((Controller_view_account) loader.getController()).setNavegador(this);
            mostrarVista(vista, null);
            mbMiCuenta.getStyleClass().add("topbar-menu-active");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void abrirHistorial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/View_purchase_history.fxml"));
            Parent vista = loader.load();
            ((Controller_view_purchase_history) loader.getController()).setNavegador(this);
            mostrarVista(vista, btnMisCompras);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------
    // Carrito y favoritos desde el catálogo
    // ---------------------------------------------------------------------

    private String correoSesion() {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        return sesion == null ? null : sesion.getUser().getEmail();
    }

    @Override
    public int agregarAlCarrito(Product producto, String talla, int cantidad) {
        String correo = correoSesion();
        if (correo == null) {
            mostrarAviso("Inicia sesión para usar el carrito.");
            return 0;
        }

        String tallaFinal = talla != null ? talla : tallaPorDefecto(producto);
        int agregadas = Data_Manager.getManager().getStacks_products().addItemToCart(producto, correo, tallaFinal, cantidad);
        actualizarContadorCarrito();

        if (agregadas == 0) {
            mostrarAviso("No hay más stock de " + producto.getTeam() + " (ya tienes las unidades disponibles en tu carrito).");
        } else if (agregadas < cantidad) {
            mostrarAviso("Solo se agregaron " + agregadas + " unidades de " + producto.getTeam() + " por stock.");
        } else {
            mostrarAviso(producto.getTeam() + (tallaFinal == null ? "" : " (talla " + tallaFinal + ")") + " se agregó al carrito.");
        }
        return agregadas;
    }

    // Talla M si el producto la tiene; si no, la primera disponible
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

    private void alternarFavorito(Product producto) {
        String correo = correoSesion();
        if (correo == null) {
            mostrarAviso("Inicia sesión para guardar favoritos.");
            return;
        }
        boolean favorito = Data_Manager.getManager().getStacks_products().toggleFavorite(producto, correo);
        actualizarFavoritosDeCards();
        mostrarAviso(favorito ? producto.getTeam() + " se agregó a tu lista de deseos." : producto.getTeam() + " se quitó de tu lista de deseos.");
    }

    // Pinta el corazón lleno en los cards que están en favoritos
    private void actualizarFavoritosDeCards() {
        String correo = correoSesion();
        Stack_Of_Product productos = Data_Manager.getManager().getStacks_products();
        for (Product_ComponentController card : cards) {
            card.setFavorito(correo != null && productos.isFavorite(card.getProducto().getIdProduct(), correo));
        }
    }

    @Override
    public void actualizarContadorCarrito() {
        String correo = correoSesion();
        int unidades = correo == null ? 0 : Data_Manager.getManager().getStacks_products().getCartUnits(correo);
        lblContadorCarrito.setText(String.valueOf(unidades));
    }

    /**
     * Muestra un aviso corto en la parte inferior de la ventana.
     */
    @Override
    public void mostrarAviso(String mensaje) {
        if (paneRaiz.getScene() == null || paneRaiz.getScene().getWindow() == null) {
            return;
        }
        if (aviso != null) {
            aviso.hide();
        }

        Label texto = new Label(mensaje);
        texto.getStyleClass().add("catalog-toast");
        texto.getStylesheets().addAll(paneRaiz.getStylesheets());

        aviso = new Popup();
        aviso.getContent().add(texto);
        aviso.setAutoHide(true);

        javafx.stage.Window ventana = paneRaiz.getScene().getWindow();
        aviso.show(ventana);
        aviso.setX(ventana.getX() + (ventana.getWidth() - texto.getWidth()) / 2);
        aviso.setY(ventana.getY() + ventana.getHeight() - 90);

        Popup mostrado = aviso;
        PauseTransition espera = new PauseTransition(Duration.seconds(2.6));
        espera.setOnFinished(e -> mostrado.hide());
        espera.play();
    }

    @Override
    public void recargarCatalogo() {
        cards.clear();
        nodosCards.clear();
        cargarProductos();
        actualizarCantidadesPorLiga();
        aplicarFiltros();
        actualizarFavoritosDeCards();
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        cerrarSesion();
    }

    @Override
    public void cerrarSesion() {
        // Regresa al inicio de sesión reutilizando la misma ventana
        Data_Manager.getManager().setCurrent_user(null);
        try {
            Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Views/View_main.fxml"));
            paneRaiz.getScene().setRoot(vistaLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
