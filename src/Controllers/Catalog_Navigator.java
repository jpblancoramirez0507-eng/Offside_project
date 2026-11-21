package Controllers;

import Models.Product;

public interface Catalog_Navigator {

    void volverAlCatalogo();

    void abrirProducto(Product producto);

    void abrirCarrito();

    void abrirHistorial();

    void abrirFavoritos();

    /**
     * Cierra la sesión del cliente y vuelve al inicio de sesión.
     */
    void cerrarSesion();

    /**
     * Agrega al carrito del cliente y muestra un aviso. Si la talla es null
     * se usa la M (o la primera disponible). Retorna las unidades agregadas.
     */
    int agregarAlCarrito(Product producto, String talla, int cantidad);

    void actualizarContadorCarrito();

    void mostrarAviso(String mensaje);

    /**
     * Vuelve a leer los productos (por ejemplo, después de una compra, para
     * que el stock se vea actualizado).
     */
    void recargarCatalogo();
}
