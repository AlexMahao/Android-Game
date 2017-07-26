package com.spearbothy.puzzle.dianchengkuai.hide;

import android.app.Service;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spearbothy.puzzle.R;
import com.spearbothy.puzzle.dianchengkuai.DCKGameView;

/**
 * Created by mahao on 17-7-25.
 */

public class GameActivity extends AppCompatActivity implements DCKGameView.GameListener {

    private DCKGameView mGameView;
    private TextView mLevelTextView;
    private TextView mHitTextView;
    private int hit = 0;
    private int unhit = 0;
    private int level = 0;
    private static final int maxUnhit = 10;
    private View goBtn;
    private static final int STATUS_START = 0;
    private static final int STATUS_PAUSE = 1;
    private static final int STATUS_OVER = 2;

    private int status = STATUS_OVER;
    private TextView pauseBtn;
    private LinearLayout mPeopleLayout;

    private Vibrator vibrator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dck_demo_game);
        mLevelTextView = (TextView) findViewById(R.id.level);
        mHitTextView = (TextView) findViewById(R.id.hit);
        mGameView = (DCKGameView) findViewById(R.id.game);
        mGameView.setGameListener(this);
        mGameView.setUnhitBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.particles));
        mPeopleLayout = (LinearLayout) findViewById(R.id.people);
        goBtn = findViewById(R.id.go);
        pauseBtn = (TextView) findViewById(R.id.pause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == STATUS_START) {
                    status = STATUS_PAUSE;
                    mGameView.pauseGame();
                } else if (status == STATUS_PAUSE) {
                    status = STATUS_START;
                    mGameView.startGame();
                }
                refreshView();
            }
        });
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hit = 0;
                level = 0;
                unhit = 0;
                status = STATUS_START;
                mGameView.restartGame();
                mPeopleLayout.removeAllViews();
                ImageView imageView = new ImageView(GameActivity.this);
                imageView.setImageResource(R.mipmap.people);
                mPeopleLayout.addView(imageView, 0);
                refreshView();
            }
        });

        refreshView();
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
    }

    public void refreshView() {
        SpannableString spannableString = new SpannableString("hit:" + hit);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), 4, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mHitTextView.setText(spannableString);
        spannableString = new SpannableString("level:" + level);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), 6, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mLevelTextView.setText(spannableString);

        switch (status) {
            case STATUS_START:
                goBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
                pauseBtn.setText("pause");
                break;
            case STATUS_PAUSE:
                goBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
                pauseBtn.setText("start");
                break;
            case STATUS_OVER:
                goBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
                pauseBtn.setText("pause");
                break;
        }
    }

    @Override
    public void onLevelUpdate(int level) {
        this.level = level;
        refreshView();
    }

    @Override
    public void onHit() {
        hit++;
        refreshView();
    }

    @Override
    public void onUnHit() {
        unhit++;
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.particles);
        mPeopleLayout.addView(imageView, 0);
        vibrator.vibrate(500);
        if (unhit >= maxUnhit) {
            status = STATUS_OVER;
            showGameOver();
            mGameView.endGame();
            refreshView();
        }
    }

    public void showGameOver() {
        new AlertDialog.Builder(this)
                .setMessage("当然是选择原谅她")
                .setPositiveButton("原谅", null)
                .show();
    }
}
