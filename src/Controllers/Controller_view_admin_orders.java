package Controllers;

import DataStructures.Data_Manager;
import DataStructures.List_Double_User;
import Models.Nodes.Node_User;
import Models.Order;
import Models.Product;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Controller_view_admin_orders implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private VBox vbPedidos;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;

    // Indicadores
    @FXML
    private HBox hbIndicadores;
    @FXML
    private Label lblTotalPedidos;
    @FXML
    private Label lblUnidadesVendidas;
    @FXML
    private Label lblIngresos;

    // Filtros
    @FXML
    private HBox hbFiltros;
    @FXML
    private TextField txtBuscarPedido;
    @FXML
    private ComboBox<String> cbOrdenPedidos;

    // Tabla
    @FXML
    private VBox vbTablaPedidos;
    @FXML
    private ScrollPane spListaPedidos;
    @FXML
    private VBox vbListaPedidos;
    @FXML
    private Label lblTotalMostrados;

    private final List<Pedido> pedidos = new ArrayList<>();
    private final NumberFormat formatoPrecio = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbOrdenPedidos.getItems().addAll("Más recientes", "Más antiguos", "Mayor total");
        cbOrdenPedidos.getSelectionModel().selectFirst();

        cargarPedidos();
        actualizarIndicadores();

        txtBuscarPedido.textProperty().addListener((obs, anterior, nuevo) -> mostrarPedidos());
        cbOrdenPedidos.valueProperty().addListener((obs, anterior, nuevo) -> mostrarPedidos());
        mostrarPedidos();
    }

    private void cargarPedidos() {
        pedidos.clear();
        List_Double_User usuarios = Data_Manager.getManager().getList_user();

        for (Order orden : Data_Manager.getManager().getStacks_products().groupHistoryIntoOrders()) {
            Node_User usuario = orden.getEmail_customer() == null ? null : usuarios.search_email(orden.getEmail_customer());
            pedidos.add(new Pedido(orden, usuario == null ? "Usuario eliminado" : usuario.getUser().getName()));
        }
    }

    private void actualizarIndicadores() {
        int unidades = 0;
        double ingresos = 0;
        for (Pedido pedido : pedidos) {
            unidades += pedido.unidades;
            ingresos += pedido.total();
        }

        lblTotalPedidos.setText(formatoPrecio.format(pedidos.size()));
        lblUnidadesVendidas.setText(formatoPrecio.format(unidades));
        lblIngresos.setText("$ " + formatoPrecio.format(ingresos));
    }

    private void mostrarPedidos() {
        String busqueda = txtBuscarPedido.getText() == null ? "" : txtBuscarPedido.getText().trim().toLowerCase();

        List<Pedido> visibles = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            if (busqueda.isEmpty() || pedido.coincide(busqueda)) {
                visibles.add(pedido);
            }
        }

        String orden = cbOrdenPedidos.getValue();
        if ("Más antiguos".equals(orden)) {
            visibles.sort(Comparator.comparingInt(p -> p.numero));
        } else if ("Mayor total".equals(orden)) {
            visibles.sort(Comparator.comparingDouble(Pedido::total).reversed());
        } else {
            visibles.sort(Comparator.comparingInt((Pedido p) -> p.numero).reversed());
        }

        vbListaPedidos.getChildren().clear();
        for (Pedido pedido : visibles) {
            vbListaPedidos.getChildren().add(crearFila(pedido));
        }

        if (visibles.isEmpty()) {
            Label vacio = new Label(pedidos.isEmpty()
                    ? "Aún no hay pedidos.\nAparecerán aquí cuando los clientes compren desde el catálogo."
                    : "Ningún pedido coincide con la búsqueda.");
            vacio.getStyleClass().add("empty-state");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vbListaPedidos.getChildren().add(vacio);
        }

        lblTotalMostrados.setText("Mostrando " + visibles.size() + " de " + pedidos.size() + " pedidos");
    }

    private HBox crearFila(Pedido pedido) {
        Label lblNumero = new Label("#" + String.format("%04d", pedido.numero));
        lblNumero.getStyleClass().add("cell-title");
        lblNumero.setMinWidth(90);
        lblNumero.setPrefWidth(90);

        Label lblCliente = new Label(pedido.cliente);
        lblCliente.getStyleClass().add("cell-title");
        Label lblCorreo = new Label(pedido.correo == null ? "" : pedido.correo);
        lblCorreo.getStyleClass().add("cell-muted");
        VBox vbCliente = new VBox(1, lblCliente, lblCorreo);
        vbCliente.setMinWidth(0);
        vbCliente.setPrefWidth(200);
        HBox.setHgrow(vbCliente, Priority.ALWAYS);

        Label lblFecha = new Label(pedido.fecha == null ? "Sin fecha" : FORMATO_FECHA.format(pedido.fecha));
        lblFecha.getStyleClass().add("cell-text");
        lblFecha.setMinWidth(140);
        lblFecha.setPrefWidth(140);

        VBox vbProductos = new VBox(1);
        vbProductos.setMinWidth(260);
        vbProductos.setPrefWidth(260);
        for (Product producto : pedido.productos) {
            String talla = producto.getSelectedSize() == null ? "" : "  ·  Talla " + producto.getSelectedSize();
            String cantidad = producto.getQuantity() > 1 ? producto.getQuantity() + " × " : "";
            Label lblProducto = new Label(cantidad + producto.getTeam() + talla);
            lblProducto.getStyleClass().add("cell-text");
            vbProductos.getChildren().add(lblProducto);
        }
        Label lblUnidades = new Label(pedido.unidades + (pedido.unidades == 1 ? " camiseta" : " camisetas"));
        lblUnidades.getStyleClass().add("cell-muted");
        vbProductos.getChildren().add(lblUnidades);

        Label lblTotal = new Label("$ " + formatoPrecio.format(pedido.total()));
        lblTotal.getStyleClass().add("cell-amount");
        lblTotal.setAlignment(Pos.CENTER_RIGHT);
        lblTotal.setMinWidth(110);
        lblTotal.setPrefWidth(110);

        HBox fila = new HBox(lblNumero, vbCliente, lblFecha, vbProductos, lblTotal);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("section-row");
        return fila;
    }

    private static class Pedido {

        private final String correo;
        private final LocalDateTime fecha;
        private final List<Product> productos;
        private final String cliente;
        private final int numero;
        private final double total;
        private final int unidades;

        private Pedido(Order orden, String cliente) {
            this.correo = orden.getEmail_customer();
            this.fecha = orden.getDate_purchase();
            this.productos = orden.getProducts();
            this.numero = orden.getNumber();
            this.total = orden.getTotal();
            this.unidades = orden.getUnits();
            this.cliente = cliente;
        }

        private double total() {
            return total;
        }

        private boolean coincide(String busqueda) {
            if ((cliente != null && cliente.toLowerCase().contains(busqueda)) || (correo != null && correo.toLowerCase().contains(busqueda))) {
                return true;
            }
            for (Product producto : productos) {
                if (producto.getName().toLowerCase().contains(busqueda) || producto.getTeam().toLowerCase().contains(busqueda)) {
                    return true;
                }
            }
            return String.format("%04d", numero).contains(busqueda.replace("#", ""));
        }
    }
}
