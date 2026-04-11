package gestorincidencias.vista;

import gestorincidencias.modelo.*;
import gestorincidencias.util.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

/**
 * Panel de gestión de perfil personal del usuario autenticado.
 *
 * <p>
 * Proporciona una interfaz completa para la gestión del perfil personal
 * incluyendo:</p>
 * <ul>
 * <li>Visualización y edición limitada de datos personales (email
 * editable)</li>
 * <li>Sistema seguro de cambio de contraseña con validaciones múltiples</li>
 * <li>Estadísticas personalizadas según el rol del usuario
 * (Cliente/Técnico/Administrador)</li>
 * <li>Área de notas personales con timestamp de modificación</li>
 * <li>Interfaz dividida en dos paneles: datos personales y
 * estadísticas/notas</li>
 * </ul>
 *
 * <p>
 * <strong>Control de acceso:</strong> Disponible para todos los roles
 * autenticados.</p>
 *
 * <p>
 * <strong>Funcionalidades por rol:</strong></p>
 * <ul>
 * <li><strong>CLIENTE:</strong> Estadísticas de incidencias creadas, pendientes
 * y resueltas</li>
 * <li><strong>TECNICO:</strong> Estadísticas de incidencias asignadas y su
 * resolución</li>
 * <li><strong>ADMINISTRADOR:</strong> Estadísticas globales del sistema
 * completo</li>
 * </ul>
 *
 * <p>
 * <strong>Campos editables:</strong></p>
 * <ul>
 * <li><strong>Email:</strong> Validación de formato y unicidad</li>
 * <li><strong>Contraseña:</strong> Diálogo modal con validación segura</li>
 * <li><strong>Notas personales:</strong> Área de texto libre con timestamp</li>
 * </ul>
 *
 * <p>
 * <strong>Campos de solo lectura:</strong> Nombre, apellidos y rol
 * (determinados por el sistema).</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class PanelPerfil extends javax.swing.JPanel {

    private GestorUsuarios gestorUsuarios;
    private GestorIncidencias gestorIncidencias;
    private Usuario usuarioActual;

    // Colores del tema
    private final Color COLOR_CYAN = new Color(0, 230, 255);
    private final Color COLOR_VERDE = new Color(46, 204, 113);
    private final Color COLOR_GRIS = new Color(150, 150, 150);

    /**
     * Inicializa el panel de perfil y configura todos sus componentes. Carga
     * datos del usuario actual, estadísticas personalizadas, notas, y configura
     * estilos visuales y eventos de interacción.
     */
    public PanelPerfil() {
        gestorUsuarios = GestorUsuarios.getInstance();
        gestorIncidencias = GestorIncidencias.getInstance();
        usuarioActual = SesionUsuario.getInstance().getUsuarioActual();

        initComponents();
        cargarDatosUsuario();
        cargarEstadisticas();
        cargarNotas();
        configurarEstilos();
        configurarEventos();
    }

    /**
     * Carga los datos básicos del usuario actual desde la sesión.
     */
    private void cargarDatosUsuario() {
        if (usuarioActual != null) {
            txtNombre.setText(usuarioActual.getNombre());
            txtApellidos.setText(usuarioActual.getApellidos());
            txtEmail.setText(usuarioActual.getEmail());
            txtRol.setText(SesionUsuario.getInstance().getRolActual().getNombre());
            txtPassword.setText("••••••••");
            txtPassword.setEditable(false);
            txtPassword.setBackground(new Color(240, 240, 240));
        }
    }

    /**
     * Carga las estadísticas personalizadas según el rol del usuario
     * autenticado.
     */
    private void cargarEstadisticas() {
        if (usuarioActual == null) {
            return;
        }

        List<Incidencia> todasIncidencias = gestorIncidencias.obtenerTodas();
        Rol rolActual = SesionUsuario.getInstance().getRolActual();

        int creadas = 0;
        int pendientes = 0;
        int resueltas = 0;
        long sumaDias = 0;
        int conFecha = 0;

        for (Incidencia inc : todasIncidencias) {
            // Estadísticas según rol
            if (rolActual == Rol.CLIENTE) {
                // Cliente: incidencias que él creó
                if (inc.getIdCliente() == usuarioActual.getId()) {
                    creadas++;
                    if (inc.getEstado() == Estado.PENDIENTE) {
                        pendientes++;
                    }
                    if (inc.getEstado() == Estado.RESUELTA || inc.getEstado() == Estado.CERRADA) {
                        resueltas++;
                        if (inc.getFechaCreacion() != null && inc.getFechaResolucion() != null) {
                            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                                    inc.getFechaCreacion(), inc.getFechaResolucion());
                            sumaDias += dias;
                            conFecha++;
                        }
                    }
                }
            } else if (rolActual == Rol.TECNICO) {
                // Técnico: incidencias que tiene asignadas
                if (inc.getIdTecnicoAsignado() != null
                        && inc.getIdTecnicoAsignado() == usuarioActual.getId()) {
                    creadas++;
                    if (inc.getEstado() == Estado.PENDIENTE || inc.getEstado() == Estado.EN_PROCESO) {
                        pendientes++;
                    }
                    if (inc.getEstado() == Estado.RESUELTA || inc.getEstado() == Estado.CERRADA) {
                        resueltas++;
                        if (inc.getFechaCreacion() != null && inc.getFechaResolucion() != null) {
                            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                                    inc.getFechaCreacion(), inc.getFechaResolucion());
                            sumaDias += dias;
                            conFecha++;
                        }
                    }
                }
            } else if (rolActual == Rol.ADMINISTRADOR) {
                // Admin: todas las incidencias
                creadas++;
                if (inc.getEstado() == Estado.PENDIENTE) {
                    pendientes++;
                }
                if (inc.getEstado() == Estado.RESUELTA || inc.getEstado() == Estado.CERRADA) {
                    resueltas++;
                    if (inc.getFechaCreacion() != null && inc.getFechaResolucion() != null) {
                        long dias = java.time.temporal.ChronoUnit.DAYS.between(
                                inc.getFechaCreacion(), inc.getFechaResolucion());
                        sumaDias += dias;
                        conFecha++;
                    }
                }
            }
        }

        String tiempoPromedio = conFecha > 0 ? (sumaDias / conFecha) + " días" : "N/A";

        // Actualizar labels según rol
        if (rolActual == Rol.CLIENTE) {
            lblIncidenciasCreadas.setText("Incidencias creadas: " + creadas);
            lblIncidenciasPendientes.setText("Pendientes: " + pendientes);
            lblIncidenciasResueltas.setText("Resueltas: " + resueltas);
            lblTiempoPromedio.setText("Tiempo promedio: " + tiempoPromedio);
        } else if (rolActual == Rol.TECNICO) {
            lblIncidenciasCreadas.setText("Incidencias asignadas: " + creadas);
            lblIncidenciasPendientes.setText("Pendientes/En proceso: " + pendientes);
            lblIncidenciasResueltas.setText("Resueltas: " + resueltas);
            lblTiempoPromedio.setText("Tiempo promedio: " + tiempoPromedio);
        } else if (rolActual == Rol.ADMINISTRADOR) {
            lblIncidenciasCreadas.setText("Total incidencias: " + creadas);
            lblIncidenciasPendientes.setText("Pendientes: " + pendientes);
            lblIncidenciasResueltas.setText("Resueltas: " + resueltas);
            lblTiempoPromedio.setText("Tiempo promedio: " + tiempoPromedio);
        }
    }

    /**
     * Carga las notas personales del usuario y su timestamp de modificación.
     * Muestra el contenido de notas existente y la fecha/hora de última
     * modificación en formato legible (dd/MM/yyyy HH:mm).
     */
    private void cargarNotas() {
        if (usuarioActual != null && usuarioActual.getNotas() != null) {
            txtNotas.setText(usuarioActual.getNotas());

            if (usuarioActual.getFechaModificacionNotas() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblUltimaModificacion.setText("Última modificación: "
                        + usuarioActual.getFechaModificacionNotas().format(formatter));
            }
        }
    }

    /**
     * Guarda los cambios realizados en los datos editables del usuario.
     */
    private void guardarCambios() {
        try {
            // Validar email
            String nuevoEmail = txtEmail.getText().trim();
            if (nuevoEmail.isEmpty() || !nuevoEmail.contains("@")) {
                JOptionPane.showMessageDialog(this,
                        "Por favor ingrese un email válido",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar si el email ya existe (excepto el del usuario actual)
            Usuario existente = gestorUsuarios.obtenerPorEmail(nuevoEmail);
            if (existente != null && existente.getId() != usuarioActual.getId()) {
                JOptionPane.showMessageDialog(this,
                        "El email ya está en uso por otro usuario",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Actualizar datos
            usuarioActual.setEmail(nuevoEmail);

            if (gestorUsuarios.actualizar(usuarioActual)) {
                JOptionPane.showMessageDialog(this,
                        "Cambios guardados correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar los cambios",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cancela los cambios realizados y restaura los datos originales.
     */
    private void cancelarCambios() {
        cargarDatosUsuario();
        JOptionPane.showMessageDialog(this,
                "Cambios cancelados",
                "Cancelar",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Guarda las notas personales del usuario con timestamp automático.
     */
    private void guardarNotas() {
        try {
            String notas = txtNotas.getText();
            usuarioActual.setNotas(notas);
            usuarioActual.setFechaModificacionNotas(LocalDateTime.now());

            if (gestorUsuarios.actualizar(usuarioActual)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblUltimaModificacion.setText("Última modificación: "
                        + usuarioActual.getFechaModificacionNotas().format(formatter));

                JOptionPane.showMessageDialog(this,
                        "Notas guardadas correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar las notas",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar notas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre diálogo modal para cambio seguro de contraseña.
     */
    private void abrirDialogoCambiarPassword() {
        JDialog dialogo = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "Cambiar Contraseña", true);
        dialogo.setLayout(new GridBagLayout());
        dialogo.setSize(500, 400);
        dialogo.setLocationRelativeTo(this);
        dialogo.setResizable(false);
        dialogo.getContentPane().setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();

        // Título
        JLabel lblTitulo = new JLabel("Cambiar Contraseña");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(33, 47, 61));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        dialogo.add(lblTitulo, gbc);

        // Contraseña actual
        JLabel lblActual = new JLabel("Contraseña actual:");
        lblActual.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        dialogo.add(lblActual, gbc);

        JPasswordField txtPasswordActual = new JPasswordField();
        txtPasswordActual.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPasswordActual.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 20, 15, 20);
        dialogo.add(txtPasswordActual, gbc);

        // Nueva contraseña
        JLabel lblNueva = new JLabel("Nueva contraseña:");
        lblNueva.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 20, 5, 10);
        dialogo.add(lblNueva, gbc);

        JPasswordField txtPasswordNueva = new JPasswordField();
        txtPasswordNueva.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 20, 15, 20);
        dialogo.add(txtPasswordNueva, gbc);

        // Confirmar contraseña
        JLabel lblConfirmar = new JLabel("Confirmar contraseña:");
        lblConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 20, 5, 10);
        dialogo.add(lblConfirmar, gbc);

        JPasswordField txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 20, 20, 20);
        dialogo.add(txtConfirmarPassword, gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        panelBotones.setOpaque(false);

        JButton btnGuardar = new JButton("Cambiar Contraseña");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setBackground(COLOR_VERDE);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setPreferredSize(new Dimension(160, 35));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnCancelarDlg = new JButton("Cancelar");
        btnCancelarDlg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelarDlg.setBackground(COLOR_GRIS);
        btnCancelarDlg.setForeground(Color.WHITE);
        btnCancelarDlg.setFocusPainted(false);
        btnCancelarDlg.setBorderPainted(false);
        btnCancelarDlg.setPreferredSize(new Dimension(100, 35));
        btnCancelarDlg.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelarDlg);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 20, 20);
        dialogo.add(panelBotones, gbc);

        // Eventos
        btnGuardar.addActionListener(e -> {
            String passwordActual = new String(txtPasswordActual.getPassword());
            String passwordNueva = new String(txtPasswordNueva.getPassword());
            String confirmar = new String(txtConfirmarPassword.getPassword());

            // Validar contraseña actual
            if (!PasswordUtil.verifyPassword(passwordActual, usuarioActual.getPassword())) {
                JOptionPane.showMessageDialog(dialogo,
                        "La contraseña actual es incorrecta",
                        "Error", JOptionPane.ERROR_MESSAGE);
                txtPasswordActual.requestFocus();
                return;
            }

            // Validar nueva contraseña
            if (passwordNueva.length() < 6) {
                JOptionPane.showMessageDialog(dialogo,
                        "La nueva contraseña debe tener al menos 6 caracteres",
                        "Error", JOptionPane.ERROR_MESSAGE);
                txtPasswordNueva.requestFocus();
                return;
            }

            // Validar confirmación
            if (!passwordNueva.equals(confirmar)) {
                JOptionPane.showMessageDialog(dialogo,
                        "Las contraseñas no coinciden",
                        "Error", JOptionPane.ERROR_MESSAGE);
                txtConfirmarPassword.requestFocus();
                return;
            }

            // Actualizar contraseña
            usuarioActual.setPassword(passwordNueva);
            if (gestorUsuarios.actualizar(usuarioActual)) {
                JOptionPane.showMessageDialog(dialogo,
                        "Contraseña cambiada exitosamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialogo.dispose();
            } else {
                JOptionPane.showMessageDialog(dialogo,
                        "Error al cambiar la contraseña",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelarDlg.addActionListener(e -> dialogo.dispose());

        // Mostrar diálogo
        dialogo.setVisible(true);
    }

    /**
     * Configura estilos visuales y efectos hover para todos los botones.
     */
    private void configurarEstilos() {
        // Cursores
        btnGuardarCambios.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardarNotas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCambiarPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover btnGuardarCambios
        btnGuardarCambios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGuardarCambios.setBackground(new Color(40, 180, 100));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnGuardarCambios.setBackground(COLOR_VERDE);
            }
        });

        // Hover btnCancelar
        btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancelar.setBackground(new Color(130, 130, 130));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancelar.setBackground(COLOR_GRIS);
            }
        });

        // Hover btnGuardarNotas
        btnGuardarNotas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGuardarNotas.setBackground(new Color(0, 200, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnGuardarNotas.setBackground(COLOR_CYAN);
            }
        });
    }

    /**
     * Configura los eventos de acción para todos los botones del panel.
     */
    private void configurarEventos() {
        btnGuardarCambios.addActionListener(e -> guardarCambios());
        btnCancelar.addActionListener(e -> cancelarCambios());
        btnGuardarNotas.addActionListener(e -> guardarNotas());
        btnCambiarPassword.addActionListener(e -> abrirDialogoCambiarPassword());
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

        panelBarraSuperior = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelDatos = new javax.swing.JPanel();
        panelTituloDatos = new javax.swing.JPanel();
        lblTituloDatos = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnAyuda = new javax.swing.JButton();
        panelMisDatos = new javax.swing.JPanel();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblApellidos = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblRol = new javax.swing.JLabel();
        txtRol = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        btnCambiarPassword = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();
        panelBotones = new javax.swing.JPanel();
        btnGuardarCambios = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        panelPersonal = new javax.swing.JPanel();
        panelEstadisticas = new javax.swing.JPanel();
        panelTituloEstadisticas = new javax.swing.JPanel();
        lblTituloEstadisticas = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        panelDatosEstadisticas = new javax.swing.JPanel();
        lblIncidenciasCreadas = new javax.swing.JLabel();
        lblIncidenciasPendientes = new javax.swing.JLabel();
        lblIncidenciasResueltas = new javax.swing.JLabel();
        lblTiempoPromedio = new javax.swing.JLabel();
        panelNotas = new javax.swing.JPanel();
        panelTitulo = new javax.swing.JPanel();
        lblTituloNotas = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        scrollNotas = new javax.swing.JScrollPane();
        txtNotas = new javax.swing.JTextArea();
        panelBoton = new javax.swing.JPanel();
        btnGuardarNotas = new javax.swing.JButton();
        lblUltimaModificacion = new javax.swing.JLabel();

        setBackground(new java.awt.Color(245, 245, 245));
        setMaximumSize(new java.awt.Dimension(1400, 800));
        setMinimumSize(new java.awt.Dimension(1400, 800));
        setPreferredSize(new java.awt.Dimension(1300, 750));
        setLayout(new java.awt.GridBagLayout());

        panelBarraSuperior.setBackground(new java.awt.Color(33, 47, 61));
        panelBarraSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelBarraSuperior.setPreferredSize(new java.awt.Dimension(1300, 70));
        panelBarraSuperior.setLayout(new java.awt.BorderLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 230, 255));
        lblTitulo.setText("MI PERFIL");
        lblTitulo.setPreferredSize(new java.awt.Dimension(1000, 70));
        panelBarraSuperior.add(lblTitulo, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panelBarraSuperior, gridBagConstraints);

        panelDatos.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)), javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        panelDatos.setPreferredSize(new java.awt.Dimension(500, 650));
        panelDatos.setLayout(new java.awt.BorderLayout());

        panelTituloDatos.setBackground(new java.awt.Color(255, 255, 255));
        panelTituloDatos.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelTituloDatos.setLayout(new java.awt.BorderLayout());

        lblTituloDatos.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloDatos.setForeground(new java.awt.Color(33, 47, 61));
        lblTituloDatos.setText("Datos Personales");
        panelTituloDatos.add(lblTituloDatos, java.awt.BorderLayout.CENTER);
        panelTituloDatos.add(jSeparator1, java.awt.BorderLayout.SOUTH);

        btnAyuda.setBackground(new java.awt.Color(190, 193, 196));
        btnAyuda.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAyuda.setForeground(new java.awt.Color(255, 255, 255));
        btnAyuda.setText("?");
        btnAyuda.setToolTipText("Ayuda de Incidencias (F1)");
        btnAyuda.setIgnoreRepaint(true);
        btnAyuda.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAyuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAyudaActionPerformed(evt);
            }
        });
        panelTituloDatos.add(btnAyuda, java.awt.BorderLayout.LINE_END);

        panelDatos.add(panelTituloDatos, java.awt.BorderLayout.NORTH);

        panelMisDatos.setBackground(new java.awt.Color(255, 255, 255));
        panelMisDatos.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        panelMisDatos.setLayout(new java.awt.GridBagLayout());

        lblNombre.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(33, 47, 61));
        lblNombre.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 46;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 20, 10, 0);
        panelMisDatos.add(lblNombre, gridBagConstraints);

        txtNombre.setEditable(false);
        txtNombre.setBackground(new java.awt.Color(240, 240, 240));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtNombre.setText("jTextField1");
        txtNombre.setPreferredSize(new java.awt.Dimension(76, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 20);
        panelMisDatos.add(txtNombre, gridBagConstraints);

        lblApellidos.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblApellidos.setForeground(new java.awt.Color(33, 47, 61));
        lblApellidos.setText("Apellidos:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 38;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 0);
        panelMisDatos.add(lblApellidos, gridBagConstraints);

        txtApellidos.setEditable(false);
        txtApellidos.setBackground(new java.awt.Color(240, 240, 240));
        txtApellidos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtApellidos.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 20);
        panelMisDatos.add(txtApellidos, gridBagConstraints);

        lblEmail.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEmail.setForeground(new java.awt.Color(33, 47, 13));
        lblEmail.setText("Email:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 62;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 0);
        panelMisDatos.add(lblEmail, gridBagConstraints);

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmail.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 20);
        panelMisDatos.add(txtEmail, gridBagConstraints);

        lblRol.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRol.setForeground(new java.awt.Color(33, 47, 61));
        lblRol.setText("Rol:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.ipadx = 76;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 0);
        panelMisDatos.add(lblRol, gridBagConstraints);

        txtRol.setEditable(false);
        txtRol.setBackground(new java.awt.Color(240, 240, 240));
        txtRol.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtRol.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 20);
        panelMisDatos.add(txtRol, gridBagConstraints);

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(33, 47, 13));
        lblPassword.setText("Contraseña:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.ipadx = 27;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 0);
        panelMisDatos.add(lblPassword, gridBagConstraints);

        btnCambiarPassword.setBackground(new java.awt.Color(220, 220, 220));
        btnCambiarPassword.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        btnCambiarPassword.setText("Cambiar contraseña");
        btnCambiarPassword.setBorderPainted(false);
        btnCambiarPassword.setFocusPainted(false);
        btnCambiarPassword.setMaximumSize(new java.awt.Dimension(130, 28));
        btnCambiarPassword.setMinimumSize(new java.awt.Dimension(130, 28));
        btnCambiarPassword.setPreferredSize(new java.awt.Dimension(130, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 20);
        panelMisDatos.add(btnCambiarPassword, gridBagConstraints);

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPassword.setText("jPasswordField1");
        txtPassword.setPreferredSize(new java.awt.Dimension(240, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        panelMisDatos.add(txtPassword, gridBagConstraints);

        panelDatos.add(panelMisDatos, java.awt.BorderLayout.CENTER);

        panelBotones.setBackground(new java.awt.Color(255, 255, 255));
        panelBotones.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panelBotones.setPreferredSize(new java.awt.Dimension(450, 60));
        panelBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 70, 5));

        btnGuardarCambios.setBackground(new java.awt.Color(80, 200, 120));
        btnGuardarCambios.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardarCambios.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarCambios.setText("Guardar Cambios");
        btnGuardarCambios.setBorderPainted(false);
        btnGuardarCambios.setFocusPainted(false);
        btnGuardarCambios.setPreferredSize(new java.awt.Dimension(136, 35));
        panelBotones.add(btnGuardarCambios);

        btnCancelar.setBackground(new java.awt.Color(150, 150, 150));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cancelar");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new java.awt.Dimension(82, 35));
        panelBotones.add(btnCancelar);

        panelDatos.add(panelBotones, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        add(panelDatos, gridBagConstraints);

        panelPersonal.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)), javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        panelPersonal.setLayout(new java.awt.BorderLayout());

        panelEstadisticas.setBackground(new java.awt.Color(255, 255, 255));
        panelEstadisticas.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelEstadisticas.setPreferredSize(new java.awt.Dimension(650, 200));
        panelEstadisticas.setLayout(new java.awt.BorderLayout());

        panelTituloEstadisticas.setOpaque(false);
        panelTituloEstadisticas.setLayout(new java.awt.BorderLayout());

        lblTituloEstadisticas.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloEstadisticas.setForeground(new java.awt.Color(33, 47, 61));
        lblTituloEstadisticas.setText("Mis Estadísticas");
        panelTituloEstadisticas.add(lblTituloEstadisticas, java.awt.BorderLayout.NORTH);
        panelTituloEstadisticas.add(jSeparator2, java.awt.BorderLayout.SOUTH);

        panelEstadisticas.add(panelTituloEstadisticas, java.awt.BorderLayout.NORTH);

        panelDatosEstadisticas.setOpaque(false);
        panelDatosEstadisticas.setLayout(new java.awt.GridLayout(2, 2));

        lblIncidenciasCreadas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblIncidenciasCreadas.setForeground(new java.awt.Color(60, 60, 60));
        lblIncidenciasCreadas.setText("Incidencias creadas: --");
        panelDatosEstadisticas.add(lblIncidenciasCreadas);

        lblIncidenciasPendientes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblIncidenciasPendientes.setForeground(new java.awt.Color(60, 60, 60));
        lblIncidenciasPendientes.setText("Pendientes: --");
        panelDatosEstadisticas.add(lblIncidenciasPendientes);

        lblIncidenciasResueltas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblIncidenciasResueltas.setForeground(new java.awt.Color(60, 60, 60));
        lblIncidenciasResueltas.setText("Resueltas: --");
        panelDatosEstadisticas.add(lblIncidenciasResueltas);

        lblTiempoPromedio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTiempoPromedio.setForeground(new java.awt.Color(60, 60, 60));
        lblTiempoPromedio.setText("Tiempo promedio resolución: --");
        panelDatosEstadisticas.add(lblTiempoPromedio);

        panelEstadisticas.add(panelDatosEstadisticas, java.awt.BorderLayout.CENTER);

        panelPersonal.add(panelEstadisticas, java.awt.BorderLayout.NORTH);

        panelNotas.setBackground(new java.awt.Color(255, 255, 255));
        panelNotas.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 10, 15));
        panelNotas.setLayout(new java.awt.BorderLayout());

        panelTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panelTitulo.setOpaque(false);
        panelTitulo.setPreferredSize(new java.awt.Dimension(650, 50));
        panelTitulo.setLayout(new javax.swing.BoxLayout(panelTitulo, javax.swing.BoxLayout.Y_AXIS));

        lblTituloNotas.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloNotas.setForeground(new java.awt.Color(33, 47, 61));
        lblTituloNotas.setText("Mis Notas");
        panelTitulo.add(lblTituloNotas);
        panelTitulo.add(jSeparator3);

        panelNotas.add(panelTitulo, java.awt.BorderLayout.NORTH);

        scrollNotas.setBorder(null);

        txtNotas.setColumns(20);
        txtNotas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtNotas.setLineWrap(true);
        txtNotas.setRows(10);
        txtNotas.setWrapStyleWord(true);
        scrollNotas.setViewportView(txtNotas);

        panelNotas.add(scrollNotas, java.awt.BorderLayout.CENTER);

        panelBoton.setOpaque(false);
        panelBoton.setPreferredSize(new java.awt.Dimension(650, 35));
        panelBoton.setLayout(new java.awt.BorderLayout());

        btnGuardarNotas.setBackground(new java.awt.Color(0, 230, 255));
        btnGuardarNotas.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardarNotas.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarNotas.setText("Guardar Notas");
        btnGuardarNotas.setBorderPainted(false);
        btnGuardarNotas.setFocusPainted(false);
        panelBoton.add(btnGuardarNotas, java.awt.BorderLayout.WEST);

        lblUltimaModificacion.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblUltimaModificacion.setForeground(new java.awt.Color(150, 150, 150));
        lblUltimaModificacion.setText("Última modificación: --");
        panelBoton.add(lblUltimaModificacion, java.awt.BorderLayout.EAST);

        panelNotas.add(panelBoton, java.awt.BorderLayout.SOUTH);

        panelPersonal.add(panelNotas, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        add(panelPersonal, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAyudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAyudaActionPerformed
        SistemaAyuda.mostrarAyudaPerfil();
    }//GEN-LAST:event_btnAyudaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAyuda;
    private javax.swing.JButton btnCambiarPassword;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardarCambios;
    private javax.swing.JButton btnGuardarNotas;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblApellidos;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblIncidenciasCreadas;
    private javax.swing.JLabel lblIncidenciasPendientes;
    private javax.swing.JLabel lblIncidenciasResueltas;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblTiempoPromedio;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloDatos;
    private javax.swing.JLabel lblTituloEstadisticas;
    private javax.swing.JLabel lblTituloNotas;
    private javax.swing.JLabel lblUltimaModificacion;
    private javax.swing.JPanel panelBarraSuperior;
    private javax.swing.JPanel panelBoton;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelDatos;
    private javax.swing.JPanel panelDatosEstadisticas;
    private javax.swing.JPanel panelEstadisticas;
    private javax.swing.JPanel panelMisDatos;
    private javax.swing.JPanel panelNotas;
    private javax.swing.JPanel panelPersonal;
    private javax.swing.JPanel panelTitulo;
    private javax.swing.JPanel panelTituloDatos;
    private javax.swing.JPanel panelTituloEstadisticas;
    private javax.swing.JScrollPane scrollNotas;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextArea txtNotas;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtRol;
    // End of variables declaration//GEN-END:variables
}
