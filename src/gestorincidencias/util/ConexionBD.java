package gestorincidencias.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.*;

/**
 * Gestión de conexiones a la base de datos MySQL.
 * Utiliza configuración externa desde archivo config.properties.
 * 
 * @author martamorales
 * @version 1.0
 */
public class ConexionBD {
    
    private static Properties config;
    private static String urlConexion;
    private static String usuario;
    private static String password;
    
    static {
        cargarConfiguracion();
    }
    
    /**
     * Carga la configuración de base de datos desde archivo externo
     */
    private static void cargarConfiguracion() {
        config = new Properties();
        
        try {
            // Buscar config.properties junto al JAR
            File configFile = new File("config.properties");
            
            if (configFile.exists()) {
                // Cargar configuración existente
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    config.load(fis);
                    System.out.println("Configuración cargada desde config.properties");
                }
            } else {
                // Crear configuración por defecto
                crearConfiguracionPorDefecto();
                System.out.println("Archivo config.properties creado con valores por defecto");
            }
            
            // Construir URL de conexión
            construirParametrosConexion();
            
        } catch (IOException e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
            // Usar valores por defecto en memoria
            establecerValoresPorDefecto();
        }
    }
    
    /**
     * Construye los parámetros de conexión a partir de la configuración
     */
    private static void construirParametrosConexion() {
        String host = config.getProperty("db.host", "localhost");
        String puerto = config.getProperty("db.puerto", "3306");
        String nombreBD = config.getProperty("db.nombre", "gestor_incidencias");
        
        urlConexion = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC", 
                                   host, puerto, nombreBD);
        usuario = config.getProperty("db.usuario", "root");
        password = config.getProperty("db.password", "");
        
        System.out.println("Conexión configurada: " + usuario + "@" + host + ":" + puerto + "/" + nombreBD);
    }
    
    /**
     * Crea archivo de configuración con valores por defecto
     */
    private static void crearConfiguracionPorDefecto() {
        // Establecer valores por defecto
        config.setProperty("db.host", "localhost");
        config.setProperty("db.puerto", "3306");
        config.setProperty("db.nombre", "gestor_incidencias");
        config.setProperty("db.usuario", "root");
        config.setProperty("db.password", "");
        
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            config.store(fos, 
                "Configuración de Base de Datos - Gestor de Incidencias\n" +
                "# Modifica estos valores según tu configuración de MySQL\n" +
                "# La contraseña se puede dejar vacía si MySQL no tiene contraseña");
            
        } catch (IOException e) {
            System.err.println("Error creando archivo de configuración: " + e.getMessage());
        }
    }
    
    /**
     * Establece valores por defecto en memoria si falla la carga de archivo
     */
    private static void establecerValoresPorDefecto() {
        urlConexion = "jdbc:mysql://localhost:3306/gestor_incidencias?useSSL=false&serverTimezone=UTC";
        usuario = "root";
        password = "";
        System.out.println("Usando configuración por defecto en memoria");
    }
    
    /**
     * Obtiene una conexión a la base de datos
     * 
     * @return Conexión activa a MySQL
     * @throws SQLException si no se puede establecer la conexión
     */
    public static Connection getConexion() throws SQLException {
        try {
            // Verificar que el driver de MySQL esté disponible
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Crear conexión
            Connection conn = DriverManager.getConnection(urlConexion, usuario, password);
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de conexión a BD: " + e.getMessage());
            System.err.println("   Verificar config.properties y que MySQL esté ejecutándose");
            throw e;
        }
    }
    
    /**
     * Prueba la conexión a la base de datos
     * 
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public static boolean probarConexion() {
        try (Connection conn = getConexion()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Obtiene información de la configuración actual
     * 
     * @return String con datos de configuración (sin contraseña)
     */
    public static String obtenerInfoConfiguracion() {
        return String.format("Host: %s | Puerto: %s | BD: %s | Usuario: %s",
            config.getProperty("db.host", "localhost"),
            config.getProperty("db.puerto", "3306"), 
            config.getProperty("db.nombre", "gestor_incidencias"),
            config.getProperty("db.usuario", "root")
        );
    }
    
    /**
     * Permite recargar la configuración sin reiniciar la aplicación
     */
    public static void recargarConfiguracion() {
        cargarConfiguracion();
        System.out.println("Configuración recargada");
    }
 
    /**
     * URL de conexión sin nombre de base de datos. Necesaria para crear la BD
     * si aún no existe.
     */
    public static String getUrlSinBaseDatos() {
        String host = config.getProperty("db.host", "localhost");
        String puerto = config.getProperty("db.puerto", "3306");
        return "jdbc:mysql://" + host + ":" + puerto
                + "/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    /**
     * Expone el usuario configurado (necesario para conectar sin BD).
     */
    public static String getUsuario() {
        return usuario;
    }

    /**
     * Expone la contraseña configurada (necesario para conectar sin BD).
     */
    public static String getPassword() {
        return password;
    }
}
