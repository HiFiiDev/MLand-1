package com.kyler.mland.egg.activities;

import android.annotation.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import com.facebook.common.logging.*;
import com.facebook.drawee.backends.pipeline.*;
import com.facebook.drawee.view.*;
import com.google.samples.apps.iosched.ui.widget.*;
import com.kyler.mland.egg.*;
import com.kyler.mland.egg.utils.*;

import com.kyler.mland.egg.R;

import static android.view.ViewTreeObserver.OnGlobalLayoutListener;

import androidx.core.view.ViewCompat;

/**
 * Created by kyler on 10/6/15.
 */
public class About extends MLandBase implements ObservableScrollView.Callbacks
{
    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;
    private static final float DRAWEE_PHOTO_ASPECT_RATIO = 1.33f;
    public SimpleDraweeView draweeView;
    private int mPhotoHeightPixels;
    private View mAddScheduleButtonContainer;
    private CheckableFloatingActionButton mAddScheduleButton;
    private int mAddScheduleButtonContainerHeightPixels;
    private View mScrollViewChild;
    private int mHeaderHeightPixels;
    private View mDetailsContainer;
    private ObservableScrollView mScrollView;
    private View mPhotoViewContainer;
    private boolean mHasPhoto;
    private float mMaxHeaderElevation;
    private View mHeaderBox;
    private Handler mHandler;
    private float mFABElevation;
    //  private static String draweeUrlString;
    private OnGlobalLayoutListener mGlobalLayoutListener
            = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout()
        {
            recomputePhotoAndScrollingMetrics();
        }
    };

    @Override
    protected int getSelfNavDrawerItem()
    {
        return NAVDRAWER_ITEM_ABOUT;
    }

    @Override
    protected Context getContext()
    {
        return About.this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getContext());
        super.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        boolean shouldBeFloatingWindow = false;
        setContentView(R.layout.about);
        getSupportActionBar().setTitle(null);

        mHasPhoto = true;

        mHandler = new Handler();
        initViews();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mScrollView == null)
        {
            return;
        }
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive())
        {
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
    }

    private void initViews()
    {
        mFABElevation = getResources().getDimensionPixelSize(R.dimen.fab_elevation);
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.session_detail_max_header_elevation);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive())
        {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        mScrollViewChild = findViewById(R.id.scroll_view_child);
        mScrollViewChild.setVisibility(View.VISIBLE);
        mDetailsContainer = findViewById(R.id.details_container);
        mHeaderBox = findViewById(R.id.header_session);
        mActionBarToolbar.setVisibility(View.VISIBLE);
        mPhotoViewContainer = findViewById(R.id.session_photo_container);
        mAddScheduleButtonContainer = findViewById(R.id.add_schedule_button_container);
        mAddScheduleButton = (CheckableFloatingActionButton) findViewById(R.id.add_schedule_button);

        Uri mDraweeUri = Uri.parse("https://i.ibb.co/SNSdL6X/cartogram-1635520782312.png");

        draweeView = (SimpleDraweeView) findViewById(R.id.session_photo);
       // draweeView.setAspectRatio(DRAWEE_PHOTO_ASPECT_RATIO);
        draweeView.setImageURI(mDraweeUri);
        displayData();

    }

    private void recomputePhotoAndScrollingMetrics()
    {
        mHeaderHeightPixels = mHeaderBox.getHeight();

        mPhotoHeightPixels = 0;
        if (mHasPhoto)
        {
            mPhotoHeightPixels = (int) (draweeView.getWidth() / PHOTO_ASPECT_RATIO);
            mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() / 3);
        }

        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels)
        {
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels)
        {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
            mDetailsContainer.setPadding(16, 150, 16, 150);
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY)
    {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);

        mHeaderBox.setTranslationY(newTop);
        mAddScheduleButtonContainer.setTranslationY(newTop + mHeaderHeightPixels
                - mAddScheduleButtonContainerHeightPixels / 2);

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0)
        {
            gapFillProgress = Math.min(Math.max(UIUtils.getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);

            if (gapFillProgress == 1)
            {
                //    Toast.makeText(this, "Okay we're locked", Toast.LENGTH_LONG).show();
            }
            else if (gapFillProgress >= 1)
            {

            }
        }

        // The code below is to change the statusbar color from transparent to a teal
        // green color I used.
		/*  int baseColor = getResources().getColor(R.color.tealish_green__primary);
         float alpha = Math.min(1, (float) scrollY / mHeaderBox.getHeight());
         Window window = getWindow();
         window.setStatusBarColor(UIUtils.getColorWithAlpha(alpha, (darkenColor(baseColor)))); */

        ViewCompat.setElevation(draweeView, gapFillProgress * mMaxHeaderElevation);
        ViewCompat.setElevation(mDetailsContainer, gapFillProgress * mMaxHeaderElevation);
        ViewCompat.setElevation(mHeaderBox, gapFillProgress * mMaxHeaderElevation);
        ViewCompat.setElevation(mAddScheduleButtonContainer, gapFillProgress * mMaxHeaderElevation
                + mFABElevation);
        ViewCompat.setElevation(mAddScheduleButton, gapFillProgress * mMaxHeaderElevation
                + mFABElevation);

        ViewCompat.setTranslationZ(mHeaderBox, gapFillProgress * mMaxHeaderElevation);

        // testing
        ViewCompat.setTranslationZ(draweeView, gapFillProgress * mMaxHeaderElevation);
        ViewCompat.setTranslationZ(mDetailsContainer, gapFillProgress * mMaxHeaderElevation);

        // Move background photo (parallax effect)
        mPhotoViewContainer.setTranslationY(scrollY * 0.5f);

    }

    public void displayData()
    {
        mHandler.post(new Runnable() {
            @Override
            public void run()
            {
                Uri mDraweeUri = Uri.parse("https://i.ibb.co/SNSdL6X/cartogram-1635520782312.png");

                draweeView = (SimpleDraweeView) findViewById(R.id.session_photo);
           //     draweeView.setAspectRatio(DRAWEE_PHOTO_ASPECT_RATIO);
                draweeView.setImageURI(mDraweeUri);
                mScrollViewChild.setVisibility(View.VISIBLE);
            }
        });
    }
}