package gestorincidencias.modelo;

/**
 * Enumeración que define los roles de usuario en el sistema.
 * 
 * <p>Determina los permisos y funcionalidades disponibles:
 * - ADMINISTRADOR: Acceso completo al sistema
 * - TECNICO: Gestión de incidencias asignadas
 * - CLIENTE: Creación y seguimiento de incidencias propias</p>
 * 
 * @author martamorales
 * @version 1.0
 */
public enum Rol {
    ADMINISTRADOR("Administrador"),
    TECNICO("Técnico"),
    CLIENTE("Cliente");

    private final String nombre;

    Rol(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
