package gestorincidencias.dao;

import gestorincidencias.modelo.Usuario;
import gestorincidencias.modelo.Rol;
import gestorincidencias.util.ConexionBD;
import gestorincidencias.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de usuarios en la base de datos.
 *
 * <p>
 * Esta clase proporciona métodos para realizar operaciones CRUD (Crear, Leer,
 * Actualizar, Eliminar) sobre la tabla USUARIO. Incluye funcionalidades
 * específicas como autenticación segura, búsqueda por rol y validación de
 * emails únicos.</p>
 *
 * <p>
 * Utiliza conexiones seguras con try-with-resources para garantizar el cierre
 * automático de recursos de base de datos y manejo robusto de excepciones
 * SQL.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class UsuarioDAO {

    /**
     * Obtiene todos los usuarios activos de la base de datos. Solo incluye
     * usuarios con estado activo = true, ordenados por nombre y apellidos.
     *
     * @return Lista de usuarios activos. Lista vacía si no hay usuarios o error
     * en BD
     */
    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre, apellidos, email, password, rol, fecha_registro, activo "
                + "FROM USUARIO WHERE activo = true ORDER BY nombre, apellidos";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

            System.out.println("✓ Obtenidos " + usuarios.size() + " usuarios de la BD");

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Busca un usuario por su ID único. Solo incluye usuarios activos en la
     * búsqueda.
     *
     * @param id Identificador único del usuario a buscar
     * @return Usuario encontrado o null si no existe o está inactivo
     */
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT id_usuario, nombre, apellidos, email, password, rol, fecha_registro, activo "
                + "FROM USUARIO WHERE id_usuario = ? AND activo = true";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo usuario por ID: " + id + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca un usuario por su dirección de email. Utilizado principalmente para
     * login y validación de emails únicos.
     *
     * @param email Dirección de correo electrónico a buscar
     * @return Usuario encontrado o null si no existe el email
     */
    public Usuario obtenerPorEmail(String email) {
        String sql = "SELECT id_usuario, nombre, apellidos, email, password, rol, fecha_registro, activo "
                + "FROM USUARIO WHERE email = ? AND activo = true";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo usuario por email: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todos los usuarios que tienen un rol específico. Útil para
     * obtener listas de técnicos, clientes o administradores.
     *
     * @param rol Rol de usuario a filtrar (ADMINISTRADOR, TECNICO, CLIENTE)
     * @return Lisra de usuarios con el rol especificado, ordenados por nombre
     */
    public List<Usuario> obtenerPorRol(Rol rol) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre, apellidos, email, password, rol, fecha_registro, activo "
                + "FROM USUARIO WHERE rol = ? AND activo = true ORDER BY nombre, apellidos";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    usuarios.add(usuario);
                }
            }

            System.out.println("✓ Obtenidos " + usuarios.size() + " usuarios con rol " + rol.name());

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo usuarios por rol: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Crea un nuevo usuario en la base de datos. La contraseña se encripta
     * automáticamente antes del almacenamiento. Establece automáticamente el
     * campo 'activo' como true.
     *
     * @param usuario Objeto Usuario con los datos a insertar (ID se genera
     * automáticamente)
     * @return true si la creación fue exitosa, false si falló o email duplicado
     */
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO USUARIO (nombre, apellidos, email, password, rol) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellidos());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, PasswordUtil.hashPassword(usuario.getPassword()));
            stmt.setString(5, usuario.getRol().name());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                // Obtener el ID generado
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                    }
                }
                System.out.println("✓ Usuario creado: " + usuario.getNombreCompleto());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error creando usuario: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("   El email ya existe en la base de datos");
            }
        }

        return false;
    }

    /**
     * Actualiza los datos de un usuario existente. Solo encripta la contraseña
     * si no está ya encriptada (verificación automática).
     *
     * @param usuario Usuario con los datos actualizados (debe tener ID válido)
     * @return true si la actualización fue exitosa, false si falló o usuario no
     * encontrado
     */
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE USUARIO SET nombre = ?, apellidos = ?, email = ?, password = ?, rol = ? "
                + "WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellidos());
            stmt.setString(3, usuario.getEmail());

            // Solo encriptar si la contraseña ha cambiado
            String passwordToStore = usuario.getPassword();
            if (!PasswordUtil.isPasswordHashed(passwordToStore)) {
                passwordToStore = PasswordUtil.hashPassword(passwordToStore);
            }
            stmt.setString(4, passwordToStore);

            stmt.setString(5, usuario.getRol().name());
            stmt.setInt(6, usuario.getId());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("✓ Usuario actualizado: " + usuario.getNombreCompleto());
                return true;
            } else {
                System.err.println("⚠️ No se encontró usuario con ID: " + usuario.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error actualizando usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Autentica un usuario con email y contraseña encriptada. Verifica la
     * contraseña usando hash seguro y devuelve el usuario si es válido. Solo
     * autentica usuarios activos.
     *
     * @param email Dirección de correo electrónico del usuario
     * @param password Contraseña en texto plano (se verifica contra hash
     * almacenado)
     * @return Usuario autenticado o null si credenciales inválidas
     */
    public Usuario autenticar(String email, String password) {
        String sql = "SELECT id_usuario, nombre, apellidos, email, password, rol, fecha_registro, activo "
                + "FROM USUARIO WHERE email = ? AND activo = true";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // Verificar contraseña
                    if (PasswordUtil.verifyPassword(password, storedPassword)) {
                        Usuario usuario = mapearUsuario(rs);
                        System.out.println("✓ Autenticación exitosa: " + usuario.getNombreCompleto());
                        return usuario;
                    }
                }
            }

            System.out.println("❌ Credenciales inválidas para: " + email);
            return null;

        } catch (SQLException e) {
            System.err.println("❌ Error en autenticación para " + email + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Elimina lógicamente un usuario marcándolo como inactivo. No elimina
     * físicamente el registro de la base de datos.
     *
     * @param id ID del usuario a eliminar
     * @return true si la eliminación fue exitosa, false si falló o usuario no
     * encontrado
     */
    public boolean eliminar(int id) {
        String sql = "UPDATE USUARIO SET activo = false WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("✓ Usuario eliminado (ID: " + id + ")");
                return true;
            } else {
                System.err.println("⚠️ No se encontró usuario con ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error eliminando usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Mapea un ResultSet de base de datos a un objeto Usuario. Método privado
     * utilizado internamente por otros métodos DAO.
     *
     * @param rs ResultSet con datos de usuario desde consulta SQL
     * @return Objeto Usuario con los datos mapeados
     * @throws SQLException si hay error al acceder a los datos del ResultSet
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setRol(Rol.valueOf(rs.getString("rol")));

        return usuario;
    }

    /**
     * Verifica si un email ya existe en la base de datos. Útil para validar
     * unicidad antes de crear o actualizar usuarios. Solo considera usuarios
     * activos.
     *
     * @param email Dirección de correo electrónico a verificar
     * @return true si el email ya existe, false si está disponible
     */
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM USUARIO WHERE email = ? AND activo = true";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error verificando email: " + e.getMessage());
        }

        return false;
    }
}
