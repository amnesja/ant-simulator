package it.unibo.antsim.model;

/**
 * Class representing a cell in the grid.
 * Each cell has a type (EMPTY, FOOD, OBSTACLE, NEST) and a pheromone level.
 */
public class Cell {
    private CellType type;
    private double pheromoneLevel;

    public Cell(CellType type) {
        this.type = type;
        this.pheromoneLevel = 0.0;
    }

    public CellType getType(){

        return type;
    }

    public void setType(CellType type){

        this.type = type;
    }

    public double getPheromoneLevel() {

        return pheromoneLevel;
    }

    public void addPheromoneLevel(double value){

        this.pheromoneLevel += value;
    }

    public void evaporate(double rate){

        this.pheromoneLevel -= rate;
    }

    public boolean hasFood(){
        return type == CellType.FOOD;
    }

    public boolean isObstacle(){
        return type == CellType.OBSTACLE;
    }

    public boolean isNest(){
        return type == CellType.NEST;
    }
}
