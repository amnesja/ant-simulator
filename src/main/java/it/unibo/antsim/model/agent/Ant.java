package it.unibo.antsim.model.agent;

import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;

import java.util.Random;
import java.util.Comparator;
import java.util.List;

public class Ant {
    private int x;
    private int y;
    private AntState state;
    private final Random random;

    public Ant(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = AntState.SEARCHING_FOOD;
        this.random = new Random();
    }

    public void move(Environment env){
        List<Position> candidates = env.getWalkableNeighborPositions(x, y);

        if(candidates.isEmpty()){
            return;
        }

        Position nextPosition = switch (state){
            case SEARCHING_FOOD -> chooseSearchingMove(env, candidates);
            case RETURNING_TO_NEST -> chooseReturningMove(env, candidates);
        };

        x = nextPosition.x();
        y = nextPosition.y();
    }

    public void pickFood(){
        state = AntState.RETURNING_TO_NEST;
    }

    public void dropFood(){
        state = AntState.SEARCHING_FOOD;
    }

    public boolean isCarryingFood(){
        return state == AntState.RETURNING_TO_NEST;
    }

    public AntState getState(){
        return state;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private Position chooseSearchingMove(Environment environment, List<Position> candidates) {
        List<Position> foodPositions = candidates.stream()
                .filter(position -> environment.isFood(position.x(), position.y()))
                .toList();

        if (!foodPositions.isEmpty()) {
            return randomElement(foodPositions);
        }

        return randomElement(candidates);
    }

    private Position chooseReturningMove(Environment environment, List<Position> candidates) {
        Position nestPosition = environment.getNestPosition();

        return candidates.stream()
                .min(Comparator.comparingInt(position -> position.manhattanDistanceFrom(nestPosition)))
                .orElseGet(() -> randomElement(candidates));
    }

    private Position randomElement(List<Position> positions) {
        return positions.get(random.nextInt(positions.size()));
    }

}
