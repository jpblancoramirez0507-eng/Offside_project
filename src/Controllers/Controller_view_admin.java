package Controllers;

import Controllers.ComponentsControllers.Admin_Product_ComponentController;
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
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbFiltroLiga.getItems().addAll("Todas las ligas", "Premier League", "La Liga", "Serie A", "Bundesliga", "Ligue 1");
        cbFiltroLiga.getSelectionModel().selectFirst();

        cargarProductos();
    }

    /**
     * Lee los productos de src/Files/ProductsBase y agrega una fila
     * (Admin_Product_Component) por cada uno.
     */
    private void cargarProductos() {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();
        File carpetaProductos = new File(System.getProperty("user.dir"), "src/Files/ProductsBase");

        vbListaProductos.getChildren().clear();
        filasProductos.clear();

        int id = 1;
        while (new File(carpetaProductos, String.format("%04d", id) + " - Product Data.txt").exists()) {
            Product producto = catalogo.loadProductFromCatalog(id);
            id++;

            if (producto == null) {
                continue;
            }

            int stock = producto.getStock();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Admin_Product_Component.fxml"));
                Parent fila = loader.load();
                Admin_Product_ComponentController controladorFila = loader.getController();
                controladorFila.setProducto(producto, stock);
                // Cada vez que la fila cambie su stock se recalcula el resumen
                controladorFila.setAlCambiarStock(this::actualizarResumen);
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
            if (stock < Admin_Product_ComponentController.STOCK_BAJO) {
                stockBajo++;
            }
            valorInventario += (double) producto.getPrice() * stock;
        }

        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

        lblTotalMostrados.setText("Mostrando " + totalProductos + " de " + totalProductos + " productos");
        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblUnidadesStock.setText(formato.format(unidadesStock));
        lblStockBajo.setText(String.valueOf(stockBajo));
        lblValorInventario.setText("$ " + formato.format(valorInventario));
    }

    @FXML
    private void onRegistrarProducto(ActionEvent event) {
        // TODO
    }

    @FXML
    private void onEliminarDelCatalogo(ActionEvent event) {
        // TODO
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        // Regresa al inicio de sesión reutilizando la misma ventana
        try {
            Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Views/View_main.fxml"));
            btnCerrarSesion.getScene().setRoot(vistaLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
