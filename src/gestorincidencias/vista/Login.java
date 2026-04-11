package gestorincidencias.vista;

import gestorincidencias.modelo.Usuario;
import gestorincidencias.util.GestorUsuarios;
import gestorincidencias.util.SesionUsuario;

/**
 * Ventana de autenticación del sistema Gestor de Incidencias.
 *
 * <p>
 * Proporciona una interfaz de login con diseño moderno que incluye:</p>
 * <ul>
 * <li>Validación de credenciales con encriptación segura</li>
 * <li>Placeholders dinámicos en campos de entrada</li>
 * <li>Fondo personalizable (imagen o gradiente)</li>
 * <li>Efectos visuales y bordes redondeados</li>
 * <li>Acceso a ventana de registro</li>
 * </ul>
 *
 * <p>
 * La clase maneja la autenticación de usuarios y establece la sesión para
 * dirigir al usuario a la ventana principal según su rol.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class Login extends javax.swing.JFrame {

    /**
     * Inicializa la ventana de login y configura todos sus elementos visuales.
     * Establece el fondo, logo, placeholders, estilos y configuración general.
     */
    public Login() {
        initComponents();
        configurarFondo();
        agregarLogo();
        configurarPlaceholders();
        configurarEstilos();
        configurarVentana();
    }

    /**
     * Valida las credenciales del usuario utilizando autenticación segura.
     * Delega la verificación al gestor de usuarios que maneja encriptación.
     *
     * @param email Email del usuario a autenticar
     * @param password Contraseña en texto plano a verificar
     * @return Usuario autenticado o null si las credenciales son inválidas
     */
    private Usuario validarCredenciales(String email, String password) {
        GestorUsuarios gestor = GestorUsuarios.getInstance();
        return gestor.autenticar(email, password);
    }

    /**
     * Obtiene el valor real de un campo de texto, excluyendo el placeholder.
     *
     * @param campo Campo de texto a evaluar
     * @param placeholder Texto del placeholder a excluir
     * @return Valor real del campo o cadena vacía si contiene solo el
     * placeholder
     */
    private String obtenerValorCampo(javax.swing.JTextField campo, String placeholder) {
        String texto = campo.getText().trim();
        return texto.equals(placeholder) ? "" : texto;
    }

    /**
     * Obtiene el valor real de un campo de contraseña, excluyendo el
     * placeholder.
     *
     * @param campo Campo de contraseña a evaluar
     * @param placeholder Texto del placeholder a excluir
     * @return Valor real del campo o cadena vacía si contiene solo el
     * placeholder
     */
    private String obtenerValorPassword(javax.swing.JPasswordField campo, String placeholder) {
        String texto = String.valueOf(campo.getPassword()).trim();
        return texto.equals(placeholder) ? "" : texto;
    }

    /**
     * Abre la ventana principal según el rol del usuario autenticado. Muestra
     * información de login exitoso en consola y cierra la ventana actual.
     */
    private void abrirVentanaPrincipal() {
        SesionUsuario sesion = SesionUsuario.getInstance();

        // Mostrar información en consola
        System.out.println("=== LOGIN EXITOSO ===");
        System.out.println(sesion.getInfoSesion());
        System.out.println("====================");

        // Abrir ventana principal
        java.awt.EventQueue.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });

        // Cerrar login
        this.dispose();
    }

    /**
     * Configura la imagen de fondo con fallback a gradiente si falla la carga.
     * Intenta cargar la imagen desde recursos y usa gradiente alternativo si no
     * está disponible.
     */
    private void configurarFondo() {
        try {
            System.out.println("Intentando cargar imagen de fondo...");

            // Intentar cargar la imagen
            java.net.URL urlImagen = getClass().getResource("/gestorincidencias/recursos/fondo.png");

            if (urlImagen == null) {
                System.err.println("ERROR: No se encontró la imagen en /gestorincidencias/recursos/fondo.png");
                System.err.println("Usando gradiente como fondo...");
                usarGradienteComoFondo();
                return;
            }

            final java.awt.Image imagenFondo = javax.imageio.ImageIO.read(urlImagen);

            if (imagenFondo == null) {
                System.err.println("ERROR: La imagen se encontró pero no se pudo leer");
                usarGradienteComoFondo();
                return;
            }

            System.out.println("Imagen cargada correctamente!");
            System.out.println("Tamaño de imagen: " + imagenFondo.getWidth(null) + "x" + imagenFondo.getHeight(null));

            // Crear panel personalizado con la imagen
            javax.swing.JPanel nuevoPanel = new javax.swing.JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    super.paintComponent(g);
                    // Dibujar imagen escalada al tamaño del panel
                    g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
                }
            };

            // Configurar el nuevo panel
            nuevoPanel.setLayout(null);
            nuevoPanel.setPreferredSize(new java.awt.Dimension(1000, 700));
            nuevoPanel.setSize(1000, 700);

            // Copiar todos los componentes del panel actual al nuevo
            java.awt.Component[] componentes = panelFondo.getComponents();
            setContentPane(nuevoPanel);

            for (java.awt.Component comp : componentes) {
                nuevoPanel.add(comp);
            }

            // Actualizar referencia
            panelFondo = nuevoPanel;

            // Forzar repintado
            revalidate();
            repaint();

        } catch (Exception e) {
            System.err.println("ERROR al cargar la imagen de fondo: " + e.getMessage());
            usarGradienteComoFondo();
        }
    }

    /**
     * Añade el logo a la pantalla de login si está disponible en los recursos.
     * Escala automáticamente el logo a 120x120 píxeles y lo centra debajo del
     * título.
     */
    private void agregarLogo() {
        try {
            // Cargar la imagen del logo
            java.awt.Image imagenLogo = javax.imageio.ImageIO.read(
                    getClass().getResource("/gestorincidencias/recursos/logo.png")
            );

            // Escalar el logo a un tamaño apropiado
            int tamañoLogo = 120;
            java.awt.Image logoEscalado = imagenLogo.getScaledInstance(
                    tamañoLogo,
                    tamañoLogo,
                    java.awt.Image.SCALE_SMOOTH
            );

            // Crear JLabel con el logo
            javax.swing.JLabel lblLogo = new javax.swing.JLabel(
                    new javax.swing.ImageIcon(logoEscalado)
            );

            // Posicionar el logo
            // Debajo del título y centrado horizontalmente
            int xLogo = (1000 - tamañoLogo) / 2; // Centrado (ancho ventana = 1000)
            int yLogo = 140;

            lblLogo.setBounds(xLogo, yLogo, tamañoLogo, tamañoLogo);

            // Añadir al panel de fondo
            panelFondo.add(lblLogo);

            // Asegurarse de que esté visible (traer al frente)
            panelFondo.setComponentZOrder(lblLogo, 0);

            System.out.println("Logo añadido correctamente");

        } catch (Exception e) {
            System.err.println("ERROR: No se pudo cargar el logo: " + e.getMessage());
        }
    }

    /**
     * Configura los placeholders dinámicos en los campos de entrada. Establece
     * "Email" como placeholder del campo usuario y "Contraseña" para el campo
     * password.
     */
    private void configurarPlaceholders() {
        // Placeholder para campo Usuario
        agregarPlaceholder(txtUsuario, "Email");

        // Placeholder para campo Contraseña
        agregarPlaceholderPassword(txtPassword, "Contraseña");
    }

    /**
     * Configura estilos visuales avanzados incluyendo bordes redondeados y
     * efectos hover. Aplica anti-aliasing, bordes personalizados y eventos de
     * mouse para mejor experiencia visual.
     */
    private void configurarEstilos() {
        // Hacer el panel formulario semi-transparente
        panelFormulario.setOpaque(false);

        // ===== ESTILO DE CAMPOS DE TEXTO CON BORDES REDONDEADOS =====
        // Campo Usuario - Borde redondeado
        txtUsuario.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();

                // Anti-aliasing para bordes suaves
                g2d.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Color del borde (cyan)
                g2d.setColor(new java.awt.Color(100, 200, 255));
                g2d.setStroke(new java.awt.BasicStroke(2));

                // Dibujar rectángulo redondeado
                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 15, 15);

                g2d.dispose();
            }

            @Override
            public java.awt.Insets getBorderInsets(java.awt.Component c) {
                return new java.awt.Insets(10, 15, 10, 15); 
            }
        });

        // Campo Contraseña - Borde redondeado
        txtPassword.setBorder(new javax.swing.border.AbstractBorder() {
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

        // ===== ESTILO DE BOTONES CON BORDES REDONDEADOS =====
        // Configurar botón Entrar
        btnEntrar.setContentAreaFilled(false); // No usar fondo por defecto
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setOpaque(false);

        // Pintar el botón Entrar con bordes redondeados
        btnEntrar.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();

                // Anti-aliasing
                g2d.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Fondo del botón
                g2d.setColor(b.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);

                // Texto del botón
                g2d.setColor(b.getForeground());
                g2d.setFont(b.getFont());

                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int textX = (c.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2d.drawString(b.getText(), textX, textY);

                g2d.dispose();
            }
        });

        // Configurar botón Registrarse
        btnRegistrarse.setContentAreaFilled(false);
        btnRegistrarse.setFocusPainted(false);
        btnRegistrarse.setBorderPainted(false);
        btnRegistrarse.setOpaque(false);

        // Pintar el botón Registrarse con bordes redondeados
        btnRegistrarse.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();

                g2d.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Fondo del botón
                g2d.setColor(b.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);

                // Borde del botón
                g2d.setColor(new java.awt.Color(100, 200, 255));
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 15, 15);

                // Texto del botón
                g2d.setColor(b.getForeground());
                g2d.setFont(b.getFont());

                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int textX = (c.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2d.drawString(b.getText(), textX, textY);

                g2d.dispose();
            }
        });

        // ===== EFECTOS HOVER =====
        // Efecto hover para campo Usuario
        txtUsuario.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtUsuario.setBorder(new javax.swing.border.AbstractBorder() {
                    @Override
                    public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                        // Borde más brillante al hacer focus
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
                
                configurarBordeNormalCampos();
            }
        });

        // Efecto hover para campo Contraseña
        txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPassword.setBorder(new javax.swing.border.AbstractBorder() {
                    @Override
                    public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

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
                configurarBordeNormalCampos();
            }
        });

        // Efecto hover para botón Entrar
        btnEntrar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEntrar.setBackground(new java.awt.Color(0, 200, 230));
                btnEntrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEntrar.setBackground(new java.awt.Color(0, 230, 255));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnEntrar.setBackground(new java.awt.Color(0, 180, 210));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnEntrar.setBackground(new java.awt.Color(0, 230, 255));
            }
        });

        // Efecto hover para botón Registrarse
        btnRegistrarse.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnRegistrarse.setBackground(new java.awt.Color(30, 60, 90));
                btnRegistrarse.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnRegistrarse.setBackground(new java.awt.Color(20, 40, 60));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnRegistrarse.setBackground(new java.awt.Color(40, 70, 100));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnRegistrarse.setBackground(new java.awt.Color(30, 60, 90));
            }
        });

        // Permitir Enter para login
        txtPassword.addActionListener(evt -> {
            if (btnEntrar.isEnabled()) {
                btnEntrar.doClick();
            }
        });
    }

    /**
     * Configuración final de la ventana incluyendo tamaño, posición y
     * comportamiento del foco. Centra la ventana, la hace no redimensionable y
     * configura el manejo de foco inicial.
     */
    private void configurarVentana() {
        setSize(1000, 700);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);

        setFocusTraversalKeysEnabled(false);

        // Poner el foco en la ventana, no en los campos
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                // Quitar foco de cualquier campo y ponerlo en la ventana
                Login.this.requestFocusInWindow();
            }
        });

        // Enfocar el panel de fondo
        panelFondo.setFocusable(true);
        panelFondo.requestFocusInWindow();
    }

    /**
     * Usa un gradiente como fondo alternativo
     */
    private void usarGradienteComoFondo() {
        System.out.println("Configurando gradiente como fondo...");

        javax.swing.JPanel nuevoPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;

                g2d.setRenderingHint(
                        java.awt.RenderingHints.KEY_RENDERING,
                        java.awt.RenderingHints.VALUE_RENDER_QUALITY
                );

                // Crear gradiente con los colores de imagen
                java.awt.GradientPaint gradiente = new java.awt.GradientPaint(
                        0, 0, new java.awt.Color(10, 25, 47), 
                        0, getHeight(), new java.awt.Color(29, 53, 87) 
                );
                g2d.setPaint(gradiente);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new java.awt.Color(69, 123, 157, 50)); 
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawLine(100, 200, 900, 300);
                g2d.drawLine(100, 500, 900, 400);
            }
        };

        // Configurar el nuevo panel
        nuevoPanel.setLayout(null);
        nuevoPanel.setPreferredSize(new java.awt.Dimension(1000, 700));
        nuevoPanel.setSize(1000, 700);

        // Copiar todos los componentes
        java.awt.Component[] componentes = panelFondo.getComponents();
        setContentPane(nuevoPanel);

        for (java.awt.Component comp : componentes) {
            nuevoPanel.add(comp);
        }

        // Actualizar referencia
        panelFondo = nuevoPanel;

        revalidate();
        repaint();
    }

    /**
     * Añade placeholder a un JTextField.
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
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(new java.awt.Color(150, 180, 200));
                }
            }
        });
    }

    /**
     * Añade placeholder a un JPasswordField.
     */
    private void agregarPlaceholderPassword(javax.swing.JPasswordField campo, String placeholder) {
        campo.setEchoChar((char) 0); 
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
                if (String.valueOf(campo.getPassword()).isEmpty()) {
                    campo.setEchoChar((char) 0); 
                    campo.setText(placeholder);
                    campo.setForeground(new java.awt.Color(150, 180, 200));
                }
            }
        });
    }

    /**
     * Configura el borde normal de los campos cuando pierden el foco.
     */
    private void configurarBordeNormalCampos() {
        txtUsuario.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
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

        txtPassword.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
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
        txtUsuario = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        btnEntrar = new javax.swing.JButton();
        btnRegistrarse = new javax.swing.JButton();
        lblTitulo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Login - Gestor de Incidencias");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelFondo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelFormulario.setOpaque(false);
        panelFormulario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtUsuario.setBackground(new java.awt.Color(30, 60, 90));
        txtUsuario.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtUsuario.setForeground(new java.awt.Color(255, 255, 255));
        txtUsuario.setText("jTextField1");
        txtUsuario.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 200, 255), 2));
        panelFormulario.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 300, 45));

        txtPassword.setBackground(new java.awt.Color(30, 60, 90));
        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtPassword.setForeground(new java.awt.Color(255, 255, 255));
        txtPassword.setText("jPasswordField1");
        txtPassword.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 200, 255), 2));
        panelFormulario.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 300, 45));

        btnEntrar.setBackground(new java.awt.Color(0, 230, 255));
        btnEntrar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnEntrar.setForeground(new java.awt.Color(10, 30, 255));
        btnEntrar.setText("ENTRAR");
        btnEntrar.setBorderPainted(false);
        btnEntrar.setFocusPainted(false);
        btnEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrarActionPerformed(evt);
            }
        });
        panelFormulario.add(btnEntrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 170, 300, 50));

        btnRegistrarse.setBackground(new java.awt.Color(20, 40, 60));
        btnRegistrarse.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRegistrarse.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrarse.setText("REGISTRARSE");
        btnRegistrarse.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 200, 255), 2));
        btnRegistrarse.setFocusPainted(false);
        btnRegistrarse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarseActionPerformed(evt);
            }
        });
        panelFormulario.add(btnRegistrarse, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 245, 300, 45));

        panelFondo.add(panelFormulario, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 290, 300, 340));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("GESTOR DE INCIDENCIAS");
        panelFondo.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 80, 400, 50));

        getContentPane().add(panelFondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1000, 700));
        panelFondo.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEntrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntrarActionPerformed
        try {
            // Obtener valores 
            String usuario = obtenerValorCampo(txtUsuario, "Email");
            String password = obtenerValorPassword(txtPassword, "Contraseña");

            // Verificar campos vacíos
            if (usuario.isEmpty()) {
                mostrarError("Por favor, ingrese su email");
                txtUsuario.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                mostrarError("Por favor, ingrese su contraseña");
                txtPassword.requestFocus();
                return;
            }

            // Validar formato de email básico
            if (!usuario.contains("@")) {
                mostrarError("Por favor, ingrese un email válido");
                txtUsuario.requestFocus();
                return;
            }

            // Validar credenciales
            Usuario usuarioAutenticado = validarCredenciales(usuario, password);

            if (usuarioAutenticado != null) {
                // Iniciar sesión
                SesionUsuario sesion = SesionUsuario.getInstance();
                sesion.iniciarSesion(usuarioAutenticado);

                // Login exitoso - cerrar login
                this.dispose();

                // Abrir ventana principal
                abrirVentanaPrincipal();

            } else {
                mostrarError("Email o contraseña incorrectos");
                txtPassword.setText("");
                txtPassword.setEchoChar((char) 0);
                txtPassword.setText("Contraseña");
                txtPassword.setForeground(new java.awt.Color(150, 180, 200));
                txtUsuario.requestFocus();
            }
        } catch (Exception e) {
            System.err.println("Error durante el login: " + e.getMessage());
            mostrarError("Error interno del sistema. Inténtelo de nuevo.");
        }
    }

    /**
     * Muestra un mensaje de error en un diálogo modal con estilo apropiado.
     *
     * @param mensaje Texto del error a mostrar al usuario
     */
    private void mostrarError(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(this,
                mensaje,
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_btnEntrarActionPerformed

    private void btnRegistrarseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarseActionPerformed
        // Ocultar la ventana de login temporalmente
        this.setVisible(false);

        // Crear la ventana de registro
        Registro ventanaRegistro = new Registro();

        // Añadir listener para cuando se cierre el registro
        ventanaRegistro.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                
                Login.this.setVisible(true);
            }
        });

        // Mostrar la ventana de registro
        ventanaRegistro.setVisible(true);

    }//GEN-LAST:event_btnRegistrarseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEntrar;
    private javax.swing.JButton btnRegistrarse;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelFondo;
    private javax.swing.JPanel panelFormulario;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
