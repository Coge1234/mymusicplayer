package com.example.mymusicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.model.MusicData;
import com.example.mymusicplayer.service.MusicService;
import com.example.mymusicplayer.utils.DisplayUtil;
import com.example.mymusicplayer.utils.FastBlurUtil;
import com.example.mymusicplayer.widget.BackgourndAnimationRelativeLayout;
import com.example.mymusicplayer.widget.DiscView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.mymusicplayer.widget.DiscView.DURATION_NEEDLE_ANIMATOR;

public class MainActivity extends AppCompatActivity implements DiscView.IPlayInfo, View.OnClickListener {

    private DiscView mDisc;
    private Toolbar mToolbar;
    private SeekBar mSeekBar;
    private ImageView mIvPlayOrPause, mIvNext, mIvLast;
    private TextView mTvMusicDuration, mTvTotalMusicDuration;
    private BackgourndAnimationRelativeLayout mRootLayout;
    public static final int MUSIC_MESSAGE = 0;

    public static final String PARAM_MUSIC_LIST = "PARAM_MUSIC_LIST";

    private Handler mMusicHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSeekBar.setProgress(mSeekBar.getProgress() + 1000);
            mTvMusicDuration.setText(durationToTime(mSeekBar.getProgress()));
            startUpdateSeekBarProgress();
        }
    };

    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private List<MusicData> mMusicDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMusicDatas();
        initView();
        initMusicReceiver();
        makeStatusBarTransparent();
    }

    private void initMusicReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        //注册本地广播
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, intentFilter);
    }

    private void initView() {
        mDisc = (DiscView) findViewById(R.id.discview);
        mIvNext = (ImageView) findViewById(R.id.ivNext);
        mIvLast = (ImageView) findViewById(R.id.ivLast);
        mIvPlayOrPause = (ImageView) findViewById(R.id.ivPlayOrPause);
        mTvMusicDuration = (TextView) findViewById(R.id.tvCurrentTime);
        mTvTotalMusicDuration = (TextView) findViewById(R.id.tvTotalTime);
        mSeekBar = (SeekBar) findViewById(R.id.musicSeekBar);
        mRootLayout = (BackgourndAnimationRelativeLayout) findViewById(R.id.rootlayout);
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);

        mDisc.setPlayInfoListener(this);
        mIvLast.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPlayOrPause.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvMusicDuration.setText(durationToTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateSeekBarProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
                startUpdateSeekBarProgress();
            }
        });
        mTvMusicDuration.setText(durationToTime(0));
        mTvTotalMusicDuration.setText(durationToTime(0));
        mDisc.setMusicDataList(mMusicDatas);
    }

    private void stopUpdateSeekBarProgress() {
        mMusicHandler.removeMessages(MUSIC_MESSAGE);
    }

    //设置透明状态栏
    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initMusicDatas() {
        MusicData musicData1 = new MusicData(R.raw.music1, R.raw.ic_music1, "Ink", "ColdPlay");
        MusicData musicData2 = new MusicData(R.raw.music2, R.raw.ic_music2, "With Or Without You", "2CELLOS");
        MusicData musicData3 = new MusicData(R.raw.music3, R.raw.ic_music3, "Not A Bad Thing", "Justin Timberlake");

        mMusicDatas.add(musicData1);
        mMusicDatas.add(musicData2);
        mMusicDatas.add(musicData3);

        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(PARAM_MUSIC_LIST, (Serializable) mMusicDatas);
        startService(intent);
    }

    private void tryToUpdateMusicPicBackground(final int musicPicRes) {
        if (mRootLayout.isNeedToUpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRootLayout.setForeground(foregroundDrawable);
                            mRootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        //得到屏幕的宽高比，以便按比例切割图片一部分
        final float widthHeightSize = (float) (DisplayUtil.getScreenWidth(this) * 1.0 / DisplayUtil.getScreenHeight(this) * 1.0);

        Bitmap bitmap = getForegroundBitmap(musicPicRes);
        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);
        //切割部分图片
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth, bitmap.getHeight());
        //缩小图片
        Bitmap scalebitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50, bitmap.getHeight() / 50, false);
        // 模糊化
        final Bitmap blurBitmap = FastBlurUtil.doBlur(scalebitmap, 8, true);

        final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
        //加入灰色遮罩层，避免图片过亮影响其他控件
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    private Bitmap getForegroundBitmap(int musicPicRes) {
        int screenWidth = DisplayUtil.getScreenWidth(this);
        int screenHeight = DisplayUtil.getScreenHeight(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        //表示只读图片，不加载到内存中，设置这个参数为ture，就不会给图片分配内存空间，
        //但是可以获取到图片的大小等属性; 设置为false, 就是要加载这个图片
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(getResources(), musicPicRes);
        }

        int sample = 2;
        int sampleX = imageWidth / DisplayUtil.getScreenWidth(this);
        int sampleY = imageHeight / DisplayUtil.getScreenHeight(this);

        if (sampleX > sampleY && sampleY > 1) {
            sample = sampleX;
        } else if (sampleY > sampleX && sampleX > 1) {
            sample = sampleY;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeResource(getResources(), musicPicRes, options);
    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicSinger) {
        getSupportActionBar().setTitle(musicName);
        getSupportActionBar().setSubtitle(musicSinger);
    }

    @Override
    public void onMusicPicChanged(int musicPicRes) {
        tryToUpdateMusicPicBackground(musicPicRes);
    }

    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
        switch (musicChangedStatus) {
            case PLAY: {
                play();
                break;
            }
            case PAUSE: {
                pause();
                break;
            }
            case NEXT: {
                next();
                break;
            }
            case LAST: {
                last();
                break;
            }
            case STOP: {
                stop();
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mIvPlayOrPause) {
            mDisc.PlayOrPause();
        } else if (v == mIvNext) {
            mDisc.next();
        } else if (v == mIvLast) {
            mDisc.last();
        }
    }

    private void play() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
        startUpdateSeekBarProgress();
    }

    private void pause() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
        stopUpdateSeekBarProgress();
    }

    private void stop() {
        stopUpdateSeekBarProgress();
        mIvPlayOrPause.setImageResource(R.drawable.ic_play);
        mTvMusicDuration.setText(durationToTime(0));
        mTvTotalMusicDuration.setText(durationToTime(0));
        mSeekBar.setProgress(0);
    }

    private void next() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        }, DURATION_NEEDLE_ANIMATOR);
        stopUpdateSeekBarProgress();
        mTvMusicDuration.setText(durationToTime(0));
        mTvTotalMusicDuration.setText(durationToTime(0));
    }

    private void last() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
            }
        }, DURATION_NEEDLE_ANIMATOR);
        stopUpdateSeekBarProgress();
        mTvMusicDuration.setText(durationToTime(0));
        mTvTotalMusicDuration.setText(durationToTime(0));
    }

    private void complete(boolean isOver) {
        if (isOver) {
            mDisc.stop();
        } else {
            mDisc.next();
        }
    }

    private void optMusic(final String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO, position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startUpdateSeekBarProgress() {
        //避免重复发送Message
        stopUpdateSeekBarProgress();
        mMusicHandler.sendEmptyMessageDelayed(0, 1000);
    }

    //根据时长格式化称时间文本
    private String durationToTime(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        return (min < 10 ? "0" + min : min + "") + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateMusicDurationinfo(int totalDuration) {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(totalDuration);
        mTvTotalMusicDuration.setText(durationToTime(totalDuration));
        mTvMusicDuration.setText(durationToTime(0));
        startUpdateSeekBarProgress();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_pause);
                int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                mSeekBar.setProgress(currentPosition);
                if (!mDisc.isPlaying()) {
                    mDisc.PlayOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_play);
                if (mDisc.isPlaying()) {
                    mDisc.PlayOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_DURATION)) {
                int duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
                updateMusicDurationinfo(duration);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_COMPLETE)) {
                boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
                complete(isOver);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
    }
}
