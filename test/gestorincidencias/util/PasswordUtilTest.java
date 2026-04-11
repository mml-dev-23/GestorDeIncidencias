package gestorincidencias.util;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase PasswordUtil
 * Incluye validación de encriptación SHA-256, verificación de contraseñas
 * y generación de salts únicos para seguridad
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class PasswordUtilTest {
    
    private String passwordPrueba;
    private String passwordIncorrecta;
    
    @Before
    public void setUp() {
        passwordPrueba = "password123";
        passwordIncorrecta = "passwordIncorrecta";
    }

    /**
     * PRUEBA 1: Verificar que hashPassword funciona correctamente
     */
    @Test
    public void testHashPassword() {
        
        String passwordHasheada = PasswordUtil.hashPassword(passwordPrueba);
        
        assertNotNull("La contraseña hasheada no debe ser null", passwordHasheada);
        assertNotEquals("La contraseña hasheada debe ser diferente a la original", passwordPrueba, passwordHasheada);
        assertTrue("El hash debe tener longitud >= 24 caracteres", passwordHasheada.length() >= 24);
        
        // Verificar que es Base64 válido
        assertTrue("El hash debe ser detectado como contraseña hasheada", 
                  PasswordUtil.isPasswordHashed(passwordHasheada));
    }

    /**
     * PRUEBA 2: Verificar contraseña correcta debe validar como verdadera
     */
    @Test
    public void testVerifyPasswordCorrecta() {
        
        String passwordHasheada = PasswordUtil.hashPassword(passwordPrueba);
        
        boolean esValida = PasswordUtil.verifyPassword(passwordPrueba, passwordHasheada);
        
        assertTrue("La contraseña correcta debe validarse como verdadera", esValida);
    }

    /**
     * PRUEBA 3: Verificar contraseña incorrecta debe validar como falsa
     */
    @Test
    public void testVerifyPasswordIncorrecta() {
        
        String passwordHasheada = PasswordUtil.hashPassword(passwordPrueba);
        
        boolean esValida = PasswordUtil.verifyPassword(passwordIncorrecta, passwordHasheada);
        
        assertFalse("La contraseña incorrecta debe validarse como falsa", esValida);
    }

    /**
     * PRUEBA 4: Verificar que misma contraseña genera hashes diferentes (salt único)
     */
    @Test
    public void testSaltUnico() {
        
        String hash1 = PasswordUtil.hashPassword(passwordPrueba);
        String hash2 = PasswordUtil.hashPassword(passwordPrueba);
        
        
        assertNotNull("El primer hash no debe ser null", hash1);
        assertNotNull("El segundo hash no debe ser null", hash2);
        assertNotEquals("Misma contraseña debe generar hashes diferentes (salt único)", hash1, hash2);
        
        // Ambos hashes deben verificar la misma contraseña original
        assertTrue("El primer hash debe verificar la contraseña original", 
                  PasswordUtil.verifyPassword(passwordPrueba, hash1));
        assertTrue("El segundo hash debe verificar la contraseña original", 
                  PasswordUtil.verifyPassword(passwordPrueba, hash2));
    }

    /**
     * PRUEBA 5: Verificar detección de contraseñas ya hasheadas
     */
    @Test
    public void testIsPasswordHashed() {
        
        String passwordHasheada = PasswordUtil.hashPassword(passwordPrueba);
        String passwordTextoPlano = "passwordSimple";
        String cadenaCorta = "abc";
        String cadenaInvalida = "esto-no-es-base64!@#";
        
        
        assertTrue("Hash válido debe ser detectado como hasheado", 
                  PasswordUtil.isPasswordHashed(passwordHasheada));
        assertFalse("Password de texto plano no debe ser detectado como hasheado", 
                   PasswordUtil.isPasswordHashed(passwordTextoPlano));
        assertFalse("Cadena corta no debe ser detectada como hasheada", 
                   PasswordUtil.isPasswordHashed(cadenaCorta));
        assertFalse("Cadena inválida no debe ser detectada como hasheada", 
                   PasswordUtil.isPasswordHashed(cadenaInvalida));
    }

    /**
     * PRUEBA 6: Verificar manejo de hashes malformados en verify
     */
    @Test
    public void testVerifyConHashMalformado() {
        
        boolean resultado1 = PasswordUtil.verifyPassword(passwordPrueba, "hash_malformado");
        boolean resultado2 = PasswordUtil.verifyPassword(passwordPrueba, "");
        boolean resultado3 = PasswordUtil.verifyPassword(passwordPrueba, "abc123");
        
        assertFalse("Hash malformado debe devolver false", resultado1);
        assertFalse("Hash vacío debe devolver false", resultado2);
        assertFalse("Hash corto debe devolver false", resultado3);
    }

    /**
     * PRUEBA 7: Verificar seguridad del algoritmo SHA-256
     */
    @Test
    public void testSeguridadAlgoritmo() {
        
        String hash = PasswordUtil.hashPassword(passwordPrueba);
        
        assertTrue("El hash debe tener longitud de seguridad adecuada", hash.length() >= 44);
        
        // Verificar que no contiene la contraseña original
        assertFalse("El hash no debe contener la contraseña original", 
                   hash.toLowerCase().contains(passwordPrueba.toLowerCase()));
        
        // Verificar que genera hashes diferentes para passwords similares
        String hash2 = PasswordUtil.hashPassword(passwordPrueba + "X");
        assertNotEquals("Passwords similares deben generar hashes diferentes", hash, hash2);
    }
}