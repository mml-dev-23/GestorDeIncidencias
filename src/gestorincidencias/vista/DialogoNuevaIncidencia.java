package gestorincidencias.vista;

import gestorincidencias.modelo.*;
import gestorincidencias.util.SesionUsuario;
import gestorincidencias.util.GestorIncidencias;
import gestorincidencias.util.GestorUsuarios;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Diálogo modal para creación de nuevas incidencias con permisos por rol.
 *
 * <p>
 * Proporciona formulario completo para crear incidencias con validaciones y
 * configuración automática de permisos según el rol del usuario
 * autenticado:</p>
 * <ul>
 * <li><strong>CLIENTE:</strong> Crea incidencias sin asignación de técnico</li>
 * <li><strong>TECNICO:</strong> Crea incidencias con opción de
 * auto-asignación</li>
 * <li><strong>ADMINISTRADOR:</strong> Control completo de asignación de
 * técnicos</li>
 * </ul>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class DialogoNuevaIncidencia extends javax.swing.JDialog {

    private boolean incidenciaCreada = false;
    private Incidencia incidenciaResultado = null;

    /**
     * Inicializa el diálogo y configura componentes según permisos del usuario.
     *
     * @param parent Ventana padre del diálogo
     * @param modal Si el diálogo es modal o no
     */
    public DialogoNuevaIncidencia(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarDialogo();
        cargarCombos();
        configurarPermisos();
        configurarEstilos();
    }

    /**
     * Configura las propiedades del diálogo
     */
    private void configurarDialogo() {
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);

        // Configurar áreas de texto
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
    }

    /**
     * Carga los datos en los ComboBox
     */
    private void cargarCombos() {
        // Cargar categorías
        comboCategoria.removeAllItems();
        for (Categoria cat : Categoria.values()) {
            comboCategoria.addItem(cat.getNombreConIcono());
        }

        // Cargar prioridades
        comboPrioridad.removeAllItems();
        for (Prioridad pri : Prioridad.values()) {
            comboPrioridad.addItem(pri.getNombreConIcono());
        }

        // Seleccionar prioridad media por defecto
        comboPrioridad.setSelectedIndex(1); // Media

        // Cargar técnicos 
        comboTecnico.removeAllItems();

        try {
            GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
            List<Usuario> tecnicos = gestorUsuarios.obtenerPorRol(Rol.TECNICO);

            for (Usuario tecnico : tecnicos) {
                comboTecnico.addItem(tecnico.getNombreCompleto());
            }

            System.out.println("Cargados " + tecnicos.size() + " técnicos en combo");

        } catch (Exception e) {
            System.err.println("Error cargando técnicos: " + e.getMessage());
            comboTecnico.addItem("Error cargando técnicos");
        }
    }

    /**
     * Configura permisos y visibilidad de controles según el rol del usuario.
     * Administradores pueden asignar técnicos, técnicos se auto-asignan,
     * clientes crean sin asignación específica.
     */
    private void configurarPermisos() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();

        if (rol == Rol.ADMINISTRADOR) {
            checkAsignarTecnico.setVisible(true);
            checkAsignarTecnico.setEnabled(true);
            checkAsignarTecnico.setSelected(false);
            lblTecnicoLabel.setVisible(false);
            comboTecnico.setVisible(true);
            comboTecnico.setEnabled(false);

        } else if (rol == Rol.TECNICO) {
            checkAsignarTecnico.setVisible(false);
            lblTecnicoLabel.setVisible(true);
            lblTecnicoLabel.setText("Técnico asignado:");
            comboTecnico.setVisible(true);
            comboTecnico.setEnabled(false);
            comboTecnico.setSelectedIndex(0); // "Sin asignar"

        } else if (rol == Rol.CLIENTE) {
            checkAsignarTecnico.setVisible(false);
            lblTecnicoLabel.setVisible(true);
            lblTecnicoLabel.setText("Técnico asignado:");
            comboTecnico.setVisible(true);
            comboTecnico.removeAllItems();
            comboTecnico.addItem("Será asignado por el sistema");
            comboTecnico.setEnabled(false);
            comboTecnico.setSelectedIndex(0); // "Sin asignar"
        }
    }

    /**
     * Configura estilos visuales
     */
    private void configurarEstilos() {
        // Cursor en botones
        btnCrear.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover en botón crear
        btnCrear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCrear.setBackground(new java.awt.Color(0, 200, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCrear.setBackground(new java.awt.Color(0, 230, 255));
            }
        });

        // Hover en botón cancelar
        btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancelar.setBackground(new java.awt.Color(140, 140, 140));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancelar.setBackground(new java.awt.Color(120, 120, 120));
            }
        });
    }

    /**
     * Valida todos los campos del formulario antes de crear la incidencia.
     * Verifica longitud mínima del título (10 caracteres) y descripción (20
     * caracteres).
     *
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        // Validar título
        if (txtTitulo.getText().trim().isEmpty()) {
            mostrarError("Por favor, ingrese un título para la incidencia");
            txtTitulo.requestFocus();
            return false;
        }

        if (txtTitulo.getText().trim().length() < 10) {
            mostrarError("El título debe tener al menos 10 caracteres");
            txtTitulo.requestFocus();
            return false;
        }

        // Validar descripción
        if (txtDescripcion.getText().trim().isEmpty()) {
            mostrarError("Por favor, ingrese una descripción de la incidencia");
            txtDescripcion.requestFocus();
            return false;
        }

        if (txtDescripcion.getText().trim().length() < 20) {
            mostrarError("La descripción debe tener al menos 20 caracteres");
            txtDescripcion.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Crea la incidencia con los datos del formulario y la guarda en base de
     * datos.
     */
    private void crearIncidencia() {
        if (!validarCampos()) {
            return;
        }

        try {
            SesionUsuario sesion = SesionUsuario.getInstance();

            // Crear objeto incidencia
            Incidencia nuevaIncidencia = new Incidencia();
            nuevaIncidencia.setTitulo(txtTitulo.getText().trim());
            nuevaIncidencia.setDescripcion(txtDescripcion.getText().trim());

            // Obtener categoría seleccionada
            Categoria categoriaSeleccionada = Categoria.values()[comboCategoria.getSelectedIndex()];
            nuevaIncidencia.setCategoria(categoriaSeleccionada);

            // Obtener prioridad seleccionada
            Prioridad prioridadSeleccionada = Prioridad.values()[comboPrioridad.getSelectedIndex()];
            nuevaIncidencia.setPrioridad(prioridadSeleccionada);

            // Estado inicial
            nuevaIncidencia.setEstado(Estado.PENDIENTE);

            // Datos del cliente 
            nuevaIncidencia.setIdCliente(sesion.getIdUsuarioActual());
            nuevaIncidencia.setNombreCliente(sesion.getUsuarioActual().getNombreCompleto());

            // Técnico asignado 
            if (checkAsignarTecnico.isSelected() && comboTecnico.getSelectedIndex() >= 0) {
                String tecnicoSeleccionado = (String) comboTecnico.getSelectedItem();

                System.out.println("DEBUG - Checkbox marcado: " + checkAsignarTecnico.isSelected());
                System.out.println("DEBUG - Índice seleccionado: " + comboTecnico.getSelectedIndex());
                System.out.println("DEBUG - Técnico seleccionado: '" + tecnicoSeleccionado + "'");

                // Buscar el técnico real por nombre completo
                GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
                List<Usuario> tecnicos = gestorUsuarios.obtenerPorRol(Rol.TECNICO);

                System.out.println("DEBUG - Técnicos disponibles:");
                for (Usuario tec : tecnicos) {
                    System.out.println("   - '" + tec.getNombreCompleto() + "' (ID: " + tec.getId() + ")");
                }

                Usuario tecnicoEncontrado = null;
                for (Usuario tecnico : tecnicos) {
                    if (tecnico.getNombreCompleto().equals(tecnicoSeleccionado)) {
                        tecnicoEncontrado = tecnico;
                        break;
                    }
                }

                if (tecnicoEncontrado != null) {
                    nuevaIncidencia.setIdTecnicoAsignado(tecnicoEncontrado.getId());
                    nuevaIncidencia.setNombreTecnico(tecnicoEncontrado.getNombreCompleto());
                    System.out.println("Técnico asignado: " + tecnicoEncontrado.getNombreCompleto() + " (ID: " + tecnicoEncontrado.getId() + ")");
                } else {
                    System.out.println("No se encontró técnico con nombre: '" + tecnicoSeleccionado + "'");
                }
            } else {
                System.out.println("DEBUG - No se asignó técnico:");
                System.out.println("Checkbox: " + checkAsignarTecnico.isSelected());
                System.out.println("Índice: " + comboTecnico.getSelectedIndex());
            }

            // Guardar en el gestor
            incidenciaResultado = GestorIncidencias.getInstance().crear(nuevaIncidencia);
            incidenciaCreada = true;

            // Mostrar confirmación
            JOptionPane.showMessageDialog(
                    this,
                    "Incidencia creada exitosamente\n"
                    + "ID: " + incidenciaResultado.getIdFormateado(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Cerrar diálogo
            dispose();

        } catch (Exception e) {
            mostrarError("Error al crear la incidencia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Verifica si se creó exitosamente una incidencia en el diálogo.
     *
     * @return true si se creó la incidencia, false en caso contrario
     */
    public boolean isIncidenciaCreada() {
        return incidenciaCreada;
    }

    /**
     * Obtiene la incidencia creada para actualización de vistas.
     *
     * @return Objeto Incidencia creada o null si no se creó ninguna
     */
    public Incidencia getIncidenciaCreada() {
        return incidenciaResultado;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblTituloLabel = new javax.swing.JLabel();
        txtTitulo = new javax.swing.JTextField();
        lblDescripcionLabel = new javax.swing.JLabel();
        scrollDescripcion = new javax.swing.JScrollPane();
        txtDescripcion = new javax.swing.JTextArea();
        lblCategoriaLabel = new javax.swing.JLabel();
        comboCategoria = new javax.swing.JComboBox<>();
        lblPrioridadLabel = new javax.swing.JLabel();
        comboPrioridad = new javax.swing.JComboBox<>();
        checkAsignarTecnico = new javax.swing.JCheckBox();
        btnCrear = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        comboTecnico = new javax.swing.JComboBox<>();
        lblTecnicoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Nueva Incidencia");
        setModal(true);
        setResizable(false);
        setSize(new java.awt.Dimension(600, 650));

        panelPrincipal.setBackground(new java.awt.Color(245, 250, 255));
        panelPrincipal.setBounds(new java.awt.Rectangle(0, 0, 500, 600));
        panelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 150, 200));
        lblTitulo.setText("CREAR NUEVA INCIDENCIA");
        panelPrincipal.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 30, 500, 30));

        lblTituloLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTituloLabel.setText("Título de la incidencia:");
        panelPrincipal.add(lblTituloLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, 500, 35));

        txtTitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        panelPrincipal.add(txtTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 105, 500, 35));

        lblDescripcionLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDescripcionLabel.setText("Descripción detallada:");
        panelPrincipal.add(lblDescripcionLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 155, 500, 20));

        txtDescripcion.setColumns(20);
        txtDescripcion.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setRows(5);
        txtDescripcion.setWrapStyleWord(true);
        scrollDescripcion.setViewportView(txtDescripcion);

        panelPrincipal.add(scrollDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 180, 500, 120));

        lblCategoriaLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCategoriaLabel.setText("Categoría:");
        panelPrincipal.add(lblCategoriaLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 315, 200, 20));

        comboCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        panelPrincipal.add(comboCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 340, 200, 35));

        lblPrioridadLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPrioridadLabel.setText("Prioridad:");
        panelPrincipal.add(lblPrioridadLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 315, 200, 20));

        comboPrioridad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        panelPrincipal.add(comboPrioridad, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 340, 250, 35));

        checkAsignarTecnico.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        checkAsignarTecnico.setText("Asignar técnico ahora");
        checkAsignarTecnico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAsignarTecnicoActionPerformed(evt);
            }
        });
        panelPrincipal.add(checkAsignarTecnico, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 390, 500, 30));

        btnCrear.setBackground(new java.awt.Color(0, 230, 255));
        btnCrear.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCrear.setForeground(new java.awt.Color(10, 30, 50));
        btnCrear.setText("CREAR INCIDENCIA");
        btnCrear.setFocusPainted(false);
        btnCrear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearActionPerformed(evt);
            }
        });
        panelPrincipal.add(btnCrear, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 500, 240, 50));

        btnCancelar.setBackground(new java.awt.Color(120, 120, 120));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cancelar");
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        panelPrincipal.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 500, 240, 50));

        comboTecnico.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboTecnico.setEnabled(false);
        panelPrincipal.add(comboTecnico, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 425, 500, 35));

        lblTecnicoLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTecnicoLabel.setForeground(new java.awt.Color(33, 47, 61));
        lblTecnicoLabel.setText("Técnico asignado:");
        panelPrincipal.add(lblTecnicoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 390, 200, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCrearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearActionPerformed
        crearIncidencia();
    }//GEN-LAST:event_btnCrearActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cancelar?\nLos datos no se guardarán.",
                "Cancelar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            dispose();
        }
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void checkAsignarTecnicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAsignarTecnicoActionPerformed
        comboTecnico.setEnabled(checkAsignarTecnico.isSelected());
    }//GEN-LAST:event_checkAsignarTecnicoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCrear;
    private javax.swing.JCheckBox checkAsignarTecnico;
    private javax.swing.JComboBox<String> comboCategoria;
    private javax.swing.JComboBox<String> comboPrioridad;
    private javax.swing.JComboBox<String> comboTecnico;
    private javax.swing.JLabel lblCategoriaLabel;
    private javax.swing.JLabel lblDescripcionLabel;
    private javax.swing.JLabel lblPrioridadLabel;
    private javax.swing.JLabel lblTecnicoLabel;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloLabel;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JScrollPane scrollDescripcion;
    private javax.swing.JTextArea txtDescripcion;
    private javax.swing.JTextField txtTitulo;
    // End of variables declaration//GEN-END:variables
}
