package gestorincidencias.util;

import gestorincidencias.modelo.Rol;
import gestorincidencias.modelo.Usuario;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para GestorUsuarios 
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class GestorUsuariosTest {

    private GestorUsuarios gestor;
    private Usuario usuarioPrueba;

    @Before
    public void setUp() {
        gestor = GestorUsuarios.getInstance();

        // Crear usuario de prueba
        usuarioPrueba = new Usuario();
        usuarioPrueba.setNombre("TestUser");
        usuarioPrueba.setApellidos("Prueba Unitaria");
        usuarioPrueba.setEmail("test." + System.currentTimeMillis() + "@prueba.com");
        usuarioPrueba.setPassword("password123");
        usuarioPrueba.setRol(Rol.CLIENTE);
    }

    @After
    public void tearDown() {
        // Limpiar usuario de prueba
        if (usuarioPrueba != null && usuarioPrueba.getId() > 0) {
            try {
                gestor.eliminar(usuarioPrueba.getId());
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
        GestorUsuarios instancia1 = GestorUsuarios.getInstance();
        GestorUsuarios instancia2 = GestorUsuarios.getInstance();

        assertNotNull("La instancia no debe ser null", instancia1);
        assertSame("Debe ser la misma instancia (Singleton)", instancia1, instancia2);
    }

    /**
     * PRUEBA 2: Crear usuario correctamente
     */
    @Test
    public void testCrearUsuario() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);

        assertNotNull("El usuario creado no debe ser null", usuarioCreado);
        assertNotNull("El usuario debe tener un ID asignado", usuarioCreado.getId());
        assertTrue("El ID debe ser positivo", usuarioCreado.getId() > 0);
        assertEquals("El nombre debe coincidir", "TestUser", usuarioCreado.getNombre());
    }

    /**
     * PRUEBA 3: Obtener usuario por email
     */
    @Test
    public void testObtenerPorEmail() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);

        Usuario encontrado = gestor.obtenerPorEmail(usuarioCreado.getEmail());

        assertNotNull("Debe encontrar el usuario", encontrado);
        assertEquals("Los emails deben coincidir", usuarioCreado.getEmail(), encontrado.getEmail());
    }

    /**
     * PRUEBA 4: Email inexistente
     */
    @Test
    public void testObtenerPorEmailInexistente() {
        Usuario resultado = gestor.obtenerPorEmail("noexiste@test.com");
        assertNull("Debe devolver null para email inexistente", resultado);
    }

    /**
     * PRUEBA 5: Autenticación correcta
     */
    @Test
    public void testAutenticarCredencialesCorrectas() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);

        Usuario autenticado = gestor.autenticar(usuarioCreado.getEmail(), "password123");

        assertNotNull("La autenticación debe ser exitosa", autenticado);
        assertEquals("Los emails deben coincidir", usuarioCreado.getEmail(), autenticado.getEmail());
    }

    /**
     * PRUEBA 6: Autenticación incorrecta
     */
    @Test
    public void testAutenticarPasswordIncorrecta() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);

        Usuario resultado = gestor.autenticar(usuarioCreado.getEmail(), "passwordIncorrecta");

        assertNull("La autenticación debe fallar", resultado);
    }

    /**
     * PRUEBA 7: Actualizar usuario
     */
    @Test
    public void testActualizarUsuario() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);
        usuarioCreado.setNombre("NombreActualizado");

        boolean resultado = gestor.actualizar(usuarioCreado);

        assertTrue("La actualización debe ser exitosa", resultado);
    }

    /**
     * PRUEBA 8: Eliminar usuario correctamente
     */
    @Test
    public void testEliminarUsuario() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);
        int idUsuario = usuarioCreado.getId();

        boolean resultado = gestor.eliminar(idUsuario);
        Usuario usuarioEliminado = gestor.obtenerPorId(idUsuario);

        assertTrue("La eliminación debe ser exitosa", resultado);
        assertNull("El usuario eliminado no debe encontrarse", usuarioEliminado);

        // Evitar doble eliminación en tearDown
        usuarioPrueba = null;
    }

    /**
     * PRUEBA 9: Obtener usuarios por rol específico
     */
    @Test
    public void testObtenerPorRol() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);

        List<Usuario> clientesEncontrados = gestor.obtenerPorRol(Rol.CLIENTE);

        assertNotNull("La lista no debe ser null", clientesEncontrados);
        assertFalse("Debe encontrar al menos un cliente", clientesEncontrados.isEmpty());

        // Verificar que nuestro usuario de prueba está en la lista
        boolean encontrado = false;
        for (Usuario usuario : clientesEncontrados) {
            assertEquals("Todos deben tener rol CLIENTE", Rol.CLIENTE, usuario.getRol());
            if (usuario.getId() == usuarioCreado.getId()) {
                encontrado = true;
            }
        }
        assertTrue("El usuario de prueba debe estar en la lista", encontrado);
    }

    /**
     * PRUEBA 10: Verificar existencia de email
     */
    @Test
    public void testExisteEmail() {
        Usuario usuarioCreado = gestor.crear(usuarioPrueba);
        String emailExistente = usuarioCreado.getEmail();
        String emailInexistente = "no.existe." + System.currentTimeMillis() + "@test.com";

        boolean existe = gestor.existeEmail(emailExistente);
        boolean noExiste = gestor.existeEmail(emailInexistente);

        assertTrue("Debe encontrar email existente", existe);
        assertFalse("No debe encontrar email inexistente", noExiste);
    }

}
