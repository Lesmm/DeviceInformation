package com.google.applicationgui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.applicationgui.R;

import java.util.Random;

public class LoadingView extends RelativeLayout {

    private Context context;
    private View containerView;

    private ImageView loadingBgImageView;
    private ImageView loadingImageView;
    private ImageView loadingSmallImageView;
    public TextView loadingTextView;

    public int selectedShapeId = 0;
    public int selectedSvgId = 0;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        // views
        View view = LayoutInflater.from(context).inflate(R.layout.view_loading, this, true);
        containerView = view;
        loadingBgImageView = view.findViewById(R.id.loadingBgImageView);
        loadingImageView = view.findViewById(R.id.loadingImageView);
        loadingSmallImageView = view.findViewById(R.id.loadingSmallImageView);
        loadingTextView = view.findViewById(R.id.loadingTextView);

        containerView.setVisibility(View.VISIBLE);
    }

    public void setLoadingText(String text) {
        loadingTextView.setText(text);
    }

    public void startAnimation() {
        loadingBgImageView.setImageResource(selectedShapeId);

        loadingImageView.setImageResource(selectedSvgId);

        containerView.setVisibility(View.VISIBLE);

        if (new Random().nextBoolean()) {
            rotateAnticlockwise(loadingImageView);
            rotate(loadingSmallImageView);
        } else {
            rotateAnticlockwise(loadingSmallImageView);
            rotate(loadingImageView);
        }
    }

    public void stopAnimation() {
        containerView.setVisibility(View.GONE);

        stop(loadingImageView);
        stop(loadingSmallImageView);
    }

    // static methods
    public static void rotate(View animationView) {
        animationView.setVisibility(View.VISIBLE);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360 * 2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(800 * 2);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        // RotateAnimation rotateAnimation = (RotateAnimation)AnimationUtils.loadAnimation(ApplicationImpl.getTopActivity(), R.anim.rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        animationView.startAnimation(rotateAnimation);
    }

    public static void rotateAnticlockwise(ImageView animationView) {
        animationView.setVisibility(View.VISIBLE);
        RotateAnimation rotateAnimation = new RotateAnimation(360 * 5, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(900 * 5);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        animationView.startAnimation(rotateAnimation);
    }

    public static void stop(ImageView loadingImageView) {
        loadingImageView.setVisibility(View.INVISIBLE);
        loadingImageView.clearAnimation();
    }

}
