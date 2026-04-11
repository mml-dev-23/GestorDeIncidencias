package gestorincidencias.vista;

import gestorincidencias.modelo.*;
import gestorincidencias.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.Normalizer;

/**
 * Panel de gestión completa de usuarios del sistema - Exclusivo para
 * Administradores.
 *
 * <p>
 * Proporciona funcionalidad administrativa completa para la gestión de usuarios
 * incluyendo:</p>
 * <ul>
 * <li>Vista tabular con información completa de todos los usuarios</li>
 * <li>Sistema de filtros avanzados con búsqueda sin tildes y por rol</li>
 * <li>Operaciones CRUD: crear, editar, eliminar usuarios con validaciones</li>
 * <li>Controles de seguridad para proteger cuenta de administrador único</li>
 * <li>Edición inline mediante botones integrados en cada fila</li>
 * <li>Validaciones de email, contraseñas y restricciones de rol</li>
 * <li>Renderers personalizados con colores diferenciados por tipo de
 * usuario</li>
 * <li>Manejo de placeholders dinámicos y búsqueda en tiempo real</li>
 * </ul>
 *
 * <p>
 * <strong>Restricciones de seguridad implementadas:</strong></p>
 * <ul>
 * <li>Prevención de eliminación del último administrador del sistema</li>
 * <li>Validación de cambios de rol para mantener al menos un administrador</li>
 * <li>Validación de emails únicos y formato correcto</li>
 * <li>Contraseñas con longitud mínima de 6 caracteres</li>
 * </ul>
 *
 * <p>
 * La tabla utiliza renderers personalizados para mostrar roles con colores
 * distintivos y botones de acción integrados directamente en cada fila.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class PanelUsuarios extends javax.swing.JPanel {

    private GestorUsuarios gestorUsuarios;
    private DefaultTableModel modeloTabla;

    // Lista completa de usuarios (sin filtrar)
    private List<Usuario> usuariosCompletos;

    // Constante para el placeholder
    private static final String PLACEHOLDER_BUSQUEDA = "Buscar por nombre, apellidos o email...";

    // Colores del tema
    private final Color COLOR_HEADER = new Color(33, 47, 61);
    private final Color COLOR_CYAN = new Color(0, 230, 255);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BLANCO = Color.WHITE;
    private final Color COLOR_TEXTO = new Color(60, 60, 60);
    private final Color COLOR_BOTON_EDITAR = new Color(0, 230, 255);
    private final Color COLOR_BOTON_GUARDAR = new Color(80, 200, 120);
    private final Color COLOR_BOTON_ELIMINAR = new Color(255, 100, 100);
    private final Color COLOR_GRID = new Color(220, 220, 220);
    private final Color COLOR_SELECCION = new Color(230, 245, 255);

    /**
     * Inicializa el panel de usuarios y configura todos sus componentes.
     * Establece el gestor, inicializa la tabla, carga usuarios y configura
     * eventos de filtros y estilos visuales.
     */
    public PanelUsuarios() {
        gestorUsuarios = GestorUsuarios.getInstance();

        initComponents();
        configurarTabla();
        cargarUsuarios();
        configurarEventosFiltros();
        configurarEstilos();

    }

    /**
     * Configura la tabla de usuarios con 8 columnas y renderers personalizados.
     * Establece modelo de datos, configuración visual, anchos de columnas,
     * renderers con colores por rol y editores para botones de acción.
     */
    private void configurarTabla() {
        // Crear modelo de tabla con 8 columnas 
        String[] columnas = {"ID", "Nombre", "Apellidos", "Email", "Rol", "Contraseña", "Editar", "Eliminar"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Columnas 6 y 7 son editables (botones)
                return column == 6 || column == 7;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return Object.class;
            }
        };

        tablaUsuarios.setModel(modeloTabla);

        // Configuración de tamaño y resize
        tablaUsuarios.setPreferredScrollableViewportSize(new Dimension(1020, 600));
        tablaUsuarios.setFillsViewportHeight(true);
        tablaUsuarios.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Configurar apariencia general
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.setRowHeight(50);
        tablaUsuarios.setShowGrid(true);
        tablaUsuarios.setGridColor(COLOR_GRID);
        tablaUsuarios.setIntercellSpacing(new Dimension(1, 1));
        tablaUsuarios.setSelectionBackground(COLOR_SELECCION);
        tablaUsuarios.setSelectionForeground(COLOR_TEXTO);
        tablaUsuarios.setBackground(COLOR_BLANCO);

        // Configurar header
        JTableHeader header = tablaUsuarios.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(COLOR_HEADER);
        header.setForeground(COLOR_BLANCO);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Configurar anchos de columnas
        TableColumnModel columnModel = tablaUsuarios.getColumnModel();

        columnModel.getColumn(0).setPreferredWidth(50);   // ID
        columnModel.getColumn(0).setMinWidth(50);
        columnModel.getColumn(0).setMaxWidth(60);

        columnModel.getColumn(1).setPreferredWidth(130);  // Nombre
        columnModel.getColumn(1).setMinWidth(110);

        columnModel.getColumn(2).setPreferredWidth(180);  // Apellidos 
        columnModel.getColumn(2).setMinWidth(150);

        columnModel.getColumn(3).setPreferredWidth(260);  // Email 
        columnModel.getColumn(3).setMinWidth(230);

        columnModel.getColumn(4).setPreferredWidth(140);  // Rol
        columnModel.getColumn(4).setMinWidth(120);

        columnModel.getColumn(5).setPreferredWidth(150);  // Contraseña 
        columnModel.getColumn(5).setMinWidth(130);

        columnModel.getColumn(6).setPreferredWidth(90);   // Editar
        columnModel.getColumn(6).setMinWidth(90);
        columnModel.getColumn(6).setMaxWidth(90);

        columnModel.getColumn(7).setPreferredWidth(90);   // Eliminar
        columnModel.getColumn(7).setMinWidth(90);
        columnModel.getColumn(7).setMaxWidth(90);

        // Renderer para centrar ID
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(0).setCellRenderer(centrado);

        // Renderer para Nombre 
        DefaultTableCellRenderer nombreRenderer = new DefaultTableCellRenderer();
        nombreRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(1).setCellRenderer(nombreRenderer);

        // Renderer para Apellidos 
        DefaultTableCellRenderer apellidosRenderer = new DefaultTableCellRenderer();
        apellidosRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(2).setCellRenderer(apellidosRenderer);

        // Renderer para Email 
        DefaultTableCellRenderer emailRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.PLAIN, 11));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        };
        columnModel.getColumn(3).setCellRenderer(emailRenderer);

        // Renderer para Rol con colores según el tipo
        DefaultTableCellRenderer rolRenderer = new DefaultTableCellRenderer() {
            private final Color COLOR_ADMIN_BG = new Color(231, 76, 60);
            private final Color COLOR_TECNICO_BG = new Color(52, 152, 219);
            private final Color COLOR_CLIENTE_BG = new Color(46, 204, 113);
            private final Color COLOR_TEXT = Color.WHITE;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setOpaque(true);

                if (value != null) {
                    String rol = value.toString();

                    if (isSelected) {
                        setBackground(table.getSelectionBackground());
                        setForeground(table.getSelectionForeground());
                    } else {

                        if ("Administrador".equals(rol)) {
                            setBackground(COLOR_ADMIN_BG);
                            setForeground(COLOR_TEXT);
                        } else if ("Técnico".equals(rol)) {
                            setBackground(COLOR_TECNICO_BG);
                            setForeground(COLOR_TEXT);
                        } else if ("Cliente".equals(rol)) {
                            setBackground(COLOR_CLIENTE_BG);
                            setForeground(COLOR_TEXT);
                        } else {
                            setBackground(Color.WHITE);
                            setForeground(COLOR_TEXTO);
                        }
                    }
                } else {
                    setBackground(Color.WHITE);
                    setForeground(COLOR_TEXTO);
                }

                return this;
            }
        };
        columnModel.getColumn(4).setCellRenderer(rolRenderer);

        DefaultTableCellRenderer passwordRenderer = new DefaultTableCellRenderer();
        passwordRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        passwordRenderer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        columnModel.getColumn(5).setCellRenderer(passwordRenderer);

        // Renderer y Editor para columna EDITAR (6)
        columnModel.getColumn(6).setCellRenderer(new ButtonRenderer("Editar", COLOR_BOTON_EDITAR));
        columnModel.getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), "Editar"));

        // Renderer y Editor para columna ELIMINAR (7)
        columnModel.getColumn(7).setCellRenderer(new ButtonRenderer("Eliminar", COLOR_BOTON_ELIMINAR));
        columnModel.getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), "Eliminar"));
    }

    /**
     * Configura estilos visuales del panel y efectos hover para botones.
     */
    private void configurarEstilos() {
        btnNuevoUsuario.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnNuevoUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNuevoUsuario.setBackground(new Color(0, 200, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNuevoUsuario.setBackground(COLOR_CYAN);
            }
        });

        btnNuevoUsuario.addActionListener(evt -> abrirDialogoNuevoUsuario());
    }

    /**
     * Configura el placeholder dinámico del campo de búsqueda.
     */
    private void configurarPlaceholderBusqueda() {
        final String PLACEHOLDER = "Buscar por nombre, apellidos o email...";
        final Color COLOR_PLACEHOLDER = new Color(150, 150, 150);
        final Color COLOR_TEXTO = new Color(60, 60, 60);

        // Establecer placeholder inicial
        txtBuscar.setText(PLACEHOLDER);
        txtBuscar.setForeground(COLOR_PLACEHOLDER);

        // Evento cuando gana el foco
        txtBuscar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtBuscar.getText().equals(PLACEHOLDER)) {
                    txtBuscar.setText("");
                    txtBuscar.setForeground(COLOR_TEXTO);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtBuscar.getText().trim().isEmpty()) {
                    txtBuscar.setText(PLACEHOLDER);
                    txtBuscar.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });
    }

    /**
     * Configura todos los eventos de los filtros y búsqueda del panel.
     */
    private void configurarEventosFiltros() {
        final String PLACEHOLDER = "Buscar por nombre, apellidos o email...";

        // Configurar placeholder
        configurarPlaceholderBusqueda();

        // Evento del campo de búsqueda 
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Solo filtrar si no es el placeholder
                if (!PLACEHOLDER.equals(txtBuscar.getText())) {
                    aplicarFiltros();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Solo filtrar si no es el placeholder
                if (!PLACEHOLDER.equals(txtBuscar.getText())) {
                    aplicarFiltros();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Solo filtrar si no es el placeholder
                if (!PLACEHOLDER.equals(txtBuscar.getText())) {
                    aplicarFiltros();
                }
            }
        });

        // Evento del combo de roles
        comboFiltroRol.addActionListener(e -> aplicarFiltros());

        // Evento del botón limpiar
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());

        // Efecto hover en botón limpiar
        btnLimpiarFiltros.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLimpiarFiltros.setBackground(new Color(130, 130, 130));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLimpiarFiltros.setBackground(new Color(150, 150, 150));
            }
        });

        // Configurar cursor en todos los componentes interactivos
        txtBuscar.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        comboFiltroRol.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarFiltros.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }

    /**
     * Carga los usuarios en la tabla y actualiza contador de resultados.
     */
    private void cargarUsuarios() {
        // Guardar lista completa sin filtros
        usuariosCompletos = gestorUsuarios.obtenerTodos();

        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Mostrar TODOS los usuarios directamente
        for (Usuario usuario : usuariosCompletos) {
            Object[] fila = {
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                "••••••••",
                "Editar",
                "Eliminar"
            };
            modeloTabla.addRow(fila);
        }

        // Actualizar contador
        actualizarContadorResultados(usuariosCompletos.size());
    }

    /**
     * Aplica los filtros de búsqueda de texto y rol con normalización de
     * tildes.
     */
    private void aplicarFiltros() {
        // Obtener valores de los filtros
        String textoBusqueda = txtBuscar.getText().trim();

        // Ignorar si es el placeholder
        if (PLACEHOLDER_BUSQUEDA.equals(textoBusqueda)) {
            textoBusqueda = "";
        }

        // Normalizar texto de búsqueda
        String busquedaNormalizada = normalizarTexto(textoBusqueda);
        String rolSeleccionado = (String) comboFiltroRol.getSelectedItem();

        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Si no hay usuarios, salir
        if (usuariosCompletos == null || usuariosCompletos.isEmpty()) {
            actualizarContadorResultados(0);
            return;
        }

        // Filtrar usuarios
        List<Usuario> usuariosFiltrados = new ArrayList<>();

        for (Usuario usuario : usuariosCompletos) {
            boolean coincideTexto = false;

            if (busquedaNormalizada.isEmpty()) {
                // Si no hay búsqueda, mostrar todos
                coincideTexto = true;
            } else {
                // Normalizar textos del usuario para comparación
                String nombreNormalizado = normalizarTexto(usuario.getNombre());
                String apellidosNormalizado = normalizarTexto(usuario.getApellidos());
                String nombreCompletoNormalizado = normalizarTexto(usuario.getNombre() + " " + usuario.getApellidos());
                String emailNormalizado = normalizarTexto(usuario.getEmail());

                // Buscar si el texto está en cualquiera de los campos normalizados
                coincideTexto = nombreNormalizado.contains(busquedaNormalizada)
                        || apellidosNormalizado.contains(busquedaNormalizada)
                        || nombreCompletoNormalizado.contains(busquedaNormalizada)
                        || emailNormalizado.contains(busquedaNormalizada);
            }

            // Filtro por rol
            boolean coincideRol = rolSeleccionado == null
                    || "Todos".equals(rolSeleccionado)
                    || convertirEnumATexto(usuario.getRol()).equals(rolSeleccionado);

            // Si cumple ambos filtros, añadir a la lista
            if (coincideTexto && coincideRol) {
                usuariosFiltrados.add(usuario);
            }
        }

        // Mostrar usuarios filtrados en la tabla
        for (Usuario usuario : usuariosFiltrados) {
            Object[] fila = {
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                "••••••••",
                "Editar",
                "Eliminar"
            };
            modeloTabla.addRow(fila);
        }

        // Actualizar contador de resultados
        actualizarContadorResultados(usuariosFiltrados.size());
    }

    /**
     * Limpia todos los filtros y muestra todos los usuarios.
     */
    private void limpiarFiltros() {
        final String PLACEHOLDER = "Buscar por nombre, apellidos o email...";

        txtBuscar.setText(PLACEHOLDER);
        txtBuscar.setForeground(new Color(150, 150, 150));
        comboFiltroRol.setSelectedIndex(0);
        aplicarFiltros();
    }

    /**
     * Abre un diálogo para editar un usuario existente con validaciones
     * completas.
     *
     * @param fila Índice de la fila en la tabla correspondiente al usuario a
     * editar
     */
    private void abrirDialogoEdicion(int fila) {
        try {
            int id = (int) tablaUsuarios.getValueAt(fila, 0);
            Usuario usuario = gestorUsuarios.obtenerPorId(id);

            if (usuario == null) {
                JOptionPane.showMessageDialog(this,
                        "Usuario no encontrado",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear diálogo personalizado 
            JDialog dialogo = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Editar Usuario", true);
            dialogo.setLayout(null);
            dialogo.setSize(500, 530);
            dialogo.setLocationRelativeTo(this);
            dialogo.setResizable(false);
            dialogo.getContentPane().setBackground(COLOR_FONDO);

            // Título
            JLabel lblTitulo = new JLabel("Editar Usuario");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTitulo.setForeground(COLOR_HEADER);
            lblTitulo.setBounds(30, 20, 440, 30);
            dialogo.add(lblTitulo);

            // ID 
            JLabel lblId = new JLabel("ID: " + usuario.getId());
            lblId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblId.setForeground(new Color(120, 120, 120));
            lblId.setBounds(30, 55, 440, 20);
            dialogo.add(lblId);

            // Campo Nombre
            JLabel lblNombre = new JLabel("Nombre:");
            lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblNombre.setBounds(30, 90, 100, 25);
            dialogo.add(lblNombre);

            JTextField txtNombre = new JTextField(usuario.getNombre());
            txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtNombre.setBounds(30, 115, 440, 35);
            dialogo.add(txtNombre);

            // Campo Apellidos
            JLabel lblApellidos = new JLabel("Apellidos:");
            lblApellidos.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblApellidos.setBounds(30, 160, 100, 25);
            dialogo.add(lblApellidos);

            JTextField txtApellidos = new JTextField(usuario.getApellidos());
            txtApellidos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtApellidos.setBounds(30, 185, 440, 35);
            dialogo.add(txtApellidos);

            // Campo Email
            JLabel lblEmail = new JLabel("Email:");
            lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblEmail.setBounds(30, 230, 100, 25);
            dialogo.add(lblEmail);

            JTextField txtEmail = new JTextField(usuario.getEmail());
            txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtEmail.setBounds(30, 255, 440, 35);
            dialogo.add(txtEmail);

            // Combo Rol
            JLabel lblRol = new JLabel("Rol:");
            lblRol.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblRol.setBounds(30, 300, 100, 25);
            dialogo.add(lblRol);

            JComboBox<String> comboRol = new JComboBox<>(new String[]{"ADMINISTRADOR", "TECNICO", "CLIENTE"});
            comboRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            // Seleccionar según el rol
            switch (usuario.getRol()) {
                case ADMINISTRADOR:
                    comboRol.setSelectedIndex(0);
                    break;
                case TECNICO:
                    comboRol.setSelectedIndex(1);
                    break;
                case CLIENTE:
                    comboRol.setSelectedIndex(2);
                    break;
            }

            comboRol.setBounds(30, 325, 440, 35);
            dialogo.add(comboRol);

            // Campo Contraseña
            JLabel lblPassword = new JLabel("Nueva Contraseña (opcional):");
            lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPassword.setBounds(30, 370, 350, 25);
            dialogo.add(lblPassword);

            JPasswordField txtPassword = new JPasswordField();
            txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtPassword.setBounds(30, 395, 440, 35);
            dialogo.add(txtPassword);

            // Botones
            JButton btnGuardar = new JButton("Guardar Cambios");
            btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnGuardar.setBackground(COLOR_BOTON_GUARDAR);
            btnGuardar.setForeground(COLOR_BLANCO);
            btnGuardar.setFocusPainted(false);
            btnGuardar.setBorderPainted(false);
            btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnGuardar.setBounds(30, 450, 200, 40);
            dialogo.add(btnGuardar);

            JButton btnCancelar = new JButton("Cancelar");
            btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnCancelar.setBackground(new Color(150, 150, 150));
            btnCancelar.setForeground(COLOR_BLANCO);
            btnCancelar.setFocusPainted(false);
            btnCancelar.setBorderPainted(false);
            btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCancelar.setBounds(270, 450, 200, 40);
            dialogo.add(btnCancelar);

            // Evento Guardar
            btnGuardar.addActionListener(e -> {
                String nuevoNombre = txtNombre.getText().trim();
                String nuevosApellidos = txtApellidos.getText().trim();
                String nuevoEmail = txtEmail.getText().trim();
                String nuevoRolStr = (String) comboRol.getSelectedItem();
                String nuevaPassword = new String(txtPassword.getPassword()).trim();

                // VALIDACIONES
                if (nuevoNombre.isEmpty()) {
                    JOptionPane.showMessageDialog(dialogo,
                            "El nombre es obligatorio",
                            "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    txtNombre.requestFocus();
                    return;
                }

                if (nuevosApellidos.isEmpty()) {
                    JOptionPane.showMessageDialog(dialogo,
                            "Los apellidos son obligatorios",
                            "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    txtApellidos.requestFocus();
                    return;
                }

                if (nuevoEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(dialogo,
                            "El email es obligatorio",
                            "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    txtEmail.requestFocus();
                    return;
                }

                // Validar formato de email
                if (!nuevoEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    JOptionPane.showMessageDialog(dialogo,
                            "El formato del email no es válido",
                            "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    txtEmail.requestFocus();
                    return;
                }

                // Validar que el email no exista (si es diferente al actual)
                if (!nuevoEmail.equalsIgnoreCase(usuario.getEmail())) {
                    Usuario usuarioExistente = gestorUsuarios.obtenerPorEmail(nuevoEmail);
                    if (usuarioExistente != null && usuarioExistente.getId() != usuario.getId()) {
                        JOptionPane.showMessageDialog(dialogo,
                                "Ya existe otro usuario con ese email",
                                "Email duplicado",
                                JOptionPane.ERROR_MESSAGE);
                        txtEmail.requestFocus();
                        return;
                    }
                }

                // Validar contraseña (solo si se introdujo una)
                if (!nuevaPassword.isEmpty() && nuevaPassword.length() < 6) {
                    JOptionPane.showMessageDialog(dialogo,
                            "La contraseña debe tener al menos 6 caracteres",
                            "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    txtPassword.requestFocus();
                    return;
                }

                boolean cambios = false;

                // Actualizar nombre
                if (!nuevoNombre.equals(usuario.getNombre())) {
                    usuario.setNombre(nuevoNombre);
                    cambios = true;
                }

                // Actualizar apellidos
                if (!nuevosApellidos.equals(usuario.getApellidos())) {
                    usuario.setApellidos(nuevosApellidos);
                    cambios = true;
                }

                // Actualizar email
                if (!nuevoEmail.equalsIgnoreCase(usuario.getEmail())) {
                    usuario.setEmail(nuevoEmail);
                    cambios = true;
                }

                // Actualizar rol
                Rol nuevoRol = Rol.valueOf(nuevoRolStr);
                if (nuevoRol != usuario.getRol()) {
                    // Validar único admin
                    if (usuario.getRol() == Rol.ADMINISTRADOR && nuevoRol != Rol.ADMINISTRADOR) {
                        List<Usuario> admins = gestorUsuarios.obtenerPorRol(Rol.ADMINISTRADOR);
                        if (admins.size() <= 1) {
                            JOptionPane.showMessageDialog(dialogo,
                                    "No se puede cambiar el rol del único administrador del sistema",
                                    "Validación",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                    usuario.setRol(nuevoRol);
                    cambios = true;
                }

                // Actualizar contraseña si se proporcionó
                if (!nuevaPassword.isEmpty()) {
                    usuario.setPassword(nuevaPassword);
                    cambios = true;
                }

                if (cambios) {
                    boolean actualizado = gestorUsuarios.actualizar(usuario);

                    if (actualizado) {
                        JOptionPane.showMessageDialog(dialogo,
                                "Usuario actualizado correctamente",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                        // Recargar usuarios manteniendo filtros activos
                        usuariosCompletos = gestorUsuarios.obtenerTodos();
                        aplicarFiltros();
                        dialogo.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialogo,
                                "Error al actualizar el usuario",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialogo,
                            "No se realizaron cambios",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialogo.dispose();
                }
            });

            // Evento Cancelar
            btnCancelar.addActionListener(e -> dialogo.dispose());

            // Quitar foco inicial de los campos
            SwingUtilities.invokeLater(() -> btnGuardar.requestFocusInWindow());

            dialogo.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Elimina un usuario tras confirmación con validaciones de seguridad.
     *
     * @param fila Índice de la fila en la tabla correspondiente al usuario a
     * eliminar
     */
    private void eliminarUsuario(int fila) {
        try {
            int id = (int) tablaUsuarios.getValueAt(fila, 0);
            String nombre = (String) tablaUsuarios.getValueAt(fila, 1);
            String apellidos = (String) tablaUsuarios.getValueAt(fila, 2);

            Usuario usuario = gestorUsuarios.obtenerPorId(id);

            if (usuario == null) {
                JOptionPane.showMessageDialog(this,
                        "Usuario no encontrado",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar único admin
            if (usuario.getRol() == Rol.ADMINISTRADOR) {
                List<Usuario> admins = gestorUsuarios.obtenerPorRol(Rol.ADMINISTRADOR);
                if (admins.size() <= 1) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede eliminar el único administrador del sistema",
                            "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Confirmar
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar al usuario:\n" + nombre + " " + apellidos + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (respuesta == JOptionPane.YES_OPTION) {
                boolean eliminado = gestorUsuarios.eliminar(id);

                if (eliminado) {
                    JOptionPane.showMessageDialog(this,
                            "Usuario eliminado correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    // Recargar usuarios manteniendo filtros activos
                    usuariosCompletos = gestorUsuarios.obtenerTodos();
                    aplicarFiltros();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Abre el diálogo para crear un nuevo usuario en el sistema.
     */
    private void abrirDialogoNuevoUsuario() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DialogoNuevoUsuario dialogo = new DialogoNuevoUsuario(frame, true);
        dialogo.setVisible(true);

        if (dialogo.isUsuarioCreado()) {
            cargarUsuarios();
        }
    }

    /**
     * Actualiza el label contador con resultados de búsqueda y total. Muestra
     * cantidad filtrada vs total cuando hay filtros activos, o solo total
     * cuando se muestran todos los usuarios.
     *
     * @param cantidad Número de usuarios actualmente visibles tras filtros
     */
    private void actualizarContadorResultados(int cantidad) {
        if (usuariosCompletos == null) {
            lblResultados.setText("");
            return;
        }

        int total = usuariosCompletos.size();

        if (cantidad == total) {
            lblResultados.setText("Mostrando " + total + " usuarios");
        } else {
            lblResultados.setText("Mostrando " + cantidad + " de " + total);
        }
    }

    /**
     * Normaliza texto eliminando tildes y diacríticos para búsqueda insensible.
     * Convierte a minúsculas y elimina acentos para permitir búsquedas más
     * flexibles.
     *
     * @param texto Texto original con posibles tildes y acentos
     * @return String normalizado en minúsculas sin diacríticos
     */
    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        // Normalizar y eliminar diacríticos
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        return normalizado.toLowerCase().trim();
    }

    /**
     * Normaliza texto eliminando tildes y diacríticos para búsqueda insensible.
     * Convierte a minúsculas y elimina acentos para permitir búsquedas más
     * flexibles.
     *
     * @param texto Texto original con posibles tildes y acentos
     * @return String normalizado en minúsculas sin diacríticos
     */
    private String convertirEnumATexto(Rol rol) {
        switch (rol) {
            case ADMINISTRADOR:
                return "Administrador";
            case TECNICO:
                return "Técnico";
            case CLIENTE:
                return "Cliente";
            default:
                return rol.toString();
        }
    }

    /**
     * Renderer para botones individuales
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {

        private Color colorFondo;

        public ButtonRenderer(String texto, Color color) {
            setText(texto);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(COLOR_BLANCO);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);
            this.colorFondo = color;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(colorFondo);
            return this;
        }
    }

    /**
     * Editor para botones individuales
     */
    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private boolean clicked;
        private int row;
        private String accion;

        public ButtonEditor(JCheckBox checkBox, String accion) {
            super(checkBox);
            this.accion = accion;

            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setForeground(COLOR_BLANCO);
            button.setFocusPainted(false);
            button.setBorderPainted(false);

            if ("Editar".equals(accion)) {
                button.setBackground(COLOR_BOTON_EDITAR);
            } else {
                button.setBackground(COLOR_BOTON_ELIMINAR);
            }

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            this.row = row;
            label = (value == null) ? accion : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                // Ejecutar acción según el tipo de botón
                if ("Editar".equals(accion)) {
                    SwingUtilities.invokeLater(() -> abrirDialogoEdicion(row));
                } else if ("Eliminar".equals(accion)) {
                    SwingUtilities.invokeLater(() -> eliminarUsuario(row));
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
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

        panelSuperior = new javax.swing.JPanel();
        panelBarraSuperior = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelFiltros = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        lblFiltroRol = new javax.swing.JLabel();
        comboFiltroRol = new javax.swing.JComboBox<>();
        btnLimpiarFiltros = new javax.swing.JButton();
        lblResultados = new javax.swing.JLabel();
        btnNuevoUsuario = new javax.swing.JButton();
        btnAyuda = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();

        setBackground(new java.awt.Color(245, 245, 245));
        setPreferredSize(new java.awt.Dimension(1100, 700));
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setPreferredSize(new java.awt.Dimension(1300, 125));
        panelSuperior.setLayout(new javax.swing.BoxLayout(panelSuperior, javax.swing.BoxLayout.Y_AXIS));

        panelBarraSuperior.setBackground(new java.awt.Color(33, 47, 61));
        panelBarraSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelBarraSuperior.setPreferredSize(new java.awt.Dimension(1000, 70));
        panelBarraSuperior.setLayout(new java.awt.BorderLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 230, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTitulo.setText("GESTIÓN DE USUARIOS");
        panelBarraSuperior.add(lblTitulo, java.awt.BorderLayout.WEST);

        panelSuperior.add(panelBarraSuperior);

        panelFiltros.setBackground(new java.awt.Color(255, 255, 255));
        panelFiltros.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        panelFiltros.setOpaque(false);
        panelFiltros.setPreferredSize(new java.awt.Dimension(1300, 55));
        panelFiltros.setLayout(new java.awt.GridBagLayout());

        lblBuscar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblBuscar.setForeground(new java.awt.Color(33, 47, 61));
        lblBuscar.setText("Buscar:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelFiltros.add(lblBuscar, gridBagConstraints);

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setText("jTextField1");
        txtBuscar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 0)));
        txtBuscar.setPreferredSize(new java.awt.Dimension(200, 30));
        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelFiltros.add(txtBuscar, gridBagConstraints);

        lblFiltroRol.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFiltroRol.setForeground(new java.awt.Color(33, 47, 61));
        lblFiltroRol.setText("Rol:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelFiltros.add(lblFiltroRol, gridBagConstraints);

        comboFiltroRol.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboFiltroRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Administrador", "Técnico", "Cliente" }));
        comboFiltroRol.setPreferredSize(new java.awt.Dimension(150, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelFiltros.add(comboFiltroRol, gridBagConstraints);

        btnLimpiarFiltros.setBackground(new java.awt.Color(150, 150, 150));
        btnLimpiarFiltros.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiarFiltros.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiarFiltros.setText("Limpiar");
        btnLimpiarFiltros.setBorderPainted(false);
        btnLimpiarFiltros.setFocusPainted(false);
        btnLimpiarFiltros.setPreferredSize(new java.awt.Dimension(100, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelFiltros.add(btnLimpiarFiltros, gridBagConstraints);

        lblResultados.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblResultados.setForeground(new java.awt.Color(100, 100, 100));
        lblResultados.setPreferredSize(new java.awt.Dimension(150, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelFiltros.add(lblResultados, gridBagConstraints);

        btnNuevoUsuario.setBackground(new java.awt.Color(0, 230, 255));
        btnNuevoUsuario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnNuevoUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevoUsuario.setText("Nuevo Usuario");
        btnNuevoUsuario.setToolTipText("");
        btnNuevoUsuario.setBorderPainted(false);
        btnNuevoUsuario.setFocusPainted(false);
        btnNuevoUsuario.setPreferredSize(new java.awt.Dimension(130, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panelFiltros.add(btnNuevoUsuario, gridBagConstraints);

        btnAyuda.setBackground(new java.awt.Color(190, 193, 196));
        btnAyuda.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAyuda.setForeground(new java.awt.Color(255, 255, 255));
        btnAyuda.setText("?");
        btnAyuda.setToolTipText("Ayuda de Incidencias (F1)");
        btnAyuda.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAyuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAyudaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        panelFiltros.add(btnAyuda, gridBagConstraints);

        panelSuperior.add(panelFiltros);

        add(panelSuperior, java.awt.BorderLayout.NORTH);

        tablaUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Apellidos", "Email", "Rol", "Contraseña", "Acciones"
            }
        ));
        tablaUsuarios.setGridColor(new java.awt.Color(220, 220, 220));
        tablaUsuarios.setIntercellSpacing(new java.awt.Dimension(1, 1));
        tablaUsuarios.setRowHeight(45);
        tablaUsuarios.setSelectionBackground(new java.awt.Color(230, 245, 255));
        tablaUsuarios.setShowGrid(true);
        scrollTabla.setViewportView(tablaUsuarios);

        add(scrollTabla, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed

    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnAyudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAyudaActionPerformed
        SistemaAyuda.mostrarAyudaUsuarios();
    }//GEN-LAST:event_btnAyudaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAyuda;
    private javax.swing.JButton btnLimpiarFiltros;
    private javax.swing.JButton btnNuevoUsuario;
    private javax.swing.JComboBox<String> comboFiltroRol;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblFiltroRol;
    private javax.swing.JLabel lblResultados;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelBarraSuperior;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tablaUsuarios;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
