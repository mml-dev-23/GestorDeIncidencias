package gestorincidencias.vista;

import java.awt.*;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.JPanel;

/**
 * Componente gráfico de tarta
 * @author martamorales
 */
public class GraficaTarta extends JPanel{
    private Map<String, Integer> datos;
    private Map<String, Color> colores;
    private String titulo;

    public GraficaTarta(String titulo) {
        this.titulo = titulo;
        this.datos = new LinkedHashMap<>();
        this.colores = new LinkedHashMap<>();
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    public void setDatos(Map<String, Integer> datos, Map<String, Color> colores) {
        this.datos = datos;
        this.colores = colores;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // Título
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.setColor(new Color(33, 47, 61));
        FontMetrics fm = g2d.getFontMetrics();
        int tituloX = (ancho - fm.stringWidth(titulo)) / 2;
        g2d.drawString(titulo, tituloX, 18);

        // Calcular total
        int total = datos.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.setColor(Color.GRAY);
            g2d.drawString("Sin datos", ancho / 2 - 25, alto / 2);
            return;
        }

        // Dimensiones del círculo
        int diametro = Math.min(ancho - 20, alto - 60);
        int x = (ancho - diametro) / 2;
        int y = 20;

        // Dibujar sectores
        int anguloInicio = 0;
        String[] keys = datos.keySet().toArray(new String[0]);

        for (String key : keys) {
            int valor = datos.get(key);
            int angulo = (int) Math.round(360.0 * valor / total);

            g2d.setColor(colores.getOrDefault(key, Color.GRAY));
            g2d.fillArc(x, y, diametro, diametro, anguloInicio, angulo);

            // Borde
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawArc(x, y, diametro, diametro, anguloInicio, angulo);

            anguloInicio += angulo;
        }

        // Leyenda debajo
        int leyendaY = y + diametro + 15;
        int leyendaX = 5;
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        for (String key : keys) {
            int valor = datos.get(key);
            int porcentaje = (int) Math.round(100.0 * valor / total);

            // Cuadrado de color
            g2d.setColor(colores.getOrDefault(key, Color.GRAY));
            g2d.fillRect(leyendaX, leyendaY - 8, 8, 8);

            // Texto
            g2d.setColor(new Color(60, 60, 60));
            g2d.drawString(key + " " + porcentaje + "%", leyendaX + 11, leyendaY);

            leyendaX += fm.stringWidth(key + " " + porcentaje + "%") + 20;

            // Nueva línea si no cabe
            if (leyendaX > ancho - 50) {
                leyendaX = 5;
                leyendaY += 14;
            }
        }
    }
    
}
