package gestorincidencias.vista;

import java.awt.*;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.JPanel;

/**
 * Componente gráfico de barras
 * @author martamorales
 */
public class GraficaBarras extends JPanel {
    private Map<String, Integer> datos;
    private Map<String, Color> colores;
    private String titulo;

    public GraficaBarras(String titulo) {
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

        if (datos.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.setColor(Color.GRAY);
            g2d.drawString("Sin datos", ancho / 2 - 25, alto / 2);
            return;
        }

        // Calcular máximo
        int maxValor = datos.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxValor == 0) maxValor = 1;

        // Área de barras
        int margenIzq = 25;
        int margenDer = 10;
        int margenTop = 35;
        int margenBot = 35;
        int areaAncho = ancho - margenIzq - margenDer;
        int areaAlto = alto - margenTop - margenBot;

        String[] keys = datos.keySet().toArray(new String[0]);
        int numBarras = keys.length;
        int separacion = 5;
        int anchoBarras = Math.max(30, (areaAncho - (separacion * (numBarras + 1))) / numBarras);

        // Líneas de referencia con escalas dinámicas
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, new float[]{3}, 0));

        // Calcular número de divisiones 
        int numDivisiones;
        if (maxValor <= 5) {
            numDivisiones = maxValor; // Para valores pequeños, mostrar todos
        } else if (maxValor <= 10) {
            numDivisiones = 5; 
        } else if (maxValor <= 20) {
            numDivisiones = 4; 
        } else if (maxValor <= 50) {
            numDivisiones = 5; 
        } else {
            numDivisiones = 4; 
        }

        if (numDivisiones == 0) {
            numDivisiones = 1;
        }

        for (int i = 0; i <= numDivisiones; i++) {
            int lineaY = margenTop + (areaAlto * i / numDivisiones);
            g2d.drawLine(margenIzq, lineaY, ancho - margenDer, lineaY);

            // Valor eje Y - cálculo inteligente
            g2d.setColor(new Color(150, 150, 150));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 8));

            // Calcular valor de forma proporcional
            int valorY = maxValor - (maxValor * i / numDivisiones);

            if (valorY >= 0) {
                g2d.drawString(String.valueOf(valorY), 2, lineaY + 4);
            }

            g2d.setColor(new Color(220, 220, 220));
        }

        // Dibujar barras
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < numBarras; i++) {
            String key = keys[i];
            int valor = datos.get(key);
            int altoBarra = (int) ((double) valor / maxValor * areaAlto);
            int barraX = margenIzq + separacion + i * (anchoBarras + separacion);
            int barraY = margenTop + areaAlto - altoBarra;

            // Sombra
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRoundRect(barraX + 2, barraY + 2, anchoBarras, altoBarra, 4, 4);

            // Barra
            g2d.setColor(colores.getOrDefault(key, new Color(52, 152, 219)));
            g2d.fillRoundRect(barraX, barraY, anchoBarras, altoBarra, 4, 4);

            // Valor encima
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2d.setColor(new Color(33, 47, 61));
            String valorStr = String.valueOf(valor);
            int valorX = barraX + (anchoBarras - g2d.getFontMetrics().stringWidth(valorStr)) / 2;
            if (altoBarra > 12) {
                g2d.drawString(valorStr, valorX, barraY - 2);
            }

            // Etiqueta debajo
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g2d.setColor(new Color(60, 60, 60));
            // Acortar texto si es muy largo
            String etiqueta = key.length() > 8 ? key.substring(0, 7) + "." : key;
            int etiquetaX = barraX + (anchoBarras - g2d.getFontMetrics().stringWidth(etiqueta)) / 2;
            g2d.drawString(etiqueta, etiquetaX, alto - margenBot + 12);
        }
    }
    
}
