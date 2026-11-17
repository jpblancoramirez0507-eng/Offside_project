package Utilities;

import DataStructures.Data_Manager;
import DataStructures.Stack_Of_Product;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.util.Duration;

/**
 * Hace rotar las camisetas del panel de marca (login y registro) con la
 * primera imagen ("NNNN - Equipo - 1.png") de cada producto del catálogo.
 *
 * Uso: new Shirt_Carousel(img1, img2, img3, img4, img5).start();
 *
 * Se detiene solo cuando las camisetas dejan de estar en pantalla (por
 * ejemplo, al pasar del login al registro).
 */
public class Shirt_Carousel {

    // Cada cuánto cambian las camisetas
    private static final Duration INTERVALO = Duration.seconds(4);
    // Retraso entre una camiseta y la siguiente al cambiar (efecto en cascada)
    private static final Duration RETRASO_ENTRE_CAMISETAS = Duration.millis(90);
    private static final Duration DURACION_FUNDIDO = Duration.millis(280);

    // Las imágenes con fondo blanco o de cuadros se ven como un recuadro sobre
    // el panel verde; con true solo se usan las que tienen fondo transparente.
    private static final boolean SOLO_FONDO_TRANSPARENTE = true;

    // Tamaño al que se cargan las imágenes
    private static final double TAMANO_CARGA = 260;

    // Imágenes ya cargadas, por ruta y fecha de modificación, para no volver a
    // leerlas cada vez que se cambia entre login y registro
    private static final Map<String, Camiseta> CACHE = new HashMap<>();

    private final ImageView[] espacios;
    private final List<Camiseta> camisetas = new ArrayList<>();
    private Timeline temporizador;
    private int inicio = 0;
    private boolean detenido = false;

    public Shirt_Carousel(ImageView... espacios) {
        this.espacios = espacios;
    }

    /**
     * Carga las camisetas en segundo plano (para no congelar la pantalla) y,
     * al terminar, las muestra y empieza a rotarlas si hay más productos que
     * espacios. Mientras tanto, o si no se encuentra ninguna imagen, se ven
     * las que trae el FXML.
     */
    public void start() {
        espacios[0].sceneProperty().addListener((obs, anterior, nueva) -> {
            if (anterior != null && nueva == null) {
                stop();
            }
        });

        Thread carga = new Thread(() -> {
            List<Camiseta> cargadas = cargarCamisetasDelCatalogo();
            Platform.runLater(() -> iniciar(cargadas));
        }, "carga-camisetas");
        carga.setDaemon(true);
        carga.start();
    }

    private void iniciar(List<Camiseta> cargadas) {
        if (detenido || cargadas.isEmpty()) {
            return;
        }

        camisetas.addAll(cargadas);
        mostrarGrupo(true);

        if (camisetas.size() > espacios.length) {
            temporizador = new Timeline(new KeyFrame(INTERVALO, evento -> avanzar()));
            temporizador.setCycleCount(Timeline.INDEFINITE);
            temporizador.play();
        }
    }

    public void stop() {
        detenido = true;
        if (temporizador != null) {
            temporizador.stop();
            temporizador = null;
        }
    }

    public int getCantidadCamisetas() {
        return camisetas.size();
    }

    private void avanzar() {
        inicio = (inicio + espacios.length) % camisetas.size();
        mostrarGrupo(true);
    }

    private void mostrarGrupo(boolean animar) {
        for (int i = 0; i < espacios.length; i++) {
            ImageView espacio = espacios[i];

            if (i >= camisetas.size()) {
                espacio.setVisible(false);
                espacio.setManaged(false);
                continue;
            }

            Camiseta camiseta = camisetas.get((inicio + i) % camisetas.size());
            espacio.setVisible(true);
            espacio.setManaged(true);
            if (!animar) {
                ponerCamiseta(espacio, camiseta);
                continue;
            }

            FadeTransition salida = new FadeTransition(DURACION_FUNDIDO, espacio);
            salida.setToValue(0);
            salida.setOnFinished(e -> ponerCamiseta(espacio, camiseta));

            FadeTransition entrada = new FadeTransition(DURACION_FUNDIDO, espacio);
            entrada.setToValue(1);

            new SequentialTransition(new PauseTransition(RETRASO_ENTRE_CAMISETAS.multiply(i)), salida, entrada).play();
        }
    }

