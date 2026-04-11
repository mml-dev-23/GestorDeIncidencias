/*
 * WrapLayout - Layout que envuelve componentes verticalmente
 * Evita el scroll horizontal al ajustar componentes en múltiples filas
 */
package gestorincidencias.util;

import java.awt.*;

/**
 * FlowLayout modificado que envuelve componentes verticalmente cuando no caben
 * horizontalmente en el contenedor.
 *
 * @author marta morales
 */
public class WrapLayout extends FlowLayout {

    /**
     * Constructor por defecto
     */
    public WrapLayout() {
        super();
    }

    /**
     * Constructor con alineación
     *
     * @param align Alineación (LEFT, CENTER, RIGHT, etc.)
     */
    public WrapLayout(int align) {
        super(align);
    }

    /**
     * Constructor completo
     *
     * @param align Alineación
     * @param hgap Gap horizontal entre componentes
     * @param vgap Gap vertical entre filas
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    /**
     * Calcula el tamaño preferido del layout
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    /**
     * Calcula el tamaño mínimo del layout
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    /**
     * Calcula las dimensiones del layout basándose en el ancho disponible
     *
     * @param target Contenedor objetivo
     * @param preferred Si true, usa tamaño preferido; si false, usa mínimo
     * @return Dimensiones calculadas
     */
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            // Obtener ancho disponible del contenedor
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            // Dimensión resultante
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            // Iterar sobre todos los componentes
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    // Si el componente no cabe en la fila actual, crear nueva fila
                    if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    // Añadir gap horizontal si no es el primer componente de la fila
                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }

                    // Actualizar dimensiones de la fila
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            // Añadir la última fila
            addRow(dim, rowWidth, rowHeight);

            // Añadir insets
            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            // Ajustar ancho al viewport si está dentro de un JScrollPane
            Container scrollPaneContainer = javax.swing.SwingUtilities.getAncestorOfClass(
                    javax.swing.JScrollPane.class, target);

            if (scrollPaneContainer != null && target.isValid()) {
                javax.swing.JScrollPane jsp = (javax.swing.JScrollPane) scrollPaneContainer;
                dim.width = jsp.getViewport().getWidth();
            }

            return dim;
        }
    }

    /**
     * Añade una fila al cálculo de dimensiones
     *
     * @param dim Dimensión acumulada
     * @param rowWidth Ancho de la fila
     * @param rowHeight Alto de la fila
     */
    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);

        if (dim.height > 0) {
            dim.height += getVgap();
        }

        dim.height += rowHeight;
    }
}
