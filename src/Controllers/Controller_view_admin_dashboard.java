package Controllers;

import DataStructures.Data_Manager;
import DataStructures.List_Double_User;
import Models.Admin;
import Models.Nodes.Node_User;
import Models.Order;
import Models.Product;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class Controller_view_admin_dashboard implements Initializable {

    // Secciones a las que puede llevar el panel (las abre Controller_view_admin)
    public static final String INVENTARIO = "inventario";
    public static final String REGISTRAR_PRODUCTO = "registrar";
    public static final String PEDIDOS = "pedidos";
    public static final String USUARIOS = "usuarios";
    public static final String REPORTES = "reportes";
    public static final String CONFIGURACION = "configuracion";

    private static final int ELEMENTOS_POR_LISTA = 5;
    private static final DateTimeFormatter FORMATO_FECHA_PEDIDO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_HOY = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));

    @FXML
    private ScrollPane spPanelControl;
    @FXML
    private VBox vbPanelControl;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSaludo;
    @FXML
    private Label lblFecha;

    // Indicadores
    @FXML
    private HBox hbIndicadores;
    @FXML
    private Label lblIngresos;
    @FXML
    private Label lblDetalleIngresos;
    @FXML
    private Label lblPedidos;
    @FXML
    private Label lblDetallePedidos;
    @FXML
    private Label lblProductos;
    @FXML
    private Label lblDetalleProductos;
    @FXML
    private Label lblTituloStockBajo;
    @FXML
    private Label lblStockBajo;
    @FXML
    private Label lblDetalleStockBajo;

    // Tarjetas
    @FXML
    private HBox hbFilaSuperior;
    @FXML
    private Hyperlink lnkVerPedidos;
    @FXML
    private VBox vbPedidosRecientes;
    @FXML
    private Button btnAccionRegistrar;
    @FXML
    private Button btnAccionInventario;
    @FXML
    private Button btnAccionReportes;
    @FXML
    private Button btnAccionConfiguracion;
    @FXML
    private HBox hbFilaInferior;
    @FXML
    private Label lblTituloListaStock;
    @FXML
    private Hyperlink lnkVerInventario;
    @FXML
    private VBox vbStockBajo;
    @FXML
    private Hyperlink lnkVerUsuarios;
    @FXML
    private VBox vbUsuariosRecientes;

    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
    private Consumer<String> alNavegar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        lblSaludo.setText((sesion == null ? "Hola" : "Hola, " + sesion.getUser().getName()) + ". Este es el resumen de la tienda.");
        String hoy = FORMATO_FECHA_HOY.format(LocalDate.now());
        lblFecha.setText(hoy.substring(0, 1).toUpperCase() + hoy.substring(1));

        List<Product> catalogo = Data_Manager.getManager().getStacks_products().loadActiveCatalog();
        List<Order> pedidos = Data_Manager.getManager().getStacks_products().groupHistoryIntoOrders();

        mostrarIndicadores(catalogo, pedidos);
        mostrarPedidosRecientes(pedidos);
        mostrarStockBajo(catalogo);
        mostrarUsuariosRecientes();
    }

    /**
     * Controller_view_admin indica cómo abrir cada sección (Pedidos,
     * Inventario, etc.) desde los enlaces y botones del panel.
     */
    public void setAlNavegar(Consumer<String> alNavegar) {
        this.alNavegar = alNavegar;
    }

    private void mostrarIndicadores(List<Product> catalogo, List<Order> pedidos) {
        double ingresos = 0;
        int vendidas = 0;
        int ultimaSemana = 0;
        LocalDateTime haceSieteDias = LocalDateTime.now().minusDays(7);
        for (Order pedido : pedidos) {
            ingresos += pedido.getTotal();
            vendidas += pedido.getProducts().size();
            if (pedido.getDate_purchase() != null && pedido.getDate_purchase().isAfter(haceSieteDias)) {
                ultimaSemana++;
            }
        }

        int umbral = Data_Manager.getManager().getSettings().getStockBajo();
        int unidades = 0;
        int stockBajo = 0;
        int agotados = 0;
        for (Product producto : catalogo) {
            unidades += producto.getStock();
            if (producto.getStock() < umbral) {
                stockBajo++;
            }
            if (producto.getStock() == 0) {
                agotados++;
            }
        }

        lblIngresos.setText("$ " + formato.format(ingresos));
        lblDetalleIngresos.setText(vendidas + (vendidas == 1 ? " camiseta vendida" : " camisetas vendidas"));
        lblPedidos.setText(String.valueOf(pedidos.size()));
        lblDetallePedidos.setText(ultimaSemana + " en los últimos 7 días");
        lblProductos.setText(String.valueOf(catalogo.size()));
        lblDetalleProductos.setText(formato.format(unidades) + " unidades en stock");
        lblTituloStockBajo.setText("Stock bajo (< " + umbral + ")");
        lblStockBajo.setText(String.valueOf(stockBajo));
        lblDetalleStockBajo.setText(agotados + (agotados == 1 ? " agotado" : " agotados"));
        if (stockBajo > 0) {
            lblStockBajo.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private void mostrarPedidosRecientes(List<Order> pedidos) {
        vbPedidosRecientes.getChildren().clear();
        List_Double_User usuarios = Data_Manager.getManager().getList_user();

        // Los pedidos vienen del más antiguo al más reciente
        for (int i = pedidos.size() - 1; i >= Math.max(0, pedidos.size() - ELEMENTOS_POR_LISTA); i--) {
            Order pedido = pedidos.get(i);
            Node_User usuario = pedido.getEmail_customer() == null ? null : usuarios.search_email(pedido.getEmail_customer());

            Label lblNumero = etiqueta("#" + String.format("%04d", pedido.getNumber()), "cell-title");
            lblNumero.setMinWidth(64);

            int cantidad = pedido.getProducts().size();
            String fecha = pedido.getDate_purchase() == null ? "Sin fecha" : FORMATO_FECHA_PEDIDO.format(pedido.getDate_purchase());
            VBox vbCliente = new VBox(1,
                    etiqueta(usuario == null ? "Usuario eliminado" : usuario.getUser().getName(), "cell-text"),
                    etiqueta(fecha + "  ·  " + cantidad + (cantidad == 1 ? " camiseta" : " camisetas"), "cell-muted"));

            vbPedidosRecientes.getChildren().add(fila(lblNumero, vbCliente, etiqueta("$ " + formato.format(pedido.getTotal()), "cell-amount")));
        }

        if (pedidos.isEmpty()) {
            vbPedidosRecientes.getChildren().add(vacio("Aún no hay pedidos. Aparecerán aquí cuando los clientes compren."));
        }
    }

    private void mostrarStockBajo(List<Product> catalogo) {
        int umbral = Data_Manager.getManager().getSettings().getStockBajo();
        List<Product> bajos = new ArrayList<>();
        for (Product producto : catalogo) {
            if (producto.getStock() < umbral) {
                bajos.add(producto);
            }
        }
        bajos.sort((a, b) -> Integer.compare(a.getStock(), b.getStock()));

        vbStockBajo.getChildren().clear();
        for (int i = 0; i < Math.min(ELEMENTOS_POR_LISTA, bajos.size()); i++) {
            Product producto = bajos.get(i);
            VBox vbProducto = new VBox(1, etiqueta(producto.getName(), "cell-text"), etiqueta(producto.getLeague(), "cell-muted"));
            Label lblStock = etiqueta(producto.getStock() == 0 ? "Agotado" : producto.getStock() + " und.", "cell-amount");
            lblStock.setStyle("-fx-text-fill: #d32f2f;");
            vbStockBajo.getChildren().add(fila(null, vbProducto, lblStock));
        }

        if (bajos.size() > ELEMENTOS_POR_LISTA) {
            vbStockBajo.getChildren().add(etiqueta("y " + (bajos.size() - ELEMENTOS_POR_LISTA) + " más en el inventario", "cell-muted"));
        }
        if (bajos.isEmpty()) {
            vbStockBajo.getChildren().add(vacio("Todo el inventario tiene stock suficiente."));
        }
    }

    // Los últimos usuarios de la lista doble son los que se registraron más recientemente
    private void mostrarUsuariosRecientes() {
        vbUsuariosRecientes.getChildren().clear();

        Node_User nodo = Data_Manager.getManager().getList_user().getLast();
        int mostrados = 0;
        while (nodo != null && mostrados < ELEMENTOS_POR_LISTA) {
            boolean esAdmin = nodo.getUser() instanceof Admin;
            String nombre = nodo.getUser().getName();

            Circle circulo = new Circle(14);
            circulo.getStyleClass().add(esAdmin ? "user-avatar-admin" : "user-avatar");
            Label inicial = etiqueta(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase(), "user-avatar-letter");
            if (esAdmin) {
                inicial.getStyleClass().add("user-avatar-letter-admin");
            }
            StackPane avatar = new StackPane(circulo, inicial);

            VBox vbDatos = new VBox(1, etiqueta(nombre, "cell-text"), etiqueta(nodo.getUser().getEmail(), "cell-muted"));
            HBox hbUsuario = new HBox(10, avatar, vbDatos);
            hbUsuario.setAlignment(Pos.CENTER_LEFT);

            Label chip = etiqueta(esAdmin ? "Administrador" : "Cliente", esAdmin ? "role-chip-admin" : "role-chip-client");
            vbUsuariosRecientes.getChildren().add(fila(null, hbUsuario, chip));

            nodo = nodo.getFormer();
            mostrados++;
        }

        if (mostrados == 0) {
            vbUsuariosRecientes.getChildren().add(vacio("No hay usuarios registrados."));
        }
    }

    private HBox fila(Label inicio, javafx.scene.Node centro, Label fin) {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox fila = new HBox(10);
        if (inicio != null) {
            fila.getChildren().add(inicio);
        }
        fila.getChildren().addAll(centro, espacio, fin);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("report-list-row");
        return fila;
    }

    private Label etiqueta(String texto, String clase) {
        Label label = new Label(texto);
        label.getStyleClass().add(clase);
        return label;
    }

    private Label vacio(String texto) {
        Label label = etiqueta(texto, "empty-state");
        label.setStyle("-fx-padding: 20 0 20 0;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        return label;
    }

    private void navegar(String seccion) {
        if (alNavegar != null) {
            alNavegar.accept(seccion);
        }
    }

    @FXML
    private void onVerPedidos(ActionEvent event) {
        navegar(PEDIDOS);
    }

    @FXML
    private void onVerInventario(ActionEvent event) {
        navegar(INVENTARIO);
    }

    @FXML
    private void onVerUsuarios(ActionEvent event) {
        navegar(USUARIOS);
    }

    @FXML
    private void onVerReportes(ActionEvent event) {
        navegar(REPORTES);
    }

    @FXML
    private void onVerConfiguracion(ActionEvent event) {
        navegar(CONFIGURACION);
    }

    @FXML
    private void onRegistrarProducto(ActionEvent event) {
        navegar(REGISTRAR_PRODUCTO);
    }
}
