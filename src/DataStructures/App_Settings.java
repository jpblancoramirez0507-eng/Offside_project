package DataStructures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class App_Settings {

    public static final String STOCK_BAJO = "stock_bajo";
    public static final String ULTIMAS_UNIDADES = "ultimas_unidades";
    public static final String INTERVALO_CARRUSEL = "intervalo_carrusel";
    public static final String COSTO_ENVIO = "costo_envio";

    // Límite del costo de envío que se puede configurar (en pesos)
    public static final int MAXIMO_COSTO_ENVIO = 1000000;

    private final Map<String, Integer> valores = new LinkedHashMap<>();

    public App_Settings() {
        restoreDefaults();
        load();
    }

    public int getStockBajo() {
        return valores.get(STOCK_BAJO);
    }

    public int getUltimasUnidades() {
        return valores.get(ULTIMAS_UNIDADES);
    }

    public int getIntervaloCarrusel() {
        return valores.get(INTERVALO_CARRUSEL);
    }

    /**
     * Costo del envío que se suma a cada pedido en el carrito (0 = gratis).
     */
    public int getCostoEnvio() {
        return valores.get(COSTO_ENVIO);
    }

    public void setCostoEnvio(int valor) {
        valores.put(COSTO_ENVIO, valor);
    }

    public void setStockBajo(int valor) {
        valores.put(STOCK_BAJO, valor);
    }

    public void setUltimasUnidades(int valor) {
        valores.put(ULTIMAS_UNIDADES, valor);
    }

    public void setIntervaloCarrusel(int valor) {
        valores.put(INTERVALO_CARRUSEL, valor);
    }

    public final void restoreDefaults() {
        valores.put(STOCK_BAJO, 20);
        valores.put(ULTIMAS_UNIDADES, 10);
        valores.put(INTERVALO_CARRUSEL, 4);
        valores.put(COSTO_ENVIO, 12000);
    }

    private Path filePath() {
        return Paths.get(System.getProperty("user.dir"), "src/Files/Settings");
    }

    public final void load() {
        if (!Files.exists(filePath())) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(filePath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2 && valores.containsKey(parts[0].trim())) {
                    try {
                        valores.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Valor inválido en Settings: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo Settings: " + e.getMessage());
        }
    }

    public boolean save() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Integer> entry : valores.entrySet()) {
            content.append(entry.getKey()).append("=").append(entry.getValue()).append("\r\n");
        }

        try {
            Files.write(filePath(), content.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo Settings: " + e.getMessage());
            return false;
        }
    }
}