    private void ponerCamiseta(ImageView espacio, Camiseta camiseta) {
        espacio.setImage(camiseta.imagen);
        espacio.setViewport(camiseta.areaVisible);
    }

    // ---------------------------------------------------------------------
    // Carga de imágenes
    // ---------------------------------------------------------------------

    /**
     * Recorre los productos del catálogo y toma la primera imagen de cada uno
     * que JavaFX pueda leer.
     */
    private static List<Camiseta> cargarCamisetasDelCatalogo() {
        Stack_Of_Product catalogo = Data_Manager.getManager().getStacks_products();
        File carpeta = new File(System.getProperty("user.dir"), "src/Images/ProductImages");
        List<Camiseta> resultado = new ArrayList<>();

        int ultimoId = catalogo.getNextProductId() - 1;
        for (int id = 1; id <= ultimoId; id++) {
            if (catalogo.loadProductFromCatalog(id) == null) {
                continue;
            }

            File archivo = primeraImagen(carpeta, id);
            if (archivo == null) {
                continue;
            }

            Camiseta camiseta = cargarCamiseta(archivo);
            if (camiseta != null && (!SOLO_FONDO_TRANSPARENTE || camiseta.fondoTransparente)) {
                resultado.add(camiseta);
            }
        }
        return resultado;
    }

    private static File primeraImagen(File carpeta, int idProducto) {
        String prefijo = String.format("%04d", idProducto) + " - ";
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith(prefijo) && nombre.toLowerCase().matches(".* - 1\\.(png|jpg|jpeg)"));
        return archivos == null || archivos.length == 0 ? null : archivos[0];
    }

    private static synchronized Camiseta cargarCamiseta(File archivo) {
        String clave = archivo.getAbsolutePath() + "|" + archivo.lastModified();
        if (CACHE.containsKey(clave)) {
            return CACHE.get(clave);
        }

        Image imagen = new Image(archivo.toURI().toString(), TAMANO_CARGA, TAMANO_CARGA, true, true);
        Camiseta camiseta = null;
        if (!imagen.isError() && imagen.getPixelReader() != null) {
            camiseta = new Camiseta(imagen, calcularAreaVisible(imagen), tieneFondoTransparente(imagen));
        }

        CACHE.put(clave, camiseta);
        return camiseta;
    }

    // Una imagen tiene fondo transparente si sus cuatro esquinas lo son
    private static boolean tieneFondoTransparente(Image imagen) {
        PixelReader pixeles = imagen.getPixelReader();
        int ancho = (int) imagen.getWidth() - 1;
        int alto = (int) imagen.getHeight() - 1;
        int[][] esquinas = {{0, 0}, {ancho, 0}, {0, alto}, {ancho, alto}};

        for (int[] esquina : esquinas) {
            if (pixeles.getColor(esquina[0], esquina[1]).getOpacity() > 0.1) {
                return false;
            }
        }
        return true;
    }

    private static Rectangle2D calcularAreaVisible(Image imagen) {
        PixelReader pixeles = imagen.getPixelReader();
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        int minX = ancho, minY = alto, maxX = -1, maxY = -1;

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                if (pixeles.getColor(x, y).getOpacity() > 0.08) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < 0) {
            return null;
        }
        return new Rectangle2D(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static class Camiseta {

        private final Image imagen;
        private final Rectangle2D areaVisible;
        private final boolean fondoTransparente;

        private Camiseta(Image imagen, Rectangle2D areaVisible, boolean fondoTransparente) {
            this.imagen = imagen;
            this.areaVisible = areaVisible;
            this.fondoTransparente = fondoTransparente;
        }
    }
}
