package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;

import java.util.Random;
import java.util.Comparator;
import java.util.List;

public class Ant {
    private static int nextId = 1;

    private final int id;
    private int x;
    private int y;
    private Position lastPosition;
    private AntState state;
    private final Random random;

    public Ant(int x, int y) {
        this.id = nextId++;
        this.x = x;
        this.y = y;
        this.lastPosition = null;
        this.state = AntState.SEARCHING_FOOD;
        this.random = new Random();
    }

    public void move(Environment env){
        log("start state=%s position=(%d,%d) last=%s", state, x, y, formatPosition(lastPosition));

        if (state == AntState.RETURNING_TO_NEST) {
            env.addPheromone(x, y, SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT);
            log("deposit food-pheromone at (%d,%d) level=%.2f", x, y, env.getCell(x, y).getPheromoneLevel());
        } else {
            double homePheromone = calculateHomePheromone(env);
            env.addHomePheromone(x, y, homePheromone);
            log("deposit home-pheromone at (%d,%d) amount=%.2f level=%.2f",
                    x, y, homePheromone, env.getCell(x, y).getHomePheromoneLevel());
        }

        List<Position> candidates = env.getWalkableNeighborPositions(x, y);
        log("walkable candidates=%s", candidates);

        if(candidates.isEmpty()){
            log("blocked: no walkable candidates");
            return;
        }

        Position nextPosition = switch (state){
            case SEARCHING_FOOD -> chooseSearchingMove(env, candidates);
            case RETURNING_TO_NEST -> chooseReturningMove(env, candidates);
        };

        lastPosition = new Position(x, y);
        x = nextPosition.x();
        y = nextPosition.y();
        log("move to (%d,%d)", x, y);
    }

    public void pickFood(){
        log("pick food at (%d,%d): SEARCHING_FOOD -> RETURNING_TO_NEST", x, y);
        state = AntState.RETURNING_TO_NEST;
    }

    public void dropFood(){
        log("drop food at nest (%d,%d): RETURNING_TO_NEST -> SEARCHING_FOOD", x, y);
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

    public int getId() {
        return id;
    }

    private Position chooseSearchingMove(Environment environment, List<Position> candidates) {
        List<Position> foodPositions = candidates.stream()
                .filter(position -> environment.isFood(position.x(), position.y()))
                .toList();

        if (!foodPositions.isEmpty()) {
            Position chosen = randomElement(foodPositions);
            log("adjacent food found candidates=%s chosen=%s", foodPositions, chosen);
            return chosen;
        }

        return chooseWeightedMove(environment, candidates);
    }

    private Position chooseWeightedMove(Environment environment, List<Position> candidates) {
        double totalWeight = 0;
        double[] weights = new double[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {
            Position pos = candidates.get(i);
            double pheromone = environment.getCell(pos.x(), pos.y()).getPheromoneLevel();
            double weight = pheromone + SimulationConfig.ACO_EPSILON;
            String reason = "base";
            
            if (lastPosition != null && pos.equals(lastPosition)) {
                weight *= 0.1; // Penalty to avoid immediate backtracking
                reason = "backtrack-penalty";
            }
            
            weights[i] = weight;
            totalWeight += weight;
            log("search weight pos=%s foodPheromone=%.2f weight=%.3f reason=%s",
                    pos, pheromone, weight, reason);
        }

        double r = random.nextDouble() * totalWeight;
        log("search roulette totalWeight=%.3f roll=%.3f", totalWeight, r);
        double cumulativeWeight = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulativeWeight += weights[i];
            if (r <= cumulativeWeight) {
                log("search chosen=%s cumulative=%.3f", candidates.get(i), cumulativeWeight);
                return candidates.get(i);
            }
        }

        log("search fallback chosen=%s", candidates.get(0));
        return candidates.get(0); // Fallback
    }

    private Position chooseReturningMove(Environment environment, List<Position> candidates) {
        Position nestPosition = environment.getNestPosition();

        for (Position position : candidates) {
            double homePheromone = environment.getCell(position.x(), position.y()).getHomePheromoneLevel();
            int distance = position.manhattanDistanceFrom(nestPosition);
            boolean isBacktrack = lastPosition != null && position.equals(lastPosition);
            double score = returnScore(position, environment, nestPosition);
            log("return candidate pos=%s homePheromone=%.2f distToNest=%d backtrack=%s score=%.2f",
                    position, homePheromone, distance, isBacktrack, score);
        }

        Position chosen = candidates.stream()
                .max(Comparator.comparingDouble(position -> returnScore(position, environment, nestPosition)))
                .orElseGet(() -> randomElement(candidates));
        log("return chosen=%s", chosen);
        return chosen;
    }

    private double calculateHomePheromone(Environment environment) {
        int distanceFromNest = new Position(x, y).manhattanDistanceFrom(environment.getNestPosition());
        return SimulationConfig.MAX_PHEROMONE_LEVEL / (distanceFromNest + 1.0);
    }

    private double returnScore(Position position, Environment environment, Position nestPosition) {
        double homePheromone = environment.getCell(position.x(), position.y()).getHomePheromoneLevel();
        int distance = position.manhattanDistanceFrom(nestPosition);
        double backtrackPenalty = lastPosition != null && position.equals(lastPosition)
                ? SimulationConfig.RETURN_BACKTRACK_PENALTY
                : 0.0;

        return homePheromone * SimulationConfig.RETURN_HOME_PHEROMONE_WEIGHT
                - distance * SimulationConfig.RETURN_DISTANCE_WEIGHT
                - backtrackPenalty;
    }

    private Position randomElement(List<Position> positions) {
        return positions.get(random.nextInt(positions.size()));
    }

    private void log(String format, Object... args) {
        if (SimulationConfig.ENABLE_CLI_LOGS) {
            System.out.printf("[ANT %02d] %s%n", id, String.format(format, args));
        }
    }

    private String formatPosition(Position position) {
        return position == null ? "none" : position.toString();
    }

}
