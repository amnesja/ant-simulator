package it.unibo.antsim.model.environment;

/**
 * Class representing a cell in the grid.
 * Each cell has a type (EMPTY, FOOD, OBSTACLE, NEST) and a pheromone level.
 */
public class Cell {
    private CellType type;
    private double pheromoneLevel;
    private int foodHP = 0;

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

        this.pheromoneLevel *= rate;
        if(this.pheromoneLevel < 0.0){
            this.pheromoneLevel = 0.0;
        }
    }

    public boolean hasFood(){
        return type == CellType.FOOD;
    }

    public int getFoodHP(){
        return foodHP;
    }

    public void setFoodHP(int foodHP){
        this.foodHP = Math.max(0, foodHP);
        if(this.foodHP == 0){
            setType(CellType.EMPTY);
        }
    }

    public void consumeFood(int amount){
        foodHP -= amount;
        if(foodHP <= 0){
            foodHP = 0;
            setType(CellType.EMPTY);
        }
    }

    public boolean isObstacle(){
        return type == CellType.OBSTACLE;
    }

    public boolean isNest(){
        return type == CellType.NEST;
    }
}
