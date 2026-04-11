package gestorincidencias.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import gestorincidencias.vista.Login;
import gestorincidencias.util.SesionUsuario;
import gestorincidencias.util.ConexionBD;
import gestorincidencias.util.InicializadorBD;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * Clase principal de la aplicación Gestor de Incidencias.
 *
 * Responsabilidades: - Configurar el tema visual (FlatLaf) - Inicializar la
 * conexión a base de datos - Crear BD y datos iniciales si no existen - Mostrar
 * ventana de Login
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class Main {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(Main.class.getName());

    private static final boolean USAR_TEMA_OSCURO = false;

    public static void main(String[] args) {
        System.out.println("=== INICIANDO GESTOR DE INCIDENCIAS ===");

        // PASO 1: Configurar el Look and Feel
        configurarTemaVisual();

        // PASO 2: Configurar propiedades del sistema
        configurarSistema();

        // PASO 3: Inicializar base de datos antes de mostrar login
        if (!inicializarBaseDatos()) {
            // Si falla la BD, no continuar
            mostrarErrorYSalir("No se pudo inicializar la base de datos. Verifique la configuración.");
            return;
        }

        // PASO 4: Iniciar la aplicación 
        SwingUtilities.invokeLater(() -> {
            iniciarAplicacion();
        });
    }

    /**
     * Inicializa la base de datos al arrancar la aplicación
     *
     * @return true si la inicialización fue exitosa, false si falló
     */
    private static boolean inicializarBaseDatos() {
        System.out.println("\nINICIALIZANDO BASE DE DATOS...");

        try {
            // PASO 1: Forzar carga de configuración (genera config.properties si no existe)
            System.out.println("1. Verificando configuración...");
            String infoConfig = ConexionBD.obtenerInfoConfiguracion();
            System.out.println("   " + infoConfig);

            // PASO 2: Inicializar estructura de BD y datos
            // (InicializadorBD verifica la conexión internamente en su primera fase)
            System.out.println("2. Inicializando estructura y datos...");
            InicializadorBD inicializador = new InicializadorBD();
            boolean inicializacionOK = inicializador.inicializar();

            if (!inicializacionOK) {
                System.err.println("   Error inicializando base de datos.");
                return false;
            }

            System.out.println("   Base de datos lista para usar.");
            System.out.println("\nINICIALIZACIÓN DE BD COMPLETADA\n");
            return true;

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO EN INICIALIZACIÓN DE BD:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Configura el tema visual de la aplicación (FlatLaf)
     */
    private static void configurarTemaVisual() {
        System.out.println("Configurando tema visual...");

        try {
            // Intentar usar FlatLaf
            if (USAR_TEMA_OSCURO) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                logger.info("Tema oscuro FlatLaf aplicado");
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                logger.info("Tema claro FlatLaf aplicado");
            }

            // Personalizaciones globales de FlatLaf
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("TabbedPane.showTabSeparators", true);

            System.out.println("Tema visual configurado");

        } catch (Exception ex) {
            // Si FlatLaf no está disponible, intentar usar Nimbus
            logger.warning("FlatLaf no disponible, intentando Nimbus...");
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        logger.info("Tema Nimbus aplicado");
                        break;
                    }
                }
            } catch (Exception e) {
                // Como último recurso, usar el Look and Feel del sistema
                logger.severe("Error al configurar tema visual, usando tema del sistema");
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex2) {
                    logger.severe("No se pudo aplicar ningún tema: " + ex2.getMessage());
                }
            }
        }
    }

    /**
     * Configura propiedades del sistema para mejorar el renderizado
     */
    private static void configurarSistema() {
        System.out.println("Configurando sistema...");

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        System.setProperty("sun.java2d.uiScale", "1.0");

        System.setProperty("sun.java2d.opengl", "true");

        logger.info("Propiedades del sistema configuradas");
        System.out.println("Propiedades del sistema configuradas");
    }

    /**
     * Inicia la aplicación mostrando la ventana de Login
     */
    private static void iniciarAplicacion() {
        System.out.println("Iniciando interfaz de usuario...");

        try {
            // Verificar si hay sesión activa 
            gestorincidencias.util.SesionUsuario sesion = gestorincidencias.util.SesionUsuario.getInstance();

            if (sesion.haySesionActiva()) {
                logger.info("Ya hay una sesión activa: " + sesion.getInfoSesion());
            } else {
                logger.info("No hay sesión activa. Mostrando ventana de Login...");
            }

            Login ventanaLogin = new Login();
            ventanaLogin.setVisible(true);

            logger.info("Aplicación iniciada correctamente - Ventana de Login mostrada");
            System.out.println("Ventana de Login mostrada");
            System.out.println("\nAPLICACIÓN LISTA PARA USAR\n");

        } catch (Exception ex) {
            logger.severe("Error crítico al iniciar la aplicación: " + ex.getMessage());
            ex.printStackTrace();
            mostrarErrorYSalir("Error al iniciar la interfaz: " + ex.getMessage());
        }
    }

    /**
     * Muestra un error crítico al usuario y cierra la aplicación
     *
     * @param mensaje Mensaje de error a mostrar
     */
    private static void mostrarErrorYSalir(String mensaje) {
        System.err.println("ERROR CRÍTICO: " + mensaje);

        // Mostrar mensaje de error al usuario
        JOptionPane.showMessageDialog(null,
                mensaje + "\n\nLa aplicación se cerrará.",
                "Error Crítico - Gestor de Incidencias",
                JOptionPane.ERROR_MESSAGE);

        // Cerrar la aplicación
        System.exit(1);
    }
}
