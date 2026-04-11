package gestorincidencias.modelo;

import java.time.LocalDateTime;

/**
 * Representa un usuario del sistema de gestión de incidencias.
 *
 * <p>
 * Los usuarios pueden tener diferentes roles (Administrador, Técnico, Cliente)
 * que determinan sus permisos y funcionalidades disponibles en el sistema. La
 * clase incluye funcionalidad para notas personales y validaciones de rol.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class Usuario {

    private int id;
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private Rol rol;
    private String notas;
    private LocalDateTime fechaModificacionNotas;

    public Usuario() {
    }

    /**
     * Constructor completo para crear un usuario con ID. Utilizado
     * principalmente al cargar datos desde la base de datos.
     *
     * @param id Identificador único del usuario
     * @param nombre Nombre del usuario
     * @param apellidos Apellidos del usuario
     * @param email Dirección de correo electrónico (debe ser única)
     * @param password Contraseña (se almacena encriptada en BD)
     * @param rol Rol que determina los permisos del usuario
     */
    public Usuario(int id, String nombre, String apellidos, String email, String password, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public Usuario(String nombre, String apellidos, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * Obtiene el nombre completo concatenando nombre y apellidos.
     *
     * @return Nombre completo en formato "Nombre Apellidos"
     */
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /**
     * Verifica si el usuario tiene rol de administrador
     *
     * @return true si el usuario es administrador, false en caso contrario
     */
    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    /**
     * Verifica si el usuario tiene rol de técnico
     *
     * @return true si el usuario es técnico, false en caso contrario
     */
    public boolean esTecnico() {
        return rol == Rol.TECNICO;
    }

    /**
     * Verifica si el usuario tiene rol de cliente
     *
     * @return true si el usuario es cliente, false en caso contrario
     */
    public boolean esCliente() {
        return rol == Rol.CLIENTE;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFechaModificacionNotas() {
        return fechaModificacionNotas;
    }

    public void setFechaModificacionNotas(LocalDateTime fechaModificacionNotas) {
        this.fechaModificacionNotas = fechaModificacionNotas;
    }

    @Override
    public String toString() {
        return "Usuario{"
                + "id=" + id
                + ", nombre='" + nombre + '\''
                + ", apellidos='" + apellidos + '\''
                + ", email='" + email + '\''
                + ", rol=" + rol
                + '}';
    }

}
