package gestorincidencias.util;
 
import gestorincidencias.modelo.*;
import gestorincidencias.dao.IncidenciaDAO;
import java.util.List;
import java.util.stream.Collectors;
 
/**
 * Gestor de incidencias del sistema - Patrón Singleton.
 * 
 * <p>Esta clase actúa como capa de lógica de negocio para incidencias,
 * proporcionando operaciones CRUD y funcionalidades avanzadas como búsquedas,
 * filtros por técnico/cliente y contadores estadísticos.</p>
 * 
 * <p>Utiliza streams de Java 8 para operaciones de filtrado eficientes
 * y mantiene separación clara entre lógica de negocio y acceso a datos.</p>
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class GestorIncidencias {
    
    /**
     * Instancia única del gestor (patrón Singleton).
     */
    private static GestorIncidencias instancia;
    
    /**
     * DAO para operaciones de base de datos de incidencias.
     */
    private IncidenciaDAO incidenciaDAO;
    
    /**
     * Constructor privado para implementar patrón Singleton. Inicializa el DAO
     * de incidencias y establece conexión con la base de datos.
     */
    private GestorIncidencias() {
        this.incidenciaDAO = new IncidenciaDAO();
        System.out.println("GestorIncidencias inicializado con conexión a BD");
    }
    
    /**
     * Obtiene la instancia única del gestor de incidencias. Crea la instancia
     * si no existe.
     *
     * @return Instancia única de GestorIncidencias
     */
    public static GestorIncidencias getInstance() {
        if (instancia == null) {
            instancia = new GestorIncidencias();
        }
        return instancia;
    }
    
    /**
     * Obtiene todas las incidencias del sistema con información completa.
     * Incluye datos relacionados de clientes y técnicos.
     *
     * @return Lista completa de incidencias ordenadas por fecha de creación
     */
    public List<Incidencia> obtenerTodas() {
        return incidenciaDAO.obtenerTodas();
    }

    /**
     * Busca una incidencia específica por su ID único.
     *
     * @param id Identificador único de la incidencia a buscar
     * @return Incidencia encontrada con datos completos o null si no existe
     */
    public Incidencia obtenerPorId(int id) {
        return incidenciaDAO.obtenerPorId(id);
    }

    /**
     * Crea una nueva incidencia en el sistema. Establece automáticamente fechas
     * de creación y actualización.
     *
     * @param incidencia Objeto Incidencia con los datos a insertar
     * @return Incidencia creada con ID asignado por la base de datos
     * @throws IllegalArgumentException si la incidencia es null o datos
     * inválidos
     * @throws RuntimeException si ocurre un error durante la creación en BD
     */
    public Incidencia crear(Incidencia incidencia) {
        if (incidencia == null) {
            throw new IllegalArgumentException("La incidencia no puede ser null");
        }

        if (incidencia.getTitulo() == null || incidencia.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la incidencia es obligatorio");
        }

        if (incidenciaDAO.crear(incidencia)) {
            return incidencia;
        } else {
            throw new RuntimeException("Error al crear la incidencia en la base de datos");
        }
    }
    
    /**
     * Actualiza una incidencia existente en el sistema. Actualiza
     * automáticamente la fecha de última modificación.
     *
     * @param incidencia Incidencia con datos actualizados (debe tener ID
     * válido)
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean actualizar(Incidencia incidencia) {
        return incidenciaDAO.actualizar(incidencia);
    }
    
    /**
     * Elimina físicamente una incidencia del sistema.
     *
     * @param id ID de la incidencia a eliminar
     * @return true si la eliminación fue exitosa, false si falló o no se
     * encontró
     */
    public boolean eliminar(int id) {
        return incidenciaDAO.eliminar(id);
    }
    
    // =========================================================================
    // MÉTODOS ADICIONALES NECESARIOS PARA PANELINCIDENCIAS
    // =========================================================================
    
    /**
     * Cuenta el número de incidencias que tienen un estado específico. Útil
     * para estadísticas y dashboards.
     *
     * @param estado Estado a contar (PENDIENTE, EN_PROCESO, RESUELTA, CERRADA)
     * @return Número de incidencias con el estado especificado
     */
    public int contarPorEstado(Estado estado) {
        List<Incidencia> todas = obtenerTodas();
        return (int) todas.stream()
                .filter(inc -> inc.getEstado() == estado)
                .count();
    }

    /**
     * Busca incidencias por texto en título o descripción. Realiza búsqueda
     * parcial en ambos campos.
     *
     * @param texto Texto a buscar en título y descripción
     * @return Lista de incidencias que contienen el texto especificado, lista
     * vacía si texto null/vacío
     */
    public List<Incidencia> buscar(String texto) {
        // Validar entrada
        if (texto == null || texto.trim().isEmpty()) {
            System.out.println("Texto de búsqueda vacío, retornando todas las incidencias");
            return obtenerTodas();
        }

        List<Incidencia> todas = obtenerTodas();
        String textoBusqueda = texto.toLowerCase().trim();

        return todas.stream()
                .filter(inc -> {
                    // Protección contra nulls en título y descripción
                    String titulo = inc.getTitulo() != null ? inc.getTitulo().toLowerCase() : "";
                    String descripcion = inc.getDescripcion() != null ? inc.getDescripcion().toLowerCase() : "";
                    return titulo.contains(textoBusqueda) || descripcion.contains(textoBusqueda);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las incidencias asignadas a un técnico específico. Incluye
     * incidencias en cualquier estado (pendientes, en proceso, resueltas).
     *
     * @param idTecnico ID del técnico (debe ser > 0)
     * @return Lista de incidencias asignadas al técnico especificado
     * @throws IllegalArgumentException si idTecnico es inválido
     */
    public List<Incidencia> obtenerPorTecnico(int idTecnico) {
        if (idTecnico <= 0) {
            throw new IllegalArgumentException("ID de técnico debe ser mayor a 0");
        }

        List<Incidencia> todas = obtenerTodas();

        return todas.stream()
                .filter(inc -> inc.getIdTecnicoAsignado() != null
                && inc.getIdTecnicoAsignado() == idTecnico)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las incidencias que no tienen técnico asignado. Útil para
     * mostrar incidencias disponibles para asignación.
     *
     * @return Lista de incidencias sin técnico asignado
     */
    public List<Incidencia> obtenerSinAsignar() {
        List<Incidencia> todas = obtenerTodas();
        
        return todas.stream()
                .filter(inc -> inc.getIdTecnicoAsignado() == null || 
                              inc.getIdTecnicoAsignado() == 0)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene incidencias filtradas por cliente específico. Útil para mostrar
     * solo las incidencias de un usuario cliente.
     *
     * @param idCliente ID del cliente del cual obtener sus incidencias
     * @return Lista de incidencias del cliente especificado
     */
    public List<Incidencia> obtenerPorCliente(int idCliente) {
        if (idCliente <= 0) {
            throw new IllegalArgumentException("ID de cliente debe ser mayor a 0");
        }

        List<Incidencia> todas = obtenerTodas();

        return todas.stream()
                .filter(inc -> inc.getIdCliente() == idCliente)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene lista de nombres completos de técnicos disponibles en el sistema.
     * Utiliza el gestor de usuarios para obtener usuarios con rol TECNICO.
     *
     * @return Lista de nombres completos de técnicos activos
     */
    public List<String> obtenerTecnicos() {
        GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
        List<Usuario> tecnicos = gestorUsuarios.obtenerPorRol(Rol.TECNICO);
        
        return tecnicos.stream()
                .map(Usuario::getNombreCompleto)
                .collect(Collectors.toList());
    }
}