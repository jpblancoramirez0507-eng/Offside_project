package Controllers;

import Controllers.ComponentsControllers.Cart_Item_ComponentController;
import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Discount_Code;
import Models.Nodes.Node_User;
import Models.Product;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Controller_view_cart implements Initializable {

    // Costo del envío configurado por el administrador en "Configuración" (0 = gratis)
    private static double costoEnvio() {
        return Data_Manager.getManager().getSettings().getCostoEnvio();
    }

    @FXML
    private ScrollPane spCarrito;
    @FXML
    private VBox vbCarrito;
    @FXML
    private Label lblTitulo;
    @FXML
    private HBox hbCuerpo;
    @FXML
    private VBox vbTarjetaItems;
    @FXML
    private VBox vbItems;
    @FXML
    private VBox vbCarritoVacio;
    @FXML
    private Button btnIrCatalogo;

    // Resumen
    @FXML
    private VBox vbResumen;
    @FXML
    private Label lblSubtotalPedido;
    @FXML
    private HBox hbDescuento;
    @FXML
    private Label lblTituloDescuento;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblEnvio;
    @FXML
    private Label lblTotal;
    @FXML
    private TextField txtCodigoDescuento;
    @FXML
    private Button btnAplicarCodigo;
    @FXML
    private Label lblMensajeCodigo;
    @FXML
    private Button btnFinalizarCompra;
    @FXML
    private Label lblMensajeCompra;
    @FXML
    private Hyperlink lnkSeguirComprando;

    private final Stack_Of_Product productos = Data_Manager.getManager().getStacks_products();
    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

    private Catalog_Navigator navegador;
    // Código que el cliente aplicó (null si no hay)
    private Discount_Code codigoAplicado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mostrarCarrito();
    }

    public void setNavegador(Catalog_Navigator navegador) {
        this.navegador = navegador;
    }

    private String correo() {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        return sesion == null ? null : sesion.getUser().getEmail();
    }

    /**
     * Vuelve a dibujar las líneas del carrito y el resumen.
     */
    private void mostrarCarrito() {
        List<Product> items = productos.getCartItems(correo());

        vbItems.getChildren().clear();
        for (Product item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Components/Cart_Item_Component.fxml"));
                Parent fila = loader.load();
                Cart_Item_ComponentController controlador = loader.getController();
                controlador.setItem(item, correo(), this::alCambiarCarrito);
                vbItems.getChildren().add(fila);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean vacio = items.isEmpty();
        vbCarritoVacio.setVisible(vacio);
        vbCarritoVacio.setManaged(vacio);
        btnFinalizarCompra.setDisable(vacio);

        actualizarResumen(items);
    }

    private void actualizarResumen(List<Product> items) {
        int unidades = 0;
        double subtotal = 0;
        for (Product item : items) {
            unidades += item.getQuantity();
            subtotal += (double) item.getPrice() * item.getQuantity();
        }

        double descuento = calcularDescuento(subtotal);
        double envio = items.isEmpty() ? 0 : costoEnvio();

        lblTitulo.setText("Tu carrito (" + unidades + ")");
        lblSubtotalPedido.setText("$ " + formato.format(subtotal));
        lblEnvio.setText(!items.isEmpty() && envio == 0 ? "Gratis" : "$ " + formato.format(envio));
        lblTotal.setText("$ " + formato.format(subtotal - descuento + envio));

        boolean hayCodigo = codigoAplicado != null;
        hbDescuento.setVisible(hayCodigo);
        hbDescuento.setManaged(hayCodigo);
        if (hayCodigo) {
            lblTituloDescuento.setText("Descuento (" + codigoAplicado.getCode() + " · " + codigoAplicado.getPercentage() + "%)");
        }
        lblDescuento.setText("- $ " + formato.format(descuento));
    }

    // Porcentaje del código aplicado sobre el subtotal de las camisetas
    private double calcularDescuento(double subtotal) {
        return codigoAplicado == null ? 0 : Math.round(subtotal * codigoAplicado.getPercentage() / 100.0);
    }

    // Una línea cambió de cantidad, se eliminó o pasó a favoritos
    private void alCambiarCarrito() {
        ocultar(lblMensajeCompra);
        mostrarCarrito();
        if (navegador != null) {
            navegador.actualizarContadorCarrito();
        }
    }

    @FXML
    private void onAplicarCodigo(ActionEvent event) {
        String codigo = txtCodigoDescuento.getText() == null ? "" : txtCodigoDescuento.getText().trim();
        // Solo sirven los códigos que el administrador tiene activos en "Configuración"
        codigoAplicado = codigo.isEmpty() ? null : Data_Manager.getManager().getDiscount_codes().findActive(codigo);

        if (codigoAplicado != null) {
            mostrarMensaje(lblMensajeCodigo, "Código aplicado: " + codigoAplicado.getPercentage() + "% de descuento.", true);
        } else {
            mostrarMensaje(lblMensajeCodigo, codigo.isEmpty() ? "Escribe un código de descuento." : "El código \"" + codigo + "\" no es válido.", false);
        }
        actualizarResumen(productos.getCartItems(correo()));
    }

    /**
     * Pide confirmación y compra todo el carrito: descuenta el stock, guarda
     * la compra en el historial y abre "Mis compras".
     */
    @FXML
    private void onFinalizarCompra(ActionEvent event) {
        if (correo() == null) {
            mostrarMensaje(lblMensajeCompra, "Inicia sesión para comprar.", false);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Confirmas la compra por " + lblTotal.getText() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Finalizar compra");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (!respuesta.isPresent() || respuesta.get() != ButtonType.OK) {
            return;
        }

        // El código pudo desactivarse mientras el cliente tenía el carrito abierto
        if (codigoAplicado != null && Data_Manager.getManager().getDiscount_codes().findActive(codigoAplicado.getCode()) == null) {
            String codigo = codigoAplicado.getCode();
            codigoAplicado = null;
            actualizarResumen(productos.getCartItems(correo()));
            mostrarMensaje(lblMensajeCodigo, "El código " + codigo + " ya no está disponible. Revisa el nuevo total.", false);
            return;
        }

        // Se guardan el descuento y el envío junto con el pedido
        double subtotal = 0;
        for (Product item : productos.getCartItems(correo())) {
            subtotal += (double) item.getPrice() * item.getQuantity();
        }
        double descuento = calcularDescuento(subtotal);
        List<String> problemas = productos.checkout(correo(), descuento, costoEnvio(), codigoAplicado == null ? null : codigoAplicado.getCode());
        if (!problemas.isEmpty()) {
            mostrarMensaje(lblMensajeCompra, String.join("\n", problemas), false);
            mostrarCarrito();
            return;
        }

        if (navegador != null) {
            navegador.actualizarContadorCarrito();
            navegador.recargarCatalogo();
            navegador.mostrarAviso("¡Compra realizada! Puedes verla en \"Mis compras\".");
            navegador.abrirHistorial();
        }
    }

    @FXML
    private void onSeguirComprando(ActionEvent event) {
        if (navegador != null) {
            navegador.volverAlCatalogo();
        }
    }

    private void mostrarMensaje(Label etiqueta, String mensaje, boolean exito) {
        etiqueta.setText(mensaje);
        etiqueta.getStyleClass().setAll("label", exito ? "section-message-success" : "section-message-error");
        etiqueta.setVisible(true);
        etiqueta.setManaged(true);
    }

    private void ocultar(Label etiqueta) {
        etiqueta.setVisible(false);
        etiqueta.setManaged(false);
    }
}
