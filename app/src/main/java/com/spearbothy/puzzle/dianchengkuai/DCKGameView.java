package com.spearbothy.puzzle.dianchengkuai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.spearbothy.puzzle.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mahao on 17-7-25.
 */

public class DCKGameView extends View implements TimeControl.TimeListener {

    private List<Point> points = new ArrayList<>();
    private List<Explode> explodes = new ArrayList<>();
    private int mHeight;
    private int mWidth;
    private Paint mPaint;
    private Paint mExplodePaint;
    private int mLevel = 1;
    private TimeControl mTimeControl;
    private Evaluator mEvaluator;
    private GameListener listener;
    private boolean isPause = false;
    private Bitmap mUnhitBitmap, mHitBitmap;

    public DCKGameView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DCKGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTimeControl = TimeControl.getInstance();
        mTimeControl.setTimeListener(this);
        mEvaluator = new LineEvaluator();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.CYAN);
        mPaint.setStyle(Paint.Style.FILL);
        mExplodePaint = new Paint();
        mExplodePaint.setAntiAlias(true);
        mExplodePaint.setColor(Color.GRAY);
        mExplodePaint.setStyle(Paint.Style.FILL);

        mUnhitBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.hit);
        mUnhitBitmap = resizeBitmap(mUnhitBitmap, Point.DEFAULT_WIDTH, Point.DEFAULT_WIDTH);
        mHitBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.unhit);
        mHitBitmap = resizeBitmap(mHitBitmap, Explode.DEFAULT_WIDTH, Explode.DEFAULT_WIDTH);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, float targetWidth, float targetHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = targetWidth / width;
        float scaleHeight = targetHeight / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitMap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        bitmap.recycle();
        return newBitMap;
    }

    public void setUnhitBitmap(Bitmap bitmap) {
        mUnhitBitmap = resizeBitmap(bitmap, Point.DEFAULT_WIDTH, Point.DEFAULT_WIDTH);
    }

    public void setHitBitmap(Bitmap bitmap) {
        mUnhitBitmap = resizeBitmap(bitmap, Point.DEFAULT_WIDTH, Point.DEFAULT_WIDTH);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }


    public void startGame() {
        isPause = false;
        mTimeControl.start();
    }

    public void restartGame() {
        isPause = false;
        mLevel = 0;
        explodes.clear();
        points.clear();
        mTimeControl.restart();
        onLevelUpdate();
    }

    public void pauseGame() {
        isPause = true;
        mTimeControl.pause();
    }

    public void endGame() {
        mTimeControl.end();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPoints(canvas);
        drawExplodes(canvas);
    }

    private void drawExplodes(Canvas canvas) {
        Iterator<Explode> iterator = explodes.iterator();
        while (iterator.hasNext()) {
            Explode explode = iterator.next();
            explode.y += explode.speed;
            if (explode.y > mHeight) {
                iterator.remove();
                continue;
            }
            canvas.drawBitmap(mHitBitmap, explode.x, explode.y, mExplodePaint);
//            canvas.drawRect(explode.x, explode.y, explode.x + explode.width, explode.y + explode.height, mExplodePaint);
        }
    }

    private void drawPoints(Canvas canvas) {
        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            mEvaluator.evaluate(point);
            if (point.drawY > mHeight || point.drawX > mWidth || point.drawX < -point.width) {
                iterator.remove();
                if (listener != null) {
                    listener.onUnHit();
                }
                continue;
            }
            canvas.drawBitmap(mUnhitBitmap, point.drawX, point.drawY, mPaint);
//            canvas.drawRect(point.drawX, point.drawY, point.drawX + point.width, point.drawY + point.height, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPause) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point hit = isHit((int) event.getX(), (int) event.getY());
            if (hit != null) {
                Explode explode = new Explode();
                explode.x = hit.drawX;
                explode.y = hit.drawY;
                explodes.add(explode);
                if (listener != null) {
                    listener.onHit();
                }
            }
        }
        return true;
    }

    public Point isHit(int x, int y) {
        for (int i = points.size() - 1; i >= 0; i--) {
            Point point = points.get(i);
            if ((x > point.drawX) && x < (point.drawX + point.width) && y > point.drawY && y < (point.drawY + point.height)) {
                points.remove(point);
                return point;
            }
        }
        return null;
    }

    @Override
    public void onRefresh() {
        // 在此处加
        invalidate();
    }

    @Override
    public void onLevelUpdate() {
        mLevel++;
        listener.onLevelUpdate(mLevel);
    }

    @Override
    public void onPointAdd() {
        if (random(0, 10 + mLevel) < mLevel) {
            addPoint();
        }
    }

    private void addPoint() {
        Point point = new Point();
        point.y = -point.height;
        point.x = random(0, mWidth - point.width);
        point.targetX = random(0, mWidth - point.width);
        point.targetY = mHeight;
        point.speed = random(mLevel + 3, mLevel * 6);
        if (point.speed > 15) {
            point.speed = 15;
        }
        point.speed = 5;
        point.drawX = point.x;
        point.drawY = point.y;
        points.add(point);
    }

    private int random(int min, int max) {
        return min + (int) (Math.random() * max);
    }


    public interface GameListener {
        void onLevelUpdate(int level);

        void onHit();

        void onUnHit();
    }
}
