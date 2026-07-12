package it.unibo.antsim.view;

/**
 * 2D camera that maps world coordinates (in cells) to screen pixels.
 * Supports zoom (toward the cursor) and panning by drag.
 */
public class Camera {
    private double centerX;
    private double centerY;
    private double zoom; // pixels per cell

    private final double worldWidth;
    private final double worldHeight;
    private double viewWidth;
    private double viewHeight;

    public Camera(double worldWidth, double worldHeight, double viewWidth, double viewHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.centerX = worldWidth / 2.0;
        this.centerY = worldHeight / 2.0;
        fit();
    }

    /**
     * Updates the viewport size (in screen pixels) and re-fits the camera so the
     * world keeps filling the available area. Called whenever the view is resized.
     */
    public void setViewSize(double viewWidth, double viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        fit();
    }

    public void fit() {
        // "cover": scale so the world always fills the whole view (no letterbox
        // bands). A giant world is then explored by zooming/panning.
        double fit = Math.max(viewWidth / worldWidth, viewHeight / worldHeight);
        this.zoom = fit;
        this.centerX = worldWidth / 2.0;
        this.centerY = worldHeight / 2.0;
    }

    public double screenToWorldX(double screenX) {
        return (screenX - viewWidth / 2.0) / zoom + centerX;
    }

    public double screenToWorldY(double screenY) {
        return (screenY - viewHeight / 2.0) / zoom + centerY;
    }

    public double worldToScreenX(double worldX) {
        return (worldX - centerX) * zoom + viewWidth / 2.0;
    }

    public double worldToScreenY(double worldY) {
        return (worldY - centerY) * zoom + viewHeight / 2.0;
    }

    public void zoomAt(double screenX, double screenY, double factor) {
        double beforeX = screenToWorldX(screenX);
        double beforeY = screenToWorldY(screenY);

        zoom = clampZoom(zoom * factor);

        double afterX = screenToWorldX(screenX);
        double afterY = screenToWorldY(screenY);

        centerX += beforeX - afterX;
        centerY += beforeY - afterY;
    }

    public void pan(double dxScreen, double dyScreen) {
        centerX -= dxScreen / zoom;
        centerY -= dyScreen / zoom;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getZoom() {
        return zoom;
    }

    private double clampZoom(double z) {
        double min = Math.min(viewWidth / worldWidth, viewHeight / worldHeight) * 0.4;
        double max = 12.0;
        return Math.max(min, Math.min(max, z));
    }
}
