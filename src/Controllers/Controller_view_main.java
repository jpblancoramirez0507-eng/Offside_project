package Controllers;

import DataStructures.Data_Manager;
import DataStructures.List_Double_User;
import Models.Admin;
import Models.Client;
import Models.Nodes.Node_User;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;

public class Controller_view_main implements Initializable {

    // Contenedor raíz
    @FXML
    private HBox paneRaiz;
    @FXML
    private ToggleGroup tgRol;

    // Panel izquierdo: marca
    @FXML
    private StackPane paneMarca;
    @FXML
    private AnchorPane paneColina;
    @FXML
    private Ellipse elpColina;
    @FXML
    private VBox vbMarca;
    @FXML
    private HBox hbLogo;
    @FXML
    private StackPane paneLogo;
    @FXML
    private Circle cirLogoExterior;
    @FXML
    private Circle cirLogoInterior;
    @FXML
    private VBox vbNombreMarca;
    @FXML
    private HBox hbNombreMarca;
    @FXML
    private Label lblMarcaOff;
    @FXML
    private Label lblMarcaSide;
    @FXML
    private Label lblMarcaSubtitulo;
    @FXML
    private Label lblEslogan;
    @FXML
    private Label lblDescripcionMarca;

    // Ligas
    @FXML
    private HBox hbLigas;
    @FXML
    private Label lblLigaPremier;
    @FXML
    private Label lblLigaLaLiga;
    @FXML
    private Label lblLigaSerieA;
    @FXML
    private Label lblLigaBundesliga;
    @FXML
    private Label lblLigaLigue1;

    // Camisetas
    @FXML
    private HBox hbCamisetas;
    @FXML
    private Pane paneCamisetaBarcelona;
    @FXML
    private Pane paneCamisetaLiverpool;
    @FXML
    private Pane paneCamisetaJuventus;
    @FXML
    private Pane paneCamisetaBayern;
    @FXML
    private Pane paneCamisetaPsg;

    // Panel derecho: formulario
    @FXML
    private StackPane paneFormulario;
    @FXML
    private VBox vbTarjetaLogin;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;

    // Selector de rol
    @FXML
    private HBox hbRoles;
    @FXML
    private RadioButton rbUsuario;
    @FXML
    private Circle cirIndicadorUsuario;
    @FXML
    private Label lblRolUsuario;
    @FXML
    private Label lblRolUsuarioDetalle;
    @FXML
    private RadioButton rbAdministrador;
    @FXML
    private Circle cirIndicadorAdministrador;
    @FXML
    private Label lblRolAdministrador;
    @FXML
    private Label lblRolAdministradorDetalle;

    // Credenciales
    @FXML
    private TextField txtCorreo;
    @FXML
    private StackPane paneContrasena;
    @FXML
    private PasswordField pwdContrasena;
    @FXML
    private TextField txtContrasenaVisible;
    @FXML
    private Button btnMostrarContrasena;

    // Acciones
    @FXML
    private Button btnIniciarSesion;
    @FXML
    private Hyperlink lnkOlvidoContrasena;
    @FXML
    private Separator sepFormulario;
    @FXML
    private Label lblPrimeraVez;
    @FXML
    private Button btnCrearCuenta;
    @FXML
    private Label lblMensajeLogin;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ambos campos comparten el mismo texto: lo escrito en uno aparece en el otro
        txtContrasenaVisible.textProperty().bindBidirectional(pwdContrasena.textProperty());
    }

    @FXML
    private void onMostrarContrasena(ActionEvent event) {
        boolean mostrar = !txtContrasenaVisible.isVisible();

        // Alterna cuál de los dos campos se ve (visible) y ocupa espacio (managed)
        txtContrasenaVisible.setVisible(mostrar);
        txtContrasenaVisible.setManaged(mostrar);
        pwdContrasena.setVisible(!mostrar);
        pwdContrasena.setManaged(!mostrar);

        btnMostrarContrasena.setText(mostrar ? "Ocultar" : "Mostrar");

        // Mantiene el foco y el cursor al final del texto en el campo que queda visible
        TextField campoActivo = mostrar ? txtContrasenaVisible : pwdContrasena;
        campoActivo.requestFocus();
        campoActivo.positionCaret(campoActivo.getText().length());
    }

    @FXML
    private void onIniciarSesion(ActionEvent event) {
        List_Double_User usuarios = Data_Manager.getManager().getList_user();
        Node_User encontrado = usuarios.validate_credentials(txtCorreo.getText(), pwdContrasena.getText());

        // El tipo de cuenta elegido también debe coincidir (Usuario -> Client, Administrador -> Admin)
        boolean rolCorrecto = encontrado != null && (rbAdministrador.isSelected() ? encontrado.getUser() instanceof Admin : encontrado.getUser() instanceof Client);

        if (rolCorrecto && encontrado.getUser() instanceof Admin) {
            // El administrador pasa a la pantalla de gestión de inventario
            try {
                Parent vistaAdmin = FXMLLoader.load(getClass().getResource("/Views/View_admin.fxml"));
                btnIniciarSesion.getScene().setRoot(vistaAdmin);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (rolCorrecto) {
            mostrarMensajeLogin("Credenciales correctas", true);
        } else {
            mostrarMensajeLogin("Credenciales incorrectas", false);
        }
    }

    private void mostrarMensajeLogin(String mensaje, boolean exito) {
        lblMensajeLogin.setText(mensaje);
        lblMensajeLogin.getStyleClass().removeAll("login-message-success", "login-message-error");
        lblMensajeLogin.getStyleClass().add(exito ? "login-message-success" : "login-message-error");
        lblMensajeLogin.setManaged(true);
        lblMensajeLogin.setVisible(true);
    }

    @FXML
    private void onOlvidoContrasena(ActionEvent event) {
        // TODO
    }

    @FXML
    private void onCrearCuenta(ActionEvent event) {
        // Navega a la vista de registro reutilizando la misma ventana
        try {
            Parent vistaRegistro = FXMLLoader.load(getClass().getResource("/Views/View_register.fxml"));
            btnCrearCuenta.getScene().setRoot(vistaRegistro);
        } catch (IOException e) {
        }
    }

}
