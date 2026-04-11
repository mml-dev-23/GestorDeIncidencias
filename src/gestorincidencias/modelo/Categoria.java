package gestorincidencias.modelo;

import gestorincidencias.util.IconoUtil;
import javax.swing.ImageIcon;

/**
 * Enumeración que define las categorías técnicas disponibles para clasificar incidencias.
 * 
 * <p>Permite categorizar incidencias por tipo de problema técnico,
 * facilitando su asignación a técnicos especializados y generación de reportes.</p>
 * 
 * @author martamorales
 * @version 1.0
 */
public enum Categoria {
    SOFTWARE("Software"),
    HARDWARE("Hardware"),
    RED("Red"),
    SEGURIDAD("Seguridad"),
    OTRO("Otro");

    private final String nombre;

    Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public ImageIcon getIcono() {
        return IconoUtil.cargarIconoCategoria(nombre);
    }

    public String getNombreConIcono() {
        ImageIcon icono = getIcono();
        if (icono != null) {
            return nombre;
        } else {
            switch (this) {
                case SOFTWARE: return "💻 " + nombre;
                case HARDWARE: return "🔧 " + nombre;
                case RED: return "🌐 " + nombre;
                case SEGURIDAD: return "🔒 " + nombre;
                case OTRO: return "📁 " + nombre;
                default: return nombre;
            }
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
