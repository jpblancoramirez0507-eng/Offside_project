package DataStructures;

import Models.Discount_Code;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Discount_Codes {

    public static final int MIN_PERCENTAGE = 1;
    public static final int MAX_PERCENTAGE = 90;

    private final List<Discount_Code> codes = new ArrayList<>();

    public Discount_Codes() {
        load();
    }

    public List<Discount_Code> getCodes() {
        return codes;
    }

    public Discount_Code find(String code) {
        if (code == null) {
            return null;
        }
        for (Discount_Code discountCode : codes) {
            if (discountCode.getCode().equalsIgnoreCase(code.trim())) {
                return discountCode;
            }
        }
        return null;
    }

    public Discount_Code findActive(String code) {
        Discount_Code discountCode = find(code);
        return discountCode != null && discountCode.isActive() ? discountCode : null;
    }
    
    public String add(String code, int percentage) {
        String clean = code == null ? "" : code.trim().toUpperCase();

        if (!clean.matches("[A-Z0-9]{3,20}")) {
            return "El código debe tener entre 3 y 20 letras o números, sin espacios.";
        }
        if (percentage < MIN_PERCENTAGE || percentage > MAX_PERCENTAGE) {
            return "El porcentaje debe estar entre " + MIN_PERCENTAGE + " y " + MAX_PERCENTAGE + ".";
        }
        if (find(clean) != null) {
            return "Ya existe el código " + clean + ".";
        }

        codes.add(new Discount_Code(clean, percentage, true));
        return save() ? null : "No se pudo guardar el archivo de códigos.";
    }

    public boolean setActive(String code, boolean active) {
        Discount_Code discountCode = find(code);
        if (discountCode == null) {
            return false;
        }
        discountCode.setActive(active);
        return save();
    }

    public boolean remove(String code) {
        Discount_Code discountCode = find(code);
        if (discountCode == null) {
            return false;
        }
        codes.remove(discountCode);
        return save();
    }

    private Path filePath() {
        return Paths.get(System.getProperty("user.dir"), "src/Files/DiscountCodes");
    }

    public final void load() {
        codes.clear();

        if (!Files.exists(filePath())) {
            codes.add(new Discount_Code("OFFSIDE10", 10, true));
            return;
        }

        try {
            for (String line : Files.readAllLines(filePath(), StandardCharsets.UTF_8)) {
                String[] parts = line.trim().split(", ");
                if (parts.length < 3) {
                    continue;
                }
                try {
                    codes.add(new Discount_Code(parts[0], Integer.parseInt(parts[1].trim()), parts[2].trim().equalsIgnoreCase("ACTIVO")));
                } catch (NumberFormatException e) {
                    System.out.println("Línea inválida en DiscountCodes: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer los códigos de descuento: " + e.getMessage());
        }
    }

    public boolean save() {
        StringBuilder content = new StringBuilder();
        for (Discount_Code discountCode : codes) {
            content.append(discountCode.getCode()).append(", ")
                    .append(discountCode.getPercentage()).append(", ")
                    .append(discountCode.isActive() ? "ACTIVO" : "INACTIVO").append("\r\n");
        }

        try {
            Files.write(filePath(), content.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar los códigos de descuento: " + e.getMessage());
            return false;
        }
    }
}
