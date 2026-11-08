package Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

public class Controller_view_register implements Initializable {

    // Contenedor raíz
    @FXML
    private HBox paneRaiz;
    @FXML
    private ToggleGroup tgTipoCuenta;

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

    // Beneficios
    @FXML
    private VBox vbBeneficios;
    @FXML
    private Label lblBeneficioLigas;
    @FXML
    private Label lblBeneficioFavoritos;
    @FXML
    private Label lblBeneficioHistorial;

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
    private VBox vbTarjetaRegistro;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;

    // Tipo de cuenta (Client / Admin)
    @FXML
    private HBox hbTiposCuenta;
    @FXML
    private RadioButton rbCliente;
    @FXML
    private Circle cirIndicadorCliente;
    @FXML
    private Label lblTipoCliente;
    @FXML
    private Label lblTipoClienteDetalle;
    @FXML
    private RadioButton rbAdministrador;
    @FXML
    private Circle cirIndicadorAdministrador;
    @FXML
    private Label lblTipoAdministrador;
    @FXML
    private Label lblTipoAdministradorDetalle;

    // Datos del usuario (Models.User)
    @FXML
    private HBox hbDatosPersonales;
    @FXML
    private TextField txtIdentificacion;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCorreo;
    @FXML
    private HBox hbContrasenas;
    @FXML
    private PasswordField pwdContrasena;
    @FXML
    private PasswordField pwdConfirmarContrasena;
    @FXML
    private Label lblAyudaContrasena;

    // Acciones
    @FXML
    private Button btnRegistrar;
    @FXML
    private Separator sepFormulario;
    @FXML
    private Label lblYaTienesCuenta;
    @FXML
    private Button btnIrIniciarSesion;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void onRegistrar(ActionEvent event) {
        // TODO
    }

    @FXML
    private void onIrIniciarSesion(ActionEvent event) {
        // Regresa a la vista de inicio de sesión reutilizando la misma ventana
        try {
            Parent vistaLogin = FXMLLoader.load(getClass().getResource("/Views/View_main.fxml"));
            btnIrIniciarSesion.getScene().setRoot(vistaLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
