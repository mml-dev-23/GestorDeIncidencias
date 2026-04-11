package gestorincidencias.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Sistema de ayuda en línea para la aplicación Gestor de Incidencias.
 * Proporciona información contextual y guías de uso para cada módulo.
 * 
 * @author martamorales
 * @version 1.0
 */
public class SistemaAyuda {
    
    /**
     * Muestra la ayuda general de la aplicación
     */
    public static void mostrarAyudaGeneral() {
        String contenido = """
            <html><body style='width: 500px; font-family: Arial, sans-serif;'>
            <h2>Gestor de Incidencias - Ayuda General</h2>
            
            <h3>¿Qué es esta aplicación?</h3>
            <p>Sistema de gestión de incidencias técnicas que permite:</p>
            <ul>
                <li><b>Reportar incidencias</b> (clientes)</li>
                <li><b>Asignar y resolver</b> problemas (técnicos)</li>
                <li><b>Administrar usuarios</b> y generar reportes (administradores)</li>
            </ul>
            
            <h3>Roles de Usuario:</h3>
            <ul>
                <li><b>Administrador:</b> Acceso completo al sistema</li>
                <li><b>Técnico:</b> Gestiona incidencias asignadas</li>
                <li><b>Cliente:</b> Reporta y hace seguimiento de incidencias</li>
            </ul>
            
            <h3>Navegación:</h3>
            <p>Utiliza los botones del menú lateral para acceder a cada módulo.</p>
            
            <p><i>Para ayuda específica de cada módulo, usa el botón de ayuda (?) 
            en cada pantalla.</i></p>
            </body></html>
            """;
        
        mostrarDialogoAyuda("Ayuda General", contenido);
    }
    
    /**
     * Muestra ayuda específica para el panel de incidencias
     */
    public static void mostrarAyudaIncidencias() {

        String pendiente = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/estado/pendiente.png"
        ).toExternalForm();

