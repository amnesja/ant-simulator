package it.unibo.antsim.model.environment;

public record Position(int x, int y){
    public int manhattanDistanceFrom(Position other){
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
}

