package gestorincidencias.vista;

import gestorincidencias.util.GestorIncidencias;
import gestorincidencias.modelo.*;
import java.util.List;
import java.time.format.DateTimeFormatter;
import gestorincidencias.util.SesionUsuario;
import gestorincidencias.modelo.Rol;
import gestorincidencias.util.GestorComentarios;
import gestorincidencias.util.GestorUsuarios;
import gestorincidencias.util.SistemaAyuda;
import gestorincidencias.util.WrapLayout;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Panel principal de gestión de incidencias del sistema.
 *
 * <p>
 * Proporciona funcionalidad completa para la administración de incidencias
 * incluyendo:</p>
 * <ul>
 * <li>Visualización en tarjetas con diseño responsive y WrapLayout</li>
 * <li>Sistema de filtros avanzados por prioridad, estado, técnico y texto</li>
 * <li>Control de acceso basado en roles con permisos diferenciados</li>
 * <li>Edición inline con validaciones según rol del usuario</li>
 * <li>Vista detallada lateral con información completa</li>
 * <li>Operaciones CRUD completas (crear, leer, actualizar, eliminar)</li>
 * <li>Ordenamiento configurable y búsqueda en tiempo real</li>
 * <li>Lazy loading optimizado para reutilización de instancias</li>
 * </ul>
 *
 * <p>
 * El panel implementa diferentes niveles de permisos:</p>
 * <ul>
 * <li><strong>ADMINISTRADOR:</strong> Control total sobre todas las
 * incidencias</li>
 * <li><strong>TECNICO:</strong> Puede editar incidencias asignadas o creadas
 * por él</li>
 * <li><strong>CLIENTE:</strong> Solo puede gestionar sus propias
 * incidencias</li>
 * </ul>
 *
 * <p>
 * Utiliza WrapLayout para disposición responsive de tarjetas y mantiene
 * sincronización automática con filtros aplicados tras operaciones CRUD.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class PanelIncidencias extends javax.swing.JPanel {

    // Atributos
    private GestorIncidencias gestorIncidencias;
    private Incidencia incidenciaSeleccionada;
    private DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean modoEdicion = false;
    private boolean ordenFechaDescendente = true;
    private GestorComentarios gestorComentarios;
    private DefaultListModel<String> modeloListaComentarios;
    private List<Comentario> comentariosActuales;
    

    /**
     * Inicializa el panel de incidencias y configura todos sus componentes.
     * Establece el gestor, configura permisos por rol, carga incidencias
     * iniciales y configura estilos y eventos.
     */
    public PanelIncidencias() {
        // Inicializar gestor
        gestorIncidencias = GestorIncidencias.getInstance();
        gestorComentarios = GestorComentarios.getInstance();
        modeloListaComentarios = new DefaultListModel<>();

        initComponents();
        configurarPanel();
        configurarPermisosPorRol();
        cargarIncidencias();
        configurarEstilos();
        configurarEventosEliminar();

    }

    /**
     * Configura las propiedades iniciales del panel incluyendo permisos, combos
     * y layout. Establece WrapLayout para tarjetas, configura placeholders de
     * búsqueda, eventos de filtrado y comportamiento responsive del scroll.
     */
    private void configurarPanel() {

        // Inicializar botón Guardar como deshabilitado
        btnGuardar.setEnabled(false);
        btnGuardar.setBackground(new Color(180, 180, 180));
        comboDetalleCategoria.setVisible(false);
        comboDetallePrioridad.setVisible(false);
        comboDetalleEstado.setVisible(false);
        comboDetalleTecnico.setVisible(false);

        // Cargar datos en los combos 
        cargarCombosEdicion();
        cargarComboFiltroEstados();

        // Configurar combo de asignación (solo visible para Admin)
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();

        if (rol == Rol.ADMINISTRADOR) {
            // Cargar técnicos en el combo
            cargarComboAsignacion();

            // Mostrar el combo
            comboAsignacion.setVisible(true);

            // Añadir evento para filtrar
            comboAsignacion.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    aplicarFiltros();
                }
            });
        } else {
            comboAsignacion.setVisible(false);
        }

        // ===== CONFIGURAR WRAPLAYOUT =====
        panelTarjetas.setLayout(new WrapLayout(FlowLayout.LEFT, 40, 30));

        scrollPanelIncidencias.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int viewportWidth = scrollPanelIncidencias.getViewport().getWidth();
                panelTarjetas.setSize(viewportWidth, panelTarjetas.getHeight());
                panelTarjetas.revalidate();
                panelTarjetas.repaint();
            }
        });

        panelTarjetas.setPreferredSize(new Dimension(700, 2000));

        // Al hacer clic en el área de tarjetas, quitar foco del txtBuscar
        panelTarjetas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (txtBuscar.hasFocus()) {
                    panelTarjetas.requestFocusInWindow();
                }
            }
        });

        // Configurar placeholder en búsqueda
        txtBuscar.setText("Buscar incidencia...");
        txtBuscar.setForeground(new Color(150, 150, 150));

        txtBuscar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtBuscar.getText().equals("Buscar incidencia...")) {
                    txtBuscar.setText("");
                    txtBuscar.setForeground(new Color(60, 60, 60));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String texto = txtBuscar.getText().trim();
                if (texto.isEmpty() || texto.equals("")) {
                    txtBuscar.setText("Buscar incidencia...");
                    txtBuscar.setForeground(new Color(150, 150, 150));
                }
            }
        });

        // Configurar búsqueda en tiempo real
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                aplicarFiltros();
            }
        });

        // Configurar scroll
        scrollPanelIncidencias.getVerticalScrollBar().setUnitIncrement(16);

        // Panel de detalles inicialmente vacío
        limpiarPanelDetalle();

        comboOrden.addActionListener(e -> aplicarFiltros());
        
       // Reemplazar el código anterior por este:

        // Configurar lista de comentarios  
        modeloListaComentarios = new DefaultListModel<>();
        listComentarios.setModel(modeloListaComentarios);
        listComentarios.setCellRenderer(new ComentarioListCellRenderer());
        comentariosActuales = new ArrayList<>();

        // Configurar placeholder en nuevo comentario
        txtNuevoComentario.setText("Añadir comentario...");
        txtNuevoComentario.setForeground(new Color(150, 150, 150));

        txtNuevoComentario.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtNuevoComentario.getText().equals("Añadir comentario...")) {
                    txtNuevoComentario.setText("");
                    txtNuevoComentario.setForeground(new Color(60, 60, 60));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String texto = txtNuevoComentario.getText().trim();
                if (texto.isEmpty()) {
                    txtNuevoComentario.setText("Añadir comentario...");
                    txtNuevoComentario.setForeground(new Color(150, 150, 150));
                }
            }
        });

        // Configurar visibilidad inicial de componentes de comentarios
        configurarVisibilidadComentarios();

        // Botón confirmar resolución: oculto por defecto
        btnConfirmarResolucion.setVisible(false);
        btnConfirmarResolucion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnConfirmarResolucion.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Confirmas que la incidencia ha sido resuelta correctamente?",
                    "Confirmar resolución",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                incidenciaSeleccionada.setEstado(Estado.CERRADA);
                incidenciaSeleccionada.setFechaResolucion(java.time.LocalDateTime.now());
                boolean actualizada = gestorIncidencias.actualizar(incidenciaSeleccionada);
                if (actualizada) {
                    JOptionPane.showMessageDialog(this,
                            "Incidencia cerrada correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    recargarConFiltrosActuales();
                    mostrarDetalleIncidencia(incidenciaSeleccionada);
                }
            }
        });

    }

    /**
     * Configura permisos según el rol del usuario autenticado. Todos los roles
     * pueden ver y usar los botones de edición, pero con diferentes niveles de
     * permisos en la funcionalidad.
     */
    private void configurarPermisosPorRol() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();

        if (rol == null) {
            return;
        }

        // Ambos botones siempre visibles y habilitados
        btnNuevaIncidencia.setVisible(true);
        btnEditar.setVisible(true);
        btnGuardar.setVisible(true);
        btnEditar.setEnabled(true);
        btnGuardar.setEnabled(true);
    }

    /**
     * Configura estilos visuales y efectos hover para botones del panel.
     */
    private void configurarEstilos() {
        // Cursor en botones
        btnNuevaIncidencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover en botón nueva incidencia
        btnNuevaIncidencia.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNuevaIncidencia.setBackground(new Color(0, 200, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNuevaIncidencia.setBackground(new Color(0, 230, 255));
            }
        });

    }

    /**
     * Configura eventos y estilos del botón eliminar.
     */
    private void configurarEventosEliminar() {
        // Evento del botón eliminar
        btnEliminar.addActionListener(e -> eliminarIncidencia());

        // Cursor y efectos hover
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnEliminar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btnEliminar.setBackground(new Color(200, 60, 45));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                btnEliminar.setBackground(new Color(231, 76, 60));
            }
        });
    }
    
    /**
     * Configura la visibilidad de los componentes de comentarios según el rol
     * del usuario
     */
    private void configurarVisibilidadComentarios() {
        // La lista de comentarios siempre visible para todos los roles
        lblComentarios.setVisible(true);
        scrollComentarios.setVisible(true);
        listComentarios.setVisible(true);

        // Se activa solo en modo edición
        scrollNuevoComentario.setVisible(false);
        txtNuevoComentario.setVisible(false);
        btnAñadirComentario.setVisible(false);
        btnEditarComentario.setVisible(false);
        btnEliminarComentario.setVisible(false);

        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();

        // Solo configurar eventos para técnicos
        if (rol == Rol.TECNICO) {
            // Configurar efectos hover para botón añadir
            btnAñadirComentario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnAñadirComentario.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btnAñadirComentario.isVisible()) {
                        btnAñadirComentario.setBackground(new Color(41, 128, 185));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (btnAñadirComentario.isVisible()) {
                        btnAñadirComentario.setBackground(new Color(52, 152, 219));
                    }
                }
            });

            // Configurar efectos hover para botón editar
            btnEditarComentario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnEditarComentario.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btnEditarComentario.isVisible()) {
                        btnEditarComentario.setBackground(new Color(39, 174, 96));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (btnEditarComentario.isVisible()) {
                        btnEditarComentario.setBackground(new Color(46, 204, 113));
                    }
                }
            });

            // Configurar efectos hover para botón eliminar
            btnEliminarComentario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnEliminarComentario.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btnEliminarComentario.isVisible()) {
                        btnEliminarComentario.setBackground(new Color(192, 57, 43));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (btnEliminarComentario.isVisible()) {
                        btnEliminarComentario.setBackground(new Color(231, 76, 60));
                    }
                }
            });

            // Listener para selección de comentarios 
            listComentarios.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && modoEdicion) {
                    mostrarBotonesEdicionComentario();
                }
            });
        }
    }


    /**
     * Carga y muestra todas las incidencias según el rol del usuario actual.
     * Limpia el panel, obtiene incidencias filtradas por rol, crea tarjetas y
     * actualiza contador. Muestra mensaje si no hay incidencias.
     */
    private void cargarIncidencias() {
        // Limpiar panel de tarjetas
        panelTarjetas.removeAll();

        // Obtener incidencias según el rol
        List<Incidencia> incidencias = obtenerIncidenciasSegunRol();

        // Crear tarjetas para cada incidencia
        for (Incidencia inc : incidencias) {
            JPanel tarjeta = crearTarjetaIncidencia(inc);
            panelTarjetas.add(tarjeta);
        }

        // Si no hay incidencias, mostrar mensaje
        if (incidencias.isEmpty()) {
            mostrarMensajeSinIncidencias();
        }

        panelTarjetas.revalidate();
        panelTarjetas.repaint();
        actualizarContador(incidencias.size());
        resetearScroll();

    }

    /**
     * Carga los datos en los combos de edición.
     */
    private void cargarCombosEdicion() {
        // Limpiar combos
        comboDetalleCategoria.removeAllItems();
        comboDetallePrioridad.removeAllItems();
        comboDetalleEstado.removeAllItems();
        comboDetalleTecnico.removeAllItems();

        // Cargar categorías
        for (Categoria cat : Categoria.values()) {
            comboDetalleCategoria.addItem(cat.getNombre());
        }

        // Cargar prioridades
        for (Prioridad pri : Prioridad.values()) {
            comboDetallePrioridad.addItem(pri.getNombre());
        }

        // Cargar estados
        for (Estado est : Estado.values()) {
            comboDetalleEstado.addItem(est.getNombre());
        }

        // Cargar técnicos
        comboDetalleTecnico.addItem("Sin asignar");
        // Obtener técnicos desde el gestor
        List<String> tecnicos = gestorIncidencias.obtenerTecnicos();
        for (String tecnico : tecnicos) {
            comboDetalleTecnico.addItem(tecnico);
        }
    }

    /**
     * Carga los técnicos en el combo de asignación.
     */
    private void cargarComboAsignacion() {
        comboAsignacion.removeAllItems();

        // Opción para ver todas
        comboAsignacion.addItem("Todos los técnicos");

        // Opción para ver sin asignar
        comboAsignacion.addItem("Sin asignar");

        // Obtener lista de nombres de técnicos desde el gestor
        List<String> tecnicos = gestorIncidencias.obtenerTecnicos();

        // Añadir cada técnico al combo
        for (String nombreTecnico : tecnicos) {
            comboAsignacion.addItem(nombreTecnico);
        }
    }
    
    /**
     * Carga los estados en el combo de filtro dinámicamente desde el enum.
     */
    private void cargarComboFiltroEstados() {
        java.awt.event.ActionListener[] listeners = comboEstado.getActionListeners();
        for (java.awt.event.ActionListener l : listeners) {
            comboEstado.removeActionListener(l);
        }

        comboEstado.removeAllItems();
        comboEstado.addItem("Todos los estados");
        for (Estado est : Estado.values()) {
            comboEstado.addItem(est.getNombre());
        }

        // Restaurar el listener
        for (java.awt.event.ActionListener l : listeners) {
            comboEstado.addActionListener(l);
        }
    }

    /**
     * Obtiene las incidencias visibles según el rol del usuario actual.
     * ADMINISTRADOR: todas las incidencias del sistema. TECNICO: incidencias
     * asignadas + sin asignar. CLIENTE: solo las incidencias propias.
     *
     * @return Lista de incidencias filtradas por rol y ordenadas
     */
    private List<Incidencia> obtenerIncidenciasSegunRol() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();
        int idUsuario = sesion.getIdUsuarioActual();

        List<Incidencia> resultado;

        switch (rol) {
            case ADMINISTRADOR:
                // Admin ve todas las incidencias
                resultado = gestorIncidencias.obtenerTodas();
                break;

            case TECNICO:
                // Técnico ve sus incidencias asignadas + sin asignar
                List<Incidencia> incidenciasTecnico = gestorIncidencias.obtenerPorTecnico(idUsuario);
                incidenciasTecnico.addAll(gestorIncidencias.obtenerSinAsignar());
                resultado = incidenciasTecnico;
                break;

            case CLIENTE:
                // Cliente solo ve las suyas
                resultado = gestorIncidencias.obtenerPorCliente(idUsuario);
                break;

            default:
                resultado = new java.util.ArrayList<>();

        }
        // Aplicar ordenación configurable a TODOS los casos
        ordenarIncidencias(resultado);
        return resultado;
    }

    /**
     * Aplica todos los filtros activos a la lista de incidencias. Combina
     * filtros de texto de búsqueda, prioridad, estado y técnico asignado.
     * Mantiene las restricciones de rol del usuario y actualiza la vista.
     */
    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText().trim();
        String prioridadSeleccionada = (String) comboPrioridad.getSelectedItem();
        String estadoSeleccionado = (String) comboEstado.getSelectedItem();

        // Obtener incidencias base según rol
        List<Incidencia> incidencias = obtenerIncidenciasSegunRol();

        // Filtrar por búsqueda de texto
        if (!textoBusqueda.isEmpty() && !textoBusqueda.equals("Buscar incidencia...")) {
        final String busqueda = textoBusqueda.toLowerCase();
        incidencias = incidencias.stream()
            .filter(inc -> 
                inc.getTitulo().toLowerCase().contains(busqueda) ||
                inc.getDescripcion().toLowerCase().contains(busqueda) ||
                inc.getNombreCliente().toLowerCase().contains(busqueda) ||
                (inc.tieneTecnicoAsignado() && 
                 inc.getNombreTecnico().toLowerCase().contains(busqueda))
            )
            .collect(java.util.stream.Collectors.toList());
    }

        // Filtrar por prioridad
        if (!prioridadSeleccionada.equals("Todas las prioridades")) {
            String prioridad = prioridadSeleccionada.replace("🔴 ", "")
                    .replace("🟡 ", "")
                    .replace("🟢 ", "");
            final String prioridadFinal = prioridad;
            incidencias.removeIf(inc -> !inc.getPrioridad().getNombre().equals(prioridadFinal));
        }

        // Filtrar por estado
        if (!estadoSeleccionado.equals("Todos los estados")) {
            String estado = estadoSeleccionado.replace("⏳ ", "")
                    .replace("🔧 ", "")
                    .replace("✅ ", "");
            final String estadoFinal = estado;
            incidencias.removeIf(inc -> !inc.getEstado().getNombre().equals(estadoFinal));
        }

        // Filtrar por técnico asignado (solo para Admin)
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (sesion.getRolActual() == Rol.ADMINISTRADOR && comboAsignacion != null) {
            String asignacionSeleccionada = (String) comboAsignacion.getSelectedItem();

            if (asignacionSeleccionada != null && !asignacionSeleccionada.equals("Todos los técnicos")) {
                if (asignacionSeleccionada.equals("Sin asignar")) {
                    // Mostrar solo incidencias sin técnico asignado
                    incidencias.removeIf(inc -> inc.tieneTecnicoAsignado());
                } else {
                    // Mostrar solo incidencias asignadas al técnico seleccionado
                    final String nombreTecnico = asignacionSeleccionada;
                    incidencias.removeIf(inc -> {
                        if (!inc.tieneTecnicoAsignado()) {
                            return true;
                        }
                        return !inc.getNombreTecnico().equals(nombreTecnico);
                    });
                }
            }
        }

        // Mostrar resultados
        mostrarIncidencias(incidencias);
    }

    /**
     * Ordena una lista de incidencias según la opción seleccionada en el combo.
     * Opciones disponibles: más recientes primero, más antiguas primero, ID
     * mayor a menor, ID menor a mayor.
     *
     * @param incidencias Lista de incidencias a ordenar (se modifica
     * directamente)
     */
    private void ordenarIncidencias(List<Incidencia> incidencias) {
        String ordenSeleccionado = (String) comboOrden.getSelectedItem();

        switch (ordenSeleccionado) {
            case "Más recientes primero":
                incidencias.sort((i1, i2) -> i2.getFechaCreacion().compareTo(i1.getFechaCreacion()));
                break;

            case "Más antiguas primero":
                incidencias.sort((i1, i2) -> i1.getFechaCreacion().compareTo(i2.getFechaCreacion()));
                break;

            case "ID mayor a menor":
                incidencias.sort((i1, i2) -> Integer.compare(i2.getId(), i1.getId()));
                break;

            case "ID menor a mayor":
                incidencias.sort((i1, i2) -> Integer.compare(i1.getId(), i2.getId()));
                break;

            default:
                // Por defecto, más recientes primero
                incidencias.sort((i1, i2) -> i2.getFechaCreacion().compareTo(i1.getFechaCreacion()));
        }
    }

    /**
     * Muestra una lista específica de incidencias en el panel de tarjetas.
     * Limpia el panel actual, crea tarjetas para cada incidencia, actualiza
     * contador y refresca la vista.
     *
     * @param incidencias Lista de incidencias a mostrar en formato de tarjetas
     */
    private void mostrarIncidencias(List<Incidencia> incidencias) {
        panelTarjetas.removeAll();

        if (incidencias.isEmpty()) {
            mostrarMensajeSinIncidencias();
        } else {
            for (Incidencia inc : incidencias) {
                panelTarjetas.add(crearTarjetaIncidencia(inc));
            }
        }

        actualizarContador(incidencias.size());
        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    /**
     * Muestra los detalles completos de una incidencia en el panel lateral.
     * Carga toda la información de la incidencia seleccionada, configura
     * permisos de edición según rol y asegura que no esté en modo edición.
     *
     * @param incidencia Incidencia cuyos detalles mostrar en el panel lateral
     */
    private void mostrarDetalleIncidencia(Incidencia incidencia) {
        this.incidenciaSeleccionada = incidencia;

        // Asegurar que no esté en modo edición
        if (modoEdicion) {
            desactivarModoEdicion();
        }

        // Mostrar información básica
        lblDetalleId.setText(incidencia.getIdFormateado());

        // Prioridad con icono
        ImageIcon iconoPrioridad = incidencia.getPrioridad().getIcono();
        if (iconoPrioridad != null) {
            lblDetallePrioridad.setIcon(iconoPrioridad);
            lblDetallePrioridad.setText("Prioridad: " + incidencia.getPrioridad().getNombre());
            lblDetallePrioridad.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblDetallePrioridad.setIconTextGap(8);
        } else {
            lblDetallePrioridad.setIcon(null);
            lblDetallePrioridad.setText("Prioridad: " + incidencia.getPrioridad().getNombreConIcono());
        }
        lblDetallePrioridad.setForeground(incidencia.getPrioridad().getColor());

        // Categoría con icono
        ImageIcon iconoCategoria = incidencia.getCategoria().getIcono();
        if (iconoCategoria != null) {
            lblDetalleCategoria.setIcon(iconoCategoria);
            lblDetalleCategoria.setText("Categoría: " + incidencia.getCategoria().getNombre());
            lblDetalleCategoria.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblDetalleCategoria.setIconTextGap(8);
        } else {
            lblDetalleCategoria.setIcon(null);
            lblDetalleCategoria.setText("Categoría: " + incidencia.getCategoria().getNombreConIcono());
        }

        // Estado con icono
        ImageIcon iconoEstado = incidencia.getEstado().getIcono();
        if (iconoEstado != null) {
            lblDetalleEstado.setIcon(iconoEstado);
            lblDetalleEstado.setText("Estado: " + incidencia.getEstado().getNombre());
            lblDetalleEstado.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblDetalleEstado.setIconTextGap(8);
        } else {
            lblDetalleEstado.setIcon(null);
            lblDetalleEstado.setText("Estado: " + incidencia.getEstado().getNombreConIcono());
        }

        lblDetalleAsignacion.setText("Asignado a: " + incidencia.getTecnicoOSinAsignar());

        // Descripción
        txtAreaDescripcion.setText(incidencia.getDescripcion());
        txtAreaDescripcion.setEditable(false);
        txtAreaDescripcion.setBackground(new Color(245, 245, 245));

        // Información adicional
        ImageIcon iconoCalendario = new ImageIcon(
                getClass().getResource("/gestorincidencias/recursos/iconos/ui/calendario.png")
        );

        ImageIcon iconoUsuario = new ImageIcon(
                getClass().getResource("/gestorincidencias/recursos/iconos/ui/usuario.png")
        );

        lblDetalleFechaCreacion.setIcon(iconoCalendario);
        lblDetalleFechaCreacion.setText("Fecha creación: " + incidencia.getFechaCreacion().format(formatoFecha));
        lblDetalleFechaCreacion.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblDetalleFechaCreacion.setIconTextGap(8);

        lblDetalleReportadoPor.setIcon(iconoUsuario);
        lblDetalleReportadoPor.setText("Reportado por: " + incidencia.getNombreCliente());
        lblDetalleReportadoPor.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblDetalleReportadoPor.setIconTextGap(8);

        // Configurar botón Editar según permisos
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();
        int idUsuario = sesion.getIdUsuarioActual();

        boolean puedeEditar = false;

        if (rol == Rol.ADMINISTRADOR) {
            puedeEditar = true;
        } else if (rol == Rol.TECNICO) {
            // Técnico puede editar incidencias asignadas a él o creadas por él
            boolean esIncidenciaAsignada = incidencia.tieneTecnicoAsignado()
                    && incidencia.getIdTecnicoAsignado() == idUsuario;
            boolean esIncidenciaCreada = incidencia.getIdCliente() == idUsuario;
            puedeEditar = esIncidenciaAsignada || esIncidenciaCreada;
        } else if (rol == Rol.CLIENTE) {
            // Cliente solo puede editar sus propias incidencias
            puedeEditar = (incidencia.getIdCliente() == idUsuario);
        }

        // Habilitar/deshabilitar botón editar según permisos
        if (puedeEditar) {
            btnEditar.setEnabled(true);
            btnEditar.setBackground(new Color(0, 230, 255));
        } else {
            btnEditar.setEnabled(false);
            btnEditar.setBackground(new Color(180, 180, 180));
        }

        // Guardar sigue deshabilitado
        btnGuardar.setEnabled(false);
        btnGuardar.setBackground(new Color(180, 180, 180));

        // Mostrar botón confirmar resolución solo si es CLIENTE y estado RESUELTA
        boolean esCliente = rol == Rol.CLIENTE;
        boolean estaResuelta = incidencia.getEstado() == Estado.RESUELTA;
        boolean estaCerrada = incidencia.getEstado() == Estado.CERRADA;
        btnConfirmarResolucion.setVisible(esCliente && estaResuelta);

        // Si la incidencia está CERRADA, desactivar edición para cliente y técnico
        if (estaCerrada && (rol == Rol.CLIENTE || rol == Rol.TECNICO)) {
            btnEditar.setEnabled(false);
            btnEditar.setBackground(new Color(180, 180, 180));
            panelContenido.setBackground(new Color(240, 240, 240));
            panelDetalle.setBackground(new Color(240, 240, 240));

        }
        //Cargar comentarios de la incidencia
            cargarComentarios(incidencia.getId());
    }

    /**
     * Muestra/oculta botones de edición según selección de comentario Solo
     * funciona si estamos en modo edición
     */
    private void mostrarBotonesEdicionComentario() {
        SesionUsuario sesion = SesionUsuario.getInstance();

        // Solo técnicos pueden editar/eliminar Y solo en modo edición
        if (sesion.getRolActual() != Rol.TECNICO || !modoEdicion) {
            btnEditarComentario.setVisible(false);
            btnEliminarComentario.setVisible(false);
            return;
        }

        int indiceSeleccionado = listComentarios.getSelectedIndex();

        if (indiceSeleccionado >= 0 && indiceSeleccionado < comentariosActuales.size()) {
            // Hay comentario seleccionado, mostrar botones
            btnEditarComentario.setVisible(true);
            btnEliminarComentario.setVisible(true);
        } else {
            // No hay selección, ocultar botones
            btnEditarComentario.setVisible(false);
            btnEliminarComentario.setVisible(false);
        }

        // Actualizar layout
        panelDetalle.revalidate();
        panelDetalle.repaint();
    }

    /**
     * Activa el modo edición con validación de permisos por rol.
     */
    private void activarModoEdicion() {
        if (incidenciaSeleccionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una incidencia primero",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();
        int idUsuario = sesion.getIdUsuarioActual();

        // VERIFICAR PERMISOS DE EDICIÓN
        boolean puedeEditar = false;
        String tipoPermiso = "";

        if (rol == Rol.ADMINISTRADOR) {
            puedeEditar = true;
            tipoPermiso = "admin";
        } else if (rol == Rol.TECNICO) {
            boolean esIncidenciaAsignada = incidenciaSeleccionada.tieneTecnicoAsignado()
                    && incidenciaSeleccionada.getIdTecnicoAsignado() == idUsuario;
            boolean esIncidenciaCreada = incidenciaSeleccionada.getIdCliente() == idUsuario;

            if (esIncidenciaAsignada) {
                puedeEditar = true;
                tipoPermiso = "tecnico_asignada";
            } else if (esIncidenciaCreada) {
                puedeEditar = true;
                tipoPermiso = "tecnico_creada";
            }
        } else if (rol == Rol.CLIENTE) {
            boolean esIncidenciaPropia = incidenciaSeleccionada.getIdCliente() == idUsuario;

            if (esIncidenciaPropia) {
                puedeEditar = true;
                tipoPermiso = "cliente_propia";
            }
        }

        // VALIDAR PERMISOS
        if (!puedeEditar) {
            String mensaje;
            if (rol == Rol.TECNICO) {
                mensaje = "Solo puedes editar incidencias que creaste o que te han sido asignadas";
            } else if (rol == Rol.CLIENTE) {
                mensaje = "Solo puedes editar tus propias incidencias";
            } else {
                mensaje = "No tienes permisos para editar esta incidencia";
            }

            JOptionPane.showMessageDialog(this, mensaje, "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modoEdicion = true;

        // Habilitar botón Guardar
        btnGuardar.setEnabled(true);
        btnGuardar.setBackground(new Color(80, 200, 120));

        // Deshabilitar botón Editar
        btnEditar.setEnabled(false);
        btnEditar.setBackground(new Color(180, 180, 180));
        btnEliminar.setVisible(true);

        // CONFIGURAR PERMISOS SEGÚN TIPO
        switch (tipoPermiso) {
            case "admin":
                // ADMINISTRADOR: Control total
                configurarEdicionAdministrador();
                break;

            case "tecnico_asignada":
                // TÉCNICO con incidencia ASIGNADA: Solo estado y descripción
                configurarEdicionTecnicoAsignada();
                break;

            case "tecnico_creada":
                // TÉCNICO con incidencia CREADA: Campos básicos + estado
                configurarEdicionTecnicoCreada();
                break;

            case "cliente_propia":
                configurarEdicionClientePropia();
                break;
        }
        
        // Activar funcionalidad de comentarios para técnicos
        if (sesion.getRolActual() == Rol.TECNICO) {
            scrollNuevoComentario.setVisible(true);
            txtNuevoComentario.setVisible(true);
            btnAñadirComentario.setVisible(true);

            // Configurar placeholder si no está configurado
            if (txtNuevoComentario.getText().isEmpty()) {
                txtNuevoComentario.setText("Añadir comentario...");
                txtNuevoComentario.setForeground(new Color(150, 150, 150));
            }
}

        // Cambiar color de fondo del panel para indicar modo edición
        panelDetalle.setBackground(new Color(255, 255, 240));

        // Forzar actualización visual
        panelDetalle.revalidate();
        panelDetalle.repaint();
    }

    /**
     * Configura edición para administrador (control total).
     */
    private void configurarEdicionAdministrador() {
        // Categoría
        comboDetalleCategoria.setVisible(true);
        comboDetalleCategoria.setSelectedIndex(incidenciaSeleccionada.getCategoria().ordinal());

        // Prioridad
        comboDetallePrioridad.setVisible(true);
        comboDetallePrioridad.setSelectedIndex(incidenciaSeleccionada.getPrioridad().ordinal());

        // Estado
        comboDetalleEstado.removeAllItems();
        List<Estado> permitidos = incidenciaSeleccionada.getEstado()
                .getTransicionesPermitidas(Rol.ADMINISTRADOR);
        comboDetalleEstado.addItem(incidenciaSeleccionada.getEstado().getNombre());
        for (Estado est : permitidos) {
            comboDetalleEstado.addItem(est.getNombre());
        }
        comboDetalleEstado.setEnabled(!permitidos.isEmpty());
        comboDetalleEstado.setVisible(true);
        comboDetalleEstado.setSelectedIndex(incidenciaSeleccionada.getEstado().ordinal());

        // Técnico
        comboDetalleTecnico.setVisible(true);
        if (incidenciaSeleccionada.tieneTecnicoAsignado()) {
            comboDetalleTecnico.setSelectedItem(incidenciaSeleccionada.getNombreTecnico());
        } else {
            comboDetalleTecnico.setSelectedIndex(0);
        }

        // Descripción
        txtAreaDescripcion.setEditable(true);
        txtAreaDescripcion.setBackground(Color.WHITE);
    }

    /**
     * Configura edición para técnico con incidencia asignada.
     */
    private void configurarEdicionTecnicoAsignada() {
        // Solo puede cambiar estado 
        comboDetalleEstado.removeAllItems();
        List<Estado> permitidos = incidenciaSeleccionada.getEstado()
                .getTransicionesPermitidas(Rol.TECNICO);
        comboDetalleEstado.addItem(incidenciaSeleccionada.getEstado().getNombre());
        for (Estado est : permitidos) {
            comboDetalleEstado.addItem(est.getNombre());
        }
        comboDetalleEstado.setEnabled(!permitidos.isEmpty());
        comboDetalleEstado.setVisible(true);
        comboDetalleEstado.setSelectedIndex(incidenciaSeleccionada.getEstado().ordinal());

        txtAreaDescripcion.setEditable(false);
        txtAreaDescripcion.setBackground(new Color(245,245,245));
    }

    /**
     * Configura edición para técnico con incidencia creada por él.
     */
    private void configurarEdicionTecnicoCreada() {
        // Puede editar campos básicos + estado
        comboDetalleCategoria.setVisible(true);
        comboDetalleCategoria.setSelectedIndex(incidenciaSeleccionada.getCategoria().ordinal());

        comboDetallePrioridad.setVisible(true);
        comboDetallePrioridad.setSelectedIndex(incidenciaSeleccionada.getPrioridad().ordinal());

        
        comboDetalleEstado.removeAllItems();
        List<Estado> permitidos = incidenciaSeleccionada.getEstado()
                .getTransicionesPermitidas(Rol.TECNICO);
        comboDetalleEstado.addItem(incidenciaSeleccionada.getEstado().getNombre());
        for (Estado est : permitidos) {
            comboDetalleEstado.addItem(est.getNombre());
        }
        comboDetalleEstado.setEnabled(!permitidos.isEmpty());
        comboDetalleEstado.setVisible(true);
        comboDetalleEstado.setSelectedIndex(incidenciaSeleccionada.getEstado().ordinal());

        txtAreaDescripcion.setEditable(false);
        txtAreaDescripcion.setBackground(new Color(245,245,245));
    }

    /**
     * Configura edición para cliente con su propia incidencia.
     */
    private void configurarEdicionClientePropia() {
        // Puede editar campos básicos (categoría, prioridad, descripción)
        comboDetalleCategoria.setVisible(true);
        comboDetalleCategoria.setSelectedIndex(incidenciaSeleccionada.getCategoria().ordinal());

        comboDetallePrioridad.setVisible(true);
        comboDetallePrioridad.setSelectedIndex(incidenciaSeleccionada.getPrioridad().ordinal());

        txtAreaDescripcion.setEditable(true);
        txtAreaDescripcion.setBackground(Color.WHITE);

        // No puede cambiar estado o técnico
        comboDetalleEstado.setVisible(false);
        comboDetalleTecnico.setVisible(false);

    }

    /**
     * Desactiva el modo edición y restaura el estado de solo lectura.
     */
    private void desactivarModoEdicion() {
        modoEdicion = false;

        // Deshabilitar botón Guardar
        btnGuardar.setEnabled(false);
        btnGuardar.setBackground(new Color(180, 180, 180));

        // Habilitar botón Editar
        btnEditar.setEnabled(true);
        btnEditar.setBackground(new Color(0, 230, 255));
        btnEliminar.setVisible(false);

        // Ocultar todos los combos
        comboDetalleCategoria.setVisible(false);
        comboDetallePrioridad.setVisible(false);
        comboDetalleEstado.setVisible(false);
        comboDetalleTecnico.setVisible(false);

        // Restaurar color de fondo
        panelDetalle.setBackground(Color.WHITE);

        // Deshabilitar edición de descripción
        txtAreaDescripcion.setEditable(false);
        txtAreaDescripcion.setBackground(new Color(245, 245, 245));

        // Ocultar funcionalidad de comentarios
        scrollNuevoComentario.setVisible(false);
        txtNuevoComentario.setVisible(false);
        btnAñadirComentario.setVisible(false);
        btnEditarComentario.setVisible(false);
        btnEliminarComentario.setVisible(false);

        // Limpiar selección de comentarios
        listComentarios.clearSelection();

        // Forzar actualización visual
        panelDetalle.revalidate();
        panelDetalle.repaint();
    }

    /**
     * Guarda los cambios realizados en la incidencia seleccionada.
     */
    private void guardarCambios() {
        if (incidenciaSeleccionada == null) {
            return;
        }

        try {
            SesionUsuario sesion = SesionUsuario.getInstance();
            Rol rol = sesion.getRolActual();

            boolean cambiosRealizados = false;

            System.out.println("\n=== DEBUG GUARDAR CAMBIOS ===");
            System.out.println("Rol: " + rol);
            // ADMINISTRADOR: puede cambiar todo
            if (rol == Rol.ADMINISTRADOR) {
                System.out.println("--- Procesando cambios de ADMINISTRADOR ---");

                // Actualizar categoría usando índice
                int categoriaIndex = comboDetalleCategoria.getSelectedIndex();
                Categoria nuevaCategoria = Categoria.values()[categoriaIndex];

                System.out.println("Categoría actual: " + incidenciaSeleccionada.getCategoria());
                System.out.println("Categoría nueva (índice " + categoriaIndex + "): " + nuevaCategoria);
                System.out.println("¿Son diferentes? " + (nuevaCategoria != incidenciaSeleccionada.getCategoria()));

                if (nuevaCategoria != incidenciaSeleccionada.getCategoria()) {
                    incidenciaSeleccionada.setCategoria(nuevaCategoria);
                    cambiosRealizados = true;
                    System.out.println("Categoría actualizada a: " + nuevaCategoria);
                }

                // Actualizar prioridad usando índice
                int prioridadIndex = comboDetallePrioridad.getSelectedIndex();
                Prioridad nuevaPrioridad = Prioridad.values()[prioridadIndex];

                System.out.println("Prioridad actual: " + incidenciaSeleccionada.getPrioridad());
                System.out.println("Prioridad nueva (índice " + prioridadIndex + "): " + nuevaPrioridad);
                System.out.println("¿Son diferentes? " + (nuevaPrioridad != incidenciaSeleccionada.getPrioridad()));

                if (nuevaPrioridad != incidenciaSeleccionada.getPrioridad()) {
                    incidenciaSeleccionada.setPrioridad(nuevaPrioridad);
                    cambiosRealizados = true;
                    System.out.println("Prioridad actualizada a: " + nuevaPrioridad);
                }

                // Actualizar estado
                String estadoTexto = (String) comboDetalleEstado.getSelectedItem();
                System.out.println("Estado seleccionado en combo: '" + estadoTexto + "'");

                Estado nuevoEstado = Estado.fromNombre(estadoTexto);

                System.out.println("Estado actual: " + incidenciaSeleccionada.getEstado());
                System.out.println("Estado nuevo: " + nuevoEstado);
                System.out.println("¿Son diferentes? " + (nuevoEstado != incidenciaSeleccionada.getEstado()));

                if (nuevoEstado != incidenciaSeleccionada.getEstado()) {
                    incidenciaSeleccionada.setEstado(nuevoEstado);
                    cambiosRealizados = true;
                    System.out.println("Estado actualizado a: " + nuevoEstado);

                    if (nuevoEstado == Estado.RESUELTA) {
                        incidenciaSeleccionada.setFechaResolucion(java.time.LocalDateTime.now());
                        System.out.println("Fecha de resolución establecida");
                    }
                }

                // Actualizar técnico asignado
                String tecnicoSeleccionado = (String) comboDetalleTecnico.getSelectedItem();

                System.out.println("Técnico actual: '" + incidenciaSeleccionada.getNombreTecnico() + "'");
                System.out.println("Técnico seleccionado: '" + tecnicoSeleccionado + "'");
                System.out.println("Tiene técnico asignado? " + incidenciaSeleccionada.tieneTecnicoAsignado());

                if (tecnicoSeleccionado.equals("Sin asignar")) {
                    if (incidenciaSeleccionada.tieneTecnicoAsignado()) {
                        incidenciaSeleccionada.setIdTecnicoAsignado(null);
                        incidenciaSeleccionada.setNombreTecnico(null);
                        cambiosRealizados = true;
                        System.out.println("Técnico desasignado");
                    }
                } else {
                    if (!tecnicoSeleccionado.equals(incidenciaSeleccionada.getNombreTecnico())) {
                        // Buscar el ID real del técnico seleccionado
                        GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
                        List<Usuario> tecnicos = gestorUsuarios.obtenerPorRol(Rol.TECNICO);

                        Integer idTecnicoEncontrado = null;
                        for (Usuario tecnico : tecnicos) {
                            if (tecnico.getNombreCompleto().equals(tecnicoSeleccionado)) {
                                idTecnicoEncontrado = tecnico.getId();
                                break;
                            }
                        }

                        if (idTecnicoEncontrado != null) {
                            incidenciaSeleccionada.setIdTecnicoAsignado(idTecnicoEncontrado);
                            incidenciaSeleccionada.setNombreTecnico(tecnicoSeleccionado);
                            cambiosRealizados = true;
                            // Volver a PENDIENTE automáticamente al reasignar técnico
                            if (incidenciaSeleccionada.getEstado() != Estado.CERRADA) {
                                incidenciaSeleccionada.setEstado(Estado.PENDIENTE);
                                incidenciaSeleccionada.setFechaResolucion(null);
                            }
                            System.out.println("Técnico asignado: " + tecnicoSeleccionado + " (ID: " + idTecnicoEncontrado + ")");
                        } else {
                            System.err.println("No se encontró técnico: " + tecnicoSeleccionado);
                        }
                    }
                }

                // Actualizar descripción
                String nuevaDescripcion = txtAreaDescripcion.getText().trim();
                String descripcionActual = incidenciaSeleccionada.getDescripcion();

                System.out.println("Descripción actual length: " + descripcionActual.length());
                System.out.println("Descripción actual: [" + descripcionActual + "]");
                System.out.println("Descripción nueva length: " + nuevaDescripcion.length());
                System.out.println("Descripción nueva: [" + nuevaDescripcion + "]");
                System.out.println("¿Son diferentes? " + (!nuevaDescripcion.equals(descripcionActual)));

                if (!nuevaDescripcion.equals(descripcionActual)) {
                    if (nuevaDescripcion.length() < 20) {
                        System.out.println("Descripción muy corta (< 20 caracteres)");
                        JOptionPane.showMessageDialog(this,
                                "La descripción debe tener al menos 20 caracteres",
                                "Validación",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    incidenciaSeleccionada.setDescripcion(nuevaDescripcion);
                    cambiosRealizados = true;
                    System.out.println("Descripción actualizada");
                }
            } // TÉCNICO: puede cambiar estado 
            else if (rol == Rol.TECNICO) {
                // Actualizar estado
                String estadoTexto = (String) comboDetalleEstado.getSelectedItem();
                Estado nuevoEstado = Estado.fromNombre(estadoTexto);
                if (nuevoEstado != incidenciaSeleccionada.getEstado()) {
                    incidenciaSeleccionada.setEstado(nuevoEstado);
                    cambiosRealizados = true;

                    if (nuevoEstado == Estado.RESUELTA) {
                        incidenciaSeleccionada.setFechaResolucion(java.time.LocalDateTime.now());
                    }
                }

            } // CLIENTE: solo puede cambiar descripción
            else if (rol == Rol.CLIENTE) {
                // Actualizar categoría
                int categoriaIndex = comboDetalleCategoria.getSelectedIndex();
                Categoria nuevaCategoria = Categoria.values()[categoriaIndex];
                if (nuevaCategoria != incidenciaSeleccionada.getCategoria()) {
                    incidenciaSeleccionada.setCategoria(nuevaCategoria);
                    cambiosRealizados = true;
                }

                // Actualizar prioridad
                int prioridadIndex = comboDetallePrioridad.getSelectedIndex();
                Prioridad nuevaPrioridad = Prioridad.values()[prioridadIndex];
                if (nuevaPrioridad != incidenciaSeleccionada.getPrioridad()) {
                    incidenciaSeleccionada.setPrioridad(nuevaPrioridad);
                    cambiosRealizados = true;
                }

                // Actualizar descripción
                String nuevaDescripcion = txtAreaDescripcion.getText().trim();
                if (!nuevaDescripcion.equals(incidenciaSeleccionada.getDescripcion())) {
                    if (nuevaDescripcion.length() < 20) {
                        JOptionPane.showMessageDialog(this,
                                "La descripción debe tener al menos 20 caracteres",
                                "Validación",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    incidenciaSeleccionada.setDescripcion(nuevaDescripcion);
                    cambiosRealizados = true;
                }
            }

            // Guardar cambios en el gestor
            if (cambiosRealizados) {
                boolean actualizada = gestorIncidencias.actualizar(incidenciaSeleccionada);

                if (actualizada) {
                    JOptionPane.showMessageDialog(this,
                            "Incidencia actualizada exitosamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Desactivar modo edición
                    desactivarModoEdicion();

                    // Recargar y aplicar filtros existentes
                    recargarConFiltrosActuales();

                    // Volver a mostrar la incidencia actualizada en el panel de detalles
                    mostrarDetalleIncidencia(incidenciaSeleccionada);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al actualizar la incidencia",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se realizaron cambios",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                desactivarModoEdicion();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar cambios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Cancela los cambios y restaura los valores originales.
     */
    private void cancelarCambios() {
        if (incidenciaSeleccionada == null || !modoEdicion) {
            return;
        }

        // Restaurar valores originales de los combos
        if (comboDetalleCategoria.isVisible()) {
            comboDetalleCategoria.setSelectedIndex(incidenciaSeleccionada.getCategoria().ordinal());
        }

        if (comboDetallePrioridad.isVisible()) {
            comboDetallePrioridad.setSelectedIndex(incidenciaSeleccionada.getPrioridad().ordinal());
        }

        if (comboDetalleEstado.isVisible()) {
            comboDetalleEstado.setSelectedItem(incidenciaSeleccionada.getEstado().getNombre());
        }

        if (comboDetalleTecnico.isVisible()) {
            if (incidenciaSeleccionada.tieneTecnicoAsignado()) {
                comboDetalleTecnico.setSelectedItem(incidenciaSeleccionada.getNombreTecnico());
            } else {
                comboDetalleTecnico.setSelectedIndex(0); // "Sin asignar"
            }
        }

        // Restaurar descripción original
        txtAreaDescripcion.setText(incidenciaSeleccionada.getDescripcion());

        // Salir del modo edición
        desactivarModoEdicion();

        // Mostrar la incidencia original 
        mostrarDetalleIncidencia(incidenciaSeleccionada);
    }

    /**
     * Elimina la incidencia actual tras solicitar confirmación al usuario.
     */
    private void eliminarIncidencia() {
        if (incidenciaSeleccionada == null) {
            return;
        }

        // Confirmación con diálogo personalizado
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                String.format(
                        "¿Está seguro que desea eliminar esta incidencia?\n\n"
                        + "ID: %s\n"
                        + "Título: %s\n"
                        + "Cliente: %s\n\n"
                        + "⚠️ Esta acción NO se puede deshacer.",
                        incidenciaSeleccionada.getIdFormateado(),
                        incidenciaSeleccionada.getTitulo(),
                        incidenciaSeleccionada.getNombreCliente()
                ),
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                // Eliminar de la base de datos/gestor
                boolean eliminado = gestorIncidencias.eliminar(incidenciaSeleccionada.getId());

                if (eliminado) {
                    // Mostrar confirmación
                    JOptionPane.showMessageDialog(
                            this,
                            "Incidencia eliminada correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Recargar lista manteniendo filtros
                    recargarConFiltrosActuales();

                    // Limpiar panel de detalle
                    limpiarPanelDetalle();

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Error al eliminar la incidencia",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error inesperado al eliminar: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Carga los comentarios de una incidencia específica
     */
    private void cargarComentarios(int idIncidencia) {
        // Limpiar lista actual
        modeloListaComentarios.clear();
        comentariosActuales.clear();

        // Obtener comentarios desde la base de datos
        List<Comentario> comentarios = gestorComentarios.obtenerPorIncidencia(idIncidencia);

        // Añadir a la lista
        for (Comentario comentario : comentarios) {
            comentariosActuales.add(comentario);
            modeloListaComentarios.addElement(comentario.getFormatoLista());
        }

        // Si no hay comentarios, mostrar mensaje
        if (comentarios.isEmpty()) {
            modeloListaComentarios.addElement("No hay comentarios aún...");
        }
    }

    /**
     * Añade un nuevo comentario a la incidencia actual
     */
    private void añadirNuevoComentario() {
        // Verificar que hay incidencia seleccionada
        if (incidenciaSeleccionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una incidencia primero",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener texto del comentario
        String textoComentario = txtNuevoComentario.getText().trim();

        // Validar que no esté vacío o sea el placeholder
        if (textoComentario.isEmpty() || textoComentario.equals("Añadir comentario...")) {
            JOptionPane.showMessageDialog(this,
                    "Debe escribir un comentario",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            txtNuevoComentario.requestFocus();
            return;
        }

        // Validar longitud mínima
        if (textoComentario.length() < 5) {
            JOptionPane.showMessageDialog(this,
                    "El comentario debe tener al menos 5 caracteres",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            txtNuevoComentario.requestFocus();
            return;
        }

        try {
            // Obtener datos del usuario actual
            SesionUsuario sesion = SesionUsuario.getInstance();
            int idUsuario = sesion.getIdUsuarioActual();
            // Obtener el nombre del usuario desde el gestor de usuarios
            GestorUsuarios gestorUsuarios = GestorUsuarios.getInstance();
            Usuario usuarioActual = gestorUsuarios.obtenerPorId(idUsuario);

            if (usuarioActual == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: No se pudo obtener información del usuario",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nombreUsuario = usuarioActual.getNombreCompleto();

            // Crear nuevo comentario
            Comentario nuevoComentario = new Comentario(
                    textoComentario,
                    idUsuario,
                    incidenciaSeleccionada.getId(),
                    nombreUsuario
            );

            // Guardar en base de datos
            boolean añadido = gestorComentarios.añadir(nuevoComentario);

            if (añadido) {
                // Limpiar campo de texto
                txtNuevoComentario.setText("Añadir comentario...");
                txtNuevoComentario.setForeground(new Color(150, 150, 150));

                // Recargar comentarios
                cargarComentarios(incidenciaSeleccionada.getId());

                // Mensaje de éxito (opcional)
                JOptionPane.showMessageDialog(this,
                        "Comentario añadido correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar el comentario",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Edita el comentario seleccionado
     */
    private void editarComentarioSeleccionado() {
        int indiceSeleccionado = listComentarios.getSelectedIndex();

        if (indiceSeleccionado < 0 || indiceSeleccionado >= comentariosActuales.size()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un comentario primero",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Comentario comentario = comentariosActuales.get(indiceSeleccionado);

        // Verificar que el técnico puede editar este comentario
        SesionUsuario sesion = SesionUsuario.getInstance();
        int idUsuarioActual = sesion.getIdUsuarioActual();

        if (comentario.getIdUsuario() != idUsuarioActual) {
            JOptionPane.showMessageDialog(this,
                    "Solo puedes editar tus propios comentarios",
                    "Permiso denegado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Diálogo para editar el comentario
        String nuevoTexto = JOptionPane.showInputDialog(
                this,
                "Editar comentario:",
                comentario.getMensajeComentario()
        );

        if (nuevoTexto != null && !nuevoTexto.trim().isEmpty()) {
            nuevoTexto = nuevoTexto.trim();

            if (nuevoTexto.length() < 5) {
                JOptionPane.showMessageDialog(this,
                        "El comentario debe tener al menos 5 caracteres",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Actualizar en base de datos
            comentario.setMensajeComentario(nuevoTexto);
            boolean actualizado = gestorComentarios.actualizar(comentario);

            if (actualizado) {
                // Recargar comentarios
                cargarComentarios(incidenciaSeleccionada.getId());

                JOptionPane.showMessageDialog(this,
                        "Comentario actualizado correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar el comentario",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Elimina el comentario seleccionado
     */
    private void eliminarComentarioSeleccionado() {
        int indiceSeleccionado = listComentarios.getSelectedIndex();

        if (indiceSeleccionado < 0 || indiceSeleccionado >= comentariosActuales.size()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un comentario primero",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Comentario comentario = comentariosActuales.get(indiceSeleccionado);

        // Verificar que el técnico puede eliminar este comentario
        SesionUsuario sesion = SesionUsuario.getInstance();
        int idUsuarioActual = sesion.getIdUsuarioActual();

        if (comentario.getIdUsuario() != idUsuarioActual) {
            JOptionPane.showMessageDialog(this,
                    "Solo puedes eliminar tus propios comentarios",
                    "Permiso denegado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar eliminación
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                String.format(
                        "¿Está seguro que desea eliminar este comentario?\n\n"
                        + "Autor: %s\n"
                        + "Fecha: %s\n"
                        + "Comentario: %s\n\n"
                        + "Esta acción NO se puede deshacer.",
                        comentario.getNombreUsuario(),
                        comentario.getFechaComentario().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        ),
                        comentario.getMensajeComentario().length() > 50
                        ? comentario.getMensajeComentario().substring(0, 50) + "..."
                        : comentario.getMensajeComentario()
                ),
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = gestorComentarios.eliminar(comentario.getIdComentario());

            if (eliminado) {
                // Recargar comentarios
                cargarComentarios(incidenciaSeleccionada.getId());

                JOptionPane.showMessageDialog(this,
                        "Comentario eliminado correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar el comentario",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Recarga las incidencias desde la base de datos manteniendo filtros
     * aplicados.
     */
    private void recargarConFiltrosActuales() {
        // Primero, recargar todas las incidencias desde la base de datos
        List<Incidencia> incidenciasCompletas = obtenerIncidenciasSegunRol();

        // Luego aplicar los filtros que estaban activos
        aplicarFiltros();

        System.out.println("Incidencias recargadas manteniendo filtros actuales");
    }

    /**
     * Crea una tarjeta visual para una incidencia específica. Genera un panel
     * con diseño personalizado que incluye ID, prioridad, categoría, título,
     * descripción corta, estado, técnico asignado y fecha de creación. Aplica
     * estilos diferenciados para incidencias asignadas al técnico actual.
     *
     * @param incidencia Incidencia para la cual crear la tarjeta visual
     * @return JPanel configurado como tarjeta clickeable con información de la
     * incidencia
     */
    private JPanel crearTarjetaIncidencia(Incidencia incidencia) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(null);
        tarjeta.setPreferredSize(new Dimension(300, 200));

        // Obtener sesión para verificar si es incidencia del técnico actual
        SesionUsuario sesion = SesionUsuario.getInstance();
        Rol rol = sesion.getRolActual();
        int idUsuario = sesion.getIdUsuarioActual();

        // Verificar si es una incidencia asignada al técnico actual
        boolean esIncidenciaDelTecnico = (rol == Rol.TECNICO
                && incidencia.tieneTecnicoAsignado()
                && incidencia.getIdTecnicoAsignado() == idUsuario);

        // Configurar fondo y borde según si es del técnico
        if (esIncidenciaDelTecnico) {
            tarjeta.setBackground(new Color(245, 250, 255));
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(15, new Color(100, 150, 200), 3),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        } else {
            tarjeta.setBackground(Color.WHITE);
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(15, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }

        // ID
        JLabel lblId = new JLabel(incidencia.getIdFormateado());
        lblId.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblId.setForeground(new Color(0, 150, 200));
        lblId.setBounds(10, 10, 100, 20);
        tarjeta.add(lblId);

        // Prioridad con icono
        JLabel lblPrioridad = new JLabel();
        ImageIcon iconoPrioridad = incidencia.getPrioridad().getIcono();
        if (iconoPrioridad != null) {
            lblPrioridad.setIcon(iconoPrioridad);
            lblPrioridad.setText(incidencia.getPrioridad().getNombre());
            lblPrioridad.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblPrioridad.setIconTextGap(5);
        } else {
            lblPrioridad.setText(incidencia.getPrioridad().getNombreConIcono());
        }
        lblPrioridad.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        lblPrioridad.setForeground(incidencia.getPrioridad().getColor());
        lblPrioridad.setBounds(10, 35, 250, 20);
        tarjeta.add(lblPrioridad);

        // Categoría con icono
        JLabel lblCategoria = new JLabel();
        ImageIcon iconoCategoria = incidencia.getCategoria().getIcono();
        if (iconoCategoria != null) {
            lblCategoria.setIcon(iconoCategoria);
            lblCategoria.setText(incidencia.getCategoria().getNombre());
            lblCategoria.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblCategoria.setIconTextGap(5);
        } else {
            lblCategoria.setText(incidencia.getCategoria().getNombreConIcono());
        }
        lblCategoria.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblCategoria.setForeground(Color.DARK_GRAY);
        lblCategoria.setBounds(10, 55, 250, 20);
        tarjeta.add(lblCategoria);

        // Título
        JLabel lblTitulo = new JLabel("<html>" + cortarTexto(incidencia.getTitulo(), 35) + "</html>");
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        lblTitulo.setBounds(10, 75, 250, 30);
        tarjeta.add(lblTitulo);

        // Descripción corta
        JTextArea txtDescripcion = new JTextArea(incidencia.getDescripcionCorta());
        txtDescripcion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setBounds(10, 105, 250, 30);
        tarjeta.add(txtDescripcion);

        // Estado con icono
        JLabel lblEstado = new JLabel();
        ImageIcon iconoEstado = incidencia.getEstado().getIcono();
        if (iconoEstado != null) {
            lblEstado.setIcon(iconoEstado);
            lblEstado.setText(incidencia.getEstado().getNombre());
            lblEstado.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblEstado.setIconTextGap(5);
        } else {
            lblEstado.setText(incidencia.getEstado().getNombreConIcono());
        }
        lblEstado.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblEstado.setBounds(10, 140, 250, 20);
        tarjeta.add(lblEstado);

        // Técnico
        JLabel lblTecnico = new JLabel("Técnico: " + incidencia.getTecnicoOSinAsignar());
        lblTecnico.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        lblTecnico.setForeground(Color.GRAY);
        lblTecnico.setBounds(10, 160, 250, 20);
        tarjeta.add(lblTecnico);

        // Fecha
        JLabel lblFecha = new JLabel(incidencia.getFechaCreacion().format(formatoFecha));
        lblFecha.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 9));
        lblFecha.setForeground(Color.LIGHT_GRAY);
        lblFecha.setBounds(10, 178, 250, 15);
        tarjeta.add(lblFecha);

        // MouseAdapter compartido
        MouseAdapter clickAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (esIncidenciaDelTecnico) {
                    tarjeta.setBackground(new Color(235, 245, 255));
                    tarjeta.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(15, new Color(80, 130, 180), 3),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                } else {
                    tarjeta.setBackground(new Color(240, 248, 255));
                    tarjeta.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(15, incidencia.getPrioridad().getColor(), 2),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                }
                tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (esIncidenciaDelTecnico) {
                    tarjeta.setBackground(new Color(245, 250, 255));
                    tarjeta.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(15, new Color(100, 150, 200), 3),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                } else {
                    tarjeta.setBackground(Color.WHITE);
                    tarjeta.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(15, new Color(200, 200, 200)),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                }
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                mostrarDetalleIncidencia(incidencia);
            }
        };

        // Añadir listener a la tarjeta principal
        tarjeta.addMouseListener(clickAdapter);

        // Añadir listener a todos los componentes hijos recursivamente
        añadirListenerRecursivo(tarjeta, clickAdapter);

        return tarjeta;
    }

    /**
     * Muestra un mensaje cuando no hay incidencias que mostrar.
     */
    private void mostrarMensajeSinIncidencias() {
        JLabel lblMensaje = new JLabel("No hay incidencias para mostrar");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        lblMensaje.setForeground(new Color(150, 150, 150));
        panelTarjetas.add(lblMensaje);

        // Actualizar contador
        actualizarContador(0);
    }

    /**
     * Limpia el panel de detalle restaurando estado inicial.
     */
    private void limpiarPanelDetalle() {
        lblDetalleId.setText("ID: -");
        lblDetallePrioridad.setText("Seleccione una incidencia");
        lblDetallePrioridad.setIcon(null);
        lblDetalleCategoria.setText("");
        lblDetalleCategoria.setIcon(null);
        lblDetalleEstado.setText("");
        lblDetalleEstado.setIcon(null);
        lblDetalleAsignacion.setText("");
        txtAreaDescripcion.setText("Haga click en una incidencia para ver sus detalles...");
        txtAreaDescripcion.setEditable(false);
        txtAreaDescripcion.setBackground(new Color(245, 245, 245));
        ImageIcon iconoCalendario = new ImageIcon(
                getClass().getResource("/gestorincidencias/recursos/iconos/ui/calendario.png")
        );

        ImageIcon iconoUsuario = new ImageIcon(
                getClass().getResource("/gestorincidencias/recursos/iconos/ui/usuario.png")
        );

        lblDetalleFechaCreacion.setIcon(iconoCalendario);
        lblDetalleFechaCreacion.setText("Fecha creación: --/--/----");
        lblDetalleFechaCreacion.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblDetalleFechaCreacion.setIconTextGap(8);

        lblDetalleReportadoPor.setIcon(iconoUsuario);
        lblDetalleReportadoPor.setText("Reportado por: -----");
        lblDetalleReportadoPor.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblDetalleReportadoPor.setIconTextGap(8);

        // Deshabilitar ambos botones cuando no hay incidencia
        btnEditar.setEnabled(false);
        btnEditar.setBackground(new Color(180, 180, 180));
        btnGuardar.setEnabled(false);
        btnGuardar.setBackground(new Color(180, 180, 180));
        btnEliminar.setVisible(false);

        // Ocultar combos
        comboDetalleCategoria.setVisible(false);
        comboDetallePrioridad.setVisible(false);
        comboDetalleEstado.setVisible(false);
        comboDetalleTecnico.setVisible(false);

        // Restaurar fondo
        panelDetalle.setBackground(Color.WHITE);

        // Ocultar botón confirmar resolución y restaurar colores
        btnConfirmarResolucion.setVisible(false);
        panelContenido.setBackground(null);
        panelContenido.setOpaque(false);
        panelDetalle.setBackground(Color.WHITE);

        modoEdicion = false;
    }

    /**
     * Actualiza el texto del contador de incidencias mostradas. Muestra la
     * cantidad actual con pluralización correcta.
     *
     * @param cantidad Número de incidencias actualmente visibles
     */
    private void actualizarContador(int cantidad) {
        lblContadorIncidencias.setText("Mostrando " + cantidad
                + (cantidad == 1 ? " incidencia" : " incidencias"));
    }

    /**
     * Resetea la posición del scroll al inicio del panel. Utilizado tras
     * recargar incidencias o aplicar filtros para mejorar UX.
     */
    public void resetearScroll() {
        SwingUtilities.invokeLater(() -> {
            scrollPanelIncidencias.getVerticalScrollBar().setValue(0);
            scrollPanelIncidencias.getHorizontalScrollBar().setValue(0);
        });
    }

    /**
     * Añade el MouseListener a todos los componentes hijos recursivamente.
     * Asegura que toda la tarjeta sea clickeable independientemente del
     * componente específico que reciba el click del usuario.
     *
     * @param contenedor Contenedor al que añadir listeners recursivamente
     * @param listener MouseAdapter con los eventos de click y hover
     * configurados
     */
    private void añadirListenerRecursivo(Container contenedor, MouseAdapter listener) {
        for (Component comp : contenedor.getComponents()) {
            comp.addMouseListener(listener);
            comp.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Si el componente es un contenedor, aplicar recursivamente
            if (comp instanceof Container) {
                añadirListenerRecursivo((Container) comp, listener);
            }
        }
    }

    /**
     * Corta un texto a una longitud máxima añadiendo puntos suspensivos.
     * Utilizado para mostrar títulos y descripciones truncados en las tarjetas.
     *
     * @param texto Texto original a cortar
     * @param maxLength Longitud máxima permitida incluyendo los puntos
     * suspensivos
     * @return Texto cortado con "..." al final si excede la longitud máxima
     */
    private String cortarTexto(String texto, int maxLength) {
        if (texto.length() <= maxLength) {
            return texto;
        }
        return texto.substring(0, maxLength - 3) + "...";
    }

    /**
     * Borde redondeado personalizado.
     */
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {

        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int radius, Color color) {
            this(radius, color, 1);
        }

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new java.awt.BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            int inset = thickness;
            return new java.awt.Insets(inset, inset, inset, inset);
        }

    }

    /**
     * Renderer personalizado para mostrar comentarios en la lista
     */
    private class ComentarioListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String && index < comentariosActuales.size()) {
                Comentario comentario = comentariosActuales.get(index);

                // Mensaje completo para tooltip
                String mensajeCompleto = comentario.getMensajeComentario();

                // Mensaje truncado para mostrar (máximo 80 caracteres)
                String mensajeMostrar = mensajeCompleto.length() > 80
                        ? mensajeCompleto.substring(0, 80) + "..." : mensajeCompleto;

                // Crear texto HTML
                String texto = String.format(
                        "<html><div style='padding: 2px; width: 300px;'>"
                        + "<b style='color: #2c3e50; font-size: 10px;'>%s - %s</b><br/>"
                        + "<span style='color: #34495e; font-size: 9px;'>%s</span>"
                        + "</div></html>",
                        comentario.getFechaComentario().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")
                        ),
                        comentario.getNombreUsuario(),
                        mensajeMostrar
                );

                setText(texto);

                // Tooltip con mensaje completo si está truncado
                if (mensajeCompleto.length() > 80) {
                    setToolTipText("<html><div style='width: 250px;'><b>"
                            + comentario.getNombreUsuario() + " - "
                            + comentario.getFechaComentario().format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            ) + ":</b><br/><br/>"
                            + mensajeCompleto.replace("\n", "<br/>") + "</div></html>");
                } else {
                    setToolTipText(null);
                }

                // Colores
                if (isSelected) {
                    setBackground(new Color(52, 152, 219));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }

                // Borde
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }

            return this;
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

        panelBarraSuperior = new javax.swing.JPanel();
        txtBuscar = new javax.swing.JTextField();
        comboPrioridad = new javax.swing.JComboBox<>();
        comboEstado = new javax.swing.JComboBox<>();
        comboAsignacion = new javax.swing.JComboBox<>();
        comboOrden = new javax.swing.JComboBox<>();
        btnNuevaIncidencia = new javax.swing.JButton();
        lblContadorIncidencias = new javax.swing.JLabel();
        btnAyuda = new javax.swing.JButton();
        scrollPanelIncidencias = new javax.swing.JScrollPane();
        panelTarjetas = new javax.swing.JPanel();
        panelDetalle = new javax.swing.JPanel();
        panelTitulo = new javax.swing.JPanel();
        lblDetallesTitulo = new javax.swing.JLabel();
        btnEliminar = new javax.swing.JButton();
        panelContenido = new javax.swing.JPanel();
        lblDetalleId = new javax.swing.JLabel();
        lblDetallePrioridad = new javax.swing.JLabel();
        lblDetalleEstado = new javax.swing.JLabel();
        lblDetalleCategoria = new javax.swing.JLabel();
        lblDetalleAsignacion = new javax.swing.JLabel();
        lblDescripcionTitulo = new javax.swing.JLabel();
        scrollPane1 = new javax.swing.JScrollPane();
        txtAreaDescripcion = new javax.swing.JTextArea();
        lblComentarios = new javax.swing.JLabel();
        scrollComentarios = new javax.swing.JScrollPane();
        listComentarios = new javax.swing.JList<>();
        scrollNuevoComentario = new javax.swing.JScrollPane();
        txtNuevoComentario = new javax.swing.JTextArea();
        btnEditarComentario = new javax.swing.JButton();
        btnEliminarComentario = new javax.swing.JButton();
        btnAñadirComentario = new javax.swing.JButton();
        comboDetalleCategoria = new javax.swing.JComboBox<>();
        comboDetallePrioridad = new javax.swing.JComboBox<>();
        comboDetalleEstado = new javax.swing.JComboBox<>();
        comboDetalleTecnico = new javax.swing.JComboBox<>();
        lblDetalleFechaCreacion = new javax.swing.JLabel();
        lblDetalleReportadoPor = new javax.swing.JLabel();
        panelBotones = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnEditar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnConfirmarResolucion = new javax.swing.JButton();

        setBackground(new java.awt.Color(240, 245, 250));
        setMinimumSize(new java.awt.Dimension(1000, 600));
        setLayout(new java.awt.BorderLayout());

        panelBarraSuperior.setBackground(new java.awt.Color(255, 255, 255));
        panelBarraSuperior.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)));
        panelBarraSuperior.setPreferredSize(new java.awt.Dimension(1000, 70));
        panelBarraSuperior.setLayout(new java.awt.GridBagLayout());

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(65, 65, 65));
        txtBuscar.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelBarraSuperior.add(txtBuscar, gridBagConstraints);

        comboPrioridad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboPrioridad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todas las prioridades", "Alta", "Media", "Baja" }));
        comboPrioridad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboPrioridadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelBarraSuperior.add(comboPrioridad, gridBagConstraints);

        comboEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos los estados", "Pendiente", "En Proceso", "Resuelta" }));
        comboEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboEstadoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelBarraSuperior.add(comboEstado, gridBagConstraints);
        comboEstado.getAccessibleContext().setAccessibleName("");

        comboAsignacion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboAsignacion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelBarraSuperior.add(comboAsignacion, gridBagConstraints);

        comboOrden.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboOrden.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Más recientes primero", "Más antiguas primero", "ID mayor a menor", "ID menor a mayor" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelBarraSuperior.add(comboOrden, gridBagConstraints);

        btnNuevaIncidencia.setBackground(new java.awt.Color(0, 230, 255));
        btnNuevaIncidencia.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevaIncidencia.setForeground(new java.awt.Color(10, 30, 50));
        btnNuevaIncidencia.setText("Nueva Incidencia");
        btnNuevaIncidencia.setFocusPainted(false);
        btnNuevaIncidencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaIncidenciaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelBarraSuperior.add(btnNuevaIncidencia, gridBagConstraints);

        lblContadorIncidencias.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblContadorIncidencias.setForeground(new java.awt.Color(100, 100, 100));
        lblContadorIncidencias.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblContadorIncidencias.setText("Mostrando 0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.weightx = 0.5;
        panelBarraSuperior.add(lblContadorIncidencias, gridBagConstraints);

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
        panelBarraSuperior.add(btnAyuda, gridBagConstraints);

        add(panelBarraSuperior, java.awt.BorderLayout.NORTH);

        scrollPanelIncidencias.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPanelIncidencias.setPreferredSize(new java.awt.Dimension(700, 500));

        panelTarjetas.setBackground(new java.awt.Color(240, 245, 250));
        panelTarjetas.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 15));
        scrollPanelIncidencias.setViewportView(panelTarjetas);

        add(scrollPanelIncidencias, java.awt.BorderLayout.CENTER);

        panelDetalle.setBackground(new java.awt.Color(255, 255, 255));
        panelDetalle.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 200, 255), 2));
        panelDetalle.setPreferredSize(new java.awt.Dimension(420, 400));
        panelDetalle.setLayout(new java.awt.BorderLayout());

        panelTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelTitulo.setOpaque(false);
        panelTitulo.setPreferredSize(new java.awt.Dimension(370, 50));
        panelTitulo.setLayout(new java.awt.BorderLayout());

        lblDetallesTitulo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDetallesTitulo.setForeground(new java.awt.Color(0, 150, 200));
        lblDetallesTitulo.setText("DETALLES DE INCIDENCIA");
        panelTitulo.add(lblDetallesTitulo, java.awt.BorderLayout.WEST);

        btnEliminar.setBackground(new java.awt.Color(231, 76, 60));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/eliminar.png"))); // NOI18N
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        panelTitulo.add(btnEliminar, java.awt.BorderLayout.EAST);

        panelDetalle.add(panelTitulo, java.awt.BorderLayout.NORTH);

        panelContenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelContenido.setAlignmentX(0.0F);
        panelContenido.setOpaque(false);
        panelContenido.setLayout(new java.awt.GridBagLayout());

        lblDetalleId.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDetalleId.setText("ID: #001");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panelContenido.add(lblDetalleId, gridBagConstraints);

        lblDetallePrioridad.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDetallePrioridad.setText("Prioridad: Alta");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(lblDetallePrioridad, gridBagConstraints);

        lblDetalleEstado.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDetalleEstado.setText("Estado: pendiente");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(lblDetalleEstado, gridBagConstraints);

        lblDetalleCategoria.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDetalleCategoria.setText("Categoría: Software");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(lblDetalleCategoria, gridBagConstraints);

        lblDetalleAsignacion.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDetalleAsignacion.setText("Asignado a: sin asignar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(lblDetalleAsignacion, gridBagConstraints);

        lblDescripcionTitulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblDescripcionTitulo.setText("Descripción: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 3, 10);
        panelContenido.add(lblDescripcionTitulo, gridBagConstraints);

        scrollPane1.setMinimumSize(new java.awt.Dimension(194, 25));

        txtAreaDescripcion.setEditable(false);
        txtAreaDescripcion.setColumns(20);
        txtAreaDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        txtAreaDescripcion.setLineWrap(true);
        txtAreaDescripcion.setRows(5);
        txtAreaDescripcion.setWrapStyleWord(true);
        scrollPane1.setViewportView(txtAreaDescripcion);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(scrollPane1, gridBagConstraints);

        lblComentarios.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblComentarios.setText("Comentarios:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
        panelContenido.add(lblComentarios, gridBagConstraints);

        scrollComentarios.setPreferredSize(new java.awt.Dimension(350, 120));

        listComentarios.setBackground(new java.awt.Color(250, 250, 250));
        listComentarios.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        listComentarios.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        listComentarios.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollComentarios.setViewportView(listComentarios);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(scrollComentarios, gridBagConstraints);

        scrollNuevoComentario.setPreferredSize(new java.awt.Dimension(350, 50));

        txtNuevoComentario.setColumns(20);
        txtNuevoComentario.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        txtNuevoComentario.setForeground(new java.awt.Color(150, 150, 150));
        txtNuevoComentario.setLineWrap(true);
        txtNuevoComentario.setRows(2);
        txtNuevoComentario.setText("Añadir comentario...");
        txtNuevoComentario.setWrapStyleWord(true);
        scrollNuevoComentario.setViewportView(txtNuevoComentario);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelContenido.add(scrollNuevoComentario, gridBagConstraints);

        btnEditarComentario.setBackground(new java.awt.Color(46, 204, 113));
        btnEditarComentario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditarComentario.setForeground(new java.awt.Color(255, 255, 255));
        btnEditarComentario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/editar.png"))); // NOI18N
        btnEditarComentario.setToolTipText("");
        btnEditarComentario.setFocusPainted(false);
        btnEditarComentario.setPreferredSize(new java.awt.Dimension(30, 25));
        btnEditarComentario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarComentarioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 50);
        panelContenido.add(btnEditarComentario, gridBagConstraints);

        btnEliminarComentario.setBackground(new java.awt.Color(231, 76, 60));
        btnEliminarComentario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEliminarComentario.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarComentario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gestorincidencias/recursos/iconos/ui/eliminar.png"))); // NOI18N
        btnEliminarComentario.setFocusPainted(false);
        btnEliminarComentario.setPreferredSize(new java.awt.Dimension(30, 25));
        btnEliminarComentario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarComentarioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panelContenido.add(btnEliminarComentario, gridBagConstraints);

        btnAñadirComentario.setBackground(new java.awt.Color(52, 152, 219));
        btnAñadirComentario.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAñadirComentario.setForeground(new java.awt.Color(255, 255, 255));
        btnAñadirComentario.setText("Añadir Comentario");
        btnAñadirComentario.setFocusPainted(false);
        btnAñadirComentario.setPreferredSize(new java.awt.Dimension(135, 28));
        btnAñadirComentario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAñadirComentarioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 15, 10);
        panelContenido.add(btnAñadirComentario, gridBagConstraints);

        comboDetalleCategoria.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        comboDetalleCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panelContenido.add(comboDetalleCategoria, gridBagConstraints);

        comboDetallePrioridad.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        comboDetallePrioridad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(comboDetallePrioridad, gridBagConstraints);

        comboDetalleEstado.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        comboDetalleEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pendiente", "En Proceso", "Resuelta" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(comboDetalleEstado, gridBagConstraints);

        comboDetalleTecnico.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        comboDetalleTecnico.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sin asignar", "Técnico 1", "Técnico 2" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        panelContenido.add(comboDetalleTecnico, gridBagConstraints);

        lblDetalleFechaCreacion.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblDetalleFechaCreacion.setForeground(new java.awt.Color(100, 100, 100));
        lblDetalleFechaCreacion.setText("Fecha creación:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 3, 10);
        panelContenido.add(lblDetalleFechaCreacion, gridBagConstraints);

        lblDetalleReportadoPor.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblDetalleReportadoPor.setForeground(new java.awt.Color(100, 100, 100));
        lblDetalleReportadoPor.setText("Reportado por:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 8, 10);
        panelContenido.add(lblDetalleReportadoPor, gridBagConstraints);

        panelDetalle.add(panelContenido, java.awt.BorderLayout.CENTER);

        panelBotones.setOpaque(false);
        panelBotones.setPreferredSize(new java.awt.Dimension(420, 85));
        panelBotones.setLayout(new java.awt.BorderLayout());

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));

        btnEditar.setBackground(new java.awt.Color(130, 242, 255));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(10, 30, 50));
        btnEditar.setText("Editar");
        btnEditar.setFocusPainted(false);
        btnEditar.setPreferredSize(new java.awt.Dimension(85, 35));
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });
        jPanel1.add(btnEditar);

        btnCancelar.setBackground(new java.awt.Color(150, 150, 150));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cancelar");
        btnCancelar.setPreferredSize(new java.awt.Dimension(85, 35));
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        jPanel1.add(btnCancelar);

        btnGuardar.setBackground(new java.awt.Color(80, 200, 120));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.setFocusPainted(false);
        btnGuardar.setPreferredSize(new java.awt.Dimension(100, 35));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        jPanel1.add(btnGuardar);

        panelBotones.add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(420, 50));

        btnConfirmarResolucion.setBackground(new java.awt.Color(46, 204, 113));
        btnConfirmarResolucion.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnConfirmarResolucion.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirmarResolucion.setText("Confirmar resolución");
        btnConfirmarResolucion.setFocusPainted(false);
        btnConfirmarResolucion.setPreferredSize(new java.awt.Dimension(200, 35));
        btnConfirmarResolucion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmarResolucionActionPerformed(evt);
            }
        });
        jPanel2.add(btnConfirmarResolucion);

        panelBotones.add(jPanel2, java.awt.BorderLayout.NORTH);

        panelDetalle.add(panelBotones, java.awt.BorderLayout.SOUTH);

        add(panelDetalle, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void comboPrioridadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboPrioridadActionPerformed
        aplicarFiltros();
    }//GEN-LAST:event_comboPrioridadActionPerformed

    private void comboEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboEstadoActionPerformed
        aplicarFiltros();
    }//GEN-LAST:event_comboEstadoActionPerformed

    private void btnNuevaIncidenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaIncidenciaActionPerformed
        // Crear y mostrar el diálogo
        DialogoNuevaIncidencia dialogo = new DialogoNuevaIncidencia(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                true
        );
        dialogo.setVisible(true);

        // Si se creó la incidencia, recargar las tarjetas
        if (dialogo.isIncidenciaCreada()) {
            cargarIncidencias();

            // Mostrar la nueva incidencia en el panel de detalle
            Incidencia nuevaIncidencia = dialogo.getIncidenciaCreada();
            if (nuevaIncidencia != null) {
                mostrarDetalleIncidencia(nuevaIncidencia);
            }
        }
    }//GEN-LAST:event_btnNuevaIncidenciaActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        activarModoEdicion();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarCambios();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        cancelarCambios();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnAyudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAyudaActionPerformed
        SistemaAyuda.mostrarAyudaIncidencias();
    }//GEN-LAST:event_btnAyudaActionPerformed

    private void btnAñadirComentarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAñadirComentarioActionPerformed
        añadirNuevoComentario();
    }//GEN-LAST:event_btnAñadirComentarioActionPerformed

    private void btnEditarComentarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarComentarioActionPerformed
        editarComentarioSeleccionado();
    }//GEN-LAST:event_btnEditarComentarioActionPerformed

    private void btnEliminarComentarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarComentarioActionPerformed
        eliminarComentarioSeleccionado();
    }//GEN-LAST:event_btnEliminarComentarioActionPerformed

    private void btnConfirmarResolucionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmarResolucionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnConfirmarResolucionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAyuda;
    private javax.swing.JButton btnAñadirComentario;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnConfirmarResolucion;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEditarComentario;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnEliminarComentario;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevaIncidencia;
    private javax.swing.JComboBox<String> comboAsignacion;
    private javax.swing.JComboBox<String> comboDetalleCategoria;
    private javax.swing.JComboBox<String> comboDetalleEstado;
    private javax.swing.JComboBox<String> comboDetallePrioridad;
    private javax.swing.JComboBox<String> comboDetalleTecnico;
    private javax.swing.JComboBox<String> comboEstado;
    private javax.swing.JComboBox<String> comboOrden;
    private javax.swing.JComboBox<String> comboPrioridad;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblComentarios;
    private javax.swing.JLabel lblContadorIncidencias;
    private javax.swing.JLabel lblDescripcionTitulo;
    private javax.swing.JLabel lblDetalleAsignacion;
    private javax.swing.JLabel lblDetalleCategoria;
    private javax.swing.JLabel lblDetalleEstado;
    private javax.swing.JLabel lblDetalleFechaCreacion;
    private javax.swing.JLabel lblDetalleId;
    private javax.swing.JLabel lblDetallePrioridad;
    private javax.swing.JLabel lblDetalleReportadoPor;
    private javax.swing.JLabel lblDetallesTitulo;
    private javax.swing.JList<String> listComentarios;
    private javax.swing.JPanel panelBarraSuperior;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelDetalle;
    private javax.swing.JPanel panelTarjetas;
    private javax.swing.JPanel panelTitulo;
    private javax.swing.JScrollPane scrollComentarios;
    private javax.swing.JScrollPane scrollNuevoComentario;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JScrollPane scrollPanelIncidencias;
    private javax.swing.JTextArea txtAreaDescripcion;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextArea txtNuevoComentario;
    // End of variables declaration//GEN-END:variables
}
