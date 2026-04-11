package gestorincidencias.modelo;

import gestorincidencias.util.IconoUtil;
import java.awt.Color;
import javax.swing.ImageIcon;

/**
 * Enumeración que define los niveles de prioridad para incidencias.
 *
 * <p>
 * Cada prioridad incluye un color asociado para visualización en la interfaz,
 * permitiendo identificar rápidamente incidencias críticas.</p>
 *
 * @author martamorales
 * @version 1.0
 */
public enum Prioridad {
    BAJA("Baja", new Color(80, 200, 120)),
    MEDIA("Media", new Color(255, 193, 7)),
    ALTA("Alta", new Color(244, 67, 54));

    private final String nombre;
    private final Color color;

    Prioridad(String nombre, Color color) {
        this.nombre = nombre;
        this.color = color;
    }

    public String getNombre() {
        return nombre;
    }

    public ImageIcon getIcono() {
        return IconoUtil.cargarIconoPrioridad(nombre);
    }

    public Color getColor() {
        return color;
    }

    public String getNombreConIcono() {
        ImageIcon icono = getIcono();
        if (icono != null) {
            return nombre; 
        } else {
            switch (this) {
                case ALTA: return "🔴 " + nombre;
                case MEDIA: return "🟡 " + nombre;
                case BAJA: return "🟢 " + nombre;
                default: return nombre;
            }
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
