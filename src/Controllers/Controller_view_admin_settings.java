package Controllers;

import DataStructures.App_Settings;
import DataStructures.Data_Manager;
import Models.Nodes.Node_User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;

public class Controller_view_admin_settings implements Initializable {

    @FXML
    private ScrollPane spConfiguracion;
    @FXML
    private VBox vbConfiguracion;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;

    // Tienda
    @FXML
    private VBox vbTienda;
    @FXML
    private TextField txtStockBajo;
    @FXML
    private TextField txtUltimasUnidades;
    @FXML
    private TextField txtIntervaloCarrusel;
    @FXML
    private Label lblMensajeTienda;
    @FXML
    private Button btnRestablecer;
    @FXML
    private Button btnGuardarTienda;

    // Mi cuenta
    @FXML
    private VBox vbCuenta;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCorreo;
    @FXML
    private TextField txtIdentificacion;
    @FXML
    private PasswordField pwdActual;
    @FXML
    private PasswordField pwdNueva;
    @FXML
    private PasswordField pwdConfirmar;
    @FXML
    private Label lblMensajeCuenta;
    @FXML
    private Button btnGuardarCuenta;

    private final App_Settings ajustes = Data_Manager.getManager().getSettings();

    private Runnable alActualizarCuenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (TextField campo : new TextField[]{txtStockBajo, txtUltimasUnidades, txtIntervaloCarrusel}) {
            campo.setTextFormatter(new TextFormatter<>(cambio -> cambio.getControlNewText().matches("\\d{0,3}") ? cambio : null));
        }

        mostrarAjustes();
        mostrarCuenta();
    }

    public void setAlActualizarCuenta(Runnable alActualizarCuenta) {
        this.alActualizarCuenta = alActualizarCuenta;
    }

    private void mostrarAjustes() {
        txtStockBajo.setText(String.valueOf(ajustes.getStockBajo()));
        txtUltimasUnidades.setText(String.valueOf(ajustes.getUltimasUnidades()));
        txtIntervaloCarrusel.setText(String.valueOf(ajustes.getIntervaloCarrusel()));
    }

    @FXML
    private void onGuardarTienda(ActionEvent event) {
        int stockBajo = leerNumero(txtStockBajo);
        int ultimasUnidades = leerNumero(txtUltimasUnidades);
        int intervalo = leerNumero(txtIntervaloCarrusel);

        if (stockBajo < 1 || ultimasUnidades < 1) {
            mostrarMensaje(lblMensajeTienda, "Los umbrales deben ser números mayores que 0.", false);
            return;
        }
        if (intervalo < 2 || intervalo > 60) {
            mostrarMensaje(lblMensajeTienda, "El cambio de camisetas debe estar entre 2 y 60 segundos.", false);
            return;
        }

        ajustes.setStockBajo(stockBajo);
        ajustes.setUltimasUnidades(ultimasUnidades);
        ajustes.setIntervaloCarrusel(intervalo);

        if (ajustes.save()) {
            mostrarMensaje(lblMensajeTienda, "Ajustes guardados.", true);
        } else {
            mostrarMensaje(lblMensajeTienda, "No se pudo guardar el archivo de ajustes.", false);
        }
    }

    @FXML
    private void onRestablecer(ActionEvent event) {
        ajustes.restoreDefaults();
        mostrarAjustes();
        if (ajustes.save()) {
            mostrarMensaje(lblMensajeTienda, "Se restablecieron los valores por defecto.", true);
        } else {
            mostrarMensaje(lblMensajeTienda, "No se pudo guardar el archivo de ajustes.", false);
        }
    }

    private int leerNumero(TextField campo) {
        return campo.getText().isEmpty() ? 0 : Integer.parseInt(campo.getText());
    }

    private void mostrarCuenta() {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        if (sesion == null) {
            // Sin sesión (por ejemplo, al abrir la vista directamente) no hay cuenta que editar
            vbCuenta.setDisable(true);
            mostrarMensaje(lblMensajeCuenta, "Inicia sesión para editar tu cuenta.", false);
            return;
        }

        txtNombre.setText(sesion.getUser().getName());
        txtCorreo.setText(sesion.getUser().getEmail());
        txtIdentificacion.setText(String.valueOf(sesion.getUser().getIdentification()));
    }

    @FXML
    private void onGuardarCuenta(ActionEvent event) {
        Node_User sesion = Data_Manager.getManager().getCurrent_user();
        if (sesion == null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarMensaje(lblMensajeCuenta, "Escribe tu nombre.", false);
            return;
        }
        if (nombre.contains(",")) {
            mostrarMensaje(lblMensajeCuenta, "El nombre no puede tener comas.", false);
            return;
        }

        boolean cambiarContrasena = !pwdActual.getText().isEmpty() || !pwdNueva.getText().isEmpty() || !pwdConfirmar.getText().isEmpty();
        if (cambiarContrasena) {
            if (!pwdActual.getText().equals(sesion.getUser().getPassword())) {
                mostrarMensaje(lblMensajeCuenta, "La contraseña actual no es correcta.", false);
                return;
            }
            if (pwdNueva.getText().length() < 8) {
                mostrarMensaje(lblMensajeCuenta, "La nueva contraseña debe tener al menos 8 caracteres.", false);
                return;
            }
            if (pwdNueva.getText().contains(",")) {
                mostrarMensaje(lblMensajeCuenta, "La contraseña no puede tener comas.", false);
                return;
            }
            if (!pwdNueva.getText().equals(pwdConfirmar.getText())) {
                mostrarMensaje(lblMensajeCuenta, "Las contraseñas nuevas no coinciden.", false);
                return;
            }
        }

        sesion.getUser().setName(nombre);
        if (cambiarContrasena) {
            sesion.getUser().setPassword(pwdNueva.getText());
        }
        Data_Manager.getManager().getList_user().save_data_file_users();

        pwdActual.clear();
        pwdNueva.clear();
        pwdConfirmar.clear();
        mostrarMensaje(lblMensajeCuenta, cambiarContrasena ? "Cuenta y contraseña actualizadas." : "Cuenta actualizada.", true);

        if (alActualizarCuenta != null) {
            alActualizarCuenta.run();
        }
    }

    private void mostrarMensaje(Label etiqueta, String mensaje, boolean exito) {
        etiqueta.setText(mensaje);
        etiqueta.getStyleClass().setAll("label", exito ? "section-message-success" : "section-message-error");
        etiqueta.setVisible(true);
        etiqueta.setManaged(true);
    }
}
