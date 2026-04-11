package gestorincidencias.vista;

import gestorincidencias.modelo.*;
import gestorincidencias.util.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Panel de generación de reportes con filtros avanzados y exportación a PDF.
 *
 * <p>
 * Proporciona funcionalidad completa de reporting para el sistema
 * incluyendo:</p>
 * <ul>
 * <li>Vista tabular de incidencias con información detallada y colores
 * diferenciados</li>
 * <li>Sistema de filtros múltiples: fechas, prioridad, estado, categoría,
 * empleado y cliente</li>
 * <li>Estadísticas automáticas con tiempo promedio de resolución y contadores
 * por estado</li>
 * <li>Gráficas visuales: tarta por estados, barras por prioridades y
 * categorías</li>
 * <li>Exportación a PDF con filtros aplicados y estadísticas completas</li>
 * <li>Interfaz responsive con carga dinámica de datos</li>
 * </ul>
 *
 * <p>
 * <strong>Control de acceso:</strong> Disponible para roles ADMINISTRADOR y
 * TECNICO.</p>
 *
 * <p>
 * <strong>Funcionalidades de filtrado:</strong></p>
 * <ul>
 * <li><strong>Rango de fechas:</strong> Filtrado por período de creación</li>
 * <li><strong>Por prioridad:</strong> Alta, Media, Baja o todas</li>
 * <li><strong>Por estado:</strong> Pendiente, En Proceso, Resuelta,
 * Cerrada</li>
 * <li><strong>Por categoría:</strong> Software, Hardware, Red, Seguridad,
 * Otro</li>
 * <li><strong>Por empleado:</strong> Técnicos asignados o sin asignar</li>
 * <li><strong>Por cliente:</strong> Filtrado por usuario reportante</li>
 * </ul>
 *
 * <p>
 * <strong>Exportación PDF:</strong> Genera reportes profesionales con títulos
 * dinámicos basados en filtros aplicados, estadísticas resumidas y tabla
 * detallada de incidencias.</p>
 *
 * @author martamorales
 * @version 1.0
 * @since 2025
 */
public class PanelReportes extends javax.swing.JPanel {

    private GestorIncidencias gestorIncidencias;
    private GestorUsuarios gestorUsuarios;
    private DefaultTableModel modeloTabla;
    private List<Incidencia> incidenciasCompletas;
    // Gráficas
    private GraficaTarta graficaEstados;
    private GraficaBarras graficaPrioridades;
    private GraficaBarras graficaCategorias;

    // Colores del tema
    private final Color COLOR_HEADER = new Color(33, 47, 61);
    private final Color COLOR_CYAN = new Color(0, 230, 255);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BLANCO = Color.WHITE;
    private final Color COLOR_TEXTO = new Color(60, 60, 60);
    private final Color COLOR_GRID = new Color(220, 220, 220);
    private final Color COLOR_SELECCION = new Color(230, 245, 255);
    private final Color COLOR_ROJO = new Color(231, 76, 60);

    /**
     * Inicializa el panel de reportes y configura todos sus componentes.
     * Establece gestores, configura tabla, carga combos dinámicos, crea
     * gráficas, carga incidencias y configura estilos y eventos.
     */
    public PanelReportes() {
        gestorIncidencias = GestorIncidencias.getInstance();
        gestorUsuarios = GestorUsuarios.getInstance();
        initComponents();
        configurarTabla();
        cargarCombos();
        crearGraficas();
        cargarIncidencias();
        configurarEstilos();
        configurarEventos();
    }

    /**
     * Configura la tabla de reportes con 9 columnas y renderers personalizados.
     */
    private void configurarTabla() {
        String[] columnas = {"ID", "Título", "Cliente", "Estado",
            "Prioridad", "Categoría", "Fecha Creación",
            "Última Actualización", "Empleado"};

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaReportes.setModel(modeloTabla);
        tablaReportes.setFillsViewportHeight(true);
        tablaReportes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaReportes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaReportes.setRowHeight(40);
        tablaReportes.setShowGrid(true);
        tablaReportes.setGridColor(COLOR_GRID);
        tablaReportes.setSelectionBackground(COLOR_SELECCION);
        tablaReportes.setSelectionForeground(COLOR_TEXTO);
        tablaReportes.setBackground(COLOR_BLANCO);
        tablaReportes.setIntercellSpacing(new Dimension(1, 1));

        // Header
        JTableHeader header = tablaReportes.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_HEADER);
        header.setForeground(COLOR_BLANCO);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Anchos de columnas
        TableColumnModel cm = tablaReportes.getColumnModel();
        cm.getColumn(0).setPreferredWidth(40);
        cm.getColumn(0).setMaxWidth(50);
        cm.getColumn(1).setPreferredWidth(200);
        cm.getColumn(2).setPreferredWidth(120);
        cm.getColumn(3).setPreferredWidth(100);
        cm.getColumn(4).setPreferredWidth(80);
        cm.getColumn(5).setPreferredWidth(100);
        cm.getColumn(6).setPreferredWidth(120);
        cm.getColumn(7).setPreferredWidth(140);
        cm.getColumn(8).setPreferredWidth(120);

        // Renderer centrado
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        cm.getColumn(0).setCellRenderer(centrado);
        cm.getColumn(6).setCellRenderer(centrado);
        cm.getColumn(7).setCellRenderer(centrado);

        // Renderer colores Estado
        cm.getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setOpaque(true);

