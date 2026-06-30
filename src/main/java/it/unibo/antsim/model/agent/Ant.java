package it.unibo.antsim.model.agent;

import it.unibo.antsim.model.environment.Environment;

import java.util.Random;

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
        int[] dx = {-1,0,1,0};
        int[] dy = {0,-1,0,1};

        int dir = random.nextInt(4);

        int newX = x + dx[dir];
        int newY = y + dy[dir];

        if(env.getGrid().isInside(newX,newY)) {
            if(!env.getCell(newX,newY).isObstacle()){
                x = newX;
                y = newY;
            }
        }
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

}
