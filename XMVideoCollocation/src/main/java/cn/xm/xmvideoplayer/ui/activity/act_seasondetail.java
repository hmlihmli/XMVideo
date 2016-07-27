package cn.xm.xmvideoplayer.ui.activity;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;


import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.xm.xmvideoplayer.R;
import cn.xm.xmvideoplayer.adapter.SeasonDetailAdapter;
import cn.xm.xmvideoplayer.constant.IntenConstant;
import cn.xm.xmvideoplayer.core.BaseSwipeBackActivity;
import cn.xm.xmvideoplayer.data.jsoup.JsoupApi;
import cn.xm.xmvideoplayer.entity.PageDetailInfo;
import cn.xm.xmvideoplayer.utils.CheckApp;
import cn.xm.xmvideoplayer.utils.DensityUtil;

/**
 * 电视剧详情
 */
public class act_seasondetail extends BaseSwipeBackActivity {

 /*   @Bind(R.id.detail_text)
    TextView detailtext;*/

    @Bind(R.id.detail_image)
    ImageView detailImage;

    @Bind(R.id.toolbar)
    Toolbar mtoolbar;

    @Bind(R.id.coll_toolbar_layout)
    CollapsingToolbarLayout mcollToolbarLayout;
    @Bind(R.id.recycler_view)
    EasyRecyclerView mrecyclerView;
    private Handler mhandler = new Handler();
    private ActionBar mActionBar;
    private SeasonDetailAdapter mAdapter;
    private String pageDetailurl;
    private TextView tv_content;
    private TextView tv_actor;


    @Override
    public int getLayoutId() {
        return R.layout.act_seasondetail;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

        pageDetailurl = getIntent().getStringExtra(IntenConstant.pagedetailurl);

        //toolbar
        inittoolbar();
        //recycleview
        initrecycle();
        //getdata
        getDetaildata();

    }

    /**
     * 初始化recycle
     */
    private void initrecycle() {
        //
        mAdapter = new SeasonDetailAdapter(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        gridLayoutManager.setSpanSizeLookup(mAdapter.obtainGridSpanSizeLookUp(1));
        mrecyclerView.setLayoutManager(gridLayoutManager);
        //
        mAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return initheadview();
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
        mrecyclerView.setAdapterWithProgress(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                if (CheckApp.isAvilible(getApplicationContext(), CheckApp.XLPackget)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdapter.getItem(position)));
                    intent.addCategory("android.intent.category.DEFAULT");
                    startActivity(intent);
                    Snackbar.make(mrecyclerView, "启动迅雷下载", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(mrecyclerView, "需要下载手机迅雷", Snackbar.LENGTH_SHORT).show();
                }

            }
        });
        //

    }


    /**
     * 初始化toolbar
     */
    private void inittoolbar() {
        // 初始化ToolBar
        setSupportActionBar(mtoolbar);
        mActionBar = getSupportActionBar();
   /*     if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        //返回键
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
//        upArrow.setColorFilter(getResources().getColor(R.color.black_90), PorterDuff.Mode.SRC_ATOP);
        mtoolbar.setNavigationIcon(upArrow);
        mtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    /*    //设置title颜色
        //mcollToolbarLayout.setTitle("title");
        mcollToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.black_90));
        mcollToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.black_90));
        mtoolbar.setTitleTextColor(getResources().getColor(R.color.black_90));*/
        mcollToolbarLayout.setExpandedTitleMarginEnd(DensityUtil.dip2px(this, 12.0f));
        mcollToolbarLayout.setExpandedTitleMarginStart(DensityUtil.dip2px(this, 12.0f));
    }

    /**
     * 获取网络数据
     */
    private void getDetaildata() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final PageDetailInfo pageDetailInfo = JsoupApi.NewInstans().GetPageDetail(pageDetailurl);
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updataview(pageDetailInfo);
                        if (pageDetailInfo.getDownlist().size() > 0) {
                            for (List<String> list : pageDetailInfo.getDownlist()) {
                                if (list.size() > 0) {
                                    mAdapter.addAll(list);
                                }
                            }
                        }
                    }
                });
            }
        }).start();

    }

    /**
     * 更新view
     */
    private void updataview(PageDetailInfo pageDetailInfo) {

        //header
        updateheaer(pageDetailInfo.getAlltext(), pageDetailInfo.getActor());

        //
        mcollToolbarLayout.setTitle(pageDetailInfo.getTitle());
        Glide.with(this).load(pageDetailInfo.getCover()).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(detailImage);
    }

    /**
     * 初始化headerview
     *
     * @return
     */
    private View initheadview() {
        View view = getLayoutInflater().inflate(R.layout.item_seasonlist_header, null);
        tv_content = ButterKnife.findById(view, R.id.tv_content);
        tv_actor = ButterKnife.findById(view, R.id.tv_actor);
        return view;
    }

    /**
     * 更新headerview数据
     *
     * @param content
     */
    private void updateheaer(String content, String actor) {
        if (content.length() == 0) {
            content = "暂时没有简介哦~~";
        }
        tv_content.setText(Html.fromHtml(content));
        tv_actor.setText(Html.fromHtml(actor));
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_history:
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhandler.removeCallbacksAndMessages(null);
    }
}
