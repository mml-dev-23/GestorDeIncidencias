package gestorincidencias.util;

import gestorincidencias.modelo.*;
import gestorincidencias.dao.UsuarioDAO;
import java.util.List;

/**
 * Gestor de usuarios del sistema - Patrón Singleton.
 *
 * <p>
 * Esta clase actúa como una capa de lógica de negocio entre las vistas y el DAO
 * de usuarios. Proporciona validaciones adicionales, manejo de errores y
 * funcionalidades específicas del dominio de usuarios.</p>
 *
 * <p>
 * Implementa el patrón Singleton para garantizar una única instancia en todo el
 * sistema y mantener consistencia en las operaciones de usuario.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class GestorUsuarios {

    /**
     * Instancia única del gestor (patrón Singleton).
     */
    private static GestorUsuarios instancia;

    /**
     * DAO para operaciones de base de datos de usuarios.
     */
    private UsuarioDAO usuarioDAO;

    /**
     * Constructor privado para implementar patrón Singleton. Inicializa el DAO
     * de usuarios y establece la conexión con la base de datos.
     */
    private GestorUsuarios() {
        this.usuarioDAO = new UsuarioDAO();
        System.out.println("GestorUsuarios inicializado con conexión a BD");
    }

    /**
     * Obtiene la instancia única del gestor de usuarios. Crea la instancia si
     * no existe.
     *
     * @return Instancia única de GestorUsuarios
     */
    public static GestorUsuarios getInstance() {
        if (instancia == null) {
            instancia = new GestorUsuarios();
        }
        return instancia;
    }

    /**
     * Obtiene todos los usuarios activos del sistema. Delega la operación al
     * DAO correspondiente.
     *
     * @return Lista de todos los usuarios activos, ordenados por nombre
     */
    public List<Usuario> obtenerTodos() {
        return usuarioDAO.obtenerTodos();
    }

    /**
     * Busca un usuario específico por su ID único.
     *
     * @param id Identificador único del usuario a buscar
     * @return Usuario encontrado o null si no existe o está inactivo
     */
    public Usuario obtenerPorId(int id) {
        return usuarioDAO.obtenerPorId(id);
    }

    /**
     * Busca un usuario por su dirección de correo electrónico. Utilizado
     * principalmente para validaciones y login.
     *
     * @param email Dirección de correo electrónico a buscar
     * @return Usuario encontrado o null si el email no existe
     */
    public Usuario obtenerPorEmail(String email) {
        return usuarioDAO.obtenerPorEmail(email);
    }

    /**
     * Autentica un usuario verificando email y contraseña encriptada. Valida
     * credenciales usando hash seguro y devuelve el usuario autenticado.
     *
     * @param email Dirección de correo electrónico del usuario
     * @param password Contraseña en texto plano (se verifica contra hash)
     * @return Usuario autenticado o null si las credenciales son inválidas
     */
    public Usuario autenticar(String email, String password) {
        return usuarioDAO.autenticar(email, password);
    }

    /**
     * Crea un nuevo usuario en el sistema. Aplica validaciones de negocio como
     * verificar email único antes de crear.
     *
     * @param usuario Objeto Usuario con los datos a insertar
     * @return Usuario creado con ID asignado por la base de datos
     * @throws IllegalArgumentException si el email ya existe en el sistema
     * @throws RuntimeException si ocurre un error durante la creación en BD
     */
    public Usuario crear(Usuario usuario) {
        // Verificar que el email no exista
        if (usuarioDAO.existeEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        if (usuarioDAO.crear(usuario)) {
            return usuario;
        } else {
            throw new RuntimeException("Error al crear el usuario en la base de datos");
        }
    }

    /**
     * Actualiza los datos de un usuario existente. Delega la operación al DAO
     * sin validaciones adicionales.
     *
     * @param usuario Usuario con datos actualizados (debe tener ID válido)
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean actualizar(Usuario usuario) {
        return usuarioDAO.actualizar(usuario);
    }

    /**
     * Elimina lógicamente un usuario del sistema. El usuario se marca como
     * inactivo pero no se elimina físicamente.
     *
     * @param id ID del usuario a eliminar
     * @return true si la eliminación fue exitosa, false si falló o usuario no
     * encontrado
     */
    public boolean eliminar(int id) {
        return usuarioDAO.eliminar(id);
    }

    /**
     * Obtiene todos los usuarios que tienen un rol específico. Útil para
     * obtener listas filtradas como técnicos disponibles.
     *
     * @param rol Rol a filtrar (ADMINISTRADOR, TECNICO, CLIENTE)
     * @return Lista de usuarios con el rol especificado
     */
    public List<Usuario> obtenerPorRol(Rol rol) {
        return usuarioDAO.obtenerPorRol(rol);
    }

    /**
     * Verifica si un email ya está registrado en el sistema. Utilizado para
     * validaciones de unicidad antes de crear usuarios.
     *
     * @param email Dirección de correo electrónico a verificar
     * @return true si el email ya existe, false si está disponible
     */
    public boolean existeEmail(String email) {
        return usuarioDAO.existeEmail(email);
    }
}
