package gestorincidencias.modelo;

import java.time.LocalDateTime;

/**
 * Clase que representa una incidencia en el sistema de gestión. Una incidencia
 * contiene toda la información relacionada con un problema reportado por un
 * cliente, incluyendo su estado, prioridad y asignación.
 *
 * <p>
 * Esta clase maneja automáticamente las fechas de creación y resolución, así
 * como la lógica de negocio relacionada con el ciclo de vida de una
 * incidencia.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class Incidencia {

    private int id;
    private String titulo;
    private String descripcion;
    private Categoria categoria;
    private Prioridad prioridad;
    private Estado estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaActualizacion;
    private int idCliente;
    private String nombreCliente;
    private Integer idTecnicoAsignado;
    private String nombreTecnico;

    /**
     * Constructor vacío
     */
    public Incidencia() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = Estado.PENDIENTE;
    }

    /**
     * Constructor con parámetros básicos para crear una nueva incidencia.
     * Inicializa automáticamente la fecha de creación y establece el estado
     * como PENDIENTE.
     *
     * @param titulo Título descriptivo de la incidencia (máximo 255 caracteres)
     * @param descripcion Descripción detallada del problema
     * @param categoria Categoría técnica de la incidencia
     * @param prioridad Nivel de prioridad de resolución
     * @param idCliente ID único del cliente que reporta la incidencia
     * @param nombreCliente Nombre completo del cliente
     */
    public Incidencia(String titulo, String descripcion, Categoria categoria,
            Prioridad prioridad, int idCliente, String nombreCliente) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    /**
     * Constructor completo para crear una incidencia con todos los datos.
     * Utilizado principalmente al cargar incidencias desde la base de datos.
     *
     * @param id Identificador único de la incidencia
     * @param titulo Título descriptivo de la incidencia
     * @param descripcion Descripción detallada del problema
     * @param categoria Categoría técnica de la incidencia
     * @param prioridad Nivel de prioridad de resolución
     * @param estado Estado actual de la incidencia
     * @param fechaCreacion Fecha y hora de creación de la incidencia
     * @param fechaResolucion Fecha y hora de resolución (puede ser null)
     * @param fechaActualizacion Fecha y hora de última actualización
     * @param idCliente ID único del cliente que reporta la incidencia
     * @param nombreCliente Nombre completo del cliente
     * @param idTecnicoAsignado ID del técnico asignado (puede ser null)
     * @param nombreTecnico Nombre completo del técnico asignado (puede ser
     * null)
     */
    public Incidencia(int id, String titulo, String descripcion, Categoria categoria,
            Prioridad prioridad, Estado estado, LocalDateTime fechaCreacion,
            LocalDateTime fechaResolucion, LocalDateTime fechaActualizacion,
            int idCliente, String nombreCliente,
            Integer idTecnicoAsignado, String nombreTecnico) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaResolucion = fechaResolucion;
        this.fechaActualizacion = fechaActualizacion;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idTecnicoAsignado = idTecnicoAsignado;
        this.nombreTecnico = nombreTecnico;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public Estado getEstado() {
        return estado;
    }

    /**
     * Establece el estado de la incidencia y maneja automáticamente las fechas.
     * Cuando se marca como RESUELTA o CERRADA, establece automáticamente la
     * fecha de resolución si no se había establecido previamente.
     *
     * @param estado Nuevo estado de la incidencia
     */
    public void setEstado(Estado estado) {
        this.estado = estado;
        // Si se marca como resuelta o cerrada, guardar fecha
        if ((estado == Estado.RESUELTA || estado == Estado.CERRADA) && this.fechaResolucion == null) {
            this.fechaResolucion = LocalDateTime.now();
        }
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Integer getIdTecnicoAsignado() {
        return idTecnicoAsignado;
    }

    public void setIdTecnicoAsignado(Integer idTecnicoAsignado) {
        this.idTecnicoAsignado = idTecnicoAsignado;
    }

    public String getNombreTecnico() {
        return nombreTecnico;
    }

    public void setNombreTecnico(String nombreTecnico) {
        this.nombreTecnico = nombreTecnico;
    }

    /**
     * Verifica si la incidencia tiene técnico asignado
     *
     * @return true si hay un técnico asignado (ID > 0), false en caso contrario
     */
    public boolean tieneTecnicoAsignado() {
        return idTecnicoAsignado != null && idTecnicoAsignado > 0;
    }

    /**
     * Obtiene el nombre del técnico o "Sin asignar"
     *
     * @return Nombre del técnico si está asignado, "Sin asignar" en caso
     * contrario
     */
    public String getTecnicoOSinAsignar() {
        return tieneTecnicoAsignado() ? nombreTecnico : "Sin asignar";
    }

    /**
     * Verifica si la incidencia está cerrada o resuelta
     *
     * @return true si el estado es RESUELTA o CERRADA, false en caso contrario
     */
    public boolean estaFinalizada() {
        return estado == Estado.RESUELTA || estado == Estado.CERRADA;
    }

    /**
     * Obtiene una versión truncada de la descripción para visualización en
     * listas.
     *
     * @return Descripción limitada a 100 caracteres con "..." si es más larga,
     * o la descripción completa si es menor a 100 caracteres
     */
    public String getDescripcionCorta() {
        if (descripcion == null || descripcion.length() <= 100) {
            return descripcion;
        }
        return descripcion.substring(0, 97) + "...";
    }

    /**
     * Obtiene el ID formateado con ceros a la izquierda para visualizazión
     *
     * @return ID formateado como "#001", "#042", etc.
     */
    public String getIdFormateado() {
        return String.format("#%03d", id);
    }

    @Override
    public String toString() {
        return "Incidencia{"
                + "id=" + id
                + ", titulo='" + titulo + '\''
                + ", categoria=" + categoria
                + ", prioridad=" + prioridad
                + ", estado=" + estado
                + ", cliente='" + nombreCliente + '\''
                + ", tecnico='" + getTecnicoOSinAsignar() + '\''
                + '}';
    }
}
