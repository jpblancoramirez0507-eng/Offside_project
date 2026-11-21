package Controllers;

import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import Models.Nodes.Node_User;
import Models.Order;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Controller_view_account implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));

    @FXML
    private ScrollPane spMiCuenta;
    @FXML
    private VBox vbMiCuenta;
    @FXML
    private Label lblTitulo;
    @FXML
    private HBox hbCuerpo;

    // Perfil
    @FXML
    private HBox hbPerfil;
    @FXML
    private Label lblInicial;
    @FXML
    private Label lblNombrePerfil;
    @FXML
    private Label lblCorreoPerfil;
    @FXML
    private Label lblNivel;

    // Datos personales
    @FXML
    private VBox vbDatos;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCorreo;
    @FXML
    private TextField txtIdentificacion;
    @FXML
    private Label lblMensajeDatos;
    @FXML
    private Button btnGuardarDatos;

    // Contraseña
    @FXML
    private VBox vbContrasena;
    @FXML
    private PasswordField pwdActual;
    @FXML
    private PasswordField pwdNueva;
    @FXML
    private PasswordField pwdConfirmar;
    @FXML
    private Label lblMensajeContrasena;
    @FXML
    private Button btnCambiarContrasena;

    // Actividad
    @FXML
    private VBox vbActividad;
    @FXML
    private Label lblPedidos;
    @FXML
    private Label lblCamisetas;
    @FXML
    private Label lblTotalInvertido;
    @FXML
    private Label lblEnCarrito;
    @FXML
    private Label lblEnFavoritos;
    @FXML
    private Label lblUltimaCompra;
    @FXML
    private Button btnVerCompras;
    @FXML
    private Button btnVerFavoritos;
    @FXML
    private Button btnVerCarrito;

    // Sesión
    @FXML
    private VBox vbSesion;
    @FXML
    private Button btnCerrarSesion;

    private final NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
    private Catalog_Navigator navegador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        if (sesion == null) {
            // Sin sesión no hay cuenta que mostrar
            vbDatos.setDisable(true);
            vbContrasena.setDisable(true);
            mostrarMensaje(lblMensajeDatos, "Inicia sesión para ver tu cuenta.", false);
            return;
        }

        mostrarPerfil(sesion);
        mostrarActividad(sesion.getUser().getEmail());
    }

    public void setNavegador(Catalog_Navigator navegador) {
        this.navegador = navegador;
    }

    private void mostrarPerfil(Node_User sesion) {
        String nombre = sesion.getUser().getName();
        lblInicial.setText(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
        lblNombrePerfil.setText(nombre);
        lblCorreoPerfil.setText(sesion.getUser().getEmail());

        txtNombre.setText(nombre);
        txtCorreo.setText(sesion.getUser().getEmail());
        txtIdentificacion.setText(String.valueOf(sesion.getUser().getIdentification()));
    }

    private void mostrarActividad(String correo) {
        Stack_Of_Product productos = Data_Manager.getManager().getStacks_products();
        List<Order> pedidos = productos.getOrdersByUser(correo);

        int camisetas = 0;
        double total = 0;
        for (Order pedido : pedidos) {
            camisetas += pedido.getUnits();
            total += pedido.getTotal();
        }

        lblPedidos.setText(String.valueOf(pedidos.size()));
        lblCamisetas.setText(String.valueOf(camisetas));
        lblTotalInvertido.setText("$ " + formato.format(total));
        int enCarrito = productos.getCartUnits(correo);
        lblEnCarrito.setText(enCarrito + (enCarrito == 1 ? " camiseta" : " camisetas"));
        int enFavoritos = productos.getFavoriteItems(correo).size();
        lblEnFavoritos.setText(enFavoritos + (enFavoritos == 1 ? " camiseta" : " camisetas"));

        lblNivel.setText(pedidos.size() + (pedidos.size() == 1 ? " pedido" : " pedidos"));

        // Los pedidos vienen del más reciente al más antiguo
        if (!pedidos.isEmpty() && pedidos.get(0).getDate_purchase() != null) {
            lblUltimaCompra.setText("Última compra: " + FORMATO_FECHA.format(pedidos.get(0).getDate_purchase()) + ".");
        } else {
            lblUltimaCompra.setText("Aún no has hecho compras.");
        }
    }

    // ---------------------------------------------------------------------
    // Datos y contraseña
    // ---------------------------------------------------------------------

    @FXML
    private void onGuardarDatos(ActionEvent event) {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        if (sesion == null) {
            return;
        }

        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarMensaje(lblMensajeDatos, "Escribe tu nombre.", false);
            return;
        }
        if (nombre.contains(",")) {
            mostrarMensaje(lblMensajeDatos, "El nombre no puede tener comas.", false);
            return;
        }
        if (nombre.equals(sesion.getUser().getName())) {
            mostrarMensaje(lblMensajeDatos, "No hay cambios para guardar.", true);
            return;
        }

        sesion.getUser().setName(nombre);
        Data_Manager.getManager().getList_user().save_data_file_users();
        mostrarPerfil(sesion);
        mostrarMensaje(lblMensajeDatos, "Tus datos se actualizaron.", true);
    }

    @FXML
    private void onCambiarContrasena(ActionEvent event) {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        if (sesion == null) {
            return;
        }

        if (!pwdActual.getText().equals(sesion.getUser().getPassword())) {
            mostrarMensaje(lblMensajeContrasena, "La contraseña actual no es correcta.", false);
            return;
        }
        if (pwdNueva.getText().length() < 8) {
            mostrarMensaje(lblMensajeContrasena, "La nueva contraseña debe tener al menos 8 caracteres.", false);
            return;
        }
        if (pwdNueva.getText().contains(",")) {
            mostrarMensaje(lblMensajeContrasena, "La contraseña no puede tener comas.", false);
            return;
        }
        if (!pwdNueva.getText().equals(pwdConfirmar.getText())) {
            mostrarMensaje(lblMensajeContrasena, "Las contraseñas nuevas no coinciden.", false);
            return;
        }
        if (pwdNueva.getText().equals(sesion.getUser().getPassword())) {
            mostrarMensaje(lblMensajeContrasena, "La nueva contraseña es igual a la actual.", false);
            return;
        }

        sesion.getUser().setPassword(pwdNueva.getText());
        Data_Manager.getManager().getList_user().save_data_file_users();
        pwdActual.clear();
        pwdNueva.clear();
        pwdConfirmar.clear();
        mostrarMensaje(lblMensajeContrasena, "Tu contraseña se cambió. Úsala la próxima vez que inicies sesión.", true);
    }

    private void mostrarMensaje(Label etiqueta, String mensaje, boolean exito) {
        etiqueta.setText(mensaje);
        etiqueta.getStyleClass().setAll("label", exito ? "section-message-success" : "section-message-error");
        etiqueta.setVisible(true);
        etiqueta.setManaged(true);
    }

    // ---------------------------------------------------------------------
    // Accesos directos
    // ---------------------------------------------------------------------

    @FXML
    private void onVerCompras(ActionEvent event) {
        if (navegador != null) {
            navegador.abrirHistorial();
        }
    }

    @FXML
    private void onVerFavoritos(ActionEvent event) {
        if (navegador != null) {
            navegador.abrirFavoritos();
        }
    }

    @FXML
    private void onVerCarrito(ActionEvent event) {
        if (navegador != null) {
            navegador.abrirCarrito();
        }
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        if (navegador != null) {
            navegador.cerrarSesion();
        }
    }
}