        String enProceso = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/estado/enproceso.png"
        ).toExternalForm();

        String resuelta = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/estado/resuelta.png"
        ).toExternalForm();

        String cerrada = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/estado/cerrada.png"
        ).toExternalForm();

        String alta = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/prioridad/alta.png"
        ).toExternalForm();

        String media = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/prioridad/media.png"
        ).toExternalForm();

        String baja = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/prioridad/baja.png"
        ).toExternalForm();

        String hardware = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/categoria/hardware.png"
        ).toExternalForm();

        String software = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/categoria/software.png"
        ).toExternalForm();

        String red = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/categoria/red.png"
        ).toExternalForm();

        String seguridad = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/categoria/seguridad.png"
        ).toExternalForm();

        String otro = SistemaAyuda.class.getResource(
                "/gestorincidencias/recursos/iconos/categoria/otro.png"
        ).toExternalForm();

        String contenido = """
        <html><body style='width: 500px; font-family: Arial, sans-serif;'>
        <h2>Panel de Incidencias - Ayuda</h2>
        
        <h3>Funciones Principales:</h3>
        <ul>
            <li><b>Crear incidencia:</b> Botón "Nueva Incidencia"</li>
            <li><b>Editar incidencia:</b> Seleccionar la incidencia y en el panel detalle clic en botón editar</li>
            <li><b>Eliminar incidencia:</b> Seleccionar incidencia, botón editar y botón eliminar</li>
            <li><b>Filtrar:</b> Usa los combos superiores</li>
            <li><b>Buscar:</b> Campo de búsqueda por título/descripción</li>
        </ul>
        
        <h3>Estados de Incidencia:</h3>
        <ul>
            <li><img src='%s' width='14'> <b>Pendiente:</b> Recién creada</li>
            <li><img src='%s' width='14'> <b>En Proceso:</b> Asignada a técnico</li>
            <li><img src='%s' width='14'> <b>Resuelta:</b> Problema solucionado</li>
            <li><img src='%s' width='14'> <b>Cerrada:</b> Confirmada por cliente</li>
        </ul>
        
        <h3>Código de Prioridades:</h3>
        <ul>
            <li><img src='%s' width='14'> <b>Alta</b></li>
            <li><img src='%s' width='14'> <b>Media</b></li>
            <li><img src='%s' width='14'> <b>Baja</b></li>
        </ul>
                           
        <h3>Categorías:</h3>
        <ul>
            <li><img src='%s' width='14'> <b>Hardware:</b> Problemas físicos (equipos, periféricos)</li>
            <li><img src='%s' width='14'> <b>Software:</b> Aplicaciones, sistemas operativos</li>
            <li><img src='%s' width='14'> <b>Red:</b> Conectividad, internet, servidores</li>
            <li><img src='%s' width='14'> <b>Seguridad:</b> Accesos, permisos, incidencias críticas</li>
            <li><img src='%s' width='14'> <b>Otros:</b> Casos no clasificados</li>
        </ul>                   
        
        <h3>Consejos:</h3>
        <ul>
            <li>Usa filtros para encontrar incidencias específicas</li>
            <li>Los técnicos solo ven sus incidencias asignadas</li>
            <li>Los clientes solo ven sus propias incidencias</li>
        </ul>
        </body></html>
        """.formatted(
                pendiente, enProceso, resuelta, cerrada,
                alta, media, baja,
                hardware, software, red, seguridad, otro
        );

        mostrarDialogoAyuda("Ayuda - Incidencias", contenido);
    }
    
    /**
     * Muestra ayuda específica para el panel de usuarios
     */
    public static void mostrarAyudaUsuarios() {
        String contenido = """
            <html><body style='width: 500px; font-family: Arial, sans-serif;'>
            <h2>Panel de Usuarios - Ayuda</h2>
            
            <h3>Funciones Principales:</h3>
            <ul>
                <li><b>Crear usuario:</b> Botón "Nuevo Usuario"</li>
                <li><b>Editar usuario:</b> Botón "Editar"</li>
                <li><b>Eliminar usuario:</b> Botón "Eliminar"</li>
                <li><b>Filtrar por rol:</b> Usa el combo superior</li>
            </ul>
            
            <h3>Roles Disponibles:</h3>
            <ul>
                <li><b>Administrador:</b> Gestión completa del sistema</li>
                <li><b>Técnico:</b> Resuelve incidencias asignadas</li>
                <li><b>Cliente:</b> Reporta incidencias</li>
            </ul>
            
            <h3>Validaciones:</h3>
            <ul>
                <li>Email debe ser único en el sistema</li>
                <li>Contraseña mínimo 6 caracteres</li>
                <li>Todos los campos son obligatorios</li>
            </ul>
            
            <h3>Consejos:</h3>
            <ul>
                <li>Solo administradores pueden gestionar usuarios</li>
                <li>Las contraseñas se encriptan automáticamente</li>
                <li>Los usuarios eliminados se marcan como inactivos</li>
            </ul>
            </body></html>
            """;
        
        mostrarDialogoAyuda("Ayuda - Usuarios", contenido);
    }
    
    /**
     * Muestra ayuda específica para el panel de reportes
     */
    public static void mostrarAyudaReportes() {
        String contenido = """
            <html><body style='width: 500px; font-family: Arial, sans-serif;'>
            <h2>Panel de Reportes - Ayuda</h2>
            
            <h3>Funciones Principales:</h3>
            <ul>
                <li><b>Filtrar datos:</b> Usa los combos superiores</li>
                <li><b>Rango de fechas:</b> Selecciona período específico</li>
                <li><b>Exportar PDF:</b> Genera reporte con filtros aplicados</li>
                <li><b>Limpiar filtros:</b> Restablece vista completa</li>
            </ul>
            
            <h3>Filtros Disponibles:</h3>
            <ul>
                <li><b>Prioridad:</b> Alta, Media, Baja</li>
                <li><b>Empleado:</b> Por técnico asignado</li>
                <li><b>Categoría:</b> Software, Hardware, Red, etc.</li>
                <li><b>Estado:</b> Pendiente, En Proceso, Resuelta, Cerrada</li>
                <li><b>Cliente:</b> Por usuario que reportó</li>
                <li><b>Fechas:</b> Rango personalizable</li>
            </ul>
            
            <h3>Exportación PDF:</h3>
            <ul>
                <li>El PDF incluye solo incidencias filtradas</li>
                <li>Se genera un título dinámico según filtros</li>
                <li>Incluye estadísticas del período</li>
            </ul>
            
            <h3>Consejos:</h3>
            <ul>
                <li>Combina filtros para reportes específicos</li>
                <li>Usa "Sin asignar" para incidencias pendientes de asignación</li>
                <li>Los rangos de fecha son inclusivos</li>
            </ul>
            </body></html>
            """;
        
        mostrarDialogoAyuda("Ayuda - Reportes", contenido);
    }
    
    /**
     * Muestra ayuda específica para el panel de perfil
     */
    public static void mostrarAyudaPerfil() {
        String contenido = """
            <html><body style='width: 500px; font-family: Arial, sans-serif;'>
            <h2>Mi Perfil - Ayuda</h2>
            
            <h3>Funciones Principales:</h3>
            <ul>
                <li><b>Actualizar datos:</b> Email (otros campos no editables)</li>
                <li><b>Cambiar contraseña:</b> Proceso seguro con validación</li>
                <li><b>Gestionar notas:</b> Notas personales</li>
                <li><b>Ver estadísticas:</b> Datos según tu rol</li>
            </ul>
            
            <h3>🔒 Cambio de Contraseña:</h3>
            <ul>
                <li>Requiere contraseña actual</li>
                <li>Mínimo 6 caracteres para nueva contraseña</li>
                <li>Confirmación de nueva contraseña</li>
                <li>Encriptación automática</li>
            </ul>
            
            <h3>Estadísticas por Rol:</h3>
            <ul>
                <li><b>Cliente:</b> Incidencias creadas, pendientes, resueltas</li>
                <li><b>Técnico:</b> Incidencias asignadas, tiempo promedio</li>
                <li><b>Admin:</b> Estadísticas globales del sistema</li>
            </ul>
            
            <h3>Notas Personales:</h3>
            <ul>
                <li>Espacio libre para anotaciones</li>
                <li>Se guarda automáticamente la fecha de modificación</li>
                <li>Visible solo para ti</li>
            </ul>
            </body></html>
            """;
        
        mostrarDialogoAyuda("Ayuda - Mi Perfil", contenido);
    }
    
    /**
     * Muestra información "Acerca de" la aplicación
     */
    public static void mostrarAcercaDe() {
        String contenido = """
            <html><body style='width: 400px; font-family: Arial, sans-serif; text-align: center;'>
            <h2>Gestor de Incidencias</h2>
            <p><b>Versión 1.0</b></p>
            
            <h3>Desarrollado por:</h3>
            <p>Marta Morales Luna<br>
            Proyecto Final - DAM<br>
            2025-2026</p>
            
            <h3>Tecnologías Utilizadas:</h3>
            <ul style='text-align: left;'>
                <li>Java 17+ con Swing</li>
                <li>MySQL 8.0</li>
                <li>FlatLaf (Look & Feel moderno)</li>
                <li>iText (Generación PDF)</li>
                <li>Arquitectura MVC</li>
            </ul>
            
            <h3>Contacto:</h3>
            <p>Para soporte técnico o consultas sobre<br>
            el proyecto, contacta con el desarrollador.</p>
            
            <hr>
            <p><i>© 2025-2026 - Todos los derechos reservados</i></p>
            </body></html>
            """;
        
        mostrarDialogoAyuda("Acerca de - Gestor de Incidencias", contenido);
    }
    
    /**
     * Muestra un diálogo de ayuda con contenido HTML
     */
    private static void mostrarDialogoAyuda(String titulo, String contenidoHTML) {
        JDialog dialogo = new JDialog((Frame) null, titulo, true);
        dialogo.setLayout(new BorderLayout());
        
        // Panel de contenido con HTML
        JEditorPane editorPane = new JEditorPane("text/html", contenidoHTML);
        editorPane.setEditable(false);
        editorPane.setBackground(Color.WHITE);
        editorPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(550, 400));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialogo.dispose());
        panelBotones.add(btnCerrar);
        
        dialogo.add(scrollPane, BorderLayout.CENTER);
        dialogo.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar diálogo
        dialogo.setSize(580, 450);
        dialogo.setLocationRelativeTo(null);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Esc para cerrar
        dialogo.getRootPane().registerKeyboardAction(
            e -> dialogo.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialogo.setVisible(true);
    }
}
