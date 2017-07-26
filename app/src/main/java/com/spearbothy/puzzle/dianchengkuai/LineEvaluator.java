package com.spearbothy.puzzle.dianchengkuai;

/**
 * Created by mahao on 17-7-26.
 */

public class LineEvaluator implements Evaluator {

    @Override
    public void evaluate(Point point) {
        point.drawY = point.drawY + point.speed;
        point.drawX = (int) (point.drawX + point.speed * 1.0f / (point.targetY - point.y) * (point.targetX - point.x));
    }
}
