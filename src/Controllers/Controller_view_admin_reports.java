package Controllers;

import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Admin;
import Models.Nodes.Node_User;
import Models.Product;
import java.net.URL;
import java.text.NumberFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class Controller_view_admin_reports implements Initializable {

    private static final Map<String, String> LIGAS = new LinkedHashMap<>();

    static {
        LIGAS.put("premierleague", "Premier League");
        LIGAS.put("laliga", "La Liga");
        LIGAS.put("seriea", "Serie A");
        LIGAS.put("bundesliga", "Bundesliga");
        LIGAS.put("ligue1", "Ligue 1");
    }

    private static final int MAXIMO_EN_LISTAS = 6;

    @FXML
    private ScrollPane spReportes;
    @FXML
    private VBox vbReportes;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;

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
    private Label lblClientes;
    @FXML
    private Label lblDetalleClientes;
    @FXML
    private Label lblValorInventario;
    @FXML
    private Label lblDetalleInventario;

    // Gráficas
    @FXML
    private HBox hbGraficas;
    @FXML
    private BarChart<String, Number> chStockPorLiga;
    @FXML
    private BarChart<String, Number> chVentasPorLiga;
    @FXML
    private Label lblSinVentas;

    // Listas
    @FXML
    private HBox hbListas;
    @FXML
    private Label lblTituloStockBajo;
    @FXML
    private VBox vbStockBajo;
    @FXML
    private VBox vbMasVendidos;

    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<Product> catalogo = cargarCatalogo();

        Stack_Of_Product productos = Data_Manager.getManager().getStacks_products();
        productos.loadHistoryFromFile();
        List<Product> ventas = new ArrayList<>(productos.getHistoryProducts());

        mostrarIndicadores(catalogo, ventas);
        mostrarGraficas(catalogo, ventas);
        mostrarStockBajo(catalogo);
        mostrarMasVendidos(ventas);
    }

    private List<Product> cargarCatalogo() {
        return Data_Manager.getManager().getStacks_products().loadActiveCatalog();
    }

    private void mostrarIndicadores(List<Product> catalogo, List<Product> ventas) {
        double ingresos = 0;
        Set<String> pedidos = new HashSet<>();
        Set<String> compradores = new HashSet<>();
        for (Product venta : ventas) {
            ingresos += venta.getPrice();
            String fecha = venta.getDate_purchase() == null ? "sin-fecha" : venta.getDate_purchase().truncatedTo(ChronoUnit.MINUTES).toString();
            pedidos.add(venta.getEmail_customer() + "|" + fecha);
            compradores.add(venta.getEmail_customer());
        }

        int clientes = 0;
        Set<String> correosClientes = new HashSet<>();
        for (Node_User nodo = Data_Manager.getManager().getList_user().getHead(); nodo != null; nodo = nodo.getNext()) {
            if (!(nodo.getUser() instanceof Admin)) {
                clientes++;
                correosClientes.add(nodo.getUser().getEmail().toLowerCase());
            }
        }

        int clientesQueCompraron = 0;
        for (String correo : compradores) {
            if (correo != null && correosClientes.contains(correo.toLowerCase())) {
                clientesQueCompraron++;
            }
        }

        int unidadesStock = 0;
        double valorInventario = 0;
        for (Product producto : catalogo) {
            unidadesStock += producto.getStock();
            valorInventario += (double) producto.getPrice() * producto.getStock();
        }

        lblIngresos.setText("$ " + formato.format(ingresos));
        lblDetalleIngresos.setText(ventas.size() + (ventas.size() == 1 ? " camiseta vendida" : " camisetas vendidas"));
        lblPedidos.setText(String.valueOf(pedidos.size()));
        lblDetallePedidos.setText("Ticket promedio: $ " + formato.format(pedidos.isEmpty() ? 0 : ingresos / pedidos.size()));
        lblClientes.setText(String.valueOf(clientes));
        lblDetalleClientes.setText(clientesQueCompraron + (clientesQueCompraron == 1 ? " ha comprado" : " han comprado"));
        lblValorInventario.setText("$ " + formato.format(valorInventario));
        lblDetalleInventario.setText(formato.format(unidadesStock) + " unidades en stock");
    }

    private void mostrarGraficas(List<Product> catalogo, List<Product> ventas) {
        Map<String, Integer> stockPorLiga = contarPorLiga();
        for (Product producto : catalogo) {
            stockPorLiga.merge(claveLiga(producto.getLeague()), producto.getStock(), Integer::sum);
        }

        Map<String, Integer> ventasPorLiga = contarPorLiga();
        for (Product venta : ventas) {
            ventasPorLiga.merge(claveLiga(venta.getLeague()), 1, Integer::sum);
        }

        chStockPorLiga.getData().setAll(serie(stockPorLiga, "#148a4f"));
        chVentasPorLiga.getData().setAll(serie(ventasPorLiga, "#0e4d3a"));

        for (BarChart<String, Number> grafica : Arrays.asList(chStockPorLiga, chVentasPorLiga)) {
            ((NumberAxis) grafica.getYAxis()).setTickLabelFormatter(new StringConverter<Number>() {
                @Override
                public String toString(Number valor) {
                    return valor.doubleValue() % 1 == 0 ? formato.format(valor) : "";
                }

                @Override
                public Number fromString(String texto) {
                    return Integer.parseInt(texto.replace(".", ""));
                }
            });
        }

        boolean hayVentas = !ventas.isEmpty();
        chVentasPorLiga.setOpacity(hayVentas ? 1 : 0.25);
        lblSinVentas.setVisible(!hayVentas);
    }

    private Map<String, Integer> contarPorLiga() {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (String clave : LIGAS.keySet()) {
            conteo.put(clave, 0);
        }
        return conteo;
    }

    private XYChart.Series<String, Number> serie(Map<String, Integer> valores, String color) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, String> liga : LIGAS.entrySet()) {
            XYChart.Data<String, Number> dato = new XYChart.Data<>(liga.getValue(), valores.getOrDefault(liga.getKey(), 0));

            dato.nodeProperty().addListener((obs, anterior, barra) -> {
                if (barra != null) {
                    barra.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4 4 0 0;");
                }
            });
            serie.getData().add(dato);
        }
        return serie;
    }

    private String claveLiga(String liga) {
        return liga == null ? "" : liga.toLowerCase().replace(" ", "");
    }

    private void mostrarStockBajo(List<Product> catalogo) {
        int umbral = Data_Manager.getManager().getSettings().getStockBajo();
        lblTituloStockBajo.setText("Productos con stock bajo (< " + umbral + ")");

        List<Product> bajos = new ArrayList<>();
        for (Product producto : catalogo) {
            if (producto.getStock() < umbral) {
                bajos.add(producto);
            }
        }
        bajos.sort((a, b) -> Integer.compare(a.getStock(), b.getStock()));

        vbStockBajo.getChildren().clear();
        for (int i = 0; i < Math.min(MAXIMO_EN_LISTAS, bajos.size()); i++) {
            Product producto = bajos.get(i);
            vbStockBajo.getChildren().add(filaLista(producto.getName(), producto.getLeague(), producto.getStock() + " und.", true));
        }
        if (bajos.size() > MAXIMO_EN_LISTAS) {
            Label mas = new Label("y " + (bajos.size() - MAXIMO_EN_LISTAS) + " más en el inventario");
            mas.getStyleClass().add("cell-muted");
            vbStockBajo.getChildren().add(mas);
        }
        if (bajos.isEmpty()) {
            vbStockBajo.getChildren().add(vacio("Ningún producto tiene stock bajo."));
        }
    }

    private void mostrarMasVendidos(List<Product> ventas) {
        Map<Integer, Integer> unidades = new HashMap<>();
        Map<Integer, Product> productos = new HashMap<>();
        for (Product venta : ventas) {
            unidades.merge(venta.getIdProduct(), 1, Integer::sum);
            productos.put(venta.getIdProduct(), venta);
        }

        List<Integer> ids = new ArrayList<>(unidades.keySet());
        ids.sort((a, b) -> Integer.compare(unidades.get(b), unidades.get(a)));

        vbMasVendidos.getChildren().clear();
        for (int i = 0; i < Math.min(MAXIMO_EN_LISTAS, ids.size()); i++) {
            Product producto = productos.get(ids.get(i));
            int vendidas = unidades.get(ids.get(i));
            vbMasVendidos.getChildren().add(filaLista((i + 1) + ".  " + producto.getName(), producto.getLeague(), vendidas + (vendidas == 1 ? " vendida" : " vendidas"), false));
        }
        if (ids.isEmpty()) {
            vbMasVendidos.getChildren().add(vacio("Aún no hay ventas para mostrar."));
        }
    }

    private HBox filaLista(String titulo, String detalle, String valor, boolean resaltarValor) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("cell-text");
        Label lblDetalle = new Label(detalle);
        lblDetalle.getStyleClass().add("cell-muted");
        VBox vbTexto = new VBox(1, lblTitulo, lblDetalle);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("cell-amount");
        if (resaltarValor) {
            lblValor.getStyleClass().add("product-stock-low");
            lblValor.setStyle("-fx-text-fill: #d32f2f;");
        }

        HBox fila = new HBox(10, vbTexto, espacio, lblValor);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("report-list-row");
        return fila;
    }

    private Label vacio(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("empty-state");
        label.setStyle("-fx-padding: 20 0 20 0;");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
