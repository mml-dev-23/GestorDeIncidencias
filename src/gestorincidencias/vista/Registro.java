package gestorincidencias.vista;

import gestorincidencias.util.GestorUsuarios;
import gestorincidencias.modelo.Usuario;
import gestorincidencias.modelo.Rol;
import javax.swing.JOptionPane;

/**
 * Ventana de registro de nuevos usuarios.
 *
 * <p>
 * Proporciona un formulario completo para el registro de nuevos usuarios
 * con:</p>
 * <ul>
 * <li>Interfaz visual atractiva con bordes redondeados y efectos hover</li>
 * <li>Sistema de placeholders dinámicos en todos los campos</li>
 * <li>Validaciones exhaustivas de datos de entrada</li>
 * <li>Verificación de unicidad de email en el sistema</li>
 * <li>Creación automática de usuarios con rol CLIENTE</li>
 * <li>Efectos visuales de focus y hover en componentes interactivos</li>
 * </ul>
 *
 * <p>
 * <strong>Campos del formulario:</strong></p>
 * <ul>
 * <li><strong>Nombre:</strong> Mínimo 2 caracteres, obligatorio</li>
 * <li><strong>Apellidos:</strong> Mínimo 2 caracteres, obligatorio</li>
 * <li><strong>Email:</strong> Formato válido y único en el sistema</li>
 * <li><strong>Contraseña:</strong> Mínimo 6 caracteres</li>
 * <li><strong>Confirmar contraseña:</strong> Debe coincidir con la
 * contraseña</li>
 * </ul>
 *
 * <p>
 * <strong>Características visuales:</strong></p>
 * <ul>
 * <li>Tema oscuro con colores azules y efectos cyan</li>
 * <li>Bordes redondeados personalizados con Anti-aliasing</li>
 * <li>Placeholders que desaparecen dinámicamente al escribir</li>
 * <li>Efectos hover en botones y campos de texto</li>
 * <li>Botón de retorno con icono personalizable</li>
 * </ul>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class Registro extends javax.swing.JFrame {

    /**
     * Inicializa la ventana de registro y configura todos sus componentes.
     * Establece propiedades de ventana, placeholders, estilos visuales, botón
     * de retorno y efectos interactivos.
     */
    public Registro() {
        initComponents();
        configurarVentana();
        configurarPanelRedondeado();
        configurarBotonVolver();
        configurarPlaceholders();
        configurarEstilos();
    }

    /**
     * Configura las propiedades básicas de la ventana de registro. Establece
     * tamaño fijo, posición centrada y comportamiento no redimensionable.
     */
    private void configurarVentana() {

        setSize(600, 750);
        setPreferredSize(new java.awt.Dimension(600, 750));
        setMinimumSize(new java.awt.Dimension(600, 750));

        panelFondo.setSize(600, 750);
        panelFondo.setPreferredSize(new java.awt.Dimension(600, 750));
        panelFondo.setBounds(0, 0, 600, 750);

        // Centrar ventana
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Configura placeholders dinámicos en todos los campos del formulario.
     */
    private void configurarPlaceholders() {
        agregarPlaceholder(txtNombre, "Nombre");
        agregarPlaceholder(txtApellidos, "Apellidos");
        agregarPlaceholder(txtEmail, "Email");
        agregarPlaceholderPassword(txtPassword, "Contraseña");
        agregarPlaceholderPassword(txtConfirmarPassword, "Confirmar Contraseña");
    }

    /**
     * Añade placeholder dinámico a un campo de texto normal. El placeholder
     * aparece cuando el campo está vacío y desaparece al escribir, cambiando
     * colores apropiadamente.
     *
     * @param campo Campo de texto al que aplicar el placeholder
     * @param placeholder Texto del placeholder a mostrar
     */
    private void agregarPlaceholder(javax.swing.JTextField campo, String placeholder) {
        campo.setText(placeholder);
        campo.setForeground(new java.awt.Color(150, 180, 200));

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(java.awt.Color.WHITE);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (campo.getText().trim().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(new java.awt.Color(150, 180, 200));
                }
            }
        });
    }

    /**
     * Añade placeholder dinámico a un campo de contraseña. Maneja la
     * visibilidad de caracteres apropiadamente: muestra texto normal para
     * placeholder y oculta con bullets para contraseña real.
     *
     * @param campo Campo de contraseña al que aplicar el placeholder
     * @param placeholder Texto del placeholder a mostrar
     */
    private void agregarPlaceholderPassword(javax.swing.JPasswordField campo, String placeholder) {
        campo.setEchoChar((char) 0); // Mostrar texto normal para el placeholder
        campo.setText(placeholder);
        campo.setForeground(new java.awt.Color(150, 180, 200));

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (String.valueOf(campo.getPassword()).equals(placeholder)) {
                    campo.setText("");
                    campo.setEchoChar('•');
                    campo.setForeground(java.awt.Color.WHITE);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (String.valueOf(campo.getPassword()).trim().isEmpty()) {
                    campo.setEchoChar((char) 0);
                    campo.setText(placeholder);
                    campo.setForeground(new java.awt.Color(150, 180, 200));
                }
            }
        });
    }

    /**
     * Configura estilos visuales de todos los componentes del formulario.
     */
    private void configurarEstilos() {
        // Configurar bordes redondeados en todos los campos
        configurarBordeCampo(txtNombre);
        configurarBordeCampo(txtApellidos);
        configurarBordeCampo(txtEmail);
        configurarBordeCampo(txtPassword);
        configurarBordeCampo(txtConfirmarPassword);

        // Configurar efectos focus en campos
        configurarEfectoFocus(txtNombre);
        configurarEfectoFocus(txtApellidos);
        configurarEfectoFocus(txtEmail);
        configurarEfectoFocus(txtPassword);
        configurarEfectoFocus(txtConfirmarPassword);

        // Configurar cursor en botones
        btnRegistrarse.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Efecto hover en botón Registrarse
        btnRegistrarse.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnRegistrarse.setBackground(new java.awt.Color(0, 200, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnRegistrarse.setBackground(new java.awt.Color(0, 230, 255));
            }
        });

        // Configurar botón volver si existe
        if (btnVolver != null) {
            btnVolver.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }

    }

    /**
     * Configura efecto visual de focus en campos de texto. Aplica borde más
     * brillante y grueso cuando el campo obtiene focus, volviendo al borde
     * normal cuando pierde focus.
     *
     * @param campo Campo de texto al que aplicar el efecto focus
     */
    private void configurarEfectoFocus(javax.swing.text.JTextComponent campo) {
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Borde más brillante al hacer focus
                campo.setBorder(new javax.swing.border.AbstractBorder() {
                    @Override
                    public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                        g2d.setRenderingHint(
                                java.awt.RenderingHints.KEY_ANTIALIASING,
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        // Borde más brillante y grueso
                        g2d.setColor(new java.awt.Color(0, 230, 255));
                        g2d.setStroke(new java.awt.BasicStroke(3));
                        g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 15, 15);

                        g2d.dispose();
                    }

                    @Override
                    public java.awt.Insets getBorderInsets(java.awt.Component c) {
                        return new java.awt.Insets(10, 15, 10, 15);
                    }
                });
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                // Volver al borde normal
                configurarBordeCampo(campo);
            }
        });
    }

    /**
     * Configura borde redondeado personalizado para campos de texto. Aplica
     * borde azul redondeado con anti-aliasing y padding interno apropiado para
     * mejorar la apariencia visual.
     *
     * @param campo Campo de texto al que aplicar el borde personalizado
     */
    private void configurarBordeCampo(javax.swing.text.JTextComponent campo) {
        campo.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();

                g2d.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2d.setColor(new java.awt.Color(100, 200, 255));
                g2d.setStroke(new java.awt.BasicStroke(2));

                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 15, 15);

                g2d.dispose();
            }

            @Override
            public java.awt.Insets getBorderInsets(java.awt.Component c) {
                return new java.awt.Insets(10, 15, 10, 15);
            }
        });
        campo.setCaretColor(java.awt.Color.WHITE);
    }

    /**
     * Configura el panel formulario con bordes redondeados personalizados.
     */
    private void configurarPanelRedondeado() {
        // Crear un borde personalizado redondeado
        panelFormulario.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();

                g2d.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2d.setColor(new java.awt.Color(100, 200, 255));
                g2d.setStroke(new java.awt.BasicStroke(2));

                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 20, 20);

                g2d.dispose();
            }

            @Override
            public java.awt.Insets getBorderInsets(java.awt.Component c) {
                return new java.awt.Insets(2, 2, 2, 2);
            }
        });

        panelFormulario.setOpaque(false);
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error de Validación",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Configura el botón de retorno con icono personalizado.
     */
    private void configurarBotonVolver() {
        try {
            // Cargar icono
            java.awt.Image imagen = javax.imageio.ImageIO.read(
                    getClass().getResource("/gestorincidencias/recursos/icono_volver.png")
            );

            // Escalar a tamaño apropiado
            java.awt.Image imagenEscalada = imagen.getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);

            // Aplicar al botón
            btnVolver.setIcon(new javax.swing.ImageIcon(imagenEscalada));
            btnVolver.setText(""); // Sin texto
            btnVolver.setToolTipText("Volver al login");
            btnVolver.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Efecto hover - cambiar opacidad o tamaño
            btnVolver.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    // Hacer el icono un poco más grande
                    java.awt.Image imgHover = imagen.getScaledInstance(36, 36, java.awt.Image.SCALE_SMOOTH);
                    btnVolver.setIcon(new javax.swing.ImageIcon(imgHover));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    // Volver al tamaño normal
                    btnVolver.setIcon(new javax.swing.ImageIcon(imagenEscalada));
                }
            });

        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de volver: " + e.getMessage());
            // Fallback: usar texto
            btnVolver.setText("←");
            btnVolver.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 24));
            btnVolver.setForeground(new java.awt.Color(150, 220, 255));
        }
    }

    /**
     * Procesa el registro completo del nuevo usuario con validaciones.
     */
    private void procesarRegistro() {
        // Obtener datos del formulario
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmarPassword = new String(txtConfirmarPassword.getPassword()).trim();

        // Validaciones
        if (!validarCampos(nombre, apellidos, email, password, confirmarPassword)) {
            return;
        }

        try {
            // Crear objeto usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellidos(apellidos);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(password);
            nuevoUsuario.setRol(Rol.CLIENTE);

            // Guardar en base de datos
            GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
            Usuario usuarioCreado = gestorUsuarios.crear(nuevoUsuario);

            // Mostrar confirmación
            JOptionPane.showMessageDialog(
                    this,
                    "¡Usuario registrado exitosamente!\n\n"
                    + "Nombre: " + usuarioCreado.getNombreCompleto() + "\n"
                    + "Email: " + usuarioCreado.getEmail() + "\n"
                    + "Rol: Cliente\n\n"
                    + "Ya puedes iniciar sesión con tus credenciales.",
                    "Registro Exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Limpiar formulario
            limpiarFormulario();

            // Cerrar ventana de registro y volver al login
            dispose();

        } catch (IllegalArgumentException e) {
            // Email duplicado u otro error de validación
            mostrarError("Error en el registro: " + e.getMessage());
        } catch (Exception e) {
            // Error general
            mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valida todos los campos del formulario con reglas específicas. Verifica
     * campos obligatorios, longitudes mínimas, formato de email, unicidad de
     * email y coincidencia de contraseñas.
     *
     * @param nombre Nombre del usuario a validar
     * @param apellidos Apellidos del usuario a validar
     * @param email Email del usuario a validar
     * @param password Contraseña del usuario a validar
     * @param confirmarPassword Confirmación de contraseña a validar
     * @return true si todas las validaciones pasan, false en caso contrario
     */
    private boolean validarCampos(String nombre, String apellidos, String email,
            String password, String confirmarPassword) {

        // Validar nombre
        if (nombre.isEmpty()) {
            mostrarError("Por favor, ingrese su nombre");
            txtNombre.requestFocus();
            return false;
        }

        if (nombre.length() < 2) {
            mostrarError("El nombre debe tener al menos 2 caracteres");
            txtNombre.requestFocus();
            return false;
        }

        // Validar apellidos
        if (apellidos.isEmpty()) {
            mostrarError("Por favor, ingrese sus apellidos");
            txtApellidos.requestFocus();
            return false;
        }

        if (apellidos.length() < 2) {
            mostrarError("Los apellidos deben tener al menos 2 caracteres");
            txtApellidos.requestFocus();
            return false;
        }

        // Validar email
        if (email.isEmpty()) {
            mostrarError("Por favor, ingrese su email");
            txtEmail.requestFocus();
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarError("Por favor, ingrese un email válido");
            txtEmail.requestFocus();
            return false;
        }

        // Validar que el email no exista
        GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
        if (gestorUsuarios.existeEmail(email)) {
            mostrarError("Ya existe un usuario con ese email");
            txtEmail.requestFocus();
            return false;
        }

        // Validar contraseña
        if (password.isEmpty()) {
            mostrarError("Por favor, ingrese una contraseña");
            txtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            txtPassword.requestFocus();
            return false;
        }

        // Validar confirmación de contraseña
        if (confirmarPassword.isEmpty()) {
            mostrarError("Por favor, confirme su contraseña");
            txtConfirmarPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmarPassword)) {
            mostrarError("Las contraseñas no coinciden");
            txtConfirmarPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Limpia todos los campos del formulario restaurando estado inicial.
     */
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtApellidos.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtConfirmarPassword.setText("");
        txtNombre.requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelFondo = new javax.swing.JPanel();
        panelFormulario = new javax.swing.JPanel();
        txtNombre = new javax.swing.JTextField();
        txtApellidos = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        txtConfirmarPassword = new javax.swing.JPasswordField();
        btnRegistrarse = new javax.swing.JButton();
        lblTitulo = new javax.swing.JLabel();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Registro - Gestor de Incidencias");
        setResizable(false);
        setSize(new java.awt.Dimension(600, 750));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelFondo.setBackground(new java.awt.Color(20, 40, 60));
        panelFondo.setSize(new java.awt.Dimension(600, 750));
        panelFondo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelFormulario.setBackground(new java.awt.Color(30, 60, 90));
        panelFormulario.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 200, 255), 2));
        panelFormulario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtNombre.setBackground(new java.awt.Color(40, 70, 100));
        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtNombre.setForeground(new java.awt.Color(255, 255, 255));
        txtNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNombreActionPerformed(evt);
            }
        });
        panelFormulario.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, 400, 45));

        txtApellidos.setBackground(new java.awt.Color(40, 70, 100));
        txtApellidos.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtApellidos.setForeground(new java.awt.Color(255, 255, 255));
        panelFormulario.add(txtApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, 400, 45));

        txtEmail.setBackground(new java.awt.Color(40, 70, 100));
        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(255, 255, 255));
        panelFormulario.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 180, 400, 45));

        txtPassword.setBackground(new java.awt.Color(40, 70, 100));
        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtPassword.setForeground(new java.awt.Color(255, 255, 255));
        txtPassword.setText("jPasswordField1");
        panelFormulario.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 250, 400, 45));

        txtConfirmarPassword.setBackground(new java.awt.Color(40, 70, 100));
        txtConfirmarPassword.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtConfirmarPassword.setForeground(new java.awt.Color(255, 255, 255));
        txtConfirmarPassword.setText("jPasswordField1");
        panelFormulario.add(txtConfirmarPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 320, 400, 45));

        btnRegistrarse.setBackground(new java.awt.Color(0, 230, 255));
        btnRegistrarse.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnRegistrarse.setForeground(new java.awt.Color(10, 30, 50));
        btnRegistrarse.setText("REGISTRARSE");
        btnRegistrarse.setFocusPainted(false);
        btnRegistrarse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarseActionPerformed(evt);
            }
        });
        panelFormulario.add(btnRegistrarse, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 400, 400, 55));

        panelFondo.add(panelFormulario, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 120, 500, 580));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 230, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("CREAR CUENTA");
        panelFondo.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 40, 300, 50));

        btnVolver.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/icono_volver.png"))); // NOI18N
        btnVolver.setBorderPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });
        panelFondo.add(btnVolver, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 50, 50));

        getContentPane().add(panelFondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 600, 750));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNombreActionPerformed

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnRegistrarseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarseActionPerformed
        procesarRegistro();
    }//GEN-LAST:event_btnRegistrarseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRegistrarse;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelFondo;
    private javax.swing.JPanel panelFormulario;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JPasswordField txtConfirmarPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables
}
