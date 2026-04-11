package gestorincidencias.util;

import gestorincidencias.modelo.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase SesionUsuario
 * Incluye validación del patrón Singleton, gestión de sesiones,
 * y verificación de roles y permisos
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class SesionUsuarioTest {
    
    private SesionUsuario sesion;
    private GestorUsuarios gestorUsuarios;
    private Usuario usuarioAdmin;
    private Usuario usuarioTecnico;
    private Usuario usuarioCliente;
    
    @Before
    public void setUp() {
        sesion = SesionUsuario.getInstance();
        gestorUsuarios = GestorUsuarios.getInstance();
        
        // Limpiar sesión antes de cada prueba
        sesion.cerrarSesion();
        
        // Crear usuarios de prueba para diferentes roles
        usuarioAdmin = new Usuario();
        usuarioAdmin.setNombre("AdminTest");
        usuarioAdmin.setApellidos("Prueba Admin");
        usuarioAdmin.setEmail("admin.test." + System.currentTimeMillis() + "@prueba.com");
        usuarioAdmin.setPassword("password123");
        usuarioAdmin.setRol(Rol.ADMINISTRADOR);
        usuarioAdmin = gestorUsuarios.crear(usuarioAdmin);
        
        usuarioTecnico = new Usuario();
        usuarioTecnico.setNombre("TecnicoTest");
        usuarioTecnico.setApellidos("Prueba Tecnico");
        usuarioTecnico.setEmail("tecnico.test." + System.currentTimeMillis() + "@prueba.com");
        usuarioTecnico.setPassword("password123");
        usuarioTecnico.setRol(Rol.TECNICO);
        usuarioTecnico = gestorUsuarios.crear(usuarioTecnico);
        
        usuarioCliente = new Usuario();
        usuarioCliente.setNombre("ClienteTest");
        usuarioCliente.setApellidos("Prueba Cliente");
        usuarioCliente.setEmail("cliente.test." + System.currentTimeMillis() + "@prueba.com");
        usuarioCliente.setPassword("password123");
        usuarioCliente.setRol(Rol.CLIENTE);
        usuarioCliente = gestorUsuarios.crear(usuarioCliente);
    }
    
    @After
    public void tearDown() {
        // Cerrar sesión y limpiar usuarios de prueba
        sesion.cerrarSesion();
        
        if (usuarioAdmin != null && usuarioAdmin.getId() > 0) {
            try {
                gestorUsuarios.eliminar(usuarioAdmin.getId());
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
        
        if (usuarioCliente != null && usuarioCliente.getId() > 0) {
            try {
                gestorUsuarios.eliminar(usuarioCliente.getId());
            } catch (Exception e) {
                // Ignorar errores de limpieza
            }
        }
    }

    /**
     * PRUEBA 1: Verificar que getInstance devuelve la misma instancia (Singleton)
     */
    @Test
    public void testGetInstance() {
        
        SesionUsuario instancia1 = SesionUsuario.getInstance();
        SesionUsuario instancia2 = SesionUsuario.getInstance();
        
        assertNotNull("La instancia no debe ser null", instancia1);
        assertSame("Debe ser la misma instancia (Singleton)", instancia1, instancia2);
        assertSame("La sesion debe ser la misma instancia", sesion, instancia1);
    }

    /**
     * PRUEBA 2: Verificar inicio de sesión con usuario administrador
     */
    @Test
    public void testIniciarSesionAdministrador() {
        
        sesion.iniciarSesion(usuarioAdmin);
        
        assertTrue("Debe haber sesión activa", sesion.haySesionActiva());
        assertNotNull("Usuario actual no debe ser null", sesion.getUsuarioActual());
        assertEquals("El usuario actual debe coincidir", usuarioAdmin.getId(), sesion.getUsuarioActual().getId());
        assertEquals("El rol debe ser ADMINISTRADOR", Rol.ADMINISTRADOR, sesion.getRolActual());
        assertEquals("El ID debe coincidir", usuarioAdmin.getId(), sesion.getIdUsuarioActual());
        
        // Verificar métodos de rol específicos
        assertTrue("Debe ser administrador", sesion.esAdministrador());
        assertFalse("No debe ser técnico", sesion.esTecnico());
        assertFalse("No debe ser cliente", sesion.esCliente());
    }

    /**
     * PRUEBA 3: Verificar inicio de sesión con usuario técnico
     */
    @Test
    public void testIniciarSesionTecnico() {
        
        sesion.iniciarSesion(usuarioTecnico);
        
        assertTrue("Debe haber sesión activa", sesion.haySesionActiva());
        assertEquals("El rol debe ser TECNICO", Rol.TECNICO, sesion.getRolActual());
        
        // Verificar métodos de rol específicos
        assertFalse("No debe ser administrador", sesion.esAdministrador());
        assertTrue("Debe ser técnico", sesion.esTecnico());
        assertFalse("No debe ser cliente", sesion.esCliente());
    }

    /**
     * PRUEBA 4: Verificar inicio de sesión con usuario cliente
     */
    @Test
    public void testIniciarSesionCliente() {
        
        sesion.iniciarSesion(usuarioCliente);
        
        assertTrue("Debe haber sesión activa", sesion.haySesionActiva());
        assertEquals("El rol debe ser CLIENTE", Rol.CLIENTE, sesion.getRolActual());
        
        // Verificar métodos de rol específicos
        assertFalse("No debe ser administrador", sesion.esAdministrador());
        assertFalse("No debe ser técnico", sesion.esTecnico());
        assertTrue("Debe ser cliente", sesion.esCliente());
    }

    /**
     * PRUEBA 5: Verificar cierre de sesión
     */
    @Test
    public void testCerrarSesion() {
        
        sesion.iniciarSesion(usuarioAdmin);
        assertTrue("Debe haber sesión activa inicialmente", sesion.haySesionActiva());
        
        sesion.cerrarSesion();
        
        assertFalse("No debe haber sesión activa", sesion.haySesionActiva());
        assertNull("Usuario actual debe ser null", sesion.getUsuarioActual());
        assertNull("Rol actual debe ser null", sesion.getRolActual());
        assertEquals("ID usuario debe ser -1", -1, sesion.getIdUsuarioActual());
        
        // Verificar métodos de rol después del cierre
        assertFalse("No debe ser administrador", sesion.esAdministrador());
        assertFalse("No debe ser técnico", sesion.esTecnico());
        assertFalse("No debe ser cliente", sesion.esCliente());
    }

    /**
     * PRUEBA 6: Verificar estado sin sesión activa
     */
    @Test
    public void testEstadoSinSesion() {
        
        assertFalse("No debe haber sesión activa", sesion.haySesionActiva());
        assertNull("Usuario actual debe ser null", sesion.getUsuarioActual());
        assertNull("Rol actual debe ser null", sesion.getRolActual());
        assertEquals("ID usuario debe ser -1", -1, sesion.getIdUsuarioActual());
        
        // Verificar métodos de rol sin sesión
        assertFalse("No debe ser administrador", sesion.esAdministrador());
        assertFalse("No debe ser técnico", sesion.esTecnico());
        assertFalse("No debe ser cliente", sesion.esCliente());
        
        // Verificar información de sesión
        String infoSesion = sesion.getInfoSesion();
        assertEquals("Debe indicar que no hay sesión activa", "No hay sesión activa", infoSesion);
    }

    /**
     * PRUEBA 7: Verificar información de sesión con usuario logueado
     */
    @Test
    public void testGetInfoSesion() {
        
        sesion.iniciarSesion(usuarioAdmin);
        String infoSesion = sesion.getInfoSesion();
        
        assertNotNull("Info sesión no debe ser null", infoSesion);
        assertTrue("Debe contener el nombre del usuario", 
                  infoSesion.contains(usuarioAdmin.getNombreCompleto()));
        assertTrue("Debe contener información del rol", 
                  infoSesion.toUpperCase().contains("ADMINISTRADOR"));
        assertTrue("Debe contener el ID del usuario", 
                  infoSesion.contains(String.valueOf(usuarioAdmin.getId())));
    }

    /**
     * PRUEBA 8: Verificar cambio de sesión entre usuarios
     */
    @Test
    public void testCambiarSesion() {
        //Iniciar sesión con admin
        sesion.iniciarSesion(usuarioAdmin);
        assertEquals("Debe estar logueado como admin", Rol.ADMINISTRADOR, sesion.getRolActual());
        
        //Cambiar a técnico
        sesion.iniciarSesion(usuarioTecnico);
        
        assertEquals("Debe estar logueado como técnico", Rol.TECNICO, sesion.getRolActual());
        assertEquals("El usuario debe ser el técnico", usuarioTecnico.getId(), sesion.getIdUsuarioActual());
        assertTrue("Debe ser técnico", sesion.esTecnico());
        assertFalse("Ya no debe ser administrador", sesion.esAdministrador());
    }
}