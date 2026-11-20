package Controllers;

import DataStructures.Data_Manager;
import DataStructures.List_Double_User;
import Models.Admin;
import Models.Nodes.Node_User;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class Controller_view_admin_users implements Initializable {

    private static final String TODOS = "Todos los roles";
    private static final String CLIENTES = "Clientes";
    private static final String ADMINISTRADORES = "Administradores";

    @FXML
    private VBox vbUsuarios;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;

    // Indicadores
    @FXML
    private HBox hbIndicadores;
    @FXML
    private Label lblTotalUsuarios;
    @FXML
    private Label lblTotalClientes;
    @FXML
    private Label lblTotalAdministradores;

    // Filtros
    @FXML
    private HBox hbFiltros;
    @FXML
    private TextField txtBuscarUsuario;
    @FXML
    private ComboBox<String> cbFiltroRol;
    @FXML
    private Label lblMensaje;

    // Tabla
    @FXML
    private VBox vbTablaUsuarios;
    @FXML
    private ScrollPane spListaUsuarios;
    @FXML
    private VBox vbListaUsuarios;
    @FXML
    private Label lblTotalMostrados;

    private final List_Double_User usuarios = Data_Manager.getManager().getList_user();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbFiltroRol.getItems().addAll(TODOS, CLIENTES, ADMINISTRADORES);
        cbFiltroRol.getSelectionModel().selectFirst();

        txtBuscarUsuario.textProperty().addListener((obs, anterior, nuevo) -> mostrarUsuarios());
        cbFiltroRol.valueProperty().addListener((obs, anterior, nuevo) -> mostrarUsuarios());

        mostrarUsuarios();
    }

    private void mostrarUsuarios() {
        String busqueda = txtBuscarUsuario.getText() == null ? "" : txtBuscarUsuario.getText().trim().toLowerCase();
        String rol = cbFiltroRol.getValue();

        int total = 0;
        int administradores = 0;
        int visibles = 0;
        vbListaUsuarios.getChildren().clear();

        for (Node_User nodo = usuarios.getHead(); nodo != null; nodo = nodo.getNext()) {
            boolean esAdmin = nodo.getUser() instanceof Admin;
            total++;
            if (esAdmin) {
                administradores++;
            }

            boolean cumpleRol = TODOS.equals(rol) || (ADMINISTRADORES.equals(rol) && esAdmin) || (CLIENTES.equals(rol) && !esAdmin);
            boolean cumpleBusqueda = busqueda.isEmpty()
                    || nodo.getUser().getName().toLowerCase().contains(busqueda)
                    || nodo.getUser().getEmail().toLowerCase().contains(busqueda)
                    || String.valueOf(nodo.getUser().getIdentification()).contains(busqueda);

            if (cumpleRol && cumpleBusqueda) {
                vbListaUsuarios.getChildren().add(crearFila(nodo));
                visibles++;
            }
        }

        if (visibles == 0) {
            Label vacio = new Label(total == 0 ? "No hay usuarios registrados." : "Ningún usuario coincide con la búsqueda.");
            vacio.getStyleClass().add("empty-state");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vbListaUsuarios.getChildren().add(vacio);
        }

        lblTotalUsuarios.setText(String.valueOf(total));
        lblTotalClientes.setText(String.valueOf(total - administradores));
        lblTotalAdministradores.setText(String.valueOf(administradores));
        lblTotalMostrados.setText("Mostrando " + visibles + " de " + total + " usuarios");
    }

    private HBox crearFila(Node_User nodo) {
        boolean esAdmin = nodo.getUser() instanceof Admin;
        boolean esSesionActual = nodo == Data_Manager.getManager().getCurrent_user();
        String nombre = nodo.getUser().getName();

        Circle circulo = new Circle(17);
        circulo.getStyleClass().add(esAdmin ? "user-avatar-admin" : "user-avatar");
        Label inicial = new Label(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
        inicial.getStyleClass().addAll("user-avatar-letter");
        if (esAdmin) {
            inicial.getStyleClass().add("user-avatar-letter-admin");
        }
        StackPane avatar = new StackPane(circulo, inicial);

        Label lblNombre = new Label(nombre);
        lblNombre.getStyleClass().add("cell-title");
        HBox hbNombre = new HBox(8, lblNombre);
        hbNombre.setAlignment(Pos.CENTER_LEFT);
        if (esSesionActual) {
            Label tu = new Label("Tú");
            tu.getStyleClass().add("you-tag");
            hbNombre.getChildren().add(tu);
        }
        Label lblCorreo = new Label(nodo.getUser().getEmail());
        lblCorreo.getStyleClass().add("cell-muted");
        VBox vbDatos = new VBox(1, hbNombre, lblCorreo);

        HBox hbUsuario = new HBox(12, avatar, vbDatos);
        hbUsuario.setAlignment(Pos.CENTER_LEFT);
        hbUsuario.setMinWidth(0);
        hbUsuario.setPrefWidth(260);
        HBox.setHgrow(hbUsuario, Priority.ALWAYS);

        Label lblIdentificacion = new Label(String.valueOf(nodo.getUser().getIdentification()));
        lblIdentificacion.getStyleClass().add("cell-text");
        lblIdentificacion.setMinWidth(150);
        lblIdentificacion.setPrefWidth(150);

        Label chipRol = new Label(esAdmin ? "Administrador" : "Cliente");
        chipRol.getStyleClass().add(esAdmin ? "role-chip-admin" : "role-chip-client");
        HBox hbRol = new HBox(chipRol);
        hbRol.setAlignment(Pos.CENTER_LEFT);
        hbRol.setMinWidth(140);
        hbRol.setPrefWidth(140);

        Button btnCambiarRol = new Button(esAdmin ? "Hacer cliente" : "Hacer admin");
        btnCambiarRol.getStyleClass().addAll("row-button", "row-button-neutral");
        btnCambiarRol.setPrefWidth(110);
        btnCambiarRol.setOnAction(e -> cambiarRol(nodo));

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().addAll("row-button", "row-button-delete");
        btnEliminar.setPrefWidth(84);
        btnEliminar.setOnAction(e -> eliminar(nodo));

        boolean ultimoAdmin = esAdmin && usuarios.count_admins() <= 1;
        if (esSesionActual) {
            btnCambiarRol.setDisable(true);
            btnEliminar.setDisable(true);
        } else if (ultimoAdmin) {
            btnCambiarRol.setDisable(true);
            btnEliminar.setDisable(true);
        }

        HBox hbAcciones = new HBox(6, btnCambiarRol, btnEliminar);
        hbAcciones.setAlignment(Pos.CENTER_LEFT);
        hbAcciones.setMinWidth(250);
        hbAcciones.setPrefWidth(250);
        if (esSesionActual) {
            Tooltip.install(hbAcciones, new Tooltip("No puedes cambiar ni eliminar tu propia cuenta desde aquí."));
        } else if (ultimoAdmin) {
            Tooltip.install(hbAcciones, new Tooltip("La tienda necesita al menos un administrador."));
        }

        HBox fila = new HBox(hbUsuario, lblIdentificacion, hbRol, hbAcciones);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("section-row");
        return fila;
    }

    private void cambiarRol(Node_User nodo) {
        boolean esAdmin = nodo.getUser() instanceof Admin;
        String nuevoRol = esAdmin ? "CLIENT" : "ADMIN";
        String texto = esAdmin ? "cliente" : "administrador";

        if (!confirmar("Cambiar rol", "¿Convertir a " + nodo.getUser().getName() + " en " + texto + "?")) {
            return;
        }

        usuarios.change_role(nodo, nuevoRol);
        usuarios.save_data_file_users();
        mostrarMensaje(nodo.getUser().getName() + " ahora es " + texto + ".", true);
        mostrarUsuarios();
    }

    private void eliminar(Node_User nodo) {
        String nombre = nodo.getUser().getName();
        if (!confirmar("Eliminar usuario", "¿Eliminar la cuenta de " + nombre + " (" + nodo.getUser().getEmail() + ")?\nEsta acción no se puede deshacer.")) {
            return;
        }

        usuarios.remove_user(nodo);
        usuarios.save_data_file_users();
        mostrarMensaje("Se eliminó la cuenta de " + nombre + ".", true);
        mostrarUsuarios();
    }

    private boolean confirmar(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().setAll("label", exito ? "section-message-success" : "section-message-error");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
}
