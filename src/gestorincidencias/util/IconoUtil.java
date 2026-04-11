package gestorincidencias.util;

import javax.swing.ImageIcon;
import java.net.URL;

/**
 * Utilidad para cargar iconos desde recursos
 */
public class IconoUtil {
    
    private static final String RUTA_BASE = "/gestorincidencias/recursos/iconos/";
    
    /**
     * Carga un icono desde recursos con fallback a texto
     */
    public static ImageIcon cargarIcono(String subcarpeta, String nombre) {
        try {
            String ruta = RUTA_BASE + subcarpeta + "/" + nombre + ".png";
            URL iconUrl = IconoUtil.class.getResource(ruta);
            
            if (iconUrl != null) {
                return new ImageIcon(iconUrl);
            } else {
                System.out.println("Icono no encontrado: " + ruta);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error cargando icono: " + nombre);
            return null;
        }
    }
    
    /**
     * Carga icono de prioridad
     */
    public static ImageIcon cargarIconoPrioridad(String nombre) {
        return cargarIcono("prioridad", nombre.toLowerCase());
    }
    
    /**
     * Carga icono de estado
     */
    public static ImageIcon cargarIconoEstado(String nombre) {
        return cargarIcono("estado", nombre.toLowerCase().replace(" ", ""));
    }
    
    /**
     * Carga icono de categoría
     */
    public static ImageIcon cargarIconoCategoria(String nombre) {
        return cargarIcono("categoria", nombre.toLowerCase());
    }
    
    /**
     * Carga icono de UI
     */
    public static ImageIcon cargarIconoUI(String nombre) {
        return cargarIcono("ui", nombre.toLowerCase());
    }
}