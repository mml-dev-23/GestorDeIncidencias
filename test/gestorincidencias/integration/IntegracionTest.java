package gestorincidencias.integration;

import gestorincidencias.modelo.*;
import gestorincidencias.util.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Prueba de integración completa del sistema GestorDeIncidencias
 * 
 * Simula un flujo real completo:
 * 1. Login de cliente y creación de incidencia
 * 2. Login de técnico y asignación de incidencia  
 * 3. Actualización de estado y resolución
 * 4. Login de admin y generación de estadísticas
 * 5. Verificación de todo el ciclo de vida
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class IntegracionTest {
    
    private GestorUsuarios gestorUsuarios;
    private GestorIncidencias gestorIncidencias;
    private SesionUsuario sesion;
    
    private Usuario cliente;
    private Usuario tecnico;
    private Usuario admin;
    private Incidencia incidenciaIntegracion;
    
    @Before
    public void setUp() {
        // Inicializar gestores
        gestorUsuarios = GestorUsuarios.getInstance();
        gestorIncidencias = GestorIncidencias.getInstance();
        sesion = SesionUsuario.getInstance();
        
        // Limpiar sesión
        sesion.cerrarSesion();
        
        // Crear usuarios para el flujo completo
        crearUsuariosParaIntegracion();
        
        System.out.println("=== INICIANDO PRUEBA DE INTEGRACIÓN ===");
    }
    
    @After
    public void tearDown() {
        System.out.println("=== LIMPIANDO PRUEBA DE INTEGRACIÓN ===");
        
        // Limpiar incidencia
        if (incidenciaIntegracion != null && incidenciaIntegracion.getId() > 0) {
            try {
                gestorIncidencias.eliminar(incidenciaIntegracion.getId());
                System.out.println("Incidencia de integración eliminada");
            } catch (Exception e) {
                System.out.println("Error limpiando incidencia: " + e.getMessage());
            }
        }
        
        // Limpiar usuarios
        limpiarUsuarios();
        
        // Cerrar sesión
        sesion.cerrarSesion();
        
        System.out.println("=== PRUEBA DE INTEGRACIÓN COMPLETADA ===");
    }
    
    /**
     * PRUEBA DE INTEGRACIÓN COMPLETA
     * Simula el flujo completo de una incidencia desde creación hasta resolución
     */
    @Test
    public void testFlujoCompletoIncidencia() {
        System.out.println("\n INICIANDO FLUJO COMPLETO DE INCIDENCIA");
        
        // ============================================
        // FASE 1: CLIENTE CREA INCIDENCIA
        // ============================================
        System.out.println("\n FASE 1: Cliente crea nueva incidencia");
        
        // Login como cliente
        sesion.iniciarSesion(cliente);
        assertTrue("Cliente debe estar logueado", sesion.esCliente());
        assertEquals("Sesión debe ser del cliente", cliente.getId(), sesion.getIdUsuarioActual());
        System.out.println("Cliente logueado: " + sesion.getInfoSesion());
        
        // Crear incidencia
        incidenciaIntegracion = new Incidencia(
            "Integración - Error en aplicación " + System.currentTimeMillis(),
            "Error crítico detectado durante las pruebas de integración. Necesita atención inmediata del equipo técnico.",
            Categoria.SOFTWARE,
            Prioridad.ALTA,
            cliente.getId(),
            cliente.getNombreCompleto()
        );
        
        Incidencia incidenciaCreada = gestorIncidencias.crear(incidenciaIntegracion);
        assertNotNull("Incidencia debe ser creada", incidenciaCreada);
        assertTrue("Incidencia debe tener ID válido", incidenciaCreada.getId() > 0);
        assertEquals("Estado inicial debe ser PENDIENTE", Estado.PENDIENTE, incidenciaCreada.getEstado());
        assertNull("No debe tener técnico asignado inicialmente", incidenciaCreada.getIdTecnicoAsignado());
        
        incidenciaIntegracion = incidenciaCreada; // Actualizar referencia
        System.out.println("Incidencia creada: " + incidenciaCreada.getIdFormateado() + " - " + incidenciaCreada.getTitulo());
        
        // Verificar que aparece en lista de incidencias del cliente
        List<Incidencia> incidenciasCliente = gestorIncidencias.obtenerPorCliente(cliente.getId());
        boolean incidenciaEncontradaCliente = incidenciasCliente.stream()
            .anyMatch(inc -> inc.getId() == incidenciaCreada.getId());
        assertTrue("Incidencia debe aparecer en lista del cliente", incidenciaEncontradaCliente);
        
        sesion.cerrarSesion();
        System.out.println("Cliente ha cerrado sesión");
        
        // ============================================
        // FASE 2: TÉCNICO ASUME LA INCIDENCIA
        // ============================================
        System.out.println("\n FASE 2: Técnico toma la incidencia");
        
        // Login como técnico
        sesion.iniciarSesion(tecnico);
        assertTrue("Técnico debe estar logueado", sesion.esTecnico());
        System.out.println("Técnico logueado: " + sesion.getInfoSesion());
        
        // Asignar incidencia al técnico
        incidenciaIntegracion.setIdTecnicoAsignado(tecnico.getId());
        incidenciaIntegracion.setNombreTecnico(tecnico.getNombreCompleto());
        incidenciaIntegracion.setEstado(Estado.EN_PROCESO);
        
        boolean actualizada = gestorIncidencias.actualizar(incidenciaIntegracion);
        assertTrue("Incidencia debe actualizarse correctamente", actualizada);
        System.out.println("Incidencia asignada al técnico y puesta en proceso");
        
        // Verificar que aparece en lista del técnico
        List<Incidencia> incidenciasTecnico = gestorIncidencias.obtenerPorTecnico(tecnico.getId());
        boolean incidenciaEncontradaTecnico = incidenciasTecnico.stream()
            .anyMatch(inc -> inc.getId() == incidenciaIntegracion.getId());
        assertTrue("Incidencia debe aparecer en lista del técnico", incidenciaEncontradaTecnico);
        
        // Verificar estado actualizado en BD
        Incidencia incidenciaActualizada = gestorIncidencias.obtenerPorId(incidenciaIntegracion.getId());
        assertEquals("Estado debe ser EN_PROCESO", Estado.EN_PROCESO, incidenciaActualizada.getEstado());
        assertEquals("Técnico debe estar asignado", tecnico.getId(), (int)incidenciaActualizada.getIdTecnicoAsignado());
        
        sesion.cerrarSesion();
        System.out.println("Técnico ha cerrado sesión");
        
        // ============================================
        // FASE 3: TÉCNICO RESUELVE LA INCIDENCIA
        // ============================================
        System.out.println("\n FASE 3: Técnico resuelve la incidencia");
        
        // Login como técnico nuevamente
        sesion.iniciarSesion(tecnico);
        
        // Marcar como resuelta
        incidenciaIntegracion.setEstado(Estado.RESUELTA);
        actualizada = gestorIncidencias.actualizar(incidenciaIntegracion);
        assertTrue("Incidencia debe marcarse como resuelta", actualizada);
        
        // Verificar que se estableció fecha de resolución
        Incidencia incidenciaResuelta = gestorIncidencias.obtenerPorId(incidenciaIntegracion.getId());
        assertEquals("Estado debe ser RESUELTA", Estado.RESUELTA, incidenciaResuelta.getEstado());
        assertNotNull("Debe tener fecha de resolución", incidenciaResuelta.getFechaResolucion());
        assertTrue("Debe estar marcada como finalizada", incidenciaResuelta.estaFinalizada());
        
        System.out.println("Incidencia resuelta correctamente");
        System.out.println("Fecha de resolución: " + incidenciaResuelta.getFechaResolucion());
        
        sesion.cerrarSesion();
        
        // ============================================
        // FASE 4: ADMINISTRADOR REVISA ESTADÍSTICAS
        // ============================================
        System.out.println("\n FASE 4: Administrador revisa estadísticas");
        
        // Login como administrador
        sesion.iniciarSesion(admin);
        assertTrue("Admin debe estar logueado", sesion.esAdministrador());
        System.out.println("Administrador logueado: " + sesion.getInfoSesion());
        
        // Generar estadísticas
        int totalIncidencias = gestorIncidencias.obtenerTodas().size();
        int incidenciasPendientes = gestorIncidencias.contarPorEstado(Estado.PENDIENTE);
        int incidenciasEnProceso = gestorIncidencias.contarPorEstado(Estado.EN_PROCESO);
        int incidenciasResueltas = gestorIncidencias.contarPorEstado(Estado.RESUELTA);
        
        assertTrue("Debe haber al menos 1 incidencia total", totalIncidencias >= 1);
        assertTrue("Debe haber al menos 1 incidencia resuelta", incidenciasResueltas >= 1);
        
        System.out.println("Estadísticas generadas:");
        System.out.println("  - Total incidencias: " + totalIncidencias);
        System.out.println("  - Pendientes: " + incidenciasPendientes);
        System.out.println("  - En proceso: " + incidenciasEnProceso);
        System.out.println("  - Resueltas: " + incidenciasResueltas);
        
        // Buscar nuestra incidencia específica
        List<Incidencia> resultadoBusqueda = gestorIncidencias.buscar("Integración");
        boolean incidenciaEncontradaBusqueda = resultadoBusqueda.stream()
            .anyMatch(inc -> inc.getId() == incidenciaIntegracion.getId());
        assertTrue("Incidencia debe encontrarse en búsqueda", incidenciaEncontradaBusqueda);
        
        sesion.cerrarSesion();
        System.out.println("Administrador ha cerrado sesión");
        
        // ============================================
        // FASE 5: VERIFICACIÓN FINAL COMPLETA
        // ============================================
        System.out.println("\nFASE 5: Verificación final del ciclo completo");
        
        // Verificar estado final sin sesión activa
        assertFalse("No debe haber sesión activa", sesion.haySesionActiva());
        
        // Verificar que la incidencia mantiene todos los cambios
        Incidencia incidenciaFinal = gestorIncidencias.obtenerPorId(incidenciaIntegracion.getId());
        assertNotNull("Incidencia debe existir", incidenciaFinal);
        assertEquals("Estado final debe ser RESUELTA", Estado.RESUELTA, incidenciaFinal.getEstado());
        assertEquals("Cliente debe mantenerse", cliente.getId(), incidenciaFinal.getIdCliente());
        assertEquals("Técnico debe mantenerse", tecnico.getId(), (int)incidenciaFinal.getIdTecnicoAsignado());
        assertNotNull("Debe mantener fecha de creación", incidenciaFinal.getFechaCreacion());
        assertNotNull("Debe mantener fecha de resolución", incidenciaFinal.getFechaResolucion());
        
        
        // Verificar que el ciclo de vida fue correcto
        assertNotNull("Debe tener fecha de resolución", incidenciaFinal.getFechaResolucion());
        
        System.out.println("Verificación final completada");
        System.out.println("Ciclo de vida completo: PENDIENTE → EN_PROCESO → RESUELTA");
        System.out.println("Todos los componentes funcionaron correctamente integrados");
        
        // ============================================
        // RESUMEN FINAL
        // ============================================
        System.out.println("\nRESUMEN DE INTEGRACIÓN:");
        System.out.println("Gestión de usuarios: CORRECTA");
        System.out.println("Gestión de sesiones: CORRECTA");  
        System.out.println("Gestión de incidencias: CORRECTA");
        System.out.println("Flujo de estados: CORRECTA");
        System.out.println("Asignación de técnicos: CORRECTA");
        System.out.println("Búsquedas y filtros: CORRECTA");
        System.out.println("Estadísticas: CORRECTA");
        System.out.println("Persistencia de datos: CORRECTA");
        
        assertTrue("INTEGRACIÓN COMPLETA EXITOSA", true);
    }
    
    /**
     * Crea todos los usuarios necesarios para la prueba de integración
     */
    private void crearUsuariosParaIntegracion() {
        // Cliente
        cliente = new Usuario();
        cliente.setNombre("ClienteIntegracion");
        cliente.setApellidos("Prueba Completa");
        cliente.setEmail("cliente.integracion." + System.currentTimeMillis() + "@prueba.com");
        cliente.setPassword("password123");
        cliente.setRol(Rol.CLIENTE);
        cliente = gestorUsuarios.crear(cliente);
        System.out.println("Cliente creado: " + cliente.getNombreCompleto());
        
        // Técnico
        tecnico = new Usuario();
        tecnico.setNombre("TecnicoIntegracion");
        tecnico.setApellidos("Prueba Completa");
        tecnico.setEmail("tecnico.integracion." + System.currentTimeMillis() + "@prueba.com");
        tecnico.setPassword("password123");
        tecnico.setRol(Rol.TECNICO);
        tecnico = gestorUsuarios.crear(tecnico);
        System.out.println("Técnico creado: " + tecnico.getNombreCompleto());
        
        // Administrador
        admin = new Usuario();
        admin.setNombre("AdminIntegracion");
        admin.setApellidos("Prueba Completa");
        admin.setEmail("admin.integracion." + System.currentTimeMillis() + "@prueba.com");
        admin.setPassword("password123");
        admin.setRol(Rol.ADMINISTRADOR);
        admin = gestorUsuarios.crear(admin);
        System.out.println("Administrador creado: " + admin.getNombreCompleto());
    }
    
    /**
     * Limpia todos los usuarios creados para la prueba
     */
    private void limpiarUsuarios() {
        if (cliente != null && cliente.getId() > 0) {
            try {
                gestorUsuarios.eliminar(cliente.getId());
                System.out.println("Cliente eliminado");
            } catch (Exception e) {
                System.out.println("Error eliminando cliente: " + e.getMessage());
            }
        }
        
        if (tecnico != null && tecnico.getId() > 0) {
            try {
                gestorUsuarios.eliminar(tecnico.getId());
                System.out.println("Técnico eliminado");
            } catch (Exception e) {
                System.out.println("Error eliminando técnico: " + e.getMessage());
            }
        }
        
        if (admin != null && admin.getId() > 0) {
            try {
                gestorUsuarios.eliminar(admin.getId());
                System.out.println("Administrador eliminado");
            } catch (Exception e) {
                System.out.println("Error eliminando admin: " + e.getMessage());
            }
        }
    }
}