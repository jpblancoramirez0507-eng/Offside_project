package DataStructures;

import Models.Order;
import Models.Product;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;

public class Stack_Of_Product {

    private final Stack<Product> cartProducts;
    private final Stack<Product> historyProducts;
    private final Stack<Product> favoriteProducts;

    public Stack_Of_Product() {
        this.cartProducts = new Stack<>();
        this.historyProducts = new Stack<>();
        this.favoriteProducts = new Stack<>();
    }

    public Stack<Product> getCartProducts() {
        return cartProducts;
    }

    public Stack<Product> getHistoryProducts() {
        return historyProducts;
    }

    public Stack<Product> getFavoriteProducts() {
        return favoriteProducts;
    }

    public void pushProduct(Stack<Product> stack, Product product) {
        if (product == null) {
            return;
        }

        boolean exists = false;
        for (Product p : stack) {
            if (p.getIdProduct() == product.getIdProduct()
                    && product.getEmail_customer() != null
                    && product.getEmail_customer().equals(p.getEmail_customer())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            stack.push(product);
        } else {
            System.out.println("El producto ya está registrado en esta pila.");
        }
    }

    public Product getProductByEmailAndId(Stack<Product> stack, String email, int idProduct) {
        if (email == null || stack == null) {
            return null;
        }

        for (Product product : stack) {
            if (product.getEmail_customer() != null
                    && product.getEmail_customer().equals(email)
                    && product.getIdProduct() == idProduct) {
                return product;
            }
        }
        return null;
    }

    public void removeByEmailAndId(Stack<Product> stack, String email, int idProduct) {
        if (stack == null || stack.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Información", "¡No hay productos registrados!");
            return;
        }

        Product product = getProductByEmailAndId(stack, email, idProduct);
        if (product != null && stack.remove(product)) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "¡Producto eliminado!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "¡Producto no encontrado!");
        }
    }

    public Stack<Product> duplicateStack(Stack<Product> stack) {
        Stack<Product> duplicate = new Stack<>();
        duplicate.addAll(stack);
        return duplicate;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Product loadProductFromCatalog(int idProduct) {
        String basePath = System.getProperty("user.dir");
        String fileName = String.format("%04d", idProduct) + " - Product Data.txt";
        Path fullPath = Paths.get(basePath, "src/Files/ProductsBase", fileName);

        // Los archivos del catálogo están en UTF-8 (tildes, ñ, –); FileReader usaría la codificación de Windows
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fullPath.toFile()), StandardCharsets.UTF_8))) {
            String name = "";
            String brand = "";
            String team = "";
            String league = "";
            String[] sizes = {};
            float price = 0;
            String description = "";
            int stock = 0;
            boolean active = true;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.toLowerCase().startsWith("nombre")) {
                    name = line.substring(7).trim();
                } else if (line.toLowerCase().startsWith("marca")) {
                    brand = line.substring(6).trim();
                } else if (line.toLowerCase().startsWith("equipo")) {
                    team = line.substring(7).trim();
                } else if (line.toLowerCase().startsWith("liga")) {
                    league = line.substring(5).trim();
                } else if (line.toLowerCase().startsWith("precio")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        String priceStr = parts[1].trim();
                        priceStr = priceStr.replace("$", "").replace(" ", "");
                        if (priceStr.contains(".") && priceStr.lastIndexOf(".") == priceStr.length() - 4) {
                            priceStr = priceStr.replace(".", "");
                        } else if (priceStr.contains(",")) {
                            priceStr = priceStr.replace(",", "");
                        }
                        try {
                            price = Float.parseFloat(priceStr);
                        } catch (NumberFormatException e) {
                            System.out.println("Error al parsear el precio del producto " + idProduct + ": " + priceStr);
                        }
                    }
                } else if (line.toLowerCase().startsWith("tallas")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        String sizesStr = parts[1].trim();
                        // Acepta tallas separadas por comas, guiones o espacios ("S, M, L, XL")
                        String[] rawSizes = sizesStr.split("[,\\-\\s]+");
                        List<String> sizeList = new ArrayList<>();
                        for (String size : rawSizes) {
                            String trimmed = size.trim();
                            if (!trimmed.isEmpty()) {
                                sizeList.add(trimmed);
                            }
                        }
                        sizes = sizeList.toArray(new String[0]);
                    }
                } else if (line.toLowerCase().startsWith("stock")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        try {
                            stock = Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Error al parsear el stock del producto " + idProduct + ": " + parts[1].trim());
                        }
                    }
                } else if (line.toLowerCase().startsWith("estado")) {
                    active = !line.toLowerCase().contains("descontinuad");
                } else if (line.toLowerCase().startsWith("descripcion") || line.toLowerCase().startsWith("descripción")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        description = parts[1].trim();
                    }
                }
            }

            if (!name.isEmpty()) {
                Product product = new Product(idProduct, name, brand, team, league, sizes, price, description);
                product.setStock(stock);
                product.setActive(active);
                return product;
            }

        } catch (IOException e) {
            System.out.println("Error al cargar el producto desde el catálogo: " + e.getMessage());
        }

        return null;
    }

    public boolean updateStockInCatalog(int idProduct, int newStock) {
        String basePath = System.getProperty("user.dir");
        String fileName = String.format("%04d", idProduct) + " - Product Data.txt";
        Path fullPath = Paths.get(basePath, "src/Files/ProductsBase", fileName);

        try {
            String content = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
            String lineBreak = content.contains("\r\n") ? "\r\n" : "\n";
            String stockLine = "Stock: " + newStock;

            Pattern stockPattern = Pattern.compile("(?im)^stock\\s*:.*$");
            Pattern descriptionPattern = Pattern.compile("(?im)^descripci(o|ó)n");

            if (stockPattern.matcher(content).find()) {
                content = stockPattern.matcher(content).replaceFirst(stockLine);
            } else if (descriptionPattern.matcher(content).find()) {
                content = descriptionPattern.matcher(content).replaceFirst(stockLine + lineBreak + "$0");
            } else {
                content = content + lineBreak + stockLine;
            }

            Files.write(fullPath, content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("Error al actualizar el stock del producto " + idProduct + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Retorna el párrafo que está después de la línea "Descripcion: ..." en el
     * archivo del producto (Product solo guarda esa primera línea). Si no hay
     * párrafo retorna una cadena vacía.
     */
    public String loadLongDescriptionFromCatalog(int idProduct) {
        String fileName = String.format("%04d", idProduct) + " - Product Data.txt";
        Path fullPath = Paths.get(System.getProperty("user.dir"), "src/Files/ProductsBase", fileName);

        try {
            List<String> lines = Files.readAllLines(fullPath, StandardCharsets.UTF_8);
            StringBuilder paragraph = new StringBuilder();
            boolean afterDescription = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (!afterDescription) {
                    afterDescription = trimmed.toLowerCase().startsWith("descripcion") || trimmed.toLowerCase().startsWith("descripción");
                } else if (!trimmed.isEmpty()) {
                    if (paragraph.length() > 0) {
                        paragraph.append("\n\n");
                    }
                    paragraph.append(trimmed);
                }
            }
            return paragraph.toString();
        } catch (IOException e) {
            System.out.println("Error al leer la descripción del producto " + idProduct + ": " + e.getMessage());
            return "";
        }
    }

    /**
     * Retorna los productos del catálogo que siguen a la venta (sin los
     * descontinuados), en orden de id. Si falta algún archivo intermedio se
     * salta sin detenerse.
     */
    public List<Product> loadActiveCatalog() {
        List<Product> products = new ArrayList<>();
        int lastId = getNextProductId() - 1;

        for (int id = 1; id <= lastId; id++) {
            Product product = loadProductFromCatalog(id);
            if (product != null && product.isActive()) {
                products.add(product);
            }
        }
        return products;
    }

    /**
     * Quita un producto del catálogo: agrega "Estado: Descontinuado" a su
     * archivo (no se borra, para que las compras anteriores sigan apareciendo
     * en pedidos y reportes) y lo saca de los carritos y favoritos.
     */
    public boolean discontinueProduct(int idProduct) {
        String fileName = String.format("%04d", idProduct) + " - Product Data.txt";
        Path fullPath = Paths.get(System.getProperty("user.dir"), "src/Files/ProductsBase", fileName);

        try {
            String content = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
            String lineBreak = content.contains("\r\n") ? "\r\n" : "\n";
            String statusLine = "Estado: Descontinuado";

            Pattern statusPattern = Pattern.compile("(?im)^estado\\s*:.*$");
            Pattern descriptionPattern = Pattern.compile("(?im)^descripci(o|ó)n");

            if (statusPattern.matcher(content).find()) {
                content = statusPattern.matcher(content).replaceFirst(statusLine);
            } else if (descriptionPattern.matcher(content).find()) {
                content = descriptionPattern.matcher(content).replaceFirst(statusLine + lineBreak + "$0");
            } else {
                content = content + lineBreak + statusLine;
            }

            Files.write(fullPath, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Error al descontinuar el producto " + idProduct + ": " + e.getMessage());
            return false;
        }

        loadCartFromFile();
        if (cartProducts.removeIf(product -> product.getIdProduct() == idProduct)) {
            saveCartToFile();
        }
        loadFavoritesFromFile();
        if (favoriteProducts.removeIf(product -> product.getIdProduct() == idProduct)) {
            saveFavoritesToFile();
        }
        return true;
    }

    /**
     * Lee el historial de compras y agrupa como un pedido las compras de un
     * mismo cliente hechas en el mismo minuto. Los pedidos quedan numerados
     * por fecha (el más antiguo es el 1) y ordenados del más antiguo al más
     * reciente.
     */
    public List<Order> groupHistoryIntoOrders() {
        loadHistoryFromFile();

        Map<String, Order> grouped = new LinkedHashMap<>();
        for (Product product : historyProducts) {
            LocalDateTime date = product.getDate_purchase() == null ? null : product.getDate_purchase().truncatedTo(ChronoUnit.SECONDS);
            String key = product.getEmail_customer() + "|" + (date == null ? "sin-fecha" : date.toString());

            Order order = grouped.get(key);
            if (order == null) {
                order = new Order(product.getEmail_customer(), date);
                grouped.put(key, order);
            }
            order.getProducts().add(product);
        }

        // Descuento y envío de cada pedido (los pedidos sin línea en OrderCharges quedan en 0)
        for (String[] charge : loadOrderCharges()) {
            Order order = grouped.get(charge[0]);
            if (order == null) {
                // El historial guarda el correo tal como está; se busca sin distinguir mayúsculas
                for (Map.Entry<String, Order> entry : grouped.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(charge[0])) {
                        order = entry.getValue();
                    }
                }
            }
            if (order != null) {
                order.setDiscount(order.getDiscount() + Double.parseDouble(charge[1]));
                order.setShipping(order.getShipping() + Double.parseDouble(charge[2]));
                if (!charge[3].equals("NULL")) {
                    order.setDiscountCode(charge[3]);
                }
            }
        }

        List<Order> orders = new ArrayList<>(grouped.values());
        orders.sort(Comparator.comparing(Order::getDate_purchase, Comparator.nullsFirst(Comparator.naturalOrder())));
        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).setNumber(i + 1);
        }
        return orders;
    }

    /**
     * Agrega una línea a src/Files/OrderCharges con el formato:
     * correo, dd/MM/yyyy HH:mm:ss, descuento, envío, código (o NULL).
     */
    private void saveOrderCharges(String email, LocalDateTime date, double discount, double shipping, String discountCode) {
        Path filePath = Paths.get(System.getProperty("user.dir"), "src/Files/OrderCharges");
        String line = email + ", " + DATE_FORMAT.format(date) + ", "
                + Math.round(discount) + ", " + Math.round(shipping) + ", "
                + (discountCode == null || discountCode.trim().isEmpty() ? "NULL" : discountCode.trim().toUpperCase())
                + System.lineSeparator();

        try {
            Files.write(filePath, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error al guardar los cargos del pedido: " + e.getMessage());
        }
    }

    /**
     * Lee src/Files/OrderCharges. Cada elemento es {clave del pedido
     * ("correo|fecha"), descuento, envío, código}.
     */
    private List<String[]> loadOrderCharges() {
        List<String[]> charges = new ArrayList<>();
        Path filePath = Paths.get(System.getProperty("user.dir"), "src/Files/OrderCharges");
        if (!Files.exists(filePath)) {
            return charges;
        }

        try {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                String[] parts = line.trim().split(", ");
                if (parts.length < 5) {
                    continue;
                }
                try {
                    LocalDateTime date = parseDate(parts[1].trim());
                    Double.parseDouble(parts[2].trim());
                    Double.parseDouble(parts[3].trim());
                    charges.add(new String[]{parts[0].trim() + "|" + date.toString(), parts[2].trim(), parts[3].trim(), parts[4].trim()});
                } catch (Exception e) {
                    System.out.println("Línea inválida en OrderCharges: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer los cargos de los pedidos: " + e.getMessage());
        }
        return charges;
    }

    // Formato de fecha de History, CartShop, Favorites y OrderCharges
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    // Formato anterior (sin segundos): las líneas viejas se siguen leyendo
    private static final DateTimeFormatter OLD_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static LocalDateTime parseDate(String text) {
        try {
            return LocalDateTime.parse(text, DATE_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.parse(text, OLD_DATE_FORMAT);
        }
    }

    public int getNextProductId() {
        File folder = Paths.get(System.getProperty("user.dir"), "src/Files/ProductsBase").toFile();
        String[] names = folder.list((dir, name) -> name.matches("\\d{4} - Product Data\\.txt"));

        int maxId = 0;
        if (names != null) {
            for (String name : names) {
                maxId = Math.max(maxId, Integer.parseInt(name.substring(0, 4)));
            }
        }
        return maxId + 1;
    }

    public boolean existsProductWithName(String name) {
        return findProductByName(name) != null;
    }

    /**
     * Busca un producto por nombre (sin distinguir mayúsculas), esté a la
     * venta o descontinuado. Retorna null si no existe.
     */
    public Product findProductByName(String name) {
        for (int id = 1; id < getNextProductId(); id++) {
            Product product = loadProductFromCatalog(id);
            if (product != null && product.getName().equalsIgnoreCase(name.trim())) {
                return product;
            }
        }
        return null;
    }

    /**
     * Retorna los productos que el administrador quitó del catálogo.
     */
    public List<Product> loadDiscontinuedProducts() {
        List<Product> products = new ArrayList<>();
        for (int id = 1; id < getNextProductId(); id++) {
            Product product = loadProductFromCatalog(id);
            if (product != null && !product.isActive()) {
                products.add(product);
            }
        }
        return products;
    }

    /**
     * Vuelve a poner a la venta un producto descontinuado: quita la línea
     * "Estado: ..." de su archivo. Retorna true si se pudo guardar.
     */
    public boolean restoreProduct(int idProduct) {
        String fileName = String.format("%04d", idProduct) + " - Product Data.txt";
        Path fullPath = Paths.get(System.getProperty("user.dir"), "src/Files/ProductsBase", fileName);

        try {
            String content = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
            content = Pattern.compile("(?im)^estado\\s*:.*(\\r\\n|\\n)?").matcher(content).replaceAll("");
            Files.write(fullPath, content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("Error al restaurar el producto " + idProduct + ": " + e.getMessage());
            return false;
        }
    }

    public boolean saveProductToCatalog(Product product, String longDescription) {
        String fileName = String.format("%04d", product.getIdProduct()) + " - Product Data.txt";
        Path fullPath = Paths.get(System.getProperty("user.dir"), "src/Files/ProductsBase", fileName);

        if (Files.exists(fullPath)) {
            System.out.println("Ya existe un producto con el id " + product.getIdProduct());
            return false;
        }

        NumberFormat priceFormat = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
        String lineBreak = "\r\n";

        StringBuilder content = new StringBuilder();
        content.append("Nombre: ").append(product.getName()).append(lineBreak);
        content.append("Marca: ").append(product.getBrand()).append(lineBreak);
        content.append("Equipo: ").append(product.getTeam()).append(lineBreak);
        content.append("Liga: ").append(product.getLeague()).append(lineBreak);
        content.append("Tallas: ").append(String.join(", ", product.getSizes())).append(lineBreak);
        content.append("Precio: $").append(priceFormat.format(product.getPrice())).append(lineBreak);
        content.append("Stock: ").append(product.getStock()).append(lineBreak);
        content.append("Descripcion: ").append(product.getDescription());

        if (longDescription != null && !longDescription.trim().isEmpty()) {
            content.append(lineBreak).append(lineBreak).append(longDescription.trim().replaceAll("\\R", " "));
        }

        try {
            Files.write(fullPath, content.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar el producto " + product.getIdProduct() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Copia una imagen del producto a src/Images/ProductImages como
     * "NNNN - Equipo - N.png" (o .jpg). La imagen 1 es la principal, la que
     * muestran los cards.
     */
    public boolean saveProductImage(int idProduct, String team, File source, int imageNumber) {
        String extension = source.getName().toLowerCase().endsWith(".png") ? ".png" : ".jpg";
        String fileName = String.format("%04d", idProduct) + " - " + team.replaceAll("[\\\\/:*?\"<>|]", "") + " - " + imageNumber + extension;
        Path target = Paths.get(System.getProperty("user.dir"), "src/Images/ProductImages", fileName);

        try {
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar la imagen del producto " + idProduct + ": " + e.getMessage());
            return false;
        }
    }

    public void saveToFile(Stack<Product> stack, String fileName) {
        String basePath = System.getProperty("user.dir");
        Path filePath = Paths.get(basePath, "src/Files", fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), false))) {
            for (Product product : stack) {
                if (product == null) {
                    continue;
                }

                writer.write(String.valueOf(product.getIdProduct()) + ", ");
                writer.write((product.getEmail_customer() != null ? product.getEmail_customer() : "") + ", ");

                if (product.getDate_purchase() != null) {
                    // Con segundos, para que dos compras del mismo minuto sean pedidos distintos
                    writer.write(DATE_FORMAT.format(product.getDate_purchase()));
                } else {
                    writer.write("NULL");
                }

                writer.write(", ");
                writer.write((product.getSelectedSize() != null ? product.getSelectedSize() : "NULL"));
                writer.write(", ");
                writer.write(String.valueOf(product.getQuantity()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar los datos en el archivo " + fileName + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al guardar en el archivo: " + e.getMessage());
        }
    }

    public void loadFromFile(Stack<Product> stack, String fileName) {
        String basePath = System.getProperty("user.dir");
        Path filePath = Paths.get(basePath, "src/Files", fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;

            if (!stack.isEmpty()) {
                stack.clear();
            }

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] attributes = line.split(", ");

                if (attributes.length < 2) {
                    System.out.println("Línea inválida en el archivo: " + line);
                    continue;
                }

                try {
                    int idProduct = Integer.parseInt(attributes[0].trim());
                    String emailCustomer = attributes[1].trim();

                    Product product = loadProductFromCatalog(idProduct);

                    if (product != null) {
                        product.setEmail_customer(emailCustomer);

                        if (attributes.length > 2 && !attributes[2].trim().equals("NULL")) {
                            try {
                                product.setDate_purchase(parseDate(attributes[2].trim()));
                            } catch (Exception e) {
                                System.out.println("Error al parsear la fecha: " + attributes[2] + " - " + e.getMessage());
                            }
                        }

                        if (attributes.length > 3 && !attributes[3].trim().equals("NULL")) {
                            product.setSelectedSize(attributes[3].trim());
                        } else {
                            product.setSelectedSize(null);
                        }

                        // Quinto campo: cantidad (las líneas antiguas sin cantidad valen 1)
                        if (attributes.length > 4) {
                            try {
                                product.setQuantity(Math.max(1, Integer.parseInt(attributes[4].trim())));
                            } catch (NumberFormatException e) {
                                product.setQuantity(1);
                            }
                        }

                        // El archivo es la fuente de verdad: se cargan todas sus líneas (la
                        // misma camiseta puede estar en dos tallas o comprarse varias veces)
                        stack.push(product);
                    } else {
                        System.out.println("Producto no encontrado en el catálogo: ID " + idProduct);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: ID de producto inválido en la línea: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar los datos desde el archivo " + fileName + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al cargar desde el archivo: " + e.getMessage());
        }
    }

    public void saveCartToFile() {
        saveToFile(cartProducts, "CartShop");
    }

    public void loadCartFromFile() {
        loadFromFile(cartProducts, "CartShop");
    }

    public void saveHistoryToFile() {
        saveToFile(historyProducts, "History");
    }

    public void loadHistoryFromFile() {
        loadFromFile(historyProducts, "History");
    }

    public void saveFavoritesToFile() {
        saveToFile(favoriteProducts, "Favorites");
    }

    public void loadFavoritesFromFile() {
        loadFromFile(favoriteProducts, "Favorites");
    }

    public void loadAllFromFiles() {
        loadCartFromFile();
        loadFavoritesFromFile();
        loadHistoryFromFile();
    }

    public boolean isInCart(int idProduct, String email) {
        return getProductByEmailAndId(cartProducts, email, idProduct) != null;
    }

    public boolean isInFavorites(int idProduct, String email) {
        return getProductByEmailAndId(favoriteProducts, email, idProduct) != null;
    }

    public Stack<Product> getCartProductsByUser(String email) {
        Stack<Product> userProducts = new Stack<>();
        if (email == null) {
            return userProducts;
        }

        for (Product product : cartProducts) {
            if (product.getEmail_customer() != null && product.getEmail_customer().equals(email)) {
                userProducts.push(product);
            }
        }
        return userProducts;
    }

    public Stack<Product> getFavoriteProductsByUser(String email) {
        Stack<Product> userProducts = new Stack<>();
        if (email == null) {
            return userProducts;
        }

        for (Product product : favoriteProducts) {
            if (product.getEmail_customer() != null && product.getEmail_customer().equals(email)) {
                userProducts.push(product);
            }
        }
        return userProducts;
    }

    public Stack<Product> getHistoryProductsByUser(String email) {
        Stack<Product> userProducts = new Stack<>();
        if (email == null) {
            return userProducts;
        }

        for (Product product : historyProducts) {
            if (product.getEmail_customer() != null && product.getEmail_customer().equals(email)) {
                userProducts.push(product);
            }
        }
        return userProducts;
    }

    public boolean removeFromCart(int idProduct, String email) {
        Product product = getProductByEmailAndId(cartProducts, email, idProduct);
        if (product != null) {
            cartProducts.remove(product);
            saveCartToFile();
            return true;
        }
        return false;
    }

    public boolean removeFromFavorites(int idProduct, String email) {
        Product product = getProductByEmailAndId(favoriteProducts, email, idProduct);
        if (product != null) {
            favoriteProducts.remove(product);
            saveFavoritesToFile();
            return true;
        }
        return false;
    }

    public boolean moveFromFavoritesToCart(int idProduct, String email) {
        Product product = getProductByEmailAndId(favoriteProducts, email, idProduct);
        if (product != null) {
            if (isInCart(idProduct, email)) {
                return false;
            }

            favoriteProducts.remove(product);
            product.setEmail_customer(email);
            pushProduct(cartProducts, product);
            saveCartToFile();
            saveFavoritesToFile();
            return true;
        }
        return false;
    }

    public boolean moveFromCartToFavorites(int idProduct, String email) {
        Product product = getProductByEmailAndId(cartProducts, email, idProduct);
        if (product != null) {
            if (isInFavorites(idProduct, email)) {
                return false;
            }

            cartProducts.remove(product);
            product.setEmail_customer(email);
            pushProduct(favoriteProducts, product);
            saveCartToFile();
            saveFavoritesToFile();
            return true;
        }
        return false;
    }

    public boolean addToCart(Product product, String email, String selectedSize) {
        if (product == null || email == null) {
            return false;
        }

        if (isInCart(product.getIdProduct(), email)) {
            return false;
        }

        if (isInFavorites(product.getIdProduct(), email)) {
            return moveFromFavoritesToCart(product.getIdProduct(), email);
        }

        Product newProduct = new Product(
                product.getIdProduct(),
                product.getName(),
                product.getBrand(),
                product.getTeam(),
                product.getLeague(),
                product.getSizes(),
                product.getPrice(),
                product.getDescription()
        );
        newProduct.setEmail_customer(email);
        newProduct.setSelectedSize(selectedSize);
        pushProduct(cartProducts, newProduct);
        saveCartToFile();
        return true;
    }

    public boolean addToFavorites(Product product, String email, String selectedSize) {
        if (product == null || email == null) {
            return false;
        }

        if (isInFavorites(product.getIdProduct(), email)) {
            return false;
        }

        if (isInCart(product.getIdProduct(), email)) {
            return moveFromCartToFavorites(product.getIdProduct(), email);
        }

        Product newProduct = new Product(
                product.getIdProduct(),
                product.getName(),
                product.getBrand(),
                product.getTeam(),
                product.getLeague(),
                product.getSizes(),
                product.getPrice(),
                product.getDescription()
        );
        newProduct.setEmail_customer(email);
        newProduct.setSelectedSize(selectedSize);
        pushProduct(favoriteProducts, newProduct);
        saveFavoritesToFile();
        return true;
    }

    /**
     * Copia de un producto del catálogo para guardarla en una pila (carrito,
     * favoritos o historial) con los datos del cliente.
     */
    private Product copyForCustomer(Product product, String email, String size, int quantity) {
        Product copy = new Product(product.getIdProduct(), product.getName(), product.getBrand(), product.getTeam(),
                product.getLeague(), product.getSizes(), product.getPrice(), product.getDescription());
        copy.setStock(product.getStock());
        copy.setEmail_customer(email);
        copy.setSelectedSize(size);
        copy.setQuantity(quantity);
        return copy;
    }

    private boolean sameCustomer(Product product, String email) {
        return product.getEmail_customer() != null && email != null && product.getEmail_customer().equalsIgnoreCase(email);
    }

    private boolean sameSize(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * Línea del carrito de un cliente para un producto y una talla (null si no
     * está).
     */
    public Product getCartItem(int idProduct, String email, String size) {
        loadCartFromFile();
        for (Product item : cartProducts) {
            if (item.getIdProduct() == idProduct && sameCustomer(item, email) && sameSize(item.getSelectedSize(), size)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Líneas del carrito de un cliente, en el orden en que las agregó.
     */
    public List<Product> getCartItems(String email) {
        loadCartFromFile();
        List<Product> items = new ArrayList<>();
        for (Product item : cartProducts) {
            if (sameCustomer(item, email)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Total de unidades en el carrito del cliente (para el contador).
     */
    public int getCartUnits(String email) {
        int units = 0;
        for (Product item : getCartItems(email)) {
            units += item.getQuantity();
        }
        return units;
    }

    /**
     * Unidades de un producto que el cliente ya tiene en el carrito (sumando
     * todas las tallas). Sirve para no pasar del stock.
     */
    public int getUnitsInCart(int idProduct, String email) {
        int units = 0;
        for (Product item : getCartItems(email)) {
            if (item.getIdProduct() == idProduct) {
                units += item.getQuantity();
            }
        }
        return units;
    }

    /**
     * Agrega unidades de un producto en una talla al carrito. Si ya estaba en
     * esa talla se suman. No deja pasar del stock disponible. Retorna las
     * unidades que realmente se agregaron (0 si no había stock).
     */
    public int addItemToCart(Product product, String email, String size, int quantity) {
        if (product == null || email == null || quantity <= 0) {
            return 0;
        }

        Product current = loadProductFromCatalog(product.getIdProduct());
        if (current == null || !current.isActive()) {
            return 0;
        }

        int available = current.getStock() - getUnitsInCart(product.getIdProduct(), email);
        int toAdd = Math.min(quantity, Math.max(0, available));
        if (toAdd == 0) {
            return 0;
        }

        Product item = getCartItem(product.getIdProduct(), email, size);
        if (item != null) {
            item.setQuantity(item.getQuantity() + toAdd);
        } else {
            cartProducts.push(copyForCustomer(current, email, size, toAdd));
        }
        saveCartToFile();
        return toAdd;
    }

    /**
     * Cambia la cantidad de una línea del carrito (entre 1 y el stock que
     * queda, contando las otras tallas del mismo producto). Retorna la
     * cantidad final.
     */
    public int updateCartItemQuantity(int idProduct, String email, String size, int quantity) {
        // Primero lo que hay en el carrito de este producto (esto recarga la pila)...
        int unitsInCart = getUnitsInCart(idProduct, email);
        // ...y después la línea a cambiar, para modificar el objeto que se va a guardar
        Product item = getCartItem(idProduct, email, size);
        if (item == null) {
            return 0;
        }

        Product current = loadProductFromCatalog(idProduct);
        int stock = current == null ? 0 : current.getStock();
        int otherSizes = unitsInCart - item.getQuantity();
        int finalQuantity = Math.max(1, Math.min(quantity, stock - otherSizes));

        item.setQuantity(finalQuantity);
        saveCartToFile();
        return finalQuantity;
    }

    public boolean removeCartItem(int idProduct, String email, String size) {
        Product item = getCartItem(idProduct, email, size);
        if (item == null) {
            return false;
        }
        cartProducts.remove(item);
        saveCartToFile();
        return true;
    }

    public List<Product> getFavoriteItems(String email) {
        loadFavoritesFromFile();
        List<Product> items = new ArrayList<>();
        for (Product item : favoriteProducts) {
            if (sameCustomer(item, email)) {
                items.add(item);
            }
        }
        return items;
    }

    public boolean isFavorite(int idProduct, String email) {
        for (Product item : getFavoriteItems(email)) {
            if (item.getIdProduct() == idProduct) {
                return true;
            }
        }
        return false;
    }

    /**
     * Agrega o quita el producto de los favoritos del cliente (no toca el
     * carrito). Retorna true si quedó en favoritos.
     */
    public boolean toggleFavorite(Product product, String email) {
        if (product == null || email == null) {
            return false;
        }

        loadFavoritesFromFile();
        Product existing = null;
        for (Product item : favoriteProducts) {
            if (item.getIdProduct() == product.getIdProduct() && sameCustomer(item, email)) {
                existing = item;
                break;
            }
        }

        if (existing != null) {
            favoriteProducts.remove(existing);
            saveFavoritesToFile();
            return false;
        }

        favoriteProducts.push(copyForCustomer(product, email, null, 1));
        saveFavoritesToFile();
        return true;
    }

    /**
     * Compra todo el carrito del cliente: valida que cada producto siga a la
     * venta y tenga stock, descuenta el stock en su archivo, pasa cada línea
     * al historial con la fecha actual y vacía el carrito. Si algo no se
     * puede comprar no se compra nada y se retornan los problemas (lista
     * vacía = compra hecha).
     */
    public List<String> checkout(String email) {
        return checkout(email, 0, 0, null);
    }

    /**
     * Igual que checkout(email), pero además guarda el descuento y el envío
     * del pedido (en src/Files/OrderCharges) para que su total coincida con
     * lo que pagó el cliente.
     */
    public List<String> checkout(String email, double discount, double shipping, String discountCode) {
        List<String> problems = buyCart(email);
        if (problems.isEmpty() && (discount > 0 || shipping > 0)) {
            saveOrderCharges(email, lastCheckoutDate, discount, shipping, discountCode);
        }
        return problems;
    }

    // Fecha del último pedido comprado (la misma de sus líneas en el historial)
    private LocalDateTime lastCheckoutDate;

    private List<String> buyCart(String email) {
        List<String> problems = new ArrayList<>();
        List<Product> items = getCartItems(email);

        if (items.isEmpty()) {
            problems.add("El carrito está vacío.");
            return problems;
        }

        // 1. Validar todo antes de comprar
        Map<Integer, Integer> unitsByProduct = new LinkedHashMap<>();
        for (Product item : items) {
            unitsByProduct.merge(item.getIdProduct(), item.getQuantity(), Integer::sum);
        }
        Map<Integer, Product> current = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : unitsByProduct.entrySet()) {
            Product product = loadProductFromCatalog(entry.getKey());
            if (product == null || !product.isActive()) {
                problems.add("Un producto del carrito ya no está a la venta. Quítalo para continuar.");
            } else if (product.getStock() < entry.getValue()) {
                problems.add(product.getName() + ": solo quedan " + product.getStock() + " unidades y tienes " + entry.getValue() + " en el carrito.");
            } else {
                current.put(entry.getKey(), product);
            }
        }
        if (!problems.isEmpty()) {
            return problems;
        }

        // 2. Descontar stock
        for (Map.Entry<Integer, Integer> entry : unitsByProduct.entrySet()) {
            updateStockInCatalog(entry.getKey(), current.get(entry.getKey()).getStock() - entry.getValue());
        }

        // 3. Pasar al historial (misma fecha para todo el pedido) y vaciar el carrito
        loadHistoryFromFile();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        lastCheckoutDate = now;
        for (Product item : items) {
            Product purchase = copyForCustomer(current.get(item.getIdProduct()), email, item.getSelectedSize(), item.getQuantity());
            purchase.setDate_purchase(now);
            historyProducts.push(purchase);
        }
        saveHistoryToFile();

        cartProducts.removeIf(item -> sameCustomer(item, email));
        saveCartToFile();
        return problems;
    }

    /**
     * Pedidos de un cliente, del más reciente al más antiguo.
     */
    public List<Order> getOrdersByUser(String email) {
        List<Order> orders = new ArrayList<>();
        for (Order order : groupHistoryIntoOrders()) {
            if (order.getEmail_customer() != null && order.getEmail_customer().equalsIgnoreCase(email)) {
                orders.add(0, order);
            }
        }
        return orders;
    }

    public boolean addToHistory(Product product, String email) {
        if (product == null || email == null) {
            return false;
        }

        Product newProduct = new Product(
                product.getIdProduct(),
                product.getName(),
                product.getBrand(),
                product.getTeam(),
                product.getLeague(),
                product.getSizes(),
                product.getPrice(),
                product.getDescription()
        );
        newProduct.setEmail_customer(email);
        newProduct.setDate_purchase(LocalDateTime.now());
        pushProduct(historyProducts, newProduct);
        saveHistoryToFile();
        return true;
    }
}
