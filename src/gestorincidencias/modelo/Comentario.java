package gestorincidencias.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un comentario en una incidencia del sistema.
 * 
 * @author martamorales
 * @version 1.0
 */
public class Comentario {
    
    private int idComentario;
    private String mensajeComentario;
    private int idUsuario;
    private int idIncidencia;
    private String nombreUsuario;
    private LocalDateTime fechaComentario;
    
    // Constructor vacío
    public Comentario() {
        this.fechaComentario = LocalDateTime.now();
    }
    
    // Constructor completo
    public Comentario(int idComentario, String mensajeComentario, int idUsuario, 
                     int idIncidencia, String nombreUsuario, LocalDateTime fechaComentario) {
        this.idComentario = idComentario;
        this.mensajeComentario = mensajeComentario;
        this.idUsuario = idUsuario;
        this.idIncidencia = idIncidencia;
        this.nombreUsuario = nombreUsuario;
        this.fechaComentario = fechaComentario;
    }
    
    // Constructor para nuevo comentario
    public Comentario(String mensajeComentario, int idUsuario, int idIncidencia, String nombreUsuario) {
        this.mensajeComentario = mensajeComentario;
        this.idUsuario = idUsuario;
        this.idIncidencia = idIncidencia;
        this.nombreUsuario = nombreUsuario;
        this.fechaComentario = LocalDateTime.now();
    }
    
    // Getters y Setters
    public int getIdComentario() { return idComentario; }
    public void setIdComentario(int idComentario) { this.idComentario = idComentario; }
    
    public String getMensajeComentario() { return mensajeComentario; }
    public void setMensajeComentario(String mensajeComentario) { this.mensajeComentario = mensajeComentario; }
    
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    
    public int getIdIncidencia() { return idIncidencia; }
    public void setIdIncidencia(int idIncidencia) { this.idIncidencia = idIncidencia; }
    
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    
    public LocalDateTime getFechaComentario() { return fechaComentario; }
    public void setFechaComentario(LocalDateTime fechaComentario) { this.fechaComentario = fechaComentario; }
    
    /**
     * Formato para mostrar en la lista
     */
    public String getFormatoLista() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return String.format("• %s - %s: %s", 
            fechaComentario.format(formatter),
            nombreUsuario,
            mensajeComentario);
    }
    
    @Override
    public String toString() {
        return getFormatoLista();
    }
}