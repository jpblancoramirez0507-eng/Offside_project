package Utilities;

import java.io.File;
import javafx.scene.image.Image;

public class Product_Images {

    private Product_Images() {
    }

    /**
     * Retorna la primera imagen del producto cargada al tamaño pedido, o null
     * si no existe o JavaFX no la puede leer (por ejemplo, un WebP).
     */
    public static Image firstImage(int idProduct, double size) {
        File folder = new File(System.getProperty("user.dir"), "src/Images/ProductImages");
        String prefix = String.format("%04d", idProduct) + " - ";

        File[] files = folder.listFiles((dir, name) -> name.startsWith(prefix) && name.toLowerCase().matches(".* - 1\\.(png|jpg|jpeg)"));
        if (files == null || files.length == 0) {
            return null;
        }

        Image image = new Image(files[0].toURI().toString(), size, size, true, true);
        return image.isError() ? null : image;
    }

    public static String leagueColorClass(String league) {
        String key = league == null ? "" : league.toLowerCase().replace(" ", "");
        switch (key) {
            case "premierleague":
                return "league-dot-premier";
            case "laliga":
                return "league-dot-laliga";
            case "seriea":
                return "league-dot-seriea";
            case "bundesliga":
                return "league-dot-bundesliga";
            case "ligue1":
                return "league-dot-ligue1";
            default:
                return "league-dot";
        }
    }

    public static String versionText(String productName) {
        String text = productName == null ? "" : productName.toLowerCase();
        if (text.contains("visitante")) {
            return "Visitante 2026/27";
        } else if (text.contains("tercera")) {
            return "Tercera 2026/27";
        }
        return "Local 2026/27";
    }
}
