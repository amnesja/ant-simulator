package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;

import java.util.Random;

public class FakeAgents {
    private int x;
    private int y;
    private boolean carryingFood = false;
    private final Random random = new Random();

    public FakeAgents(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(Environment env){
        int[] dx = {-1,0,1,0};
        int[] dy = {0,-1,0,1};

        int dir = random.nextInt(4);

        int newx = x + dx[dir];
        int newy = y + dy[dir];

        if(env.getGrid().isInside(newx,newy)){
            x = newx;
            y = newy;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isCarryingFood() {
        return carryingFood;
    }

    public void pickFood(){
        carryingFood = true;
    }
    public void dropFood(){
        carryingFood = false;
    }
}
