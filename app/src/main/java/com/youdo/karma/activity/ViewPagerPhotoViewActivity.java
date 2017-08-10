package com.youdo.karma.activity;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.ui.widget.CircularProgress;
import com.youdo.karma.ui.widget.HackyViewPager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import me.relex.photodraweeview.PhotoDraweeView;

/**
 * @author wangyb
 * @Description:图片浏览
 * @Date:2015年7月8日上午11:09:50
 */
public class ViewPagerPhotoViewActivity extends BaseActivity implements View.OnClickListener{
    private HackyViewPager mViewPager;
    private TextView mCurPosition;
    private TextView mTotalNum;

    private int mCurrentPosition;
    private List<String> imgUrls;

    private ViewPagerAdapter adapter;

    private static final String ISLOCKED_ARG = "isLocked";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_photoview);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
            getSupportActionBar().setTitle("");
        }
        setupView();
        setupEvent();
        setupData();
        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }
    }

    private void setupView(){
        mViewPager = (HackyViewPager) findViewById(R.id.id_viewPager);
        mCurPosition = (TextView) findViewById(R.id.current_position);
        mTotalNum = (TextView) findViewById(R.id.img_totals);
    }

    private void setupEvent() {
    }

    /**
     * 设置数据
     */
    private void setupData() {
        imgUrls = getIntent().getStringArrayListExtra(ValueKey.IMAGE_URL);
        mCurrentPosition = getIntent().getIntExtra(ValueKey.POSITION, 0);
        if (imgUrls != null && imgUrls.size() > 0) {
            mTotalNum.setText(String.valueOf(imgUrls.size()));
            adapter = new ViewPagerAdapter();
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(mCurrentPosition);
        }
        mCurPosition.setText(String.valueOf(mCurrentPosition + 1));
    }

    /**
     * ViewPager的适配器
     */
    class ViewPagerAdapter extends PagerAdapter {
        private View view;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_photo_viewpager, container, false);
            final PhotoDraweeView mPhotoDraweeView = (PhotoDraweeView) view.findViewById(R.id.photo_drawee_view);
            final CircularProgress mCircularProgress = (CircularProgress) view.findViewById(R.id.progress_bar);
            mCircularProgress.setVisibility(View.VISIBLE);
            mPhotoDraweeView.setOnClickListener(ViewPagerPhotoViewActivity.this);

            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(Uri.parse(imgUrls.get(position)));
            controller.setOldController(mPhotoDraweeView.getController());
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    mCircularProgress.setVisibility(View.GONE);
                    if (imageInfo == null || mPhotoDraweeView == null) {
                        return;
                    }
                    mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
                    super.onFailure(id, throwable);
                    mCircularProgress.setVisibility(View.GONE);

                }
            });
            mPhotoDraweeView.setController(controller.build());
            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return view;
        }

        @Override
        public int getCount() {
            return imgUrls == null ? 0 : imgUrls.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentPosition  = position;
            mCurPosition.setText(String.valueOf(mCurrentPosition + 1));
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }
}
