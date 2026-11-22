package Controllers;

import DataStructures.App_Settings;
import DataStructures.Data_Manager;
import DataStructures.Discount_Codes;
import Models.Discount_Code;
import Models.Nodes.Node_User;
import Models.Order;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private TextField txtCostoEnvio;
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

    // Códigos de descuento
    @FXML
    private VBox vbCodigos;
    @FXML
    private TextField txtNuevoCodigo;
    @FXML
    private TextField txtNuevoPorcentaje;
    @FXML
    private Button btnAgregarCodigo;
    @FXML
    private Label lblMensajeCodigos;
    @FXML
    private VBox vbListaCodigos;

    private final App_Settings ajustes = Data_Manager.getManager().getSettings();
    private final NumberFormat formatoPesos = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
    private final Discount_Codes codigos = Data_Manager.getManager().getDiscount_codes();

    private Runnable alActualizarCuenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtCostoEnvio.setTextFormatter(new TextFormatter<>(cambio -> cambio.getControlNewText().matches("[\\d.]{0,9}") ? cambio : null));
        for (TextField campo : new TextField[]{txtStockBajo, txtUltimasUnidades, txtIntervaloCarrusel}) {
            campo.setTextFormatter(new TextFormatter<>(cambio -> cambio.getControlNewText().matches("\\d{0,3}") ? cambio : null));
        }

        mostrarAjustes();
        mostrarCuenta();

        txtNuevoPorcentaje.setTextFormatter(new TextFormatter<>(cambio -> cambio.getControlNewText().matches("\\d{0,2}") ? cambio : null));
        txtNuevoCodigo.setTextFormatter(new TextFormatter<>(cambio -> {
            cambio.setText(cambio.getText().toUpperCase());
            return cambio.getControlNewText().matches("[A-Za-z0-9]{0,20}") ? cambio : null;
        }));
        mostrarCodigos();
    }

    // ---------------------------------------------------------------------
    // Códigos de descuento
    // ---------------------------------------------------------------------

    /**
     * Lista los códigos con su porcentaje, cuántos pedidos los usaron y su
     * estado.
     */
    private void mostrarCodigos() {
        // Veces que se usó cada código (según los pedidos guardados)
        Map<String, Integer> usos = new HashMap<>();
        for (Order pedido : Data_Manager.getManager().getStacks_products().groupHistoryIntoOrders()) {
            if (pedido.getDiscountCode() != null) {
                usos.merge(pedido.getDiscountCode().toUpperCase(), 1, Integer::sum);
            }
        }

        vbListaCodigos.getChildren().clear();
        for (Discount_Code codigo : codigos.getCodes()) {
            Label chip = new Label(codigo.getCode());
            chip.getStyleClass().add("code-chip");
            HBox hbCodigo = new HBox(chip);
            hbCodigo.setAlignment(Pos.CENTER_LEFT);
            hbCodigo.setMinWidth(0);
            HBox.setHgrow(hbCodigo, Priority.ALWAYS);
            hbCodigo.setMaxWidth(Double.MAX_VALUE);

            Label lblPorcentaje = celda(codigo.getPercentage() + "%", "cell-amount");
            int veces = usos.getOrDefault(codigo.getCode(), 0);
            Label lblUsos = celda(veces + (veces == 1 ? " pedido" : " pedidos"), "cell-text");

            Label estado = new Label(codigo.isActive() ? "Activo" : "Inactivo");
            estado.getStyleClass().add(codigo.isActive() ? "code-status-active" : "code-status-inactive");
            HBox hbEstado = new HBox(estado);
            hbEstado.setAlignment(Pos.CENTER_LEFT);
            hbEstado.setMinWidth(110);
            hbEstado.setPrefWidth(110);

            Button btnEstado = new Button(codigo.isActive() ? "Desactivar" : "Activar");
            btnEstado.getStyleClass().addAll("row-button", codigo.isActive() ? "row-button-neutral" : "row-button-add");
            btnEstado.setPrefWidth(90);
            btnEstado.setOnAction(e -> cambiarEstado(codigo));

            Button btnEliminar = new Button("Eliminar");
            btnEliminar.getStyleClass().addAll("row-button", "row-button-delete");
            btnEliminar.setPrefWidth(84);
            btnEliminar.setOnAction(e -> eliminarCodigo(codigo));

            HBox hbAcciones = new HBox(6, btnEstado, btnEliminar);
            hbAcciones.setAlignment(Pos.CENTER_LEFT);
            hbAcciones.setMinWidth(200);
            hbAcciones.setPrefWidth(200);

            HBox fila = new HBox(hbCodigo, lblPorcentaje, lblUsos, hbEstado, hbAcciones);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("code-row");
            vbListaCodigos.getChildren().add(fila);
        }

        if (codigos.getCodes().isEmpty()) {
            Label vacio = new Label("No hay códigos de descuento. Crea uno para que los clientes lo usen en el carrito.");
            vacio.getStyleClass().add("empty-state");
            vacio.setStyle("-fx-padding: 20 0 20 0;");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vbListaCodigos.getChildren().add(vacio);
        }
    }

    private Label celda(String texto, String clase) {
        Label label = new Label(texto);
        label.getStyleClass().add(clase);
        label.setMinWidth(110);
        label.setPrefWidth(110);
        return label;
    }

    @FXML
    private void onAgregarCodigo(ActionEvent event) {
        int porcentaje = txtNuevoPorcentaje.getText().isEmpty() ? 0 : Integer.parseInt(txtNuevoPorcentaje.getText());
        String problema = codigos.add(txtNuevoCodigo.getText(), porcentaje);

        if (problema != null) {
            mostrarMensaje(lblMensajeCodigos, problema, false);
            return;
        }

        mostrarMensaje(lblMensajeCodigos, "Se creó el código " + txtNuevoCodigo.getText().trim().toUpperCase() + " (" + porcentaje + "%).", true);
        txtNuevoCodigo.clear();
        txtNuevoPorcentaje.clear();
        mostrarCodigos();
    }

    private void cambiarEstado(Discount_Code codigo) {
        boolean activar = !codigo.isActive();
        if (codigos.setActive(codigo.getCode(), activar)) {
            mostrarMensaje(lblMensajeCodigos, "El código " + codigo.getCode() + (activar ? " ya se puede usar." : " ya no se puede usar en el carrito."), true);
        } else {
            mostrarMensaje(lblMensajeCodigos, "No se pudo guardar el archivo de códigos.", false);
        }
        mostrarCodigos();
    }

    private void eliminarCodigo(Discount_Code codigo) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el código " + codigo.getCode() + "?\n\nLos pedidos que ya lo usaron conservan su descuento.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Eliminar código");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (!respuesta.isPresent() || respuesta.get() != ButtonType.OK) {
            return;
        }

        if (codigos.remove(codigo.getCode())) {
            mostrarMensaje(lblMensajeCodigos, "Se eliminó el código " + codigo.getCode() + ".", true);
        } else {
            mostrarMensaje(lblMensajeCodigos, "No se pudo guardar el archivo de códigos.", false);
        }
        mostrarCodigos();
    }

    public void setAlActualizarCuenta(Runnable alActualizarCuenta) {
        this.alActualizarCuenta = alActualizarCuenta;
    }

    private void mostrarAjustes() {
        txtStockBajo.setText(String.valueOf(ajustes.getStockBajo()));
        txtUltimasUnidades.setText(String.valueOf(ajustes.getUltimasUnidades()));
        txtIntervaloCarrusel.setText(String.valueOf(ajustes.getIntervaloCarrusel()));
        txtCostoEnvio.setText(formatoPesos.format(ajustes.getCostoEnvio()));
    }

    @FXML
    private void onGuardarTienda(ActionEvent event) {
        int stockBajo = leerNumero(txtStockBajo);
        int ultimasUnidades = leerNumero(txtUltimasUnidades);
        int intervalo = leerNumero(txtIntervaloCarrusel);
        // Se aceptan puntos de miles ("12.000"): solo se toman los dígitos
        String envioTexto = txtCostoEnvio.getText() == null ? "" : txtCostoEnvio.getText().replaceAll("\\D", "");

        if (stockBajo < 1 || ultimasUnidades < 1) {
            mostrarMensaje(lblMensajeTienda, "Los umbrales deben ser números mayores que 0.", false);
            return;
        }
        if (intervalo < 2 || intervalo > 60) {
            mostrarMensaje(lblMensajeTienda, "El cambio de camisetas debe estar entre 2 y 60 segundos.", false);
            return;
        }
        if (envioTexto.isEmpty() || envioTexto.length() > 7 || Integer.parseInt(envioTexto) > App_Settings.MAXIMO_COSTO_ENVIO) {
            mostrarMensaje(lblMensajeTienda, "Escribe un costo de envío entre 0 y $ " + formatoPesos.format(App_Settings.MAXIMO_COSTO_ENVIO) + ".", false);
            return;
        }
        int costoEnvio = Integer.parseInt(envioTexto);

        ajustes.setStockBajo(stockBajo);
        ajustes.setUltimasUnidades(ultimasUnidades);
        ajustes.setIntervaloCarrusel(intervalo);
        ajustes.setCostoEnvio(costoEnvio);

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
