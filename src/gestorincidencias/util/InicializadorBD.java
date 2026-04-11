package gestorincidencias.util;

import java.sql.Connection;
import java.sql.DriverManager; 
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;

/**
 * Clase encargada de inicializar la base de datos automáticamente.
 * 
 * Funcionalidades:
 * - Verificar conexión a base de datos existente
 * - Crear todas las tablas necesarias si no existen
 * - Insertar datos iniciales (usuario admin, categorías, etc.)
 * - Verificar integridad de la estructura
 * 
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class InicializadorBD {
    
    private static final String NOMBRE_BD = "gestor_incidencias";
    
    /**
     * Inicializa completamente la base de datos
     * 
     * @return true si la inicialización fue exitosa
     */
    public boolean inicializar() {
        try {
            System.out.println("   Verificando acceso a base de datos...");
            
            // FASE 1: crear la BD si no existe (conexión sin BD)
            if (!crearBaseDeDatos()) {
                return false;
            }
            
            // 2. Crear tablas si no existen  
            if (!crearTablas()) {
                return false;
            }
            
            // 3. Insertar datos iniciales si están vacías
            if (!insertarDatosIniciales()) {
                return false;
            }
            
            System.out.println("   Inicialización de BD completada exitosamente");
            return true;
            
        } catch (Exception e) {
            System.err.println("   Error en inicialización de BD: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    /**
     * Abre una conexión a MySQL sin seleccionar ninguna base de datos
     * y ejecuta CREATE DATABASE IF NOT EXISTS.
     */
    private boolean crearBaseDeDatos() {
 
        // Leemos host/puerto/usuario/password de ConexionBD para no duplicar config
        String url      = ConexionBD.getUrlSinBaseDatos();  
        String usuario  = ConexionBD.getUsuario();
        String password = ConexionBD.getPassword();
 
        System.out.println("      Conectando a MySQL (sin BD) para verificar gestor_incidencias...");
 
        try (Connection conn = DriverManager.getConnection(url, usuario, password);
             Statement  stmt = conn.createStatement()) {
 
            stmt.execute(
                "CREATE DATABASE IF NOT EXISTS " + NOMBRE_BD +
                " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            );
 
            System.out.println("      Base de datos '" + NOMBRE_BD + "' verificada/creada.");
            return true;
 
        } catch (SQLException e) {
            System.err.println("      No se pudo crear la base de datos: " + e.getMessage());
            System.err.println("      Comprueba que:");
            System.err.println("         - MySQL está en ejecución");
            System.err.println("         - Las credenciales en ConexionBD.java son correctas");
            return false;
        }
    }
    
    /**
     * Crea todas las tablas necesarias ejecutando el script Crear_BD.sql
     */
    private boolean crearTablas() {
    System.out.println("      Ejecutando script de creación de tablas...");

    File scriptFile = new File("Crear_BD.sql");
    if (!scriptFile.exists()) {
        System.err.println("      No se encontró Crear_BD.sql junto al JAR.");
        return false;
    }

    try (Connection conn = ConexionBD.getConexion();
         Statement stmt = conn.createStatement();
         BufferedReader reader = new BufferedReader(
                 new FileReader(scriptFile, java.nio.charset.StandardCharsets.UTF_8))) {

        StringBuilder sentenciaActual = new StringBuilder();
        String linea;

        while ((linea = reader.readLine()) != null) {
            String lineaTrim = linea.trim();

            // Ignorar comentarios y líneas vacías
            if (lineaTrim.isEmpty() 
                    || lineaTrim.startsWith("--") 
                    || lineaTrim.startsWith("#")) {
                continue;
            }

            sentenciaActual.append(linea).append(" ");

            if (lineaTrim.endsWith(";")) {
                String sql = sentenciaActual.toString().trim();
                sql = sql.substring(0, sql.lastIndexOf(';')).trim();

                if (!sql.isEmpty()) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        if (e.getErrorCode() != 1050) {
                            System.err.println("      Error en sentencia: " + e.getMessage());
                        }
                    }
                }
                sentenciaActual.setLength(0); 
            }
        }

        System.out.println("      Tablas verificadas correctamente.");
        return true;

    } catch (IOException | SQLException e) {
        System.err.println("      Error leyendo Crear_BD.sql: " + e.getMessage());
        return false;
    }
}
    
    private boolean insertarDatosIniciales() {
        // Comprobar si ya hay datos
        try (Connection conn = ConexionBD.getConexion();
             Statement  stmt = conn.createStatement()) {
 
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM USUARIO");
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("      Datos iniciales ya existen. Sin cambios.");
                return true;
            }
 
        } catch (SQLException e) {
            System.err.println("      Error verificando datos iniciales: " + e.getMessage());
            return false;
        }
 
        // Ejecutar el script de datos
        System.out.println("      Ejecutando script de datos iniciales...");
 
        File scriptFile = new File("InsertarDatos_permisos.sql");
        if (!scriptFile.exists()) {
            System.err.println("      No se encontró InsertarDatos_permisos.sql junto al JAR.");
            return false;
        }
 
        try (Connection conn = ConexionBD.getConexion();
             Statement  stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(
                     new FileReader(scriptFile, java.nio.charset.StandardCharsets.UTF_8))) {
 
            StringBuilder sentenciaActual = new StringBuilder();
            String linea;
 
            while ((linea = reader.readLine()) != null) {
                String lineaTrim = linea.trim();
 
                if (lineaTrim.isEmpty()
                        || lineaTrim.startsWith("--")
                        || lineaTrim.startsWith("#")) {
                    continue;
                }
 
                sentenciaActual.append(linea).append(" ");
 
                if (lineaTrim.endsWith(";")) {
                    String sql = sentenciaActual.toString().trim();
                    sql = sql.substring(0, sql.lastIndexOf(';')).trim();
 
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            // Ignorar duplicados (1062) y errores no críticos
                            if (e.getErrorCode() != 1062) {
                                System.err.println("      Aviso en inserción: " + e.getMessage());
                            }
                        }
                    }
                    sentenciaActual.setLength(0);
                }
            }
 
            System.out.println("      Datos iniciales cargados correctamente.");
            return true;
 
        } catch (IOException e) {
            System.err.println("      Error leyendo InsertarDatos_permisos.sql: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("      Error de conexión insertando datos: " + e.getMessage());
            return false;
        }
    }
}