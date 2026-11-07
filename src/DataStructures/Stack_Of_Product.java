package DataStructures;

import Models.Product;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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

    private Product loadProductFromCatalog(int idProduct) {
        String basePath = System.getProperty("user.dir");
        String fileName = String.format("%04d", idProduct) + " - Product Data.txt";
        Path fullPath = Paths.get(basePath, "src/Files/ProductsBase", fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath.toFile()))) {
            String name = "";
            String brand = "";
            String team = "";
            String league = "";
            String[] sizes = {};
            float price = 0;
            String description = "";

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
                        String[] rawSizes = sizesStr.split("[-\\s]+");
                        List<String> sizeList = new ArrayList<>();
                        for (String size : rawSizes) {
                            String trimmed = size.trim();
                            if (!trimmed.isEmpty()) {
                                sizeList.add(trimmed);
                            }
                        }
                        sizes = sizeList.toArray(new String[0]);
                    }
                } else if (line.toLowerCase().startsWith("descripcion") || line.toLowerCase().startsWith("descripcion")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        description = parts[1].trim();
                    }
                }
            }

            if (!name.isEmpty()) {
                return new Product(idProduct, name, brand, team, league, sizes, price, description);
            }

        } catch (IOException e) {
            System.out.println("Error al cargar el producto desde el catálogo: " + e.getMessage());
        }

        return null;
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    writer.write(formatter.format(product.getDate_purchase()));
                } else {
                    writer.write("NULL");
                }

                writer.write(", ");
                writer.write((product.getSelectedSize() != null ? product.getSelectedSize() : "NULL"));
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
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                LocalDateTime datePurchase = LocalDateTime.parse(attributes[2].trim(), formatter);
                                product.setDate_purchase(datePurchase);
                            } catch (Exception e) {
                                System.out.println("Error al parsear la fecha: " + attributes[2] + " - " + e.getMessage());
                            }
                        }

                        if (attributes.length > 3 && !attributes[3].trim().equals("NULL")) {
                            product.setSelectedSize(attributes[3].trim());
                        } else {
                            product.setSelectedSize(null);
                        }

                        pushProduct(stack, product);
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
                product.getTeam(),
                product.getLeague(),
                product.getBrand(),
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
                product.getTeam(),
                product.getLeague(),
                product.getBrand(),
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

    public boolean addToHistory(Product product, String email) {
        if (product == null || email == null) {
            return false;
        }

        Product newProduct = new Product(
                product.getIdProduct(),
                product.getName(),
                product.getTeam(),
                product.getLeague(),
                product.getBrand(),
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
