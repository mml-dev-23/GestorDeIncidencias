package gestorincidencias.vista;

import gestorincidencias.util.SesionUsuario;
import gestorincidencias.modelo.Rol;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import gestorincidencias.util.SistemaAyuda;
import java.awt.Font;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Ventana principal del sistema Gestor de Incidencias.
 *
 * <p>
 * Actúa como contenedor principal de la aplicación proporcionando:</p>
 * <ul>
 * <li>Navegación lateral con menú de opciones por rol</li>
 * <li>Gestión de paneles mediante lazy loading y reutilización</li>
 * <li>Control de acceso basado en roles de usuario</li>
 * <li>Header con información de usuario y sesión</li>
 * <li>Menú de ayuda contextual integrado</li>
 * <li>Diseño responsive y efectos visuales modernos</li>
 * </ul>
 *
 * <p>
 * La ventana utiliza BorderLayout como estructura principal con menú lateral
 * fijo, header superior y área de contenido central intercambiable. Implementa
 * lazy loading para optimizar rendimiento y controla permisos por rol.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    // Colores del menú
    private final Color COLOR_MENU_NORMAL = new Color(30, 50, 70);
    private final Color COLOR_MENU_ACTIVO = new Color(50, 90, 130);
    private final Color COLOR_MENU_HOVER = new Color(40, 70, 100);

    // Botón actualmente seleccionado
    private JButton botonActivo = null;

    // CardLayout para cambiar entre paneles
    private CardLayout cardLayout;

    //Referencias a los paneles para reutilización
    private PanelIncidencias panelIncidencias;
    private PanelUsuarios panelUsuarios;
    private PanelReportes panelReportes;
    private PanelPerfil panelPerfil;

    /**
     * Inicializa la ventana principal y configura todos sus componentes.
     * Establece el diseño, carga información del usuario, configura permisos
     * por rol y muestra el panel de incidencias por defecto.
     */
    public VentanaPrincipal() {
        initComponents();
        configurarLogo();
        configurarVentana();
        cargarInformacionUsuario();
        configurarPermisosPorRol();
        configurarEstilos();
        configurarMenuAyuda();

        // Cargar panel de incidencias por defecto
        mostrarPanelIncidencias();
    }

    /**
     * Configura el logo redimensionado.
     */
    private void configurarLogo() {
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/gestorincidencias/recursos/logo.png"));
            if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(scaledImage));
                System.out.println("Logo cargado correctamente");
            } else {
                lblLogo.setText("GESTOR DE INCIDENCIAS");
                System.out.println("Logo no disponible, usando texto alternativo");
            }
        } catch (Exception e) {
            lblLogo.setText("GESTOR DE INCIDENCIAS");
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    /**
 * Configura las propiedades básicas de la ventana principal.
 * Establece tamaño responsive, posición y CardLayout.
 */
private void configurarVentana() {
    // Obtener tamaño de pantalla para configuración responsive
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    System.out.println("Resolución detectada: " + screenSize.width + "x" + screenSize.height);
    
    // Configurar tamaños mínimos y responsivos
    setResizable(true);
    setMinimumSize(new Dimension(1200, 700));
    
    // Configuración según tamaño de pantalla
    if (screenSize.width <= 1366 || screenSize.height <= 768) {
        // Pantallas pequeñas - maximizar siempre
        System.out.println("Pantalla pequeña detectada - maximizando ventana");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    } else if (screenSize.width >= 1400 && screenSize.height >= 800) {
        // Pantallas grandes - maximizar por defecto
        setPreferredSize(new Dimension(1400, 800));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    } else {
        // Pantallas medianas - tamaño fijo centrado
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    // Configurar CardLayout para el panel de contenido
    cardLayout = (CardLayout) panelContenido.getLayout();
    
    // Escuchar cambios de tamaño para propagar a paneles
    addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            propagarTamañoAPaneles();
        }
    });
}

    /**
     * Carga y muestra la información del usuario actual en el header. Obtiene
     * datos de la sesión activa y aplica colores diferenciados por rol. Muestra
     * nombre completo, rol y aplica color según tipo de usuario.
     */
    private void cargarInformacionUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstance();

        if (sesion.haySesionActiva()) {
            lblUsuario.setText("Usuario: " + sesion.getUsuarioActual().getNombreCompleto());
            lblRol.setText("Rol: " + sesion.getRolActual().getNombre());

            // Cambiar color del rol según el tipo
            switch (sesion.getRolActual()) {
                case ADMINISTRADOR:
                    lblRol.setForeground(new java.awt.Color(100, 255, 100));
                    break;
                case TECNICO:
                    lblRol.setForeground(new java.awt.Color(100, 200, 255));
                    break;
                case CLIENTE:
                    lblRol.setForeground(new java.awt.Color(255, 200, 100));
                    break;
            }
        } else {
            lblUsuario.setText("Usuario: Desconocido");
            lblRol.setText("Rol: Sin asignar");
        }
    }

    /**
     * Configura la visibilidad de botones del menú según el rol del usuario.
     * ADMINISTRADOR: acceso completo, TECNICO: sin gestión de usuarios,
     * CLIENTE: solo incidencias y perfil. Redirige al login si no hay sesión.
     */
    private void configurarPermisosPorRol() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();

        if (rol == null) {
            // Si no hay sesión, ocultar todo y cerrar
            cerrarSesionYVolverAlLogin();
            return;
        }

        switch (rol) {
            case ADMINISTRADOR:
                // Administrador ve todo
                btnMenuIncidencias.setVisible(true);
                btnMenuUsuarios.setVisible(true);
                btnMenuReportes.setVisible(true);
                btnMenuPerfil.setVisible(true);
                break;

            case TECNICO:
                // Técnico no ve gestión de usuarios
                btnMenuIncidencias.setVisible(true);
                btnMenuUsuarios.setVisible(false);
                btnMenuReportes.setVisible(true);
                btnMenuPerfil.setVisible(true);
                break;

            case CLIENTE:
                // Cliente solo ve incidencias y perfil
                btnMenuIncidencias.setVisible(true);
                btnMenuUsuarios.setVisible(false);
                btnMenuReportes.setVisible(false);
                btnMenuPerfil.setVisible(true);
                break;
        }
    }

    /**
     * Configura estilos visuales y efectos hover para botones del menú.
     * Establece cursores de tipo mano y configura efectos de hover para todos
     * los botones de navegación y cierre de sesión.
     */
    private void configurarEstilos() {
        // Configurar cursor en todos los botones del menú
        btnMenuIncidencias.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuUsuarios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuReportes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuPerfil.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Efectos hover para botones del menú
        configurarHoverMenu(btnMenuIncidencias);
        configurarHoverMenu(btnMenuUsuarios);
        configurarHoverMenu(btnMenuReportes);
        configurarHoverMenu(btnMenuPerfil);

        // Efecto hover para botón cerrar sesión
        btnCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCerrarSesion.setBackground(new java.awt.Color(220, 70, 70));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCerrarSesion.setBackground(new java.awt.Color(200, 50, 50));
            }
        });
    }

    /**
     * Configura el menú de ayuda en la barra de menús de la aplicación. Crea
     * menú con ayuda general, separador visual y opción "Acerca de". Integra
     * con el sistema de ayuda contextual de la aplicación.
     */
    private void configurarMenuAyuda() {
        // Crear barra de menú si no existe
        JMenuBar menuBar = getJMenuBar();
        if (menuBar == null) {
            menuBar = new JMenuBar();
            setJMenuBar(menuBar);
        }

        // Crear menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Elementos del menú
        JMenuItem itemAyudaGeneral = new JMenuItem("Ayuda General");
        JMenuItem separador = new JMenuItem(); // Separador visual
        separador.setEnabled(false);
        separador.setText("─────────────");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de...");

        // Configurar eventos
        itemAyudaGeneral.addActionListener(e -> SistemaAyuda.mostrarAyudaGeneral());
        itemAcercaDe.addActionListener(e -> SistemaAyuda.mostrarAcercaDe());

        // Añadir elementos al menú
        menuAyuda.add(itemAyudaGeneral);
        menuAyuda.add(separador);
        menuAyuda.add(itemAcercaDe);

        // Añadir menú a la barra
        menuBar.add(menuAyuda);

        System.out.println("Menú de ayuda configurado");
    }

    /**
     * Muestra el panel de incidencias en el área de contenido principal.
     * Implementa lazy loading para crear el panel solo cuando es necesario y lo
     * reutiliza en navegaciones posteriores.
     */
    private void mostrarPanelIncidencias() {
        establecerBotonActivo(btnMenuIncidencias);

        panelContenido.removeAll();

        // Crear panel solo si no existe 
        if (panelIncidencias == null) {
            panelIncidencias = new PanelIncidencias();
            System.out.println("PanelIncidencias creado por primera vez");
        } else {
            System.out.println("Reutilizando PanelIncidencias existente");
        }

        // Envolver en un JPanel con GridBagLayout para centrar perfectamente
        JPanel wrapper = new JPanel(new java.awt.GridBagLayout());
        wrapper.setBackground(new java.awt.Color(240, 245, 250));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.insets = new java.awt.Insets(20, 20, 20, 20);
        wrapper.add(panelIncidencias, gbc);

        panelContenido.add(wrapper);
        panelContenido.revalidate();
        panelContenido.repaint();
        SwingUtilities.invokeLater(() -> panelIncidencias.resetearScroll());
    }

    /**
     * Muestra el panel de gestión de usuarios con validación de permisos. Solo
     * accesible para usuarios con rol de ADMINISTRADOR. Implementa lazy loading
     * y muestra mensaje de error si no tiene permisos.
     */
    private void mostrarPanelUsuarios() {
        // Verificar permisos
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.getRolActual() != Rol.ADMINISTRADOR) {
            JOptionPane.showMessageDialog(this,
                    "Solo los administradores pueden acceder a la gestión de usuarios",
                    "Acceso denegado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        establecerBotonActivo(btnMenuUsuarios);

        panelContenido.removeAll();
        // Crear panel solo si no existe 
        if (panelUsuarios == null) {
            panelUsuarios = new PanelUsuarios();
            System.out.println("PanelUsuarios creado por primera vez");
        } else {
            System.out.println("Reutilizando PanelUsuarios existente");
        }

        // Envolver en un JPanel con GridBagLayout para centrar 
        JPanel wrapper = new JPanel(new java.awt.GridBagLayout());
        wrapper.setBackground(new java.awt.Color(240, 245, 250));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.insets = new java.awt.Insets(20, 20, 20, 20);

        wrapper.add(panelUsuarios, gbc);

        panelContenido.add(wrapper);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    /**
     * Muestra el panel de reportes con validación de permisos por rol.
     * Accesible para ADMINISTRADOR y TECNICO, denegado para CLIENTE. Implementa
     * lazy loading y control de acceso.
     */
    private void mostrarPanelReportes() {
        SesionUsuario sesion = SesionUsuario.getInstance();

        // Solo administradores y técnicos pueden ver reportes
        if (sesion.getRolActual() == Rol.CLIENTE) {
            JOptionPane.showMessageDialog(this,
                    "No tienes permisos para acceder a los reportes",
                    "Acceso denegado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        establecerBotonActivo(btnMenuReportes);

        panelContenido.removeAll();
        // Crear panel solo si no existe 
        if (panelReportes == null) {
            panelReportes = new PanelReportes();
            System.out.println("PanelReportes creado por primera vez");
        } else {
            System.out.println("Reutilizando PanelReportes existente");
        }

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(240, 245, 250));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.insets = new java.awt.Insets(20, 20, 20, 20);
        wrapper.add(panelReportes, gbc);

        panelContenido.add(wrapper);
        panelContenido.revalidate();
        panelContenido.repaint();

    }

    /**
     * Muestra el panel de perfil de usuario. Accesible para todos los roles
     * autenticados. Implementa lazy loading para optimizar rendimiento.
     */
    private void mostrarPanelPerfil() {
        establecerBotonActivo(btnMenuPerfil);

        panelContenido.removeAll();

        // Crear panel solo si no existe
        if (panelPerfil == null) {
            panelPerfil = new PanelPerfil();
            System.out.println("PanelPerfil creado por primera vez");
        } else {
            System.out.println("Reutilizando PanelPerfil existente");
        }

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(240, 245, 250));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.insets = new java.awt.Insets(20, 20, 20, 20);
        wrapper.add(panelPerfil, gbc);

        panelContenido.add(wrapper);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    /**
     * Establece un botón del menú como actualmente seleccionado. Restaura el
     * color del botón anterior y aplica estilo activo al nuevo.
     *
     * @param boton Botón del menú a establecer como activo
     */
    private void establecerBotonActivo(JButton boton) {
        // Restaurar el botón anterior al color normal
        if (botonActivo != null) {
            botonActivo.setBackground(COLOR_MENU_NORMAL);
        }

        // Establecer el nuevo botón como activo
        botonActivo = boton;
        boton.setBackground(COLOR_MENU_ACTIVO);
    }

    /**
     * Configura el efectos hover interactivos para un botón del menú. Maneja
     * cambios de color en mouse
     *
     * @param boton Botón del menú al que aplicar efectos hover
     */
    private void configurarHoverMenu(javax.swing.JButton boton) {
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Solo cambiar color si no es el botón activo
                if (boton != botonActivo) {
                    boton.setBackground(COLOR_MENU_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Restaurar color según si es activo o no
                if (boton == botonActivo) {
                    boton.setBackground(COLOR_MENU_ACTIVO);
                } else {
                    boton.setBackground(COLOR_MENU_NORMAL);
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_MENU_ACTIVO);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (boton == botonActivo) {
                    boton.setBackground(COLOR_MENU_ACTIVO);
                } else {
                    boton.setBackground(COLOR_MENU_HOVER);
                }
            }
        });
    }

    /**
     * Cierra la sesión actual y regresa a la pantalla de login. Solicita
     * confirmación al usuario, limpia la sesión, cierra la ventana actual y
     * abre una nueva instancia del login.
     */
    private void cerrarSesionYVolverAlLogin() {
        int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Cerrar Sesión",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta == javax.swing.JOptionPane.YES_OPTION) {
            // Cerrar sesión
            SesionUsuario.getInstance().cerrarSesion();

            // Cerrar ventana actual
            this.dispose();

            // Abrir login
            java.awt.EventQueue.invokeLater(() -> {
                new Login().setVisible(true);
            });
        }
    }

    /**
     * Propaga cambios de tamaño de la ventana a todos los paneles internos.
     * Revalida y repinta el área de contenido para mantener el diseño
     * responsive.
     */
    private void propagarTamañoAPaneles() {
        panelContenido.revalidate();
        panelContenido.repaint();

        System.out.println("Ventana redimensionada: " + getSize());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelMenu = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        btnMenuIncidencias = new javax.swing.JButton();
        btnMenuUsuarios = new javax.swing.JButton();
        btnMenuReportes = new javax.swing.JButton();
        btnMenuPerfil = new javax.swing.JButton();
        panelHeader = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelInfoUsuario = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblRol = new javax.swing.JLabel();
        btnCerrarSesion = new javax.swing.JButton();
        panelContenido = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Gestor de Incidencias");
        setPreferredSize(new java.awt.Dimension(950, 700));
        setSize(new java.awt.Dimension(1200, 800));

        panelMenu.setBackground(new java.awt.Color(30, 50, 70));
        panelMenu.setPreferredSize(new java.awt.Dimension(250, 800));
        panelMenu.setLayout(new javax.swing.BoxLayout(panelMenu, javax.swing.BoxLayout.Y_AXIS));

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/logo.png"))); // NOI18N
        lblLogo.setPreferredSize(new java.awt.Dimension(250, 100));
        panelMenu.add(lblLogo);

        btnMenuIncidencias.setBackground(new java.awt.Color(40, 70, 100));
        btnMenuIncidencias.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnMenuIncidencias.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuIncidencias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/incidencia.png"))); // NOI18N
        btnMenuIncidencias.setText("Incidencias");
        btnMenuIncidencias.setBorderPainted(false);
        btnMenuIncidencias.setFocusPainted(false);
        btnMenuIncidencias.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuIncidencias.setMaximumSize(new java.awt.Dimension(250, 50));
        btnMenuIncidencias.setMinimumSize(new java.awt.Dimension(250, 50));
        btnMenuIncidencias.setPreferredSize(new java.awt.Dimension(250, 50));
        btnMenuIncidencias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuIncidenciasActionPerformed(evt);
            }
        });
        panelMenu.add(btnMenuIncidencias);

        btnMenuUsuarios.setBackground(new java.awt.Color(30, 50, 70));
        btnMenuUsuarios.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnMenuUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/usuarios.png"))); // NOI18N
        btnMenuUsuarios.setText("Usuarios");
        btnMenuUsuarios.setBorderPainted(false);
        btnMenuUsuarios.setFocusPainted(false);
        btnMenuUsuarios.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuUsuarios.setMaximumSize(new java.awt.Dimension(250, 50));
        btnMenuUsuarios.setMinimumSize(new java.awt.Dimension(250, 50));
        btnMenuUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuUsuariosActionPerformed(evt);
            }
        });
        panelMenu.add(btnMenuUsuarios);

        btnMenuReportes.setBackground(new java.awt.Color(30, 50, 70));
        btnMenuReportes.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnMenuReportes.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuReportes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/reportes.png"))); // NOI18N
        btnMenuReportes.setText("Reportes");
        btnMenuReportes.setBorderPainted(false);
        btnMenuReportes.setFocusPainted(false);
        btnMenuReportes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuReportes.setMaximumSize(new java.awt.Dimension(250, 50));
        btnMenuReportes.setMinimumSize(new java.awt.Dimension(250, 50));
        btnMenuReportes.setPreferredSize(new java.awt.Dimension(250, 50));
        btnMenuReportes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuReportesActionPerformed(evt);
            }
        });
        panelMenu.add(btnMenuReportes);

        btnMenuPerfil.setBackground(new java.awt.Color(30, 50, 70));
        btnMenuPerfil.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnMenuPerfil.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuPerfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/perfil.png"))); // NOI18N
        btnMenuPerfil.setText("Mi Perfil");
        btnMenuPerfil.setBorderPainted(false);
        btnMenuPerfil.setFocusPainted(false);
        btnMenuPerfil.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuPerfil.setMaximumSize(new java.awt.Dimension(250, 50));
        btnMenuPerfil.setMinimumSize(new java.awt.Dimension(250, 50));
        btnMenuPerfil.setPreferredSize(new java.awt.Dimension(250, 50));
        btnMenuPerfil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuPerfilActionPerformed(evt);
            }
        });
        panelMenu.add(btnMenuPerfil);

        getContentPane().add(panelMenu, java.awt.BorderLayout.LINE_START);

        panelHeader.setBackground(new java.awt.Color(20, 40, 60));
        panelHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelHeader.setPreferredSize(new java.awt.Dimension(1200, 80));
        panelHeader.setLayout(new java.awt.BorderLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 230, 255));
        lblTitulo.setText("GESTOR DE INCIDENCIAS");
        panelHeader.add(lblTitulo, java.awt.BorderLayout.CENTER);

        panelInfoUsuario.setOpaque(false);
        panelInfoUsuario.setLayout(new java.awt.GridBagLayout());

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUsuario.setForeground(new java.awt.Color(255, 255, 255));
        lblUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUsuario.setText("Usuario: Cargando...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        panelInfoUsuario.add(lblUsuario, gridBagConstraints);

        lblRol.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblRol.setForeground(new java.awt.Color(180, 200, 220));
        lblRol.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRol.setText("Rol: Cargando...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        panelInfoUsuario.add(lblRol, gridBagConstraints);

        btnCerrarSesion.setBackground(new java.awt.Color(200, 50, 50));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnCerrarSesion.setForeground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setPreferredSize(new java.awt.Dimension(100, 35));
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelInfoUsuario.add(btnCerrarSesion, gridBagConstraints);

        panelHeader.add(panelInfoUsuario, java.awt.BorderLayout.EAST);

        getContentPane().add(panelHeader, java.awt.BorderLayout.PAGE_START);

        panelContenido.setBackground(new java.awt.Color(240, 245, 250));
        panelContenido.setLayout(new java.awt.CardLayout());
        getContentPane().add(panelContenido, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
        cerrarSesionYVolverAlLogin();
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    private void btnMenuIncidenciasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuIncidenciasActionPerformed
        mostrarPanelIncidencias();
    }//GEN-LAST:event_btnMenuIncidenciasActionPerformed

    private void btnMenuUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuUsuariosActionPerformed
        mostrarPanelUsuarios();
    }//GEN-LAST:event_btnMenuUsuariosActionPerformed

    private void btnMenuReportesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuReportesActionPerformed
        mostrarPanelReportes();
    }//GEN-LAST:event_btnMenuReportesActionPerformed

    private void btnMenuPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuPerfilActionPerformed
        mostrarPanelPerfil();
    }//GEN-LAST:event_btnMenuPerfilActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnMenuIncidencias;
    private javax.swing.JButton btnMenuPerfil;
    private javax.swing.JButton btnMenuReportes;
    private javax.swing.JButton btnMenuUsuarios;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelInfoUsuario;
    private javax.swing.JPanel panelMenu;
    // End of variables declaration//GEN-END:variables
}
