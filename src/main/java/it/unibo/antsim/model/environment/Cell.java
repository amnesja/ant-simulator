package it.unibo.antsim.model.environment;

import it.unibo.antsim.config.SimulationConfig;

/**
 * Class representing a cell in the grid.
 * Each cell has a type (EMPTY, FOOD, OBSTACLE, NEST) and a pheromone level.
 */
public class Cell {
    private CellType type;
    private double pheromoneLevel;
    private double homePheromoneLevel;
    private int foodHP = 0;

    public Cell(CellType type) {
        this.type = type;
        this.pheromoneLevel = 0.0;
        this.homePheromoneLevel = 0.0;
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

    public double getHomePheromoneLevel() {

        return homePheromoneLevel;
    }

    public void addPheromoneLevel(double value){

        this.pheromoneLevel = Math.min(this.pheromoneLevel + value, SimulationConfig.MAX_PHEROMONE_LEVEL);
    }

    public void addHomePheromoneLevel(double value){

        this.homePheromoneLevel = Math.min(
                Math.max(this.homePheromoneLevel, value),
                SimulationConfig.MAX_PHEROMONE_LEVEL
        );
    }

    public void evaporate(double rate){

        this.pheromoneLevel *= rate;
        this.homePheromoneLevel *= rate;
        if(this.pheromoneLevel < 0.0){
            this.pheromoneLevel = 0.0;
        }
        if(this.homePheromoneLevel < 0.0){
            this.homePheromoneLevel = 0.0;
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
