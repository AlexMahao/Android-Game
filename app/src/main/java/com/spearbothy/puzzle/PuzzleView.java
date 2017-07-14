package com.spearbothy.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.TreeMap;

/**
 * Created by mahao on 17-7-13.
 */

public class PuzzleView extends View {
    private int mHeight;
    private int mWidth;
    private Bitmap mSourceBitmap;
    private Paint mPaint;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private int mLine = 3;
    private int mColumn = 3;
    private TreeMap<Integer, PuzzleBlock> mBlocks = new TreeMap<>();
    private int mBlockHeight;
    private int mBlockWidth;
    private PuzzleBlock mBlankBlock;
    private int mOffsetX;
    private int mOffsetY;
    private OnPuzzleListener mListener;

    public PuzzleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            mWidth = getScreenWidth();
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            mHeight = mWidth;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }

    public int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        mSourceBitmap = bitmap;
        reset();
    }

    /**
     * 重设图片
     */
    private void reset() {
        mBitmapWidth = mSourceBitmap.getWidth();
        mBitmapHeight = mSourceBitmap.getHeight();
        mBlockHeight = mBitmapHeight / mLine;
        mBlockWidth = mBitmapWidth / mColumn;
        mOffsetX = (mWidth - mBitmapWidth) / 2;
        mOffsetY = (mHeight - mBitmapHeight) / 2;
        resetBlocks();
        randomBlocks();
        invalidate();
    }

    private void resetBlocks() {
        mBlocks.clear();
        for (int l = 0; l < mLine; l++) {
            for (int c = 0; c < mColumn; c++) {
                Bitmap bitmap = Bitmap.createBitmap(mSourceBitmap, c * mBlockWidth, l * mBlockHeight, mBlockWidth, mBlockHeight);
                int index = l * mColumn + c;
                PuzzleBlock block = new PuzzleBlock(index, index, bitmap);
                mBlocks.put(index, block);
                if (l * mColumn + c == 8) {
                    mBlankBlock = block;
                    mBlankBlock.setBlank(true);
                }
            }
        }
    }

    public void randomBlocks() {
        for (int i = 0; i < 40; ) {
            int random = (int) (Math.random() * (mColumn * mLine));
            if (isSwap(mBlankBlock.getMoveIndex(), random)) {
                i++;
                swapBlock(mBlankBlock.getMoveIndex(), random);
            }
        }
    }

    private void swapBlock(int source, int target) {
        PuzzleBlock block = mBlocks.get(source);
        PuzzleBlock targetBlock = mBlocks.get(target);
        swapBlock(block, targetBlock);
    }

    private void swapBlock(PuzzleBlock source, PuzzleBlock target) {
        int temp = source.getMoveIndex();
        source.setMoveIndex(target.getMoveIndex());
        target.setMoveIndex(temp);
        // 更新数据
        mBlocks.put(source.getMoveIndex(), source);
        mBlocks.put(target.getMoveIndex(), target);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSourceBitmap != null) {
            canvas.save();
            canvas.translate(mOffsetX, mOffsetY);
            drawBitmap(canvas);
            canvas.restore();
        }
    }

    private void drawBitmap(Canvas canvas) {
        for (PuzzleBlock block : mBlocks.values()) {
            if (block.isBlank()) {
                continue;
            }
            int line = block.getMoveIndex() / mColumn;
            int column = block.getMoveIndex() % mColumn;
            Log.i("info", "line:" + line + "column" + column);
            canvas.drawBitmap(block.getBitmap(), column * mBlockWidth, line * mBlockHeight, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float x = event.getX();
                float y = event.getY();
                PuzzleBlock block = calculateBlock(x, y);
                if (block != null) {
                    swapBlock(mBlankBlock.getMoveIndex(), block.getMoveIndex());
                    if (mListener != null) {
                        mListener.onMove();
                        if (isSuccess()) {
                            mListener.onSuccess();
                        }
                    }
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    public boolean isSuccess() {
        for (PuzzleBlock block : mBlocks.values()) {
            if (!block.isSuccess()) {
                return false;
            }
        }
        return true;
    }

    private PuzzleBlock calculateBlock(float x, float y) {
        if (x < mOffsetX || x > mBitmapWidth + mOffsetX) {
            return null;
        }
        if (y < mOffsetY || y > mBitmapHeight + mOffsetY) {
            return null;
        }
        x -= mOffsetX;
        y -= mOffsetY;
        int column = (int) (x / mBlockWidth);
        int line = (int) (y / mBlockHeight);
        int index = line * mColumn + column;
        if (isSwap(mBlankBlock.getMoveIndex(), index)) {
            return mBlocks.get(line * mColumn + column);
        } else {
            return null;
        }
    }

    public boolean isSwap(int source, int target) {
        int sourceLine = source / mColumn;
        int targetLine = target / mColumn;
        if (sourceLine == targetLine) {
            if (Math.abs(source - target) == 1) {
                return true;
            } else {
                return false;
            }
        } else {
            if (Math.abs(source - target) == mColumn) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setOnPuzzleListener(OnPuzzleListener listener) {
        mListener = listener;
    }


    public abstract static class OnPuzzleListener {
        public abstract void onSuccess();
        protected void onMove() {}
    }
}
