package it.unibo.antsim.model;

/**
 * Class representing the grid of the environment.
 * The grid is a 2D array of cells, where each cell can be empty, contain food, be an obstacle, or be the nest.
 * The grid provides methods to access and modify the cells, as well as to get the dimensions of the grid.
 */
public class Grid {
    private final int width;
    private final int height;
    private final Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        initializeCells();
    }

    private void initializeCells() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(CellType.EMPTY);
            }
        }
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