                if (value != null && !isSelected) {
                    switch (value.toString()) {
                        case "Pendiente":
                            setBackground(new Color(241, 196, 15));
                            setForeground(Color.WHITE);
                            break;
                        case "En Proceso":
                            setBackground(new Color(52, 152, 219));
                            setForeground(Color.WHITE);
                            break;
                        case "Resuelta":
                            setBackground(new Color(46, 204, 113));
                            setForeground(Color.WHITE);
                            break;
                        case "Cerrada":
                            setBackground(new Color(149, 165, 166));
                            setForeground(Color.WHITE);
                            break;
                        default:
                            setBackground(Color.WHITE);
                            setForeground(COLOR_TEXTO);
                    }
                } else if (isSelected) {
                    setBackground(COLOR_SELECCION);
                    setForeground(COLOR_TEXTO);
                }
                return this;
            }
        });

        // Renderer colores Prioridad
        cm.getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setOpaque(true);

                if (value != null && !isSelected) {
                    switch (value.toString()) {
                        case "Alta":
                            setBackground(new Color(231, 76, 60));
                            setForeground(Color.WHITE);
                            break;
                        case "Media":
                            setBackground(new Color(230, 126, 34));
                            setForeground(Color.WHITE);
                            break;
                        case "Baja":
                            setBackground(new Color(46, 204, 113));
                            setForeground(Color.WHITE);
                            break;
                        default:
                            setBackground(Color.WHITE);
                            setForeground(COLOR_TEXTO);
                    }
                } else if (isSelected) {
                    setBackground(COLOR_SELECCION);
                    setForeground(COLOR_TEXTO);
                }
                return this;
            }
        });
    }

    /**
     * Configura estilos visuales y cursores para todos los componentes
     * interactivos.
     */
    private void configurarEstilos() {
        // Cursores
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarFiltros.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportarPDF.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboPrioridad.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboEstado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboCategoria.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboEmpleado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboCliente.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Configura eventos de botones y efectos hover para mejorar interactividad.
     */
    private void configurarEventos() {
        // Botón Aceptar
        btnAceptar.addActionListener(e -> aplicarFiltros());

        // Botón Limpiar
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());

        // Botón Exportar PDF
        btnExportarPDF.addActionListener(e -> exportarPDF());

        // Hover btnAceptar
        btnAceptar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAceptar.setBackground(new Color(0, 200, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAceptar.setBackground(COLOR_CYAN);
            }
        });

        // Hover btnLimpiarFiltros
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

        // Hover btnExportarPDF
        btnExportarPDF.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnExportarPDF.setBackground(new Color(200, 60, 45));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnExportarPDF.setBackground(COLOR_ROJO);
            }
        });

    }

    /**
     * Carga los combos con datos dinámicos obtenidos de la base de datos.
     */
    private void cargarCombos() {
        // Combo Empleados - carga nombres de técnicos desde incidencias
        comboEmpleado.removeAllItems();
        comboEmpleado.addItem("Todos");
        comboEmpleado.addItem("Sin asignar");
        List<Usuario> tecnicos = gestorUsuarios.obtenerPorRol(Rol.TECNICO);
        for (Usuario tecnico : tecnicos) {
            comboEmpleado.addItem(tecnico.getNombreCompleto());
        }

        // Combo Clientes - carga nombres de clientes desde incidencias
        comboCliente.removeAllItems();
        comboCliente.addItem("Todos");
        List<Usuario> clientes = gestorUsuarios.obtenerPorRol(Rol.CLIENTE);
        for (Usuario cliente : clientes) {
            comboCliente.addItem(cliente.getNombreCompleto());
        }
    }

    /**
     * Carga todas las incidencias del sistema en la tabla son aplicar filtros.
     */
    private void cargarIncidencias() {
        incidenciasCompletas = gestorIncidencias.obtenerTodas();
        mostrarIncidencias(incidenciasCompletas);
    }

    /**
     * Muestra una lista específica de incidencias en la tabla con formato.
     * Limpia tabla actual, formatea datos de fechas y nombres, añade filas y
     * actualiza estadísticas y gráficas basadas en los datos mostrados.
     *
     * @param incidencias Lista de incidencias a mostrar en la tabla
     */
    private void mostrarIncidencias(List<Incidencia> incidencias) {
        modeloTabla.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Incidencia inc : incidencias) {
            String cliente = inc.getNombreCliente() != null
                    ? inc.getNombreCliente() : "N/A";
            String empleado = inc.getNombreTecnico() != null
                    ? inc.getNombreTecnico() : "Sin asignar";
            String fechaCreacion = inc.getFechaCreacion() != null
                    ? inc.getFechaCreacion().format(dtf) : "N/A";
            String fechaActualizacion = inc.getFechaActualizacion() != null
                    ? inc.getFechaActualizacion().format(dtf) : "N/A";

            Object[] fila = {
                inc.getId(),
                inc.getTitulo(),
                cliente,
                inc.getEstado(),
                inc.getPrioridad(),
                inc.getCategoria(),
                fechaCreacion,
                fechaActualizacion,
                empleado
            };
            modeloTabla.addRow(fila);
        }
        actualizarEstadisticas(incidencias);
    }

    /**
     * Aplica todos los filtros seleccionados por el usuario a las incidencias.
     */
    private void aplicarFiltros() {

        if (incidenciasCompletas == null) {
            return;
        }

        List<Incidencia> filtradas = new ArrayList<>();

        String prioridadSel = (String) comboPrioridad.getSelectedItem();
        String empleadoSel = (String) comboEmpleado.getSelectedItem();
        String categoriaSel = (String) comboCategoria.getSelectedItem();
        String estadoSel = (String) comboEstado.getSelectedItem();
        String clienteSel = (String) comboCliente.getSelectedItem();
        Date fechaDesde = dateDesde.getDate();
        Date fechaHasta = dateHasta.getDate();

        for (Incidencia inc : incidenciasCompletas) {
            boolean cumple = true;

            // Filtro por prioridad - comparar con toString() del enum
            if (prioridadSel != null && !"Todas".equals(prioridadSel)) {
                if (inc.getPrioridad() == null
                        || !prioridadSel.equals(inc.getPrioridad().toString())) {
                    cumple = false;
                }
            }

            // Filtro por estado - comparar con toString() del enum
            if (estadoSel != null && !"Todos".equals(estadoSel)) {
                if (inc.getEstado() == null
                        || !estadoSel.equals(inc.getEstado().toString())) {
                    cumple = false;
                }
            }

            // Filtro por categoría - comparar con toString() del enum
            if (categoriaSel != null && !"Todas".equals(categoriaSel)) {
                if (inc.getCategoria() == null
                        || !categoriaSel.equals(inc.getCategoria().toString())) {
                    cumple = false;
                }
            }

            // Filtro por empleado
            if (empleadoSel != null && !"Todos".equals(empleadoSel)) {
                boolean empleadoCumple = false;

                if ("Sin asignar".equals(empleadoSel)) {
                    // Mostrar solo incidencias sin técnico asignado
                    if (inc.getIdTecnicoAsignado() == null || inc.getIdTecnicoAsignado() <= 0) {
                        empleadoCumple = true;
                    }
                } else {
                    // Filtrar por técnico específico
                    if (inc.getIdTecnicoAsignado() != null && inc.getIdTecnicoAsignado() > 0) {
                        Usuario tecnicoAsignado = gestorUsuarios.obtenerPorId(inc.getIdTecnicoAsignado());
                        if (tecnicoAsignado != null) {
                            String nombreCompletoTecnico = tecnicoAsignado.getNombreCompleto();
                            if (empleadoSel.equals(nombreCompletoTecnico)) {
                                empleadoCumple = true;
                            }
                        }
                    }
                }

                if (!empleadoCumple) {
                    cumple = false;
                }
            }

            // Filtro por cliente
            if (clienteSel != null && !"Todos".equals(clienteSel)) {
                String cliente = inc.getNombreCliente() != null
                        ? inc.getNombreCliente() : "";
                if (!clienteSel.equals(cliente)) {
                    cumple = false;
                }
            }

            // Filtro por fecha desde
            if (fechaDesde != null && inc.getFechaCreacion() != null) {
                LocalDate ldDesde = fechaDesde.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate fechaIncCreacion = inc.getFechaCreacion().toLocalDate();
                if (fechaIncCreacion.isBefore(ldDesde)) {
                    cumple = false;
                }
            }

            // Filtro por fecha hasta
            if (fechaHasta != null && inc.getFechaCreacion() != null) {
                LocalDate ldHasta = fechaHasta.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate fechaIncCreacion = inc.getFechaCreacion().toLocalDate();
                if (fechaIncCreacion.isAfter(ldHasta)) {
                    cumple = false;
                }
            }

            if (cumple) {
                filtradas.add(inc);
            }
        }
        mostrarIncidencias(filtradas);
    }

    /**
     * Limpia todos los filtros aplicados y restaura vista completa.
     */
    private void limpiarFiltros() {
        dateDesde.setDate(null);
        dateHasta.setDate(null);
        comboPrioridad.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
        comboCategoria.setSelectedIndex(0);
        comboEmpleado.setSelectedIndex(0);
        comboCliente.setSelectedIndex(0);
        mostrarIncidencias(incidenciasCompletas);
    }

    /**
     * Actualiza las estadísticas del panel basadas en incidencias mostradas.
     * Calcula contadores por estado, tiempo promedio de resolución y actualiza
     * labels informativos y gráficas visuales.
     *
     * @param incidencias Lista de incidencias para calcular estadísticas
     */
    private void actualizarEstadisticas(List<Incidencia> incidencias) {
        int total = incidencias.size();
        int pendientes = 0, enProceso = 0, resueltas = 0, cerradas = 0;
        long sumaDias = 0;
        int conFecha = 0;

        for (Incidencia inc : incidencias) {
            Estado estado = inc.getEstado();
            if (estado != null) {
                switch (estado) {
                    case PENDIENTE:
                        pendientes++;
                        break;
                    case EN_PROCESO:
                        enProceso++;
                        break;
                    case RESUELTA:
                        resueltas++;
                        break;
                    case CERRADA:
                        cerradas++;
                        break;
                }
            }

            if (inc.getFechaCreacion() != null && inc.getFechaResolucion() != null) {
                long dias = java.time.temporal.ChronoUnit.DAYS.between(
                        inc.getFechaCreacion(), inc.getFechaResolucion());
                sumaDias += dias;
                conFecha++;
            }
            actualizarGraficas(incidencias);
        }

        String tiempoPromedio = conFecha > 0 ? (sumaDias / conFecha) + " días" : "N/A";
        lblTiempoPromedio.setText("Tiempo promedio de resolución: " + tiempoPromedio);
        lblTotalIncidencias.setText("Total incidencias: " + total);
        lblPendientes.setText("Pendientes: " + pendientes + "   |   En Proceso: " + enProceso);
        lblResueltas.setText("Resueltas: " + resueltas + "   |   Cerradas: " + cerradas);
    }

    /**
     * Obtiene las incidencias actualmente mostradas en la tabla (con filtros
     * aplicados). Extrae IDs de las filas visibles y busca incidencias
     * completas correspondientes para exportación o procesamiento adicional.
     *
     * @return Lista de incidencias que están actualmente visibles en la tabla
     */
    private List<Incidencia> obtenerIncidenciasFiltradas() {
        List<Incidencia> incidenciasFiltradas = new ArrayList<>();

        // Recorrer las filas de la tabla para obtener solo las que se están mostrando
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            int idIncidencia = (int) modeloTabla.getValueAt(i, 0);

            // Buscar la incidencia completa por ID
            for (Incidencia inc : incidenciasCompletas) {
                if (inc.getId() == idIncidencia) {
                    incidenciasFiltradas.add(inc);
                    break;
                }
            }
        }

        return incidenciasFiltradas;
    }

    /**
     * Exporta el reporte a PDF.
     */
    private void exportarPDF() {
        try {

            // Selector de archivo
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Guardar reporte PDF");
            fileChooser.setSelectedFile(new java.io.File("Reporte_Incidencias_"
                    + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    + ".pdf"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos PDF", "pdf"));

            int resultado = fileChooser.showSaveDialog(this);
            if (resultado != javax.swing.JFileChooser.APPROVE_OPTION) {
                return;
            }

            java.io.File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
                archivo = new java.io.File(archivo.getAbsolutePath() + ".pdf");
            }

            // Crear el PDF
            crearReportePDF(archivo.getAbsolutePath());

            // Mostrar confirmación
            int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Reporte PDF generado exitosamente en:\n" + archivo.getAbsolutePath()
                    + "\n\n¿Desea abrirlo ahora?",
                    "PDF Generado",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );

            if (respuesta == javax.swing.JOptionPane.YES_OPTION) {
                abrirArchivo(archivo);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Error al generar el PDF: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    /**
     * Crea el documento PDF completo con estadísticas y tabla de datos. Utiliza
     * iText para generar PDF profesional con título dinámico, estadísticas
     * resumidas, tabla detallada de incidencias y pie de página.
     *
     * @param rutaArchivo Ruta completa donde guardar el archivo PDF
     * @throws Exception Si ocurre error durante la generación del documento
     */
    private void crearReportePDF(String rutaArchivo) throws Exception {
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(rutaArchivo);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

        // 1. TÍTULO DINÁMICO
        document.add(new com.itextpdf.layout.element.Paragraph(generarTituloPDF())
                .setFontSize(24)
                .setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE));

        // 1.2. SUBTÍTULO CON FILTROS
        document.add(new com.itextpdf.layout.element.Paragraph(generarSubtituloPDF())
                .setFontSize(12)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setMarginBottom(10));

        // 2. FECHA
        document.add(new com.itextpdf.layout.element.Paragraph(
                "Generado el: " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setMarginBottom(20));

        // 3. ESTADÍSTICAS
        document.add(new com.itextpdf.layout.element.Paragraph("ESTADÍSTICAS GENERALES")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10));

        List<Incidencia> incidenciasParaPDF = obtenerIncidenciasFiltradas();
        int total = incidenciasParaPDF.size();
        int pendientes = (int) incidenciasParaPDF.stream().filter(i -> i.getEstado() == Estado.PENDIENTE).count();
        int enProceso = (int) incidenciasParaPDF.stream().filter(i -> i.getEstado() == Estado.EN_PROCESO).count();
        int resueltas = (int) incidenciasParaPDF.stream().filter(i -> i.getEstado() == Estado.RESUELTA).count();

        // Tabla de estadísticas
        com.itextpdf.layout.element.Table tablaStats = new com.itextpdf.layout.element.Table(2);
        tablaStats.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Estado").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
        tablaStats.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Cantidad").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));

        tablaStats.addCell("Total").addCell(String.valueOf(total));
        tablaStats.addCell("Pendientes").addCell(String.valueOf(pendientes));
        tablaStats.addCell("En Proceso").addCell(String.valueOf(enProceso));
        tablaStats.addCell("Resueltas").addCell(String.valueOf(resueltas));

        document.add(tablaStats);
        document.add(new com.itextpdf.layout.element.Paragraph("\n"));

        // 4. TABLA DE INCIDENCIAS
        document.add(new com.itextpdf.layout.element.Paragraph("DETALLE DE INCIDENCIAS")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10));

        com.itextpdf.layout.element.Table tabla = new com.itextpdf.layout.element.Table(6);
        tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("ID").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
        tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Título").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
        tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Cliente").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
        tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Estado").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
        tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Prioridad").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
        tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("Fecha").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));

        for (Incidencia inc : incidenciasParaPDF) {
            tabla.addCell(inc.getIdFormateado());
            tabla.addCell(inc.getTitulo());
            tabla.addCell(inc.getNombreCliente());
            tabla.addCell(inc.getEstado().getNombre());
            tabla.addCell(inc.getPrioridad().getNombre());
            tabla.addCell(inc.getFechaCreacion().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        document.add(tabla);

        // 5. PIE DE PÁGINA
        document.add(new com.itextpdf.layout.element.Paragraph(
                "\nReporte generado automáticamente por el Sistema de Gestión de Incidencias")
                .setFontSize(10)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginTop(20));

        document.close();
    }

    /**
     * Crea las tres gráficas del panel: estados (tarta), prioridades y
     * categorías (barras). Inicializa componentes gráficos con tamaños
     * apropiados y los añade al panel.
     */
    private void crearGraficas() {
        graficaEstados = new GraficaTarta("Por Estado");
        graficaEstados.setBackground(Color.WHITE);
        graficaEstados.setPreferredSize(new Dimension(180, 180));
        panelGraficas.add(graficaEstados);

        graficaPrioridades = new GraficaBarras("Por Prioridad");
        graficaPrioridades.setBackground(Color.WHITE);
        graficaPrioridades.setPreferredSize(new Dimension(180, 180));
        panelGraficas.add(graficaPrioridades);

        graficaCategorias = new GraficaBarras("Por Categoría");
        graficaCategorias.setBackground(Color.WHITE);
        graficaCategorias.setPreferredSize(new Dimension(220, 180));
        panelGraficas.add(graficaCategorias);
    }

    /**
     * Actualiza todas las gráficas con datos de incidencias actuales. Cuenta
     * valores por estado, prioridad y categoría, asigna colores apropiados y
     * refresca las visualizaciones gráficas.
     *
     * @param incidencias Lista de incidencias para generar gráficas
     */
    private void actualizarGraficas(List<Incidencia> incidencias) {
        // ===== GRÁFICA ESTADOS =====
        Map<String, Integer> datosEstados = new LinkedHashMap<>();
        Map<String, Color> coloresEstados = new LinkedHashMap<>();

        datosEstados.put("Pendiente", 0);
        datosEstados.put("En Proceso", 0);
        datosEstados.put("Resuelta", 0);
        datosEstados.put("Cerrada", 0);

        coloresEstados.put("Pendiente", new Color(241, 196, 15));
        coloresEstados.put("En Proceso", new Color(52, 152, 219));
        coloresEstados.put("Resuelta", new Color(46, 204, 113));
        coloresEstados.put("Cerrada", new Color(149, 165, 166));

        // ===== GRÁFICA PRIORIDADES =====
        Map<String, Integer> datosPrioridades = new LinkedHashMap<>();
        Map<String, Color> coloresPrioridades = new LinkedHashMap<>();

        datosPrioridades.put("Baja", 0);
        datosPrioridades.put("Media", 0);
        datosPrioridades.put("Alta", 0);

        coloresPrioridades.put("Baja", new Color(46, 204, 113));
        coloresPrioridades.put("Media", new Color(230, 126, 34));
        coloresPrioridades.put("Alta", new Color(231, 76, 60));

        // ===== GRÁFICA CATEGORÍAS =====
        Map<String, Integer> datosCategorias = new LinkedHashMap<>();
        Map<String, Color> coloresCategorias = new LinkedHashMap<>();

        datosCategorias.put("Software", 0);
        datosCategorias.put("Hardware", 0);
        datosCategorias.put("Red", 0);
        datosCategorias.put("Seguridad", 0);
        datosCategorias.put("Otro", 0);

        coloresCategorias.put("Software", new Color(52, 152, 219));
        coloresCategorias.put("Hardware", new Color(155, 89, 182));
        coloresCategorias.put("Red", new Color(230, 126, 34));
        coloresCategorias.put("Seguridad", new Color(231, 76, 60));
        coloresCategorias.put("Otro", new Color(149, 165, 166));

        // Contar valores
        for (Incidencia inc : incidencias) {
            if (inc.getEstado() != null) {
                String estado = inc.getEstado().toString();
                if (datosEstados.containsKey(estado)) {
                    datosEstados.put(estado, datosEstados.get(estado) + 1);
                }
            }
            if (inc.getPrioridad() != null) {
                String prioridad = inc.getPrioridad().toString();
                if (datosPrioridades.containsKey(prioridad)) {
                    datosPrioridades.put(prioridad, datosPrioridades.get(prioridad) + 1);
                }
            }
            if (inc.getCategoria() != null) {
                String categoria = inc.getCategoria().toString();
                if (datosCategorias.containsKey(categoria)) {
                    datosCategorias.put(categoria, datosCategorias.get(categoria) + 1);
                }
            }
        }

        // Actualizar gráficas
        graficaEstados.setDatos(datosEstados, coloresEstados);
        graficaPrioridades.setDatos(datosPrioridades, coloresPrioridades);
        graficaCategorias.setDatos(datosCategorias, coloresCategorias);
    }

    /**
     * Genera título dinámico para el PDF según filtros aplicados. Retorna
     * "REPORTE COMPLETO" si no hay filtros o "REPORTE FILTRADO" si se han
     * aplicado restricciones de búsqueda.
     *
     * @return String con título apropiado para el documento PDF
     */
    private String generarTituloPDF() {
        List<String> filtrosActivos = new ArrayList<>();

        // Verificar cada filtro
        String prioridadSel = (String) comboPrioridad.getSelectedItem();
        if (prioridadSel != null && !"Todas".equals(prioridadSel)) {
            filtrosActivos.add("Prioridad: " + prioridadSel);
        }

        String empleadoSel = (String) comboEmpleado.getSelectedItem();
        if (empleadoSel != null && !"Todos".equals(empleadoSel)) {
            filtrosActivos.add("Empleado: " + empleadoSel);
        }

        String categoriaSel = (String) comboCategoria.getSelectedItem();
        if (categoriaSel != null && !"Todas".equals(categoriaSel)) {
            filtrosActivos.add("Categoría: " + categoriaSel);
        }

        String estadoSel = (String) comboEstado.getSelectedItem();
        if (estadoSel != null && !"Todos".equals(estadoSel)) {
            filtrosActivos.add("Estado: " + estadoSel);
        }

        String clienteSel = (String) comboCliente.getSelectedItem();
        if (clienteSel != null && !"Todos".equals(clienteSel)) {
            filtrosActivos.add("Cliente: " + clienteSel);
        }

        Date fechaDesde = dateDesde.getDate();
        Date fechaHasta = dateHasta.getDate();
        if (fechaDesde != null || fechaHasta != null) {
            String filtroFecha = "Fecha: ";
            if (fechaDesde != null && fechaHasta != null) {
                filtroFecha += java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(fechaDesde.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                        + " - "
                        + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(fechaHasta.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            } else if (fechaDesde != null) {
                filtroFecha += "desde " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(fechaDesde.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            } else {
                filtroFecha += "hasta " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(fechaHasta.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            }
            filtrosActivos.add(filtroFecha);
        }

        // Generar título según filtros
        if (filtrosActivos.isEmpty()) {
            return "REPORTE COMPLETO DE INCIDENCIAS";
        } else {
            return "REPORTE DE INCIDENCIAS (FILTRADO)";
        }
    }

    /**
     * Genera subtítulo detallado con información de filtros aplicados. Lista
     * todos los filtros activos con sus valores seleccionados para documentar
     * el alcance del reporte generado.
     *
     * @return String con descripción detallada de filtros aplicados
     */
    private String generarSubtituloPDF() {
        List<String> filtrosActivos = new ArrayList<>();

        // Reutilizar la lógica del método anterior
        String prioridadSel = (String) comboPrioridad.getSelectedItem();
        if (prioridadSel != null && !"Todas".equals(prioridadSel)) {
            filtrosActivos.add("Prioridad: " + prioridadSel);
        }

        String empleadoSel = (String) comboEmpleado.getSelectedItem();
        if (empleadoSel != null && !"Todos".equals(empleadoSel)) {
            filtrosActivos.add("Empleado: " + empleadoSel);
        }

        String categoriaSel = (String) comboCategoria.getSelectedItem();
        if (categoriaSel != null && !"Todas".equals(categoriaSel)) {
            filtrosActivos.add("Categoría: " + categoriaSel);
        }

        String estadoSel = (String) comboEstado.getSelectedItem();
        if (estadoSel != null && !"Todos".equals(estadoSel)) {
            filtrosActivos.add("Estado: " + estadoSel);
        }

        String clienteSel = (String) comboCliente.getSelectedItem();
        if (clienteSel != null && !"Todos".equals(clienteSel)) {
            filtrosActivos.add("Cliente: " + clienteSel);
        }

        Date fechaDesde = dateDesde.getDate();
        Date fechaHasta = dateHasta.getDate();
        if (fechaDesde != null || fechaHasta != null) {
            String filtroFecha = "Período: ";
            if (fechaDesde != null && fechaHasta != null) {
                filtroFecha += java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(fechaDesde.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                        + " - "
                        + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(fechaHasta.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            } else if (fechaDesde != null) {
                filtroFecha += "desde " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(fechaDesde.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            } else {
                filtroFecha += "hasta " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(fechaHasta.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            }
            filtrosActivos.add(filtroFecha);
        }

        if (filtrosActivos.isEmpty()) {
            return "Mostrando todas las incidencias del sistema";
        } else {
            return "Filtros aplicados: " + String.join(" | ", filtrosActivos);
        }
    }

    /**
     * Abre un archivo con la aplicación predeterminada del sistema operativo.
     * Utiliza Desktop API para lanzar visor de PDF o muestra mensaje
     * informativo si no es posible abrir automáticamente.
     *
     * @param archivo Archivo PDF a abrir con aplicación predeterminada
     */
    private void abrirArchivo(java.io.File archivo) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            } else {
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "No se puede abrir automáticamente el archivo.\nUbicación: " + archivo.getAbsolutePath(),
                        "Información",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Error al abrir el archivo: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
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
        lblTitulo = new javax.swing.JLabel();
        panelFiltros = new javax.swing.JPanel();
        panelDesde = new javax.swing.JPanel();
        lblDesde = new javax.swing.JLabel();
        dateDesde = new com.toedter.calendar.JDateChooser();
        panelHasta = new javax.swing.JPanel();
        lblHasta = new javax.swing.JLabel();
        dateHasta = new com.toedter.calendar.JDateChooser();
        panelPrioridad = new javax.swing.JPanel();
        lblPrioridad = new javax.swing.JLabel();
        comboPrioridad = new javax.swing.JComboBox<>();
        panelEmpleado = new javax.swing.JPanel();
        lblEmpleado = new javax.swing.JLabel();
        comboEmpleado = new javax.swing.JComboBox<>();
        panelCategoria = new javax.swing.JPanel();
        lblCategoria = new javax.swing.JLabel();
        comboCategoria = new javax.swing.JComboBox<>();
        panelEstados = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        comboEstado = new javax.swing.JComboBox<>();
        panelCliente = new javax.swing.JPanel();
        lblCliente = new javax.swing.JLabel();
        comboCliente = new javax.swing.JComboBox<>();
        panelBotones = new javax.swing.JPanel();
        btnAceptar = new javax.swing.JButton();
        btnLimpiarFiltros = new javax.swing.JButton();
        btnAyuda = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tablaReportes = new javax.swing.JTable();
        separador1 = new javax.swing.JPanel();
        panelEstadisticas = new javax.swing.JPanel();
        panelTitulo = new javax.swing.JPanel();
        lblTituloEstad = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        panelEstadisticasText = new javax.swing.JPanel();
        lblTiempoPromedio = new javax.swing.JLabel();
        lblTotalIncidencias = new javax.swing.JLabel();
        lblPendientes = new javax.swing.JLabel();
        lblResueltas = new javax.swing.JLabel();
        panelGraficas = new javax.swing.JPanel();
        separador2 = new javax.swing.JPanel();
        panelExportar = new javax.swing.JPanel();
        btnExportarPDF = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 245));
        setMinimumSize(new java.awt.Dimension(1000, 600));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        panelBarraSuperior.setBackground(new java.awt.Color(33, 47, 61));
        panelBarraSuperior.setPreferredSize(new java.awt.Dimension(1000, 70));
        panelBarraSuperior.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 15));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 230, 255));
        lblTitulo.setText("REPORTES");
        panelBarraSuperior.add(lblTitulo);

        add(panelBarraSuperior);

        panelFiltros.setBackground(new java.awt.Color(255, 255, 255));
        panelFiltros.setAlignmentY(0.0F);
        panelFiltros.setPreferredSize(new java.awt.Dimension(1000, 120));
        panelFiltros.setLayout(new java.awt.GridBagLayout());

        panelDesde.setOpaque(false);
        panelDesde.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblDesde.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDesde.setForeground(new java.awt.Color(33, 47, 61));
        lblDesde.setText("Desde: ");
        panelDesde.add(lblDesde);

        dateDesde.setDateFormatString("dd/MM/yyy");
        dateDesde.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        panelDesde.add(dateDesde);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelDesde, gridBagConstraints);

        panelHasta.setOpaque(false);
        panelHasta.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblHasta.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblHasta.setForeground(new java.awt.Color(33, 47, 61));
        lblHasta.setText("Hasta:");
        panelHasta.add(lblHasta);

        dateHasta.setDateFormatString("dd/MM/yyyy");
        dateHasta.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        panelHasta.add(dateHasta);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelHasta, gridBagConstraints);

        panelPrioridad.setOpaque(false);
        panelPrioridad.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblPrioridad.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPrioridad.setForeground(new java.awt.Color(33, 47, 61));
        lblPrioridad.setText("Prioridad: ");
        panelPrioridad.add(lblPrioridad);

        comboPrioridad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboPrioridad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todas", "Baja", "Media", "Alta" }));
        panelPrioridad.add(comboPrioridad);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelPrioridad, gridBagConstraints);

        panelEmpleado.setOpaque(false);
        panelEmpleado.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblEmpleado.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEmpleado.setForeground(new java.awt.Color(33, 47, 61));
        lblEmpleado.setText("Empleado: ");
        panelEmpleado.add(lblEmpleado);

        comboEmpleado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboEmpleado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos" }));
        panelEmpleado.add(comboEmpleado);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelEmpleado, gridBagConstraints);

        panelCategoria.setOpaque(false);
        panelCategoria.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblCategoria.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCategoria.setForeground(new java.awt.Color(33, 47, 61));
        lblCategoria.setText("Categoría: ");
        panelCategoria.add(lblCategoria);

        comboCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todas", "Software", "Hardware", "Red", "Seguridad", "Otro" }));
        panelCategoria.add(comboCategoria);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelCategoria, gridBagConstraints);

        panelEstados.setOpaque(false);
        panelEstados.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblEstado.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(33, 47, 61));
        lblEstado.setText("Estado:");
        panelEstados.add(lblEstado);

        comboEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Pendiente", "En Proceso", "Resuelta", "Cerrada" }));
        panelEstados.add(comboEstado);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelEstados, gridBagConstraints);

        panelCliente.setOpaque(false);
        panelCliente.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblCliente.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCliente.setForeground(new java.awt.Color(33, 47, 61));
        lblCliente.setText("Cliente:");
        panelCliente.add(lblCliente);

        comboCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        comboCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos" }));
        panelCliente.add(comboCliente);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelFiltros.add(panelCliente, gridBagConstraints);

        panelBotones.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 10));
        panelBotones.setOpaque(false);
        panelBotones.setPreferredSize(new java.awt.Dimension(200, 35));
        panelBotones.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        btnAceptar.setBackground(new java.awt.Color(0, 230, 255));
        btnAceptar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnAceptar.setForeground(new java.awt.Color(255, 255, 255));
        btnAceptar.setText("Aceptar");
        btnAceptar.setBorderPainted(false);
        btnAceptar.setFocusPainted(false);
        panelBotones.add(btnAceptar);

        btnLimpiarFiltros.setBackground(new java.awt.Color(150, 150, 150));
        btnLimpiarFiltros.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLimpiarFiltros.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiarFiltros.setText("Limpiar");
        panelBotones.add(btnLimpiarFiltros);

        panelFiltros.add(panelBotones, new java.awt.GridBagConstraints());

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

        add(panelFiltros);

        scrollTabla.setBorder(null);
        scrollTabla.setAlignmentY(0.0F);
        scrollTabla.setPreferredSize(new java.awt.Dimension(1000, 300));

        tablaReportes.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tablaReportes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaReportes.setFillsViewportHeight(true);
        tablaReportes.setRowHeight(40);
        scrollTabla.setViewportView(tablaReportes);

        add(scrollTabla);

        separador1.setOpaque(false);
        separador1.setPreferredSize(new java.awt.Dimension(1000, 15));
        add(separador1);

        panelEstadisticas.setBackground(new java.awt.Color(255, 255, 255));
        panelEstadisticas.setPreferredSize(new java.awt.Dimension(1000, 320));
        panelEstadisticas.setLayout(new java.awt.BorderLayout());

        panelTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 1));
        panelTitulo.setOpaque(false);
        panelTitulo.setPreferredSize(new java.awt.Dimension(1000, 50));
        panelTitulo.setLayout(new javax.swing.BoxLayout(panelTitulo, javax.swing.BoxLayout.Y_AXIS));

        lblTituloEstad.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloEstad.setForeground(new java.awt.Color(33, 47, 61));
        lblTituloEstad.setText("Estadísticas del reporte");
        lblTituloEstad.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelTitulo.add(lblTituloEstad);
        panelTitulo.add(jSeparator1);

        panelEstadisticas.add(panelTitulo, java.awt.BorderLayout.NORTH);

        panelEstadisticasText.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 5, 15, 5));
        panelEstadisticasText.setOpaque(false);
        panelEstadisticasText.setPreferredSize(new java.awt.Dimension(400, 300));
        panelEstadisticasText.setLayout(new javax.swing.BoxLayout(panelEstadisticasText, javax.swing.BoxLayout.Y_AXIS));

        lblTiempoPromedio.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTiempoPromedio.setForeground(new java.awt.Color(33, 47, 61));
        lblTiempoPromedio.setText("tiempo promedio de resolución: --");
        lblTiempoPromedio.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panelEstadisticasText.add(lblTiempoPromedio);

        lblTotalIncidencias.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTotalIncidencias.setForeground(new java.awt.Color(60, 60, 60));
        lblTotalIncidencias.setText("Total incidencias: --");
        lblTotalIncidencias.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panelEstadisticasText.add(lblTotalIncidencias);

        lblPendientes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPendientes.setForeground(new java.awt.Color(60, 60, 60));
        lblPendientes.setText("Pendientes: --  |  En Progreso: --");
        lblPendientes.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panelEstadisticasText.add(lblPendientes);

        lblResueltas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblResueltas.setForeground(new java.awt.Color(60, 60, 60));
        lblResueltas.setText("Resueltas: --  |  Cerradas: --");
        lblResueltas.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panelEstadisticasText.add(lblResueltas);

        panelEstadisticas.add(panelEstadisticasText, java.awt.BorderLayout.WEST);

        panelGraficas.setOpaque(false);
        panelGraficas.setPreferredSize(new java.awt.Dimension(600, 300));
        panelGraficas.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));
        panelEstadisticas.add(panelGraficas, java.awt.BorderLayout.CENTER);

        add(panelEstadisticas);
        panelEstadisticas.getAccessibleContext().setAccessibleName("");

        separador2.setOpaque(false);
        separador2.setPreferredSize(new java.awt.Dimension(1000, 20));
        add(separador2);

        panelExportar.setAlignmentY(0.0F);
        panelExportar.setOpaque(false);
        panelExportar.setPreferredSize(new java.awt.Dimension(1000, 60));

        btnExportarPDF.setBackground(new java.awt.Color(231, 76, 60));
        btnExportarPDF.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnExportarPDF.setForeground(new java.awt.Color(255, 255, 255));
        btnExportarPDF.setText("Exportar a PDF");
        btnExportarPDF.setBorderPainted(false);
        btnExportarPDF.setFocusPainted(false);
        btnExportarPDF.setMaximumSize(new java.awt.Dimension(150, 35));
        btnExportarPDF.setMinimumSize(new java.awt.Dimension(150, 35));
        btnExportarPDF.setPreferredSize(new java.awt.Dimension(150, 40));
        panelExportar.add(btnExportarPDF);

        add(panelExportar);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAyudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAyudaActionPerformed
        SistemaAyuda.mostrarAyudaReportes();
    }//GEN-LAST:event_btnAyudaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnAyuda;
    private javax.swing.JButton btnExportarPDF;
    private javax.swing.JButton btnLimpiarFiltros;
    private javax.swing.JComboBox<String> comboCategoria;
    private javax.swing.JComboBox<String> comboCliente;
    private javax.swing.JComboBox<String> comboEmpleado;
    private javax.swing.JComboBox<String> comboEstado;
    private javax.swing.JComboBox<String> comboPrioridad;
    private com.toedter.calendar.JDateChooser dateDesde;
    private com.toedter.calendar.JDateChooser dateHasta;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblEmpleado;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblPendientes;
    private javax.swing.JLabel lblPrioridad;
    private javax.swing.JLabel lblResueltas;
    private javax.swing.JLabel lblTiempoPromedio;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloEstad;
    private javax.swing.JLabel lblTotalIncidencias;
    private javax.swing.JPanel panelBarraSuperior;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCategoria;
    private javax.swing.JPanel panelCliente;
    private javax.swing.JPanel panelDesde;
    private javax.swing.JPanel panelEmpleado;
    private javax.swing.JPanel panelEstadisticas;
    private javax.swing.JPanel panelEstadisticasText;
    private javax.swing.JPanel panelEstados;
    private javax.swing.JPanel panelExportar;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelGraficas;
    private javax.swing.JPanel panelHasta;
    private javax.swing.JPanel panelPrioridad;
    private javax.swing.JPanel panelTitulo;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JPanel separador1;
    private javax.swing.JPanel separador2;
    private javax.swing.JTable tablaReportes;
    // End of variables declaration//GEN-END:variables
}
