package gestorincidencias.util;

import gestorincidencias.modelo.Comentario;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor para operaciones con comentarios de incidencias.
 * 
 * @author martamorales
 * @version 1.0
 */
public class GestorComentarios {
    
    private static GestorComentarios instance;
    
    private GestorComentarios() {}
    
    public static GestorComentarios getInstance() {
        if (instance == null) {
            instance = new GestorComentarios();
        }
        return instance;
    }
    
    /**
     * Obtiene todos los comentarios de una incidencia específica
     */
    public List<Comentario> obtenerPorIncidencia(int idIncidencia) {
        List<Comentario> comentarios = new ArrayList<>();
        
        String sql = """
            SELECT c.id_comentario, c.mensaje_comentario, c.id_usuario, 
                   c.id_incidencia, c.fecha_comentario,
                   CONCAT(u.nombre, ' ', u.apellidos) as nombre_usuario
            FROM COMENTARIOS c
            JOIN USUARIO u ON c.id_usuario = u.id_usuario  
            WHERE c.id_incidencia = ?
            ORDER BY c.fecha_comentario ASC
            """;
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idIncidencia);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comentario comentario = new Comentario();
                comentario.setIdComentario(rs.getInt("id_comentario"));
                comentario.setMensajeComentario(rs.getString("mensaje_comentario"));
                comentario.setIdUsuario(rs.getInt("id_usuario"));
                comentario.setIdIncidencia(rs.getInt("id_incidencia"));
                comentario.setNombreUsuario(rs.getString("nombre_usuario"));
                comentario.setFechaComentario(rs.getTimestamp("fecha_comentario").toLocalDateTime());
                
                comentarios.add(comentario);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo comentarios: " + e.getMessage());
        }
        
        return comentarios;
    }
    
    /**
     * Añade un nuevo comentario a una incidencia
     */
    public boolean añadir(Comentario comentario) {
        String sql = """
            INSERT INTO COMENTARIOS (mensaje_comentario, id_usuario, id_incidencia, fecha_comentario)
            VALUES (?, ?, ?, ?)
            """;
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, comentario.getMensajeComentario());
            stmt.setInt(2, comentario.getIdUsuario());
            stmt.setInt(3, comentario.getIdIncidencia());
            stmt.setTimestamp(4, Timestamp.valueOf(comentario.getFechaComentario()));
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Obtener ID generado
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    comentario.setIdComentario(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error añadiendo comentario: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Elimina un comentario 
     */
    public boolean eliminar(int idComentario) {
        String sql = "DELETE FROM COMENTARIOS WHERE id_comentario = ?";
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idComentario);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error eliminando comentario: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Actualiza un comentario existente
     */
    public boolean actualizar(Comentario comentario) {
        String sql = """
        UPDATE COMENTARIOS 
        SET mensaje_comentario = ?
        WHERE id_comentario = ?
        """;

        try (Connection conn = ConexionBD.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comentario.getMensajeComentario());
            stmt.setInt(2, comentario.getIdComentario());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando comentario: " + e.getMessage());
        }

        return false;
    }
}
