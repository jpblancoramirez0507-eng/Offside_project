package Controllers;

import Controllers.ComponentsControllers.Admin_Product_ComponentController;
import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Nodes.Node_User;
import Models.Product;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Controller_view_admin implements Initializable {

    // Contenedor raíz
    @FXML
    private BorderPane paneRaiz;

    // Barra superior
    @FXML
    private HBox hbBarraSuperior;
    @FXML
    private HBox hbLogo;
    @FXML
    private TextField txtBuscarGeneral;
    @FXML
    private Label lblAdministrador;
    @FXML
    private Button btnCerrarSesion;

    // Menú lateral
    @FXML
    private VBox vbMenuLateral;
    @FXML
    private Button btnPanelControl;
    @FXML
    private Button btnInventario;
    @FXML
    private Button btnPedidos;
    @FXML
    private Button btnUsuarios;
    @FXML
    private Button btnReportes;
    @FXML
    private Button btnConfiguracion;
    @FXML
    private VBox vbModoAdministrador;

    // Encabezado
    @FXML
    private Label lblTituloInventario;
    @FXML
    private Label lblSubtituloInventario;
    @FXML
    private Button btnRegistrarProductoEncabezado;

    // Filtros y lista de productos
    @FXML
    private TextField txtBuscarProducto;
    @FXML
    private ComboBox<String> cbFiltroLiga;
    @FXML
    private VBox vbTablaProductos;
    @FXML
    private ScrollPane spListaProductos;
    @FXML
    private VBox vbListaProductos;
    @FXML
    private Label lblTotalMostrados;

    // Tarjetas de acción
    @FXML
    private Button btnRegistrarProducto;
    @FXML
    private Button btnEliminarDelCatalogo;

    // Resumen de inventario
    @FXML
    private Label lblTotalProductos;
    @FXML
    private Label lblUnidadesStock;
    @FXML
    private Label lblStockBajo;
    @FXML
    private Label lblValorInventario;

    // Controladores de las filas cargadas en la lista
    private final List<Admin_Product_ComponentController> filasProductos = new ArrayList<>();

    // Contenido central del inventario (se guarda para volver desde las otras secciones)
    private Node contenidoInventario;

    @FXML
    private Label lblTituloStockBajo;
    @FXML
    private Hyperlink lnkVerEliminados;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbFiltroLiga.getItems().addAll("Todas las ligas", "Premier League", "La Liga", "Serie A", "Bundesliga", "Ligue 1");
        cbFiltroLiga.getSelectionModel().selectFirst();

        // Cada opción del menú lateral cambia solo el contenido central
        contenidoInventario = paneRaiz.getCenter();
        btnPanelControl.setOnAction(e -> mostrarSeccion("/Views/View_admin_dashboard.fxml", btnPanelControl));
        btnInventario.setOnAction(e -> mostrarInventario());
        btnPedidos.setOnAction(e -> mostrarSeccion("/Views/View_admin_orders.fxml", btnPedidos));
        btnUsuarios.setOnAction(e -> mostrarSeccion("/Views/View_admin_users.fxml", btnUsuarios));
        btnReportes.setOnAction(e -> mostrarSeccion("/Views/View_admin_reports.fxml", btnReportes));
        btnConfiguracion.setOnAction(e -> mostrarSeccion("/Views/View_admin_settings.fxml", btnConfiguracion));

        actualizarNombreUsuario();
        cargarProductos();

        // Al entrar, el administrador ve primero el panel de control
        mostrarSeccion("/Views/View_admin_dashboard.fxml", btnPanelControl);
    }

    // ---------------------------------------------------------------------
    // Navegación del menú lateral
    // ---------------------------------------------------------------------

    private void mostrarInventario() {
        // Se recarga por si cambió algo en otra sección (por ejemplo, el umbral de stock bajo)
        cargarProductos();
        paneRaiz.setCenter(contenidoInventario);
        marcarOpcionActiva(btnInventario);
    }

    /**
     * Carga la vista de una sección (Pedidos, Usuarios, Reportes o
     * Configuración) en el centro del panel.
     */
    private void mostrarSeccion(String rutaVista, Button opcion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaVista));
            Parent seccion = loader.load();

            // Configuración avisa cuando el administrador cambia su nombre
            Object controlador = loader.getController();
            if (controlador instanceof Controller_view_admin_settings) {
                ((Controller_view_admin_settings) controlador).setAlActualizarCuenta(this::actualizarNombreUsuario);
            }
            // El panel de control abre las demás secciones desde sus enlaces
            if (controlador instanceof Controller_view_admin_dashboard) {
                ((Controller_view_admin_dashboard) controlador).setAlNavegar(this::navegarDesdePanel);
            }

            paneRaiz.setCenter(seccion);
            marcarOpcionActiva(opcion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navegarDesdePanel(String seccion) {
        switch (seccion) {
            case Controller_view_admin_dashboard.INVENTARIO:
                mostrarInventario();
                break;
            case Controller_view_admin_dashboard.REGISTRAR_PRODUCTO:
                onRegistrarProducto(null);
                break;
            case Controller_view_admin_dashboard.PEDIDOS:
                btnPedidos.fire();
                break;
            case Controller_view_admin_dashboard.USUARIOS:
                btnUsuarios.fire();
                break;
            case Controller_view_admin_dashboard.REPORTES:
                btnReportes.fire();
                break;
            case Controller_view_admin_dashboard.CONFIGURACION:
                btnConfiguracion.fire();
                break;
            default:
                break;
        }
    }

    private void marcarOpcionActiva(Button opcion) {
        for (Button boton : new Button[]{btnPanelControl, btnInventario, btnPedidos, btnUsuarios, btnReportes, btnConfiguracion}) {
            boton.getStyleClass().remove("sidebar-item-active");
        }
        opcion.getStyleClass().add("sidebar-item-active");
    }

    // Muestra en la barra superior el nombre del administrador que inició sesión
    private void actualizarNombreUsuario() {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        lblAdministrador.setText(sesion == null ? "Administrador" : sesion.getUser().getName());
    }

    /**
     * Lee los productos de src/Files/ProductsBase y agrega una fila
     * (Admin_Product_Component) por cada uno.
     */
    private void cargarProductos() {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();

        vbListaProductos.getChildren().clear();
        filasProductos.clear();

        // Solo los productos a la venta (los descontinuados no se muestran)
        for (Product producto : catalogo.loadActiveCatalog()) {

            int stock = producto.getStock();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Admin_Product_Component.fxml"));
                Parent fila = loader.load();
                Admin_Product_ComponentController controladorFila = loader.getController();
                controladorFila.setProducto(producto, stock);
                // Cada vez que la fila cambie su stock se recalcula el resumen
                controladorFila.setAlCambiarStock(this::actualizarResumen);
                controladorFila.setAlEliminar(this::eliminarProducto);
                vbListaProductos.getChildren().add(fila);
                filasProductos.add(controladorFila);
            } catch (IOException e) {
                continue;
            }
        }

        actualizarResumen();
    }

    /**
     * Recalcula el resumen de inventario a partir de las filas cargadas.
     */
    private void actualizarResumen() {
        int totalProductos = 0;
        int unidadesStock = 0;
        int stockBajo = 0;
        double valorInventario = 0;

        for (Admin_Product_ComponentController fila : filasProductos) {
            Product producto = fila.getProducto();
            int stock = producto.getStock();

            totalProductos++;
            unidadesStock += stock;
            if (stock < Admin_Product_ComponentController.stockBajo()) {
                stockBajo++;
            }
            valorInventario += (double) producto.getPrice() * stock;
        }

        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

        lblTotalMostrados.setText("Mostrando " + totalProductos + " de " + totalProductos + " productos");
        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblUnidadesStock.setText(formato.format(unidadesStock));
        lblStockBajo.setText(String.valueOf(stockBajo));
        lblTituloStockBajo.setText("Stock bajo (< " + Admin_Product_ComponentController.stockBajo() + ")");
        lblValorInventario.setText("$ " + formato.format(valorInventario));

        int eliminados = Data_Manager.getManager().getStacks_products().loadDiscontinuedProducts().size();
        lnkVerEliminados.setText("Ver productos eliminados (" + eliminados + ")");
    }

    /**
     * Muestra los productos que se quitaron del catálogo para poder
     * restaurarlos.
     */
    @FXML
    private void onVerEliminados(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/View_admin_discontinued.fxml"));
            Parent vista = loader.load();
            Controller_view_admin_discontinued controlador = loader.getController();

            // Al volver se recarga el inventario si se restauró algún producto
            controlador.setAlVolver(huboCambios -> {
                if (huboCambios) {
                    cargarProductos();
                }
                paneRaiz.setCenter(contenidoInventario);
                marcarOpcionActiva(btnInventario);
            });

            paneRaiz.setCenter(vista);
            marcarOpcionActiva(btnInventario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegistrarProducto(ActionEvent event) {
        // Se cambia solo el contenido central: la barra superior y el menú lateral se mantienen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/View_admin_register_product.fxml"));
            Parent formulario = loader.load();
            Controller_view_admin_register_product controladorFormulario = loader.getController();

            // Al cancelar o guardar se vuelve al inventario (y se recarga si hay un producto nuevo)
            controladorFormulario.setAlVolver(productoGuardado -> {
                paneRaiz.setCenter(contenidoInventario);
                marcarOpcionActiva(btnInventario);
                if (productoGuardado) {
                    cargarProductos();
                }
            });

            paneRaiz.setCenter(formulario);
            marcarOpcionActiva(btnInventario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEliminarDelCatalogo(ActionEvent event) {
        if (filasProductos.isEmpty()) {
            mostrarAviso(Alert.AlertType.INFORMATION, "Eliminar producto", "No hay productos en el catálogo.");
            return;
        }

        // Se elige el producto de una lista con su referencia y nombre
        Map<String, Product> opciones = new LinkedHashMap<>();
        for (Admin_Product_ComponentController fila : filasProductos) {
            Product producto = fila.getProducto();
            opciones.put("Ref. " + String.format("%04d", producto.getIdProduct()) + "  ·  " + producto.getName(), producto);
        }

        ChoiceDialog<String> dialogo = new ChoiceDialog<>(null, new ArrayList<>(opciones.keySet()));
        dialogo.setTitle("Eliminar producto");
        dialogo.setHeaderText("¿Qué camiseta quieres quitar del catálogo?");
        dialogo.setContentText("Producto:");

        Optional<String> elegido = dialogo.showAndWait();
        if (elegido.isPresent() && opciones.containsKey(elegido.get())) {
            eliminarProducto(opciones.get(elegido.get()));
        }
    }

    /**
     * Pide confirmación y quita el producto del catálogo (queda descontinuado:
     * sus ventas anteriores se conservan en pedidos y reportes).
     */
    private void eliminarProducto(Product producto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Quitar \"" + producto.getName() + "\" del catálogo?\n\n"
                + "Dejará de verse en el catálogo y se quitará de los carritos y favoritos de los clientes. "
                + "Sus ventas anteriores se conservan en Pedidos y Reportes.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(null);

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (!respuesta.isPresent() || respuesta.get() != ButtonType.OK) {
            return;
        }

        if (Data_Manager.getManager().getStacks_products().discontinueProduct(producto.getIdProduct())) {
            cargarProductos();
            mostrarAviso(Alert.AlertType.INFORMATION, "Producto eliminado", "Se quitó \"" + producto.getName() + "\" del catálogo.");
        } else {
            mostrarAviso(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el archivo del producto.");
        }
    }

    private void mostrarAviso(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        // Regresa al inicio de sesión reutilizando la misma ventana
        Data_Manager.getManager().setCurrent_user(null);
        try {
            Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Views/View_main.fxml"));
            btnCerrarSesion.getScene().setRoot(vistaLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
