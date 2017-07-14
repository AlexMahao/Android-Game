package com.spearbothy.puzzle;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Created by mahao on 17-7-13.
 */

public class PuzzleBlock implements Comparable<Integer> {

    // 当前图片的索引
    private int index;
    // 图片移动的索引
    private int moveIndex;

    private Bitmap bitmap;

    private boolean isBlank;


    public boolean isSuccess() {
        return index == moveIndex;
    }

    public void setBlank(boolean blank) {
        isBlank = blank;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public PuzzleBlock(int index, int moveIndex, Bitmap bitmap) {
        this.index = index;
        this.moveIndex = moveIndex;
        this.bitmap = bitmap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public void setMoveIndex(int moveIndex) {
        this.moveIndex = moveIndex;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int compareTo(@NonNull Integer o) {
        return moveIndex > o ? 1 : -1;
    }
}
