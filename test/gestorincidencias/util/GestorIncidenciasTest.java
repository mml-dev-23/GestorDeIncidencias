package gestorincidencias.util;

import gestorincidencias.modelo.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase GestorIncidencias
 * Incluye validación de operaciones CRUD, cambios de estado y filtrados
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class GestorIncidenciasTest {
    
    private GestorIncidencias gestorIncidencias;
    private GestorUsuarios gestorUsuarios;
    private SesionUsuario sesion;
    private Incidencia incidenciaPrueba;
    private Usuario usuarioCliente;
    private Usuario usuarioTecnico;
    
    @Before
    public void setUp() {
        gestorIncidencias = GestorIncidencias.getInstance();
        gestorUsuarios = GestorUsuarios.getInstance();
        sesion = SesionUsuario.getInstance();
        
        // Crear usuario cliente para las incidencias de prueba
        usuarioCliente = new Usuario();
        usuarioCliente.setNombre("ClienteTest");
        usuarioCliente.setApellidos("Prueba Incidencias");
        usuarioCliente.setEmail("cliente.test." + System.currentTimeMillis() + "@prueba.com");
        usuarioCliente.setPassword("password123");
        usuarioCliente.setRol(Rol.CLIENTE);
        usuarioCliente = gestorUsuarios.crear(usuarioCliente);
        
        // Crear usuario técnico para asignaciones
        usuarioTecnico = new Usuario();
        usuarioTecnico.setNombre("TecnicoTest");
        usuarioTecnico.setApellidos("Prueba Asignaciones");
        usuarioTecnico.setEmail("tecnico.test." + System.currentTimeMillis() + "@prueba.com");
        usuarioTecnico.setPassword("password123");
        usuarioTecnico.setRol(Rol.TECNICO);
        usuarioTecnico = gestorUsuarios.crear(usuarioTecnico);
        
        
        incidenciaPrueba = new Incidencia(
            "Incidencia Test " + System.currentTimeMillis(),
            "Esta es una incidencia de prueba para los tests unitarios",
            Categoria.SOFTWARE,
            Prioridad.MEDIA,
            usuarioCliente.getId(),
            usuarioCliente.getNombreCompleto()
        );
    }
    
    @After
    public void tearDown() {
        // Limpiar incidencia de prueba
        if (incidenciaPrueba != null && incidenciaPrueba.getId() > 0) {
            try {
                gestorIncidencias.eliminar(incidenciaPrueba.getId());
            } catch (Exception e) {
                // Ignorar errores de limpieza
            }
        }
        
        // Limpiar usuarios de prueba
        if (usuarioCliente != null && usuarioCliente.getId() > 0) {
            try {
                gestorUsuarios.eliminar(usuarioCliente.getId());
            } catch (Exception e) {
                // Ignorar errores de limpieza
            }
        }
        
        if (usuarioTecnico != null && usuarioTecnico.getId() > 0) {
            try {
                gestorUsuarios.eliminar(usuarioTecnico.getId());
            } catch (Exception e) {
                // Ignorar errores de limpieza
            }
        }
    }

    /**
     * PRUEBA 1: Verificar Singleton
     */
    @Test
    public void testGetInstance() {
        GestorIncidencias instancia1 = GestorIncidencias.getInstance();
        GestorIncidencias instancia2 = GestorIncidencias.getInstance();
        
        assertNotNull("La instancia no debe ser null", instancia1);
        assertSame("Debe ser la misma instancia (Singleton)", instancia1, instancia2);
    }

    /**
     * PRUEBA 2: Crear incidencia correctamente
     */
    @Test
    public void testCrearIncidencia() {
        Incidencia incidenciaCreada = gestorIncidencias.crear(incidenciaPrueba);
        
        assertNotNull("La incidencia creada no debe ser null", incidenciaCreada);
        assertTrue("El ID debe ser positivo", incidenciaCreada.getId() > 0);
        assertEquals("El título debe coincidir", incidenciaPrueba.getTitulo(), incidenciaCreada.getTitulo());
        assertEquals("La descripción debe coincidir", incidenciaPrueba.getDescripcion(), incidenciaCreada.getDescripcion());
        assertEquals("La categoría debe coincidir", Categoria.SOFTWARE, incidenciaCreada.getCategoria());
        assertEquals("La prioridad debe coincidir", Prioridad.MEDIA, incidenciaCreada.getPrioridad());
        assertEquals("El estado debe ser PENDIENTE", Estado.PENDIENTE, incidenciaCreada.getEstado());
        assertEquals("El cliente debe coincidir", usuarioCliente.getId(), incidenciaCreada.getIdCliente());
    }

    /**
     * PRUEBA 3: Actualizar estado de incidencia
     */
    @Test
    public void testActualizarEstado() {
        Incidencia incidenciaCreada = gestorIncidencias.crear(incidenciaPrueba);
        Estado nuevoEstado = Estado.EN_PROCESO;
        
        incidenciaCreada.setEstado(nuevoEstado);
        incidenciaCreada.setIdTecnicoAsignado(usuarioTecnico.getId());
        boolean resultado = gestorIncidencias.actualizar(incidenciaCreada);
        
        Incidencia incidenciaActualizada = gestorIncidencias.obtenerPorId(incidenciaCreada.getId());
        
        assertTrue("La actualización debe ser exitosa", resultado);
        assertNotNull("La incidencia actualizada debe existir", incidenciaActualizada);
        assertEquals("El estado debe estar actualizado", nuevoEstado, incidenciaActualizada.getEstado());
        assertEquals("El técnico debe coincidir", usuarioTecnico.getId(), (int)incidenciaActualizada.getIdTecnicoAsignado());
    }

    /**
     * PRUEBA 4: Obtener incidencias por técnico
     */
    @Test
    public void testObtenerPorTecnico() {
        Incidencia incidenciaCreada = gestorIncidencias.crear(incidenciaPrueba);
        incidenciaCreada.setIdTecnicoAsignado(usuarioTecnico.getId());
        gestorIncidencias.actualizar(incidenciaCreada);
        
        List<Incidencia> incidenciasDelTecnico = gestorIncidencias.obtenerPorTecnico(usuarioTecnico.getId());
        
        assertNotNull("La lista no debe ser null", incidenciasDelTecnico);
        assertFalse("Debe encontrar al menos una incidencia", incidenciasDelTecnico.isEmpty());
        
        boolean encontrada = false;
        for (Incidencia inc : incidenciasDelTecnico) {
            assertEquals("Todas deben ser del técnico", usuarioTecnico.getId(), (int)inc.getIdTecnicoAsignado());
            if (inc.getId() == incidenciaCreada.getId()) {
                encontrada = true;
            }
        }
        assertTrue("La incidencia de prueba debe estar en la lista", encontrada);
    }

    /**
     * PRUEBA 5: Obtener todas las incidencias
     */
    @Test
    public void testObtenerTodas() {
        Incidencia incidenciaCreada = gestorIncidencias.crear(incidenciaPrueba);
        
        List<Incidencia> todas = gestorIncidencias.obtenerTodas();
        
        assertNotNull("La lista no debe ser null", todas);
        assertFalse("Debe haber al menos una incidencia", todas.isEmpty());
        
        boolean encontrada = false;
        for (Incidencia inc : todas) {
            if (inc.getId() == incidenciaCreada.getId()) {
                encontrada = true;
                break;
            }
        }
        assertTrue("La incidencia de prueba debe estar en la lista", encontrada);
    }
}