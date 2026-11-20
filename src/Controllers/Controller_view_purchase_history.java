package Controllers;

import DataStructures.Data_Manager;
import Models.Nodes.Node_User;
import Models.Order;
import Models.Product;
import Utilities.Product_Images;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class Controller_view_purchase_history implements Initializable {

    // Un pedido se considera "En curso" durante estos días; después, "Entregado"
    private static final int DIAS_DE_ENTREGA = 3;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "CO"));

    @FXML
    private ScrollPane spMisCompras;
    @FXML
    private VBox vbMisCompras;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblCantidadPedidos;
    @FXML
    private HBox hbCuerpo;
    @FXML
    private HBox hbFiltros;
    @FXML
    private ToggleGroup tgEstado;
    @FXML
    private ToggleButton tbTodos;
    @FXML
    private ToggleButton tbEnCurso;
    @FXML
    private ToggleButton tbEntregados;
    @FXML
    private VBox vbPedidos;
    @FXML
    private VBox vbResumen;
    @FXML
    private Label lblComprasRealizadas;
    @FXML
    private Label lblCamisetasCompradas;
    @FXML
    private Label lblTotalInvertido;
    @FXML
    private Button btnSeguirComprando;

    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
    private List<Order> pedidos;
    private Catalog_Navigator navegador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        String correo = sesion == null ? null : sesion.getUser().getEmail();
        pedidos = Data_Manager.getManager().getStacks_products().getOrdersByUser(correo);

        mostrarResumen();

        // Siempre queda un filtro elegido
        tgEstado.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                mostrarPedidos();
            }
        });
        mostrarPedidos();
    }

    public void setNavegador(Catalog_Navigator navegador) {
        this.navegador = navegador;
    }

    private boolean enCurso(Order pedido) {
        return pedido.getDate_purchase() != null && pedido.getDate_purchase().isAfter(LocalDateTime.now().minusDays(DIAS_DE_ENTREGA));
    }

    private void mostrarResumen() {
        int camisetas = 0;
        double total = 0;
        for (Order pedido : pedidos) {
            camisetas += pedido.getUnits();
            total += pedido.getTotal();
        }

        lblCantidadPedidos.setText(pedidos.size() + (pedidos.size() == 1 ? " pedido realizado" : " pedidos realizados"));
        lblComprasRealizadas.setText(String.valueOf(pedidos.size()));
        lblCamisetasCompradas.setText(String.valueOf(camisetas));
        lblTotalInvertido.setText("$ " + formato.format(total));
    }

    private void mostrarPedidos() {
        String filtro = (String) tgEstado.getSelectedToggle().getUserData();

        vbPedidos.getChildren().clear();
        for (Order pedido : pedidos) {
            boolean curso = enCurso(pedido);
            if ("TODOS".equals(filtro) || ("EN_CURSO".equals(filtro) && curso) || ("ENTREGADO".equals(filtro) && !curso)) {
                vbPedidos.getChildren().add(crearTarjeta(pedido, curso));
            }
        }

        if (vbPedidos.getChildren().isEmpty()) {
            Label vacio = new Label(pedidos.isEmpty()
                    ? "Aún no has hecho compras.\nCuando compres desde tu carrito, tus pedidos aparecerán aquí."
                    : "No tienes pedidos con este estado.");
            vacio.getStyleClass().add("empty-state");
            vacio.setMaxWidth(Double.MAX_VALUE);
            VBox tarjeta = new VBox(vacio);
            tarjeta.getStyleClass().addAll("admin-card", "client-card");
            vbPedidos.getChildren().add(tarjeta);
        }
    }

    private VBox crearTarjeta(Order pedido, boolean curso) {
        // Encabezado: número, fecha y estado
        Label lblNumero = etiqueta("Pedido #OFF-" + String.format("%05d", pedido.getNumber()), "order-card-title");
        String fecha = pedido.getDate_purchase() == null ? "sin fecha" : FORMATO_FECHA.format(pedido.getDate_purchase()).replace(".", "");
        Label lblFecha = etiqueta("Realizado el " + fecha, "cell-muted");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label lblEstado = etiqueta(curso ? "En curso" : "Entregado", curso ? "order-status-progress" : "order-status-delivered");
        HBox encabezado = new HBox(new VBox(1, lblNumero, lblFecha), espacio, lblEstado);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        // Miniaturas
        HBox miniaturas = new HBox(8);
        for (Product producto : pedido.getProducts()) {
            if (miniaturas.getChildren().size() < 3) {
                miniaturas.getChildren().add(miniatura(producto));
            }
        }
        if (pedido.getProducts().size() > 3) {
            miniaturas.getChildren().add(etiqueta("+" + (pedido.getProducts().size() - 3), "cell-muted"));
        }
        miniaturas.setAlignment(Pos.CENTER_LEFT);
        miniaturas.setMinWidth(130);
        miniaturas.setPrefWidth(130);

        // Líneas del pedido
        VBox lineas = new VBox(3);
        for (Product producto : pedido.getProducts()) {
            String talla = producto.getSelectedSize() == null ? "" : " · Talla " + producto.getSelectedSize();
            lineas.getChildren().add(etiqueta(producto.getName() + talla + " · x" + producto.getQuantity(), "cell-text"));
        }
        lineas.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lineas, Priority.ALWAYS);

        // Detalle con precios (se muestra con "Ver detalle")
        VBox detalle = new VBox(4);
        for (Product producto : pedido.getProducts()) {
            Region separacion = new Region();
            HBox.setHgrow(separacion, Priority.ALWAYS);
            String talla = producto.getSelectedSize() == null ? "" : "  ·  Talla " + producto.getSelectedSize();
            HBox fila = new HBox(etiqueta(producto.getQuantity() + " × " + producto.getName() + talla, "cell-text"), separacion,
                    etiqueta(producto.getQuantity() + " × $ " + formato.format(producto.getPrice()) + "  =  $ " + formato.format((double) producto.getPrice() * producto.getQuantity()), "cell-amount"));
            fila.setAlignment(Pos.CENTER_LEFT);
            detalle.getChildren().add(fila);
        }

        // Desglose: subtotal, descuento, envío y total pagado
        Separator separador = new Separator();
        VBox.setMargin(separador, new Insets(4, 0, 2, 0));
        detalle.getChildren().add(separador);
        detalle.getChildren().add(filaDesglose("Subtotal", "$ " + formato.format(pedido.getProductsTotal()), "cell-text"));
        if (pedido.getDiscount() > 0) {
            String codigo = pedido.getDiscountCode() == null ? "" : " (" + pedido.getDiscountCode() + ")";
            detalle.getChildren().add(filaDesglose("Descuento" + codigo, "- $ " + formato.format(pedido.getDiscount()), "summary-value-discount"));
        }
        detalle.getChildren().add(filaDesglose("Envío", pedido.getShipping() > 0 ? "$ " + formato.format(pedido.getShipping()) : "Sin costo", "cell-text"));
        detalle.getChildren().add(filaDesglose("Total pagado", "$ " + formato.format(pedido.getTotal()), "cell-amount"));
        detalle.getStyleClass().add("order-detail-box");
        detalle.setVisible(false);
        detalle.setManaged(false);

        // Total y botón de detalle
        Label lblTotalTitulo = etiqueta("Total", "cell-muted");
        Label lblTotal = etiqueta("$ " + formato.format(pedido.getTotal()), "order-card-total");
        Button btnDetalle = new Button("Ver detalle  →");
        btnDetalle.getStyleClass().add("order-detail-button");
        btnDetalle.setOnAction(e -> {
            boolean mostrar = !detalle.isVisible();
            detalle.setVisible(mostrar);
            detalle.setManaged(mostrar);
            btnDetalle.setText(mostrar ? "Ocultar detalle" : "Ver detalle  →");
        });
        VBox vbTotal = new VBox(2, lblTotalTitulo, lblTotal, btnDetalle);
        vbTotal.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(btnDetalle, new Insets(4, 0, 0, 0));

        HBox cuerpo = new HBox(16, miniaturas, lineas, vbTotal);
        cuerpo.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, encabezado, new Separator(), cuerpo, detalle);
        tarjeta.getStyleClass().addAll("admin-card", "order-card");
        return tarjeta;
    }

    private Node miniatura(Product producto) {
        SVGPath generica = new SVGPath();
        generica.setContent("M9.5,0 Q15,3.6 20.5,0 L30,5 L27,11.5 L22.5,9.5 L22.5,30 L7.5,30 L7.5,9.5 L3,11.5 L0,5 Z");
        generica.getStyleClass().add("product-thumb-placeholder");

        ImageView vista = new ImageView();
        vista.setFitWidth(36);
        vista.setFitHeight(36);
        vista.setPreserveRatio(true);
        Image imagen = Product_Images.firstImage(producto.getIdProduct(), 80);
        vista.setImage(imagen);
        generica.setVisible(imagen == null);

        StackPane caja = new StackPane(generica, vista);
        caja.getStyleClass().add("order-thumb");
        caja.setMinSize(40, 40);
        caja.setMaxSize(40, 40);
        return caja;
    }

    private HBox filaDesglose(String concepto, String valor, String claseValor) {
        Region separacion = new Region();
        HBox.setHgrow(separacion, Priority.ALWAYS);
        HBox fila = new HBox(etiqueta(concepto, "cell-muted"), separacion, etiqueta(valor, claseValor));
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private Label etiqueta(String texto, String clase) {
        Label label = new Label(texto);
        label.getStyleClass().add(clase);
        return label;
    }

    @FXML
    private void onSeguirComprando(ActionEvent event) {
        if (navegador != null) {
            navegador.volverAlCatalogo();
        }
    }
}
