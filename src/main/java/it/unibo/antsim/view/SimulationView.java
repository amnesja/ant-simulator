package it.unibo.antsim.view;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.model.environment.CellType;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.model.agent.Ant;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class SimulationView extends Canvas {
    private final Environment environment;
    private final Camera camera;
    private final WritableImage pheromoneField;
    private final PixelWriter pheromoneWriter;

    // Dark, professional palette
    private static final int SOIL_R = 13;
    private static final int SOIL_G = 15;
    private static final int SOIL_B = 21;
    private static final double PHERO_REF = 18.0;
    private static final Font FONT = Font.font("System", 12);

    private double lastMouseX;
    private double lastMouseY;

    public SimulationView(Environment environment) {
        super(ViewConfig.VIEW_WIDTH, ViewConfig.VIEW_HEIGHT);
        this.environment = environment;
        this.camera = new Camera(
                environment.getGrid().getWidth(),
                environment.getGrid().getHeight(),
                ViewConfig.VIEW_WIDTH,
                ViewConfig.VIEW_HEIGHT
        );
        this.pheromoneField = new WritableImage(
                environment.getGrid().getWidth(),
                environment.getGrid().getHeight()
        );
        this.pheromoneWriter = pheromoneField.getPixelWriter();

        setupMouseControls();

        // Keep the camera matched to the actual canvas size. The canvas is bound
        // to fill the center area in Main, so this makes the world always occupy
        // the whole view instead of being letterboxed into a square.
        widthProperty().addListener((obs, oldV, newV) -> camera.setViewSize(getWidth(), getHeight()));
        heightProperty().addListener((obs, oldV, newV) -> camera.setViewSize(getWidth(), getHeight()));
    }

    public void resetCamera() {
        camera.fit();
    }

    public void render(List<Ant> ants) {
        // Pheromones only change on a simulation step (10 Hz), so recomputing the
        // field every render frame (60 Hz) is wasteful. Throttle it.
        if (frame++ % 3 == 0) {
            updatePheromoneField();
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(SOIL_R, SOIL_G, SOIL_B));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.save();
        gc.translate(getWidth() / 2.0, getHeight() / 2.0);
        gc.scale(camera.getZoom(), camera.getZoom());
        gc.translate(-camera.getCenterX(), -camera.getCenterY());
        gc.setImageSmoothing(true);

        drawPheromoneField(gc);
        drawObstacles(gc);
        drawFood(gc);
        drawNest(gc);

        gc.restore();

        drawAnts(gc, ants);
        drawLegend(gc);
        drawMinimap(gc);
    }

    private int frame = 0;

    /**
     * Returns the inclusive cell range currently visible through the camera,
     * so the (potentially huge) grid is only iterated for on-screen cells.
     */
    private int[] visibleCellBounds() {
        int gx = environment.getGrid().getWidth();
        int gy = environment.getGrid().getHeight();
        int x0 = (int) Math.floor(camera.screenToWorldX(0));
        int y0 = (int) Math.floor(camera.screenToWorldY(0));
        int x1 = (int) Math.ceil(camera.screenToWorldX(getWidth()));
        int y1 = (int) Math.ceil(camera.screenToWorldY(getHeight()));
        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        x1 = Math.min(gx - 1, x1);
        y1 = Math.min(gy - 1, y1);
        return new int[]{x0, x1, y0, y1};
    }

    private void updatePheromoneField() {
        int[] b = visibleCellBounds();
        int w = environment.getGrid().getWidth();

        for (int x = b[0]; x <= b[1]; x++) {
            for (int y = b[2]; y <= b[3]; y++) {
                var cell = environment.getCell(x, y);
                double food = Math.min(cell.getPheromoneLevel() / PHERO_REF, 1.0);
                double home = Math.min(cell.getHomePheromoneLevel() / PHERO_REF, 1.0);

                // Food trails glow warm/orange, home trails glow cool/blue.
                int r = SOIL_R + (int) (food * 235) + (int) (home * 28);
                int g = SOIL_G + (int) (food * 120) + (int) (home * 150);
                int b2 = SOIL_B + (int) (food * 45) + (int) (home * 235);

                pheromoneWriter.setArgb(x, y, argb(clamp(r), clamp(g), clamp(b2)));
            }
        }
    }

    private void drawPheromoneField(GraphicsContext gc) {
        gc.drawImage(pheromoneField, 0, 0,
                environment.getGrid().getWidth(),
                environment.getGrid().getHeight());
    }

    private void drawObstacles(GraphicsContext gc) {
        int[] b = visibleCellBounds();

        for (int x = b[0]; x <= b[1]; x++) {
            for (int y = b[2]; y <= b[3]; y++) {
                if (environment.getCell(x, y).getType() != CellType.OBSTACLE) continue;

                // Stable per-cell shading so rocks read as organic blobs, not a grid.
                double shade = 0.82 + 0.18 * (((x * 31 + y * 17) % 7) / 6.0);
                int base = (int) (58 * shade);
                gc.setFill(Color.rgb(base, base + 8, base + 18));
                gc.fillOval(x - 0.08, y - 0.08, 1.16, 1.16);
            }
        }
    }

    private void drawFood(GraphicsContext gc) {
        int[] b = visibleCellBounds();

        for (int x = b[0]; x <= b[1]; x++) {
            for (int y = b[2]; y <= b[3]; y++) {
                var cell = environment.getCell(x, y);
                if (cell.getType() != CellType.FOOD) continue;

                double frac = clamp01(cell.getFoodHP() / SimulationConfig.FOOD_CLUSTER_HP);
                double radius = 0.32 + 0.30 * frac;

                if (frac > 0.15) {
                    gc.setFill(Color.rgb(40, 120, 60, 0.25 * frac));
                    gc.fillOval(x + 0.5 - radius * 1.6, y + 0.5 - radius * 1.6,
                            radius * 3.2, radius * 3.2);
                }

                int r = (int) (40 + 30 * frac);
                int g = (int) (150 + 90 * frac);
                int b2 = (int) (60 + 40 * frac);
                gc.setFill(Color.rgb(r, g, b2));
                gc.fillOval(x + 0.5 - radius, y + 0.5 - radius, radius * 2, radius * 2);
            }
        }
    }

    private void drawNest(GraphicsContext gc) {
        int nx = environment.getNestPosition().x();
        int ny = environment.getNestPosition().y();

        gc.setFill(Color.rgb(30, 24, 20));
        gc.fillOval(nx - 1.4, ny - 1.4, 2.8, 2.8);
        gc.setFill(Color.rgb(20, 14, 10));
        gc.fillOval(nx - 0.8, ny - 0.8, 1.6, 1.6);
        gc.setFill(Color.rgb(255, 176, 92, 0.9));
        gc.fillOval(nx - 0.28, ny - 0.28, 0.56, 0.56);
    }

    private void drawAnts(GraphicsContext gc, List<Ant> ants) {
        for (Ant ant : ants) {
            double sx = camera.worldToScreenX(ant.getDoubleX());
            double sy = camera.worldToScreenY(ant.getDoubleY());

            if (sx < -10 || sx > getWidth() + 10 || sy < -10 || sy > getHeight() + 10) continue;

            double angle = ant.getHeading();
            boolean carrying = ant.isCarryingFood();

            // Motion tail pointing back along the heading.
            gc.setStroke(carrying ? Color.rgb(120, 230, 140, 0.5) : Color.rgb(255, 210, 140, 0.45));
            gc.setLineWidth(1.4);
            gc.strokeLine(sx, sy, sx - Math.cos(angle) * 5, sy - Math.sin(angle) * 5);

            // Glow halo.
            gc.setFill(carrying ? Color.rgb(120, 230, 140, 0.30) : Color.rgb(255, 214, 150, 0.30));
            gc.fillOval(sx - 4, sy - 4, 8, 8);

            // Bright core.
            gc.setFill(carrying ? Color.rgb(190, 255, 200) : Color.rgb(255, 238, 210));
            gc.fillOval(sx - 1.7, sy - 1.7, 3.4, 3.4);
        }
    }

    private void drawLegend(GraphicsContext gc) {
        String[] labels = {"Scia verso il cibo", "Scia verso il nido", "Cibo", "Formiche"};
        Color[] colors = {
                Color.rgb(255, 170, 70),
                Color.rgb(110, 170, 255),
                Color.rgb(110, 220, 120),
                Color.rgb(255, 222, 165)
        };

        double x = 14;
        double y = 14;
        double w = 132;
        double h = 12 + labels.length * 18;

        gc.setFill(Color.rgb(10, 12, 18, 0.72));
        gc.fillRoundRect(x, y, w, h, 8, 8);
        gc.setStroke(Color.rgb(80, 90, 110, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, w, h, 8, 8);

        gc.setFont(FONT);
        for (int i = 0; i < labels.length; i++) {
            double rowY = y + 22 + i * 18;
            gc.setFill(colors[i]);
            gc.fillOval(x + 12, rowY - 5, 9, 9);
            gc.setFill(Color.rgb(220, 226, 236));
            gc.fillText(labels[i], x + 30, rowY + 3);
        }
    }

    private void drawMinimap(GraphicsContext gc) {
        int mw = 150;
        int mh = 150;
        double mx = getWidth() - mw - 14;
        double my = getHeight() - mh - 14;

        // Semi-transparent frame.
        gc.setFill(Color.rgb(8, 10, 16, 0.72));
        gc.fillRect(mx, my, mw, mh);
        gc.setStroke(Color.rgb(90, 100, 120, 0.7));
        gc.setLineWidth(1);
        gc.strokeRect(mx, my, mw, mh);

        int gx = environment.getGrid().getWidth();
        int gy = environment.getGrid().getHeight();
        double sx = mw / (double) gx;
        double sy = mh / (double) gy;

        // Nest marker.
        Position nest = environment.getNestPosition();
        gc.setFill(Color.rgb(255, 176, 92));
        gc.fillOval(mx + nest.x() * sx - 3, my + nest.y() * sy - 3, 6, 6);

        // Current camera viewport (so you can tell where you are in the world).
        double wx0 = camera.screenToWorldX(0);
        double wy0 = camera.screenToWorldY(0);
        double wx1 = camera.screenToWorldX(getWidth());
        double wy1 = camera.screenToWorldY(getHeight());
        gc.setStroke(Color.rgb(210, 220, 235, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeRect(mx + wx0 * sx, my + wy0 * sy, (wx1 - wx0) * sx, (wy1 - wy0) * sy);
    }

    private void setupMouseControls() {
        setOnScroll(e -> {
            double factor = e.getDeltaY() < 0 ? 0.9 : 1.1;
            camera.zoomAt(e.getX(), e.getY(), factor);
            e.consume();
        });

        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                camera.pan(e.getX() - lastMouseX, e.getY() - lastMouseY);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int argb(int r, int g, int b) {
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}
