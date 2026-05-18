package it.unibo.antsim.model;

public class SimulationState {
    private long stepCount;
    private int foodCollected;
    private int foodAtNest;
    private int agentCount;

    public SimulationState() {
        this.stepCount = 0;
        this.foodCollected = 0;
        this.foodAtNest = 0;
        this.agentCount = 0;
    }

    /**
     * Getters
     */
    public long getStepCount() {
        return stepCount;
    }

    public int  getFoodCollected() {
        return foodCollected;
    }

    public int getFoodAtNest() {
        return foodAtNest;
    }

    public int getAgentCount() {
        return agentCount;
    }

    /**
     * Setters || mutators
     */
    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public void incrementFoodCollected() {
        this.foodCollected++;
    }

    public void incrementFoodAtNest() {
        this.foodAtNest++;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public void reset() {
        this.stepCount = 0;
        this.foodCollected = 0;
        this.foodAtNest = 0;
    }

    @Override
    public String toString() {
        return "SimulationState{" +
                "step=" + stepCount +
                ", foodCollected=" + foodCollected +
                ", foodAtNest=" + foodAtNest +
                ", agents=" + agentCount +
                '}';
    }
}
