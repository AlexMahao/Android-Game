package com.spearbothy.puzzle.dianchengkuai;

/**
 * Created by mahao on 17-7-25.
 */

public class Point {

    static final int DEFAULT_HEIGHT = 100;
    static final int DEFAULT_WIDTH = 100;


    int x;

    int y;

    int targetX;

    int targetY;

    int speed;

    int drawX;

    int drawY;

    int height = DEFAULT_HEIGHT;

    int width = DEFAULT_WIDTH;

    @Override
    public String toString() {
        return "Point{" +
                ", x=" + x +
                ", y=" + y +
                ", targetX=" + targetX +
                ", targetY=" + targetY +
                ", speed=" + speed +
                ", drawX=" + drawX +
                ", drawY=" + drawY +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
