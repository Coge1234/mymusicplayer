package com.example.mymusicplayer.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.example.mymusicplayer.R;

/**
 * Created by Administrator on 2017/3/1.
 */

public class BackgourndAnimationRelativeLayout extends RelativeLayout {
    private final int DURATION_ANIMATION = 500;
    private final int INDEX_BACKGROUNG = 0;
    private final int INDEX_FOREGROUNG = 1;

    private LayerDrawable mLayerDrawable;
    private ObjectAnimator mObjectAnimator;
    private int musicPicRes = -1;
    private Drawable backgroundDrawable, tempDrawable;

    public BackgourndAnimationRelativeLayout(Context context) {
        this(context, null);
    }

    public BackgourndAnimationRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackgourndAnimationRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayerDrawable();
        initObjectAnimator();
    }

    private void initLayerDrawable() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            backgroundDrawable = getContext().getDrawable(R.drawable.ic_blackground);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            backgroundDrawable = getContext().getResources().getDrawable(R.drawable.ic_blackground);
        }
//        Drawable[] drawables = new Drawable[2];
//        //初始化时先将前景与背景颜色设为一致
//        drawables[INDEX_BACKGROUNG] = backgroundDrawable;
//        drawables[INDEX_FOREGROUNG] = backgroundDrawable;
//
//        mLayerDrawable = new LayerDrawable(drawables);
        mLayerDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(),R.drawable.layer_drawable);

    }

    private void initObjectAnimator() {
        mObjectAnimator = ObjectAnimator.ofFloat(this, "number", 0f, 1.0f);
        mObjectAnimator.setDuration(DURATION_ANIMATION);
        mObjectAnimator.setInterpolator(new AccelerateInterpolator());
        mObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int foregroundAlpha = (int) ((float) animation.getAnimatedValue() * 255);
                //动态设置Drawable的透明度，让前景图逐渐显示
                mLayerDrawable.getDrawable(INDEX_FOREGROUNG).setAlpha(foregroundAlpha);
                BackgourndAnimationRelativeLayout.this.setBackground(mLayerDrawable);
            }
        });
        mObjectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //动画结束后，记得将原来的背景图及时更新
//                mLayerDrawable.setDrawableByLayerId(INDEX_BACKGROUNG, mLayerDrawable.getDrawable(INDEX_FOREGROUNG));
                mLayerDrawable.setDrawableByLayerId(R.id.layerdrawableback,mLayerDrawable.getDrawable(INDEX_FOREGROUNG));
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    mLayerDrawable.setDrawable(INDEX_BACKGROUNG, mLayerDrawable.getDrawable(
//                            INDEX_FOREGROUNG));
//                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void setForeground(Drawable drawable) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mLayerDrawable.setDrawable(INDEX_FOREGROUNG, drawable);
//        }
//        Log.i("PicBackground", "背景图宽高"+drawable.getIntrinsicWidth()+","+drawable.getIntrinsicHeight());
        mLayerDrawable.setDrawableByLayerId(R.id.layerdrawablefore,drawable);
    }

    //对外提供方法，用于开始渐变动画
    public void beginAnimation() {
        mObjectAnimator.start();
    }

    public boolean isNeedToUpdateBackground(int musicPicRes) {
//        if (this.musicPicRes == -1)
//            return true;
//        if (musicPicRes != -1 && musicPicRes != this.musicPicRes) {
//            this.musicPicRes = musicPicRes;
//            return true;
//        }
        if (this.musicPicRes == -1) return true;
        if (musicPicRes != this.musicPicRes) {
            return true;
        }
        return false;
    }
}
