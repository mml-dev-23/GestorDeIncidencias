package gestorincidencias.dao;
 
import gestorincidencias.modelo.*;
import gestorincidencias.util.ConexionBD;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Data Access Object (DAO) para la gestión de incidencias en la base de datos.
 * 
 * <p>Esta clase maneja todas las operaciones CRUD sobre la tabla INCIDENCIA,
 * incluyendo relaciones con usuarios (clientes y técnicos). Proporciona funcionalidades
 * avanzadas como filtrado por múltiples criterios, búsquedas por rango de fechas
 * y gestión automática de fechas de actualización.</p>
 * 
 * <p>Las incidencias mantienen relaciones con usuarios a través de IDs de cliente
 * y técnico asignado, permitiendo consultas eficientes y reportes detallados.</p>
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class IncidenciaDAO {
    
    /**
     * Obtiene todas las incidencias del sistema con información completa de
     * usuarios. Incluye datos del cliente que reportó y del técnico asignado
     * (si existe).
     *
     * @return Lista completa de incidencias con datos de relaciones. Lista
     * vacía si no hay datos
     */
    public List<Incidencia> obtenerTodas() {
        List<Incidencia> incidencias = new ArrayList<>();
        String sql = """
                SELECT i.id_incidencia, i.titulo, i.descripcion, i.prioridad,
                       i.id_usuario_creador, i.id_usuario_asignado, i.id_estado, i.id_categoria,
                       i.fecha_creacion, i.fecha_actualizacion, i.fecha_resolucion,
                       uc.nombre as nombre_cliente, uc.apellidos as apellidos_cliente,
                       ua.nombre as nombre_tecnico, ua.apellidos as apellidos_tecnico,
                       e.nombre_estado, c.nombre_categoria
                FROM INCIDENCIAS i
                LEFT JOIN USUARIO uc ON i.id_usuario_creador = uc.id_usuario
                LEFT JOIN USUARIO ua ON i.id_usuario_asignado = ua.id_usuario
                LEFT JOIN ESTADOS e ON i.id_estado = e.id_estado
                LEFT JOIN CATEGORIAS c ON i.id_categoria = c.id_categoria
                ORDER BY i.fecha_creacion DESC
                """;
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Incidencia incidencia = mapearIncidencia(rs);
                incidencias.add(incidencia);
            }
            
            System.out.println("✓ Obtenidas " + incidencias.size() + " incidencias de la BD");
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo incidencias: " + e.getMessage());
        }
        
        return incidencias;
    }
    
    /**
     * Busca una incidencia específica por su ID único. 
     * Incluye información completa del cliente y técnico relacionados.
     *
     * @param id Identificador único de la incidencia a buscar
     * @return Incidencia encontrada con datos completos o null si no existe
     */
    public Incidencia obtenerPorId(int id) {
        String sql = """
                SELECT i.id_incidencia, i.titulo, i.descripcion, i.prioridad,
                       i.id_usuario_creador, i.id_usuario_asignado, i.id_estado, i.id_categoria,
                       i.fecha_creacion, i.fecha_actualizacion, i.fecha_resolucion,
                       uc.nombre as nombre_cliente, uc.apellidos as apellidos_cliente,
                       ua.nombre as nombre_tecnico, ua.apellidos as apellidos_tecnico,
                       e.nombre_estado, c.nombre_categoria
                FROM INCIDENCIAS i
                LEFT JOIN USUARIO uc ON i.id_usuario_creador = uc.id_usuario
                LEFT JOIN USUARIO ua ON i.id_usuario_asignado = ua.id_usuario
                LEFT JOIN ESTADOS e ON i.id_estado = e.id_estado
                LEFT JOIN CATEGORIAS c ON i.id_categoria = c.id_categoria
                WHERE i.id_incidencia = ?
                """;
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearIncidencia(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo incidencia por ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Crea una nueva incidencia en la base de datos. 
     * Establece automáticamente fecha_creacion y fecha_actualizacion al
     * momento actual. El ID se genera automáticamente por la base de datos.
     *
     * @param incidencia Objeto Incidencia con los datos a insertar (ID se
     * ignora)
     * @return true si la creación fue exitosa, false si falló la operación
     */
    public boolean crear(Incidencia incidencia) {
        String sql = """
                INSERT INTO INCIDENCIAS 
                (titulo, descripcion, prioridad, id_usuario_creador, id_usuario_asignado, id_estado, id_categoria)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Obtener IDs de estado y categoría
            int idEstado = obtenerIdEstado(incidencia.getEstado().getNombre());
            int idCategoria = obtenerIdCategoria(incidencia.getCategoria().getNombre());
            
            stmt.setString(1, incidencia.getTitulo());
            stmt.setString(2, incidencia.getDescripcion());
            stmt.setString(3, incidencia.getPrioridad().getNombre());
            stmt.setInt(4, incidencia.getIdCliente());
            
            if (incidencia.getIdTecnicoAsignado() != null && incidencia.getIdTecnicoAsignado() > 0) {
                stmt.setInt(5, incidencia.getIdTecnicoAsignado());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setInt(6, idEstado);
            stmt.setInt(7, idCategoria);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Obtener el ID generado
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        incidencia.setId(rs.getInt(1));
                    }
                }
                System.out.println("✓ Incidencia creada: " + incidencia.getTitulo());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando incidencia: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Actualiza una incidencia existente en la base de datos. 
     * Actualiza automáticamente el campo fecha_actualizacion al momento actual.
     * Preserva la fecha_creacion original.
     *
     * @param incidencia Incidencia con datos actualizados (debe tener ID válido)
     * @return true si la actualización fue exitosa, false si falló o incidencia
     * no encontrada
     */
    public boolean actualizar(Incidencia incidencia) {
        String sql = """
                UPDATE INCIDENCIAS SET 
                titulo = ?, descripcion = ?, prioridad = ?, 
                id_usuario_asignado = ?, id_estado = ?, id_categoria = ?,
                fecha_resolucion = ?, fecha_actualizacion = ?
                WHERE id_incidencia = ?
                """;
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Obtener IDs de estado y categoría
            int idEstado = obtenerIdEstado(incidencia.getEstado().getNombre());
            int idCategoria = obtenerIdCategoria(incidencia.getCategoria().getNombre());
            
            stmt.setString(1, incidencia.getTitulo());
            stmt.setString(2, incidencia.getDescripcion());
            stmt.setString(3, incidencia.getPrioridad().getNombre());
            
            if (incidencia.getIdTecnicoAsignado() != null && incidencia.getIdTecnicoAsignado() > 0) {
                stmt.setInt(4, incidencia.getIdTecnicoAsignado());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            stmt.setInt(5, idEstado);
            stmt.setInt(6, idCategoria);
            
            if (incidencia.getFechaResolucion() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(incidencia.getFechaResolucion()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(9, incidencia.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✓ Incidencia actualizada: " + incidencia.getTitulo());
                return true;
            } else {
                System.err.println("⚠️ No se encontró incidencia con ID: " + incidencia.getId());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error actualizando incidencia: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Elimina una incidencia de la base de datos. 
     * Realiza eliminación física del registro (no lógica como en usuarios).
     *
     * @param id ID de la incidencia a eliminar
     * @return true si la eliminación fue exitosa, false si falló o incidencia
     * no encontrada
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM INCIDENCIAS WHERE id_incidencia = ?";
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✓ Incidencia eliminada (ID: " + id + ")");
                return true;
            } else {
                System.err.println("⚠️ No se encontró incidencia con ID: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error eliminando incidencia: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Obtiene el ID de estado desde la tabla ESTADOS por nombre. 
     * Método utilitario para convertir enum Estado a ID de base de datos.
     *
     * @param nombreEstado Nombre del estado a buscar (ej: "Pendiente", "En
     * Proceso")
     * @return ID del estado en la tabla ESTADOS
     * @throws SQLException si no se encuentra el estado especificado
     */
    private int obtenerIdEstado(String nombreEstado) throws SQLException {
        String sql = "SELECT id_estado FROM ESTADOS WHERE nombre_estado = ?";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreEstado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_estado");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error consultando estado '" + nombreEstado + "': " + e.getMessage());
            throw e; // Re-lanzar para que el método padre maneje
        }

        throw new SQLException("Estado no encontrado: " + nombreEstado);
    }
    
    /**
     * Obtiene el ID de categoría desde la tabla CATEGORIAS por nombre. 
     * Método utilitario para convertir enum Categoria a ID de base de datos.
     *
     * @param nombreCategoria Nombre de la categoría a buscar (ej: "Software",
     * "Hardware")
     * @return ID de la categoría en la tabla CATEGORIAS
     * @throws SQLException si no se encuentra la categoría especificada
     */
    private int obtenerIdCategoria(String nombreCategoria) throws SQLException {
        String sql = "SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = ?";

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreCategoria);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error consultando categoría '" + nombreCategoria + "': " + e.getMessage());
            throw e; // Re-lanzar para que el método padre maneje
        }
        
        throw new SQLException("Categoría no encontrada: " + nombreCategoria);
    }
    
    /**
     * Mapea un ResultSet de base de datos a un objeto Incidencia. Maneja las
     * relaciones con usuarios y convierte tipos de datos SQL a Java. Método
     * privado utilizado internamente por las consultas.
     *
     * @param rs ResultSet con datos de incidencia desde consulta SQL con JOINs
     * @return Objeto Incidencia completamente poblado con datos relacionados
     * @throws SQLException si hay error al acceder a columnas del ResultSet
     */
    private Incidencia mapearIncidencia(ResultSet rs) throws SQLException {
        try {
            Incidencia incidencia = new Incidencia();

            incidencia.setId(rs.getInt("id_incidencia"));
            incidencia.setTitulo(rs.getString("titulo"));
            incidencia.setDescripcion(rs.getString("descripcion"));

            // Mapear prioridad con validación
            String prioridadStr = rs.getString("prioridad");
            boolean prioridadEncontrada = false;
            for (Prioridad p : Prioridad.values()) {
                if (p.getNombre().equals(prioridadStr)) {
                    incidencia.setPrioridad(p);
                    prioridadEncontrada = true;
                    break;
                }
            }
            if (!prioridadEncontrada) {
                System.err.println("Prioridad no reconocida: " + prioridadStr + ", usando MEDIA por defecto");
                incidencia.setPrioridad(Prioridad.MEDIA);
            }

            // Mapear estado con validación
            String estadoStr = rs.getString("nombre_estado");
            boolean estadoEncontrado = false;
            for (Estado e : Estado.values()) {
                if (e.getNombre().equals(estadoStr)) {
                    incidencia.setEstado(e);
                    estadoEncontrado = true;
                    break;
                }
            }
            if (!estadoEncontrado) {
                System.err.println("Estado no reconocido: " + estadoStr + ", usando PENDIENTE por defecto");
                incidencia.setEstado(Estado.PENDIENTE);
            }

            // Mapear categoría con validación
            String categoriaStr = rs.getString("nombre_categoria");
            boolean categoriaEncontrada = false;
            for (Categoria c : Categoria.values()) {
                if (c.getNombre().equals(categoriaStr)) {
                    incidencia.setCategoria(c);
                    categoriaEncontrada = true;
                    break;
                }
            }
            if (!categoriaEncontrada) {
                System.err.println("Categoría no reconocida: " + categoriaStr + ", usando OTRO por defecto");
                incidencia.setCategoria(Categoria.OTRO);
            }

            // IDs de usuarios
            incidencia.setIdCliente(rs.getInt("id_usuario_creador"));
            incidencia.setIdTecnicoAsignado(rs.getObject("id_usuario_asignado", Integer.class));

            // Nombres completos con validación
            String nombreCliente = rs.getString("nombre_cliente");
            String apellidosCliente = rs.getString("apellidos_cliente");
            if (nombreCliente != null && apellidosCliente != null) {
                incidencia.setNombreCliente(nombreCliente + " " + apellidosCliente);
            } else {
                incidencia.setNombreCliente("Cliente desconocido");
            }

            String nombreTecnico = rs.getString("nombre_tecnico");
            String apellidosTecnico = rs.getString("apellidos_tecnico");
            if (nombreTecnico != null && apellidosTecnico != null) {
                incidencia.setNombreTecnico(nombreTecnico + " " + apellidosTecnico);
            }
            // Si es null, se queda null (sin asignar)

            // Fechas con validación
            Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
            if (fechaCreacion != null) {
                incidencia.setFechaCreacion(fechaCreacion.toLocalDateTime());
            } else {
                // Si no hay fecha de creación, usar fecha actual como fallback
                incidencia.setFechaCreacion(LocalDateTime.now());
                System.err.println("Incidencia ID " + incidencia.getId() + " sin fecha de creación, usando fecha actual");
            }

            Timestamp fechaResolucion = rs.getTimestamp("fecha_resolucion");
            if (fechaResolucion != null) {
                incidencia.setFechaResolucion(fechaResolucion.toLocalDateTime());
            }

            Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
            if (fechaActualizacion != null) {
                incidencia.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
            }

            return incidencia;

        } catch (SQLException e) {
            System.err.println("❌ Error mapeando incidencia: " + e.getMessage());
            throw e; // Re-lanzar para manejo superior
        }
    }
}
