package com.spearbothy.puzzle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.TreeMap;

/**
 * Created by mahao on 17-7-13.
 */

public class PuzzleView extends View {
    private static final int DEFAULT_COLUMN = 3;
    private static final int DEFAULT_LINE = 3;

    private int mHeight;
    private int mWidth;
    private Bitmap mSourceBitmap;
    private Paint mPaint;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private int mLine;
    private int mColumn;
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
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PuzzleView);
        mColumn = ta.getInt(R.styleable.PuzzleView_puzzleColumn, DEFAULT_COLUMN);
        mLine = ta.getInt(R.styleable.PuzzleView_puzzleLine, DEFAULT_LINE);
        ta.recycle();
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

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        mSourceBitmap = resizeBitmap(bitmap);
        reset();
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale;
        if (width > height) {
            float targetWidth = getMeasuredWidth();
            scale = targetWidth / width;
        } else {
            float targetHeight = getMeasuredHeight();
            scale = targetHeight / height;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBitMap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        bitmap.recycle();
        return newBitMap;
    }

    public Bitmap getBitmap() {
        return mSourceBitmap;
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

    private void randomBlocks() {
        for (int i = 0; i < mColumn * mLine * 4; ) {
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
                    invalidate();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isSuccess() {
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

    private boolean isSwap(int source, int target) {
        int sourceLine = source / mColumn;
        int targetLine = target / mColumn;
        if (sourceLine == targetLine) {
            return Math.abs(source - target) == 1;
        } else {
            return Math.abs(source - target) == mColumn;
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
