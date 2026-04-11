package gestorincidencias.util;

import gestorincidencias.modelo.Usuario;
import gestorincidencias.modelo.Rol;

/**
 * Gestión de la sesión del usuario autenticado - Patrón Singleton.
 * 
 * <p>Esta clase mantiene el estado de la sesión del usuario que ha iniciado
 * sesión en la aplicación. Proporciona acceso global al usuario actual,
 * su rol y métodos de utilidad para validaciones de permisos.</p>
 * 
 * <p>Implementa el patrón Singleton para garantizar una única sesión
 * activa en toda la aplicación y mantener consistencia en los datos
 * del usuario autenticado.</p>
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class SesionUsuario {
    
    // Instancia única (Singleton)
    private static SesionUsuario instancia;
    
    // Usuario actualmente logueado
    private Usuario usuarioActual;
    
    /**
     * Constructor privado para implementar el patrón Singleton
     */
    private SesionUsuario() {
        // Constructor privado
    }
    
    /**
     * Obtiene la instancia única de SesionUsuario
     * @return instancia única de SesionUsuario
     */
    public static SesionUsuario getInstance() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }
    
    /**
     * Establece el usuario actual en la sesión
     * @param usuario usuario que inició sesión
     */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
        System.out.println("Sesión iniciada para: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
    }
    
    /**
     * Obtiene el usuario actualmente logueado
     * @return usuario actual o null si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Obtiene el rol del usuario actual
     * @return rol del usuario o null si no hay sesión activa
     */
    public Rol getRolActual() {
        return usuarioActual != null ? usuarioActual.getRol() : null;
    }
    
    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o -1 si no hay sesión activa
     */
    public int getIdUsuarioActual() {
        return usuarioActual != null ? usuarioActual.getId() : -1;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     * @return true si es administrador, false en caso contrario
     */
    public boolean esAdministrador() {
        return usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR;
    }
    
    /**
     * Verifica si el usuario actual es técnico
     * @return true si es técnico, false en caso contrario
     */
    public boolean esTecnico() {
        return usuarioActual != null && usuarioActual.getRol() == Rol.TECNICO;
    }
    
    /**
     * Verifica si el usuario actual es cliente
     * @return true si es cliente, false en caso contrario
     */
    public boolean esCliente() {
        return usuarioActual != null && usuarioActual.getRol() == Rol.CLIENTE;
    }
    
    /**
     * Verifica si hay una sesión activa
     * @return true si hay un usuario logueado, false en caso contrario
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }
    
    /**
     * Cierra la sesión actual
     */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("Sesión cerrada para: " + usuarioActual.getNombreCompleto());
            usuarioActual = null;
        }
    }
    
    /**
     * Obtiene información de la sesión para debugging
     * @return String con información de la sesión
     */
    public String getInfoSesion() {
        if (usuarioActual == null) {
            return "No hay sesión activa";
        }
        return "Usuario: " + usuarioActual.getNombreCompleto() + 
               " | Rol: " + usuarioActual.getRol() + 
               " | ID: " + usuarioActual.getId();
    }
}
