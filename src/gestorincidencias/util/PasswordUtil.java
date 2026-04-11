package gestorincidencias.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para el manejo seguro de contraseñas en el sistema.
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class PasswordUtil {
    
    /**
     * Salt fijo usado para mantener compatibilidad con datos iniciales.
     */
    private static final String SALT = "salt_gestor";
    
    /**
     * Genera un hash de la contraseña usando SHA-256 con salt fijo.
     * 
     * @param password Contraseña en texto plano
     * @return Hash SHA-256 en hexadecimal
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + SALT;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes("UTF-8"));
            
            // Convertir a hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar contraseña", e);
        }
    }
    
    /**
     * Verifica si una contraseña coincide con el hash almacenado.
     * 
     * @param password Contraseña en texto plano
     * @param storedHash Hash almacenado en la base de datos
     * @return true si coinciden
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String hashedInput = hashPassword(password);
            return hashedInput.equals(storedHash);
        } catch (Exception e) {
            System.err.println("Error verificando contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si una cadena parece ser un hash.
     * 
     * @param password Cadena a verificar
     * @return true si parece hash
     */
    public static boolean isPasswordHashed(String password) {
        // Un hash SHA-256 en hex tiene exactamente 64 caracteres
        return password != null && password.length() == 64 && password.matches("[a-fA-F0-9]+");
    }
}