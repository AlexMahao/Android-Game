package com.spearbothy.puzzle;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int SELECT = 1;
    private ImageView mTargetView;
    private PuzzleView mPuzzleView;
    private TextView mHintView;
    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTargetView = (ImageView) findViewById(R.id.target);
        mPuzzleView = (PuzzleView) findViewById(R.id.puzzle);
        findViewById(R.id.select).setOnClickListener(this);
        mHintView = (TextView) findViewById(R.id.hint);
        // 回调监听
        mPuzzleView.setOnPuzzleListener(new PuzzleView.OnPuzzleListener() {
            @Override
            public void onSuccess() {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("我对你的敬仰如涛涛江水般奔腾不息！！！")
                        .setPositiveButton("关闭", null)
                        .setCancelable(false)
                        .show();
            }

            @Override
            protected void onMove() {
                mCount++;
                SpannableString spannableString = new SpannableString("你已经费了九牛二虎之" + mCount + "次");
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), 10, 10 + String.valueOf(mCount).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mHintView.setText(spannableString);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select:
                selectImageFromSystem();
                break;
            default:
                break;
        }
    }

    private void selectImageFromSystem() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); //照片类型
        startActivityForResult(intent, SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT) {
            Uri localUri = data.getData();
            String scheme = localUri.getScheme();
            String imagePath = "";
            if ("content".equals(scheme)) {
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(localUri, filePathColumns, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                imagePath = c.getString(columnIndex);
                c.close();
            } else if ("file".equals(scheme)) {//小米4选择云相册中的图片是根据此方法获得路径
                imagePath = localUri.getPath();
            }
            Bitmap sourceBitmap = BitmapFactory.decodeFile(imagePath);
            mPuzzleView.setBitmap(sourceBitmap);
            mTargetView.setImageBitmap(mPuzzleView.getBitmap());
            mCount = 0;
        }
    }

}
