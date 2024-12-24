package bsu.rfe.java.group7.lab4.Los.A10;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class InteractiveGraphPlotter extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private double xMin = -10;
    private double xMax = 10;
    private double step = 0.1;

    private List<Point> points = new ArrayList<>();
    private Rectangle selectionRect = null; // Прямоугольник для выделения области
    private double scaleX, scaleY;

    private Point mouseOverPoint = null; // Точка, над которой находится курсор

    public InteractiveGraphPlotter() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseOverPoint = findNearestPoint(e.getPoint());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectionRect != null) {
                    selectionRect.setSize(e.getX() - selectionRect.x, e.getY() - selectionRect.y);
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    selectionRect = new Rectangle(e.getX(), e.getY(), 0, 0);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    resetView();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && selectionRect != null) {
                    zoomToSelection();
                    selectionRect = null;
                }
            }
        });
    }

    private double function(double x) {
        return Math.sin(x); // Пример функции
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        scaleX = getWidth() / (xMax - xMin);
        scaleY = getHeight() / (xMax - xMin);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, centerY, getWidth(), centerY);
        g2d.drawLine(centerX, 0, centerX, getHeight());

        g2d.setColor(Color.BLUE);
        points.clear();

        double prevX = xMin, prevY = function(prevX);
        for (double x = xMin + step; x <= xMax; x += step) {
            double y = function(x);

            int x1 = centerX + (int) (prevX * scaleX);
            int y1 = centerY - (int) (prevY * scaleY);
            int x2 = centerX + (int) (x * scaleX);
            int y2 = centerY - (int) (y * scaleY);

            g2d.drawLine(x1, y1, x2, y2);

            if (Math.abs(y - Math.round(y)) <= 0.1) {
                drawTriangleMarker(g2d, x2, y2, Color.RED);
            } else {
                drawTriangleMarker(g2d, x2, y2, Color.BLACK);
            }

            points.add(new Point(x2, y2));
            prevX = x;
            prevY = y;
        }

        if (mouseOverPoint != null) {
            g2d.setColor(Color.GREEN);
            g2d.drawString(String.format("X: %.2f, Y: %.2f", 
                (mouseOverPoint.x - centerX) / scaleX, 
                (centerY - mouseOverPoint.y) / scaleY), 
                mouseOverPoint.x + 10, mouseOverPoint.y - 10);
        }

        if (selectionRect != null) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2d.draw(selectionRect);
        }
    }

    private void drawTriangleMarker(Graphics2D g2d, int x, int y, Color color) {
        int size = 11;

        Path2D triangle = new Path2D.Double();
        triangle.moveTo(x, y - size / 2);
        triangle.lineTo(x - size / 2, y + size / 2);
        triangle.lineTo(x + size / 2, y + size / 2);
        triangle.closePath();

        g2d.setColor(color);
        g2d.fill(triangle);
    }

    private Point findNearestPoint(Point mouse) {
        for (Point point : points) {
            if (mouse.distance(point) < 10) {
                return point;
            }
        }
        return null;
    }

    private void zoomToSelection() {
        if (selectionRect == null || selectionRect.width == 0 || selectionRect.height == 0) {
            return;
        }

        double newXMin = xMin + selectionRect.x / scaleX;
        double newXMax = xMin + (selectionRect.x + selectionRect.width) / scaleX;

        double newYMax = xMax - selectionRect.y / scaleY;
        double newYMin = xMax - (selectionRect.y + selectionRect.height) / scaleY;

        xMin = newXMin;
        xMax = newXMax;

        repaint();
    }

    private void resetView() {
        xMin = -10;
        xMax = 10;
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Интерактивный график");
        InteractiveGraphPlotter plotter = new InteractiveGraphPlotter();

        frame.add(plotter);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}