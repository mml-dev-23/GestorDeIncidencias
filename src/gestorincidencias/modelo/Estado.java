package gestorincidencias.modelo;

import gestorincidencias.util.IconoUtil;
import javax.swing.ImageIcon;

/**
 * Enumeración que define los posibles estados de una incidencia en el sistema.
 *
 * <p>
 * Los estados siguen un flujo lógico: PENDIENTE → EN_PROCESO → RESUELTA →
 * CERRADA. Cada estado incluye un nombre descriptivo y un icono para la
 * interfaz de usuario.</p>
 *
 * @author martamorales
 * @version 1.0
 */
public enum Estado {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En Proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada");

    private final String nombre;

    Estado(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public ImageIcon getIcono() {
        String nombreArchivo = nombre.toLowerCase().replace(" ", "");
        return IconoUtil.cargarIconoEstado(nombreArchivo);
    }

    public String getNombreConIcono() {
        ImageIcon icono = getIcono();
        if (icono != null) {
            return nombre;
        } else {
            switch (this) {
                case PENDIENTE: return "⏳ " + nombre;
                case EN_PROCESO: return "⚡ " + nombre;
                case RESUELTA: return "✅ " + nombre;
                case CERRADA: return "🔒 " + nombre;
                default: return nombre;
            }
        }
    }

    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene el estado a partir del nombre. Busca en todos los valores del
     * enum el que coincida con el nombre proporcionado.
     *
     * @param nombre Nombre del estado a buscar (ej: "Pendiente", "En Proceso")
     * @return El enum Estado correspondiente al nombre
     * @throws IllegalArgumentException si no se encuentra un estado con ese
     * nombre
     */
    public static Estado fromNombre(String nombre) {
        for (Estado estado : Estado.values()) {
            if (estado.getNombre().equals(nombre)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + nombre);
    }
}
