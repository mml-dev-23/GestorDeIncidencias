package gestorincidencias.vista;

import gestorincidencias.modelo.*;
import gestorincidencias.modelo.Rol;
import gestorincidencias.modelo.Usuario;
import gestorincidencias.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Diálogo modal para creación de nuevos usuarios con sistema de placeholders.
 *
 * <p>
 * Proporciona formulario de registro con campos de texto que muestran
 * placeholders dinámicos y validaciones robustas. Los usuarios se crean con rol
 * CLIENTE por defecto.</p>
 *
 * <p>
 * <strong>Características:</strong></p>
 * <ul>
 * <li>Placeholders dinámicos en campos de texto</li>
 * <li>Validación de formato de email con expresión regular</li>
 * <li>Verificación de unicidad de email en el sistema</li>
 * <li>Contraseña mínima de 6 caracteres</li>
 * <li>Todos los campos son obligatorios</li>
 * </ul>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class DialogoNuevoUsuario extends javax.swing.JDialog {

    private GestorUsuarios gestorUsuarios;
    private boolean usuarioCreado = false;

    // Colores para placeholder
    private final Color COLOR_PLACEHOLDER = new Color(150, 150, 150);
    private final Color COLOR_TEXTO = new Color(60, 60, 60);

    /**
     * Inicializa el diálogo de nuevo usuario con configuración completa.
     *
     * <p>
     * Configura el diálogo modal, inicializa el gestor de usuarios, establece
     * placeholders en campos de texto y configura el manejo de foco para evitar
     * que el primer campo esté activo al abrir.</p>
     *
     * @param parent Ventana padre del diálogo
     * @param modal Si el diálogo debe ser modal (bloquea interacción con
     * ventana padre)
     */
    public DialogoNuevoUsuario(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        gestorUsuarios = GestorUsuarios.getInstance();

        initComponents();
        configurarDialogo();
        configurarPlaceholders();
        // QUITAR FOCO DE LOS CAMPOS AL ABRIR
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                // Poner el foco en el diálogo en lugar del primer campo
                DialogoNuevoUsuario.this.requestFocusInWindow();
            }
        });
    }

    /**
     * Configura el diálogo.
     */
    private void configurarDialogo() {
        setLocationRelativeTo(getParent());

        // Configurar eventos de botones
        btnCrear.addActionListener(evt -> crearUsuario());
        btnCancelar.addActionListener(evt -> cancelar());

        // Estilos
        btnCrear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Configura los placeholders en los campos de texto.
     */
    private void configurarPlaceholders() {
        // Configurar placeholder para txtNombre
        configurarPlaceholder(txtNombre, "Nombre");

        // Configurar placeholder para txtApellidos
        configurarPlaceholder(txtApellidos, "Apellidos");

        // Configurar placeholder para txtEmail
        configurarPlaceholder(txtEmail, "Email");

        // Configurar placeholder para txtPassword (especial)
        configurarPlaceholderPassword(txtPassword, "Contraseña");
    }

    /**
     * Configura placeholder dinámico para un campo de texto normal. El
     * placeholder desaparece al obtener foco y reaparece si el campo queda
     * vacío.
     *
     * @param textField Campo de texto a configurar
     * @param placeholder Texto del placeholder a mostrar
     */
    private void configurarPlaceholder(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(COLOR_PLACEHOLDER);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(COLOR_TEXTO);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });
    }

    /**
     * Configura placeholder dinámico para un campo de contraseña. Maneja la
     * ocultación de caracteres cuando el campo contiene texto real.
     *
     * @param passwordField Campo de contraseña a configurar
     * @param placeholder Texto del placeholder a mostrar
     */
    private void configurarPlaceholderPassword(JPasswordField passwordField, String placeholder) {
        // Inicialmente mostrar el placeholder como texto normal
        passwordField.setEchoChar((char) 0);
        passwordField.setText(placeholder);
        passwordField.setForeground(COLOR_PLACEHOLDER);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String text = new String(passwordField.getPassword());
                if (text.equals(placeholder)) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•'); // Activar ocultación
                    passwordField.setForeground(COLOR_TEXTO);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String text = new String(passwordField.getPassword());
                if (text.trim().isEmpty()) {
                    passwordField.setEchoChar((char) 0); // Desactivar ocultación
                    passwordField.setText(placeholder);
                    passwordField.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });
    }

    /**
     * Obtiene el texto real del campo (sin placeholder).
     */
    private String obtenerTexto(JTextField textField, String placeholder) {
        String texto = textField.getText().trim();
        return texto.equals(placeholder) ? "" : texto;
    }

    /**
     * Obtiene la contraseña real del campo (sin placeholder).
     */
    private String obtenerPassword(JPasswordField passwordField, String placeholder) {
        String texto = new String(passwordField.getPassword()).trim();
        return texto.equals(placeholder) ? "" : texto;
    }

    /**
     * Crea un nuevo usuario con validaciones completas. Verifica campos
     * obligatorios, formato de email, longitud de contraseña y unicidad del
     * email antes de guardar en base de datos.
     */
    private void crearUsuario() {
        // Obtener datos (sin placeholders)
        String nombre = obtenerTexto(txtNombre, "Nombre");
        String apellidos = obtenerTexto(txtApellidos, "Apellidos");
        String email = obtenerTexto(txtEmail, "Email");
        String password = obtenerPassword(txtPassword, "Contraseña");

        // Validaciones
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        if (apellidos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Los apellidos son obligatorios",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtApellidos.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El email es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                    "El formato del email no es válido",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "La contraseña es obligatoria",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "La contraseña debe tener al menos 6 caracteres",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }

        // Verificar que el email no exista
        if (gestorUsuarios.obtenerPorEmail(email) != null) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un usuario con ese email",
                    "Email duplicado",
                    JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        try {
            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellidos(apellidos);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(password);
            // Por defecto se crea como Cliente
            nuevoUsuario.setRol(Rol.CLIENTE);

            // Guardar
            gestorUsuarios.crear(nuevoUsuario);

            usuarioCreado = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al crear el usuario: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Cancela la creación.
     */
    private void cancelar() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea cancelar?",
                "Confirmar cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.YES_OPTION) {
            usuarioCreado = false;
            dispose();
        }
    }

    /**
     * Indica si se creó exitosamente un usuario en el diálogo.
     *
     * @return true si se creó el usuario, false en caso contrario
     */
    public boolean isUsuarioCreado() {
        return usuarioCreado;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtApellidos = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        btnCrear = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Nuevo Usuario");
        setMinimumSize(new java.awt.Dimension(500, 450));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(500, 450));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(33, 47, 61));
        lblTitulo.setText("Crear Nuevo Usuario");
        getContentPane().add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 440, 30));

        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtNombre.setForeground(new java.awt.Color(150, 150, 150));
        txtNombre.setText("Nombre");
        getContentPane().add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 440, 40));

        txtApellidos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtApellidos.setForeground(new java.awt.Color(150, 150, 150));
        txtApellidos.setText("Apellidos");
        getContentPane().add(txtApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 440, 40));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(150, 150, 150));
        txtEmail.setText("Email");
        getContentPane().add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 440, 40));

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPassword.setForeground(new java.awt.Color(150, 150, 150));
        txtPassword.setText("Contraseña");
        txtPassword.setEchoChar('0');
        getContentPane().add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 220, 440, 40));

        btnCrear.setBackground(new java.awt.Color(0, 230, 255));
        btnCrear.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCrear.setForeground(new java.awt.Color(255, 255, 255));
        btnCrear.setText("Crear Usuario");
        btnCrear.setFocusPainted(false);
        getContentPane().add(btnCrear, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, 200, 45));

        btnCancelar.setBackground(new java.awt.Color(220, 220, 220));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(60, 60, 60));
        btnCancelar.setText("Cancelar");
        btnCancelar.setFocusPainted(false);
        getContentPane().add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 290, 200, 45));

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCrear;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables
}
