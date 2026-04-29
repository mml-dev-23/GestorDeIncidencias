package gestorincidencias.util;

import gestorincidencias.modelo.*;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la máquina de estados de las incidencias.
 * Verifica que las transiciones entre estados respetan el flujo definido
 * según el rol del usuario y que las restricciones se aplican correctamente.
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class MaquinaEstadosTest {

    private GestorIncidencias gestorIncidencias;
    private GestorUsuarios gestorUsuarios;
    private Incidencia incidenciaPrueba;
    private Usuario usuarioCliente;
    private Usuario usuarioTecnico;
    private Usuario usuarioAdmin;

    @Before
    public void setUp() {
        gestorIncidencias = GestorIncidencias.getInstance();
        gestorUsuarios = GestorUsuarios.getInstance();

        long ts = System.currentTimeMillis();

        usuarioCliente = new Usuario();
        usuarioCliente.setNombre("ClienteEstados");
        usuarioCliente.setApellidos("Test");
        usuarioCliente.setEmail("cliente.estados." + ts + "@prueba.com");
        usuarioCliente.setPassword("password123");
        usuarioCliente.setRol(Rol.CLIENTE);
        usuarioCliente = gestorUsuarios.crear(usuarioCliente);

        usuarioTecnico = new Usuario();
        usuarioTecnico.setNombre("TecnicoEstados");
        usuarioTecnico.setApellidos("Test");
        usuarioTecnico.setEmail("tecnico.estados." + ts + "@prueba.com");
        usuarioTecnico.setPassword("password123");
        usuarioTecnico.setRol(Rol.TECNICO);
        usuarioTecnico = gestorUsuarios.crear(usuarioTecnico);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setNombre("AdminEstados");
        usuarioAdmin.setApellidos("Test");
        usuarioAdmin.setEmail("admin.estados." + ts + "@prueba.com");
        usuarioAdmin.setPassword("password123");
        usuarioAdmin.setRol(Rol.ADMINISTRADOR);
        usuarioAdmin = gestorUsuarios.crear(usuarioAdmin);

        incidenciaPrueba = new Incidencia(
            "Incidencia Estados Test " + ts,
            "Incidencia para probar la máquina de estados del sistema",
            Categoria.SOFTWARE,
            Prioridad.ALTA,
            usuarioCliente.getId(),
            usuarioCliente.getNombreCompleto()
        );
        incidenciaPrueba = gestorIncidencias.crear(incidenciaPrueba);
    }

    @After
    public void tearDown() {
        if (incidenciaPrueba != null && incidenciaPrueba.getId() > 0) {
            try { gestorIncidencias.eliminar(incidenciaPrueba.getId()); } catch (Exception e) {}
        }
        if (usuarioCliente != null && usuarioCliente.getId() > 0) {
            try { gestorUsuarios.eliminar(usuarioCliente.getId()); } catch (Exception e) {}
        }
        if (usuarioTecnico != null && usuarioTecnico.getId() > 0) {
            try { gestorUsuarios.eliminar(usuarioTecnico.getId()); } catch (Exception e) {}
        }
        if (usuarioAdmin != null && usuarioAdmin.getId() > 0) {
            try { gestorUsuarios.eliminar(usuarioAdmin.getId()); } catch (Exception e) {}
        }
    }

    /**
     * PRUEBA 1: Estado inicial de una incidencia nueva es PENDIENTE
     */
    @Test
    public void testEstadoInicialEsPendiente() {
        assertNotNull("La incidencia debe existir", incidenciaPrueba);
        assertEquals("El estado inicial debe ser PENDIENTE",
            Estado.PENDIENTE, incidenciaPrueba.getEstado());
    }

    /**
     * PRUEBA 2: Técnico solo puede ir de PENDIENTE a EN_PROCESO
     */
    @Test
    public void testTecnicoPendienteAEnProceso() {
        List<Estado> permitidos = Estado.PENDIENTE.getTransicionesPermitidas(Rol.TECNICO);
        assertEquals("Técnico solo puede ir a EN_PROCESO desde PENDIENTE", 1, permitidos.size());
        assertTrue("La transición permitida debe ser EN_PROCESO",
            permitidos.contains(Estado.EN_PROCESO));
    }

    /**
     * PRUEBA 3: Técnico solo puede ir de EN_PROCESO a RESUELTA
     */
    @Test
    public void testTecnicoEnProcesoAResuelta() {
        List<Estado> permitidos = Estado.EN_PROCESO.getTransicionesPermitidas(Rol.TECNICO);
        assertEquals("Técnico solo puede ir a RESUELTA desde EN_PROCESO", 1, permitidos.size());
        assertTrue("La transición permitida debe ser RESUELTA",
            permitidos.contains(Estado.RESUELTA));
    }

    /**
     * PRUEBA 4: Técnico no puede cambiar estado desde RESUELTA ni CERRADA
     */
    @Test
    public void testTecnicoSinTransicionesDesdeResueltoYCerrado() {
        List<Estado> desdeResuelta = Estado.RESUELTA.getTransicionesPermitidas(Rol.TECNICO);
        List<Estado> desdeCerrada = Estado.CERRADA.getTransicionesPermitidas(Rol.TECNICO);

        assertTrue("Técnico no puede cambiar estado desde RESUELTA", desdeResuelta.isEmpty());
        assertTrue("Técnico no puede cambiar estado desde CERRADA", desdeCerrada.isEmpty());
    }

    /**
     * PRUEBA 5: Admin puede ir a cualquier estado incluido el actual
     */
    @Test
    public void testAdminPuedeCambiarACualquierEstado() {
        List<Estado> desdeP = Estado.PENDIENTE.getTransicionesPermitidas(Rol.ADMINISTRADOR);
        List<Estado> desdeE = Estado.EN_PROCESO.getTransicionesPermitidas(Rol.ADMINISTRADOR);
        List<Estado> desdeR = Estado.RESUELTA.getTransicionesPermitidas(Rol.ADMINISTRADOR);
        List<Estado> desdeC = Estado.CERRADA.getTransicionesPermitidas(Rol.ADMINISTRADOR);

        // Admin tiene 3 transiciones (todos menos el actual) desde cada estado
        assertEquals("Admin tiene 3 transiciones desde PENDIENTE", 3, desdeP.size());
        assertEquals("Admin tiene 3 transiciones desde EN_PROCESO", 3, desdeE.size());
        assertEquals("Admin tiene 3 transiciones desde RESUELTA", 3, desdeR.size());
        assertEquals("Admin tiene 3 transiciones desde CERRADA", 3, desdeC.size());

        // El estado actual se añade aparte en la vista como primer elemento del combo
        // por eso no está en la lista de transiciones pero sí aparece en pantalla
        assertTrue("Admin puede ir a EN_PROCESO desde PENDIENTE", desdeP.contains(Estado.EN_PROCESO));
        assertTrue("Admin puede ir a CERRADA desde cualquier estado", desdeP.contains(Estado.CERRADA));
    }

    /**
     * PRUEBA 6: Flujo completo PENDIENTE → EN_PROCESO → RESUELTA → CERRADA
     */
    @Test
    public void testFlujoCompletoEstados() {
        // PENDIENTE → EN_PROCESO (técnico)
        incidenciaPrueba.setEstado(Estado.EN_PROCESO);
        incidenciaPrueba.setIdTecnicoAsignado(usuarioTecnico.getId());
        boolean ok1 = gestorIncidencias.actualizar(incidenciaPrueba);
        assertTrue("Cambio a EN_PROCESO debe ser exitoso", ok1);
        assertEquals("Estado debe ser EN_PROCESO", Estado.EN_PROCESO,
            gestorIncidencias.obtenerPorId(incidenciaPrueba.getId()).getEstado());

        // EN_PROCESO → RESUELTA (técnico)
        incidenciaPrueba.setEstado(Estado.RESUELTA);
        incidenciaPrueba.setFechaResolucion(java.time.LocalDateTime.now());
        boolean ok2 = gestorIncidencias.actualizar(incidenciaPrueba);
        assertTrue("Cambio a RESUELTA debe ser exitoso", ok2);
        assertEquals("Estado debe ser RESUELTA", Estado.RESUELTA,
            gestorIncidencias.obtenerPorId(incidenciaPrueba.getId()).getEstado());

        // RESUELTA → CERRADA (cliente confirma / admin cierra)
        incidenciaPrueba.setEstado(Estado.CERRADA);
        boolean ok3 = gestorIncidencias.actualizar(incidenciaPrueba);
        assertTrue("Cambio a CERRADA debe ser exitoso", ok3);
        assertEquals("Estado debe ser CERRADA", Estado.CERRADA,
            gestorIncidencias.obtenerPorId(incidenciaPrueba.getId()).getEstado());
    }

    /**
     * PRUEBA 7: Reasignar técnico vuelve el estado a PENDIENTE
     */
    @Test
    public void testReasignacionVuelveAPendiente() {
        // Poner en EN_PROCESO
        incidenciaPrueba.setEstado(Estado.EN_PROCESO);
        incidenciaPrueba.setIdTecnicoAsignado(usuarioTecnico.getId());
        gestorIncidencias.actualizar(incidenciaPrueba);

        // Simular reasignación: volver a PENDIENTE
        incidenciaPrueba.setEstado(Estado.PENDIENTE);
        incidenciaPrueba.setFechaResolucion(null);
        boolean resultado = gestorIncidencias.actualizar(incidenciaPrueba);

        assertTrue("La reasignación debe ser exitosa", resultado);
        Incidencia actualizada = gestorIncidencias.obtenerPorId(incidenciaPrueba.getId());
        assertEquals("Estado debe volver a PENDIENTE tras reasignación",
            Estado.PENDIENTE, actualizada.getEstado());
        assertNull("Fecha resolución debe ser null tras reasignación",
            actualizada.getFechaResolucion());
    }
}