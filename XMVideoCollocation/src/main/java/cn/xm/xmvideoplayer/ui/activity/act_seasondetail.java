package cn.xm.xmvideoplayer.ui.activity;


import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import cn.xm.xmvideoplayer.constant.DbConstant;
import cn.xm.xmvideoplayer.constant.IntenConstant;
import cn.xm.xmvideoplayer.core.BaseSwipeBackActivity;
import cn.xm.xmvideoplayer.data.jsoup.JsoupApi;
import cn.xm.xmvideoplayer.data.realm.DbFav;
import cn.xm.xmvideoplayer.entity.PageDetailInfo;
import cn.xm.xmvideoplayer.utils.CheckAppUtil;
import cn.xm.xmvideoplayer.utils.DensityUtil;

/**
 * 电视剧详情
 */
public class act_seasondetail extends BaseSwipeBackActivity {

    @Bind(R.id.detail_image)
    ImageView detailImage;

    @Bind(R.id.rl_progress)
    RelativeLayout rlprogress;

    @Bind(R.id.toolbar)
    Toolbar mtoolbar;

    @Bind(R.id.coll_toolbar_layout)
    CollapsingToolbarLayout mcollToolbarLayout;

    @Bind(R.id.recycler_view)
    EasyRecyclerView mrecyclerView;

    /**
     * handler
     */
    private Handler mhandler = new Handler();

    /**
     * actionbar
     */
    private ActionBar mActionBar;
    /**
     * 下载链接adapter
     */
    private SeasonDetailAdapter mAdapter;
    /**
     * intent传入的链接
     */
    private String pageDetailurl;
    /**
     * 简介内容
     */
    private TextView tv_content;
    /**
     * 演员上映日期
     */
    private TextView tv_actor;
    /**
     * 展开收起简介
     */
    private TextView tv_expend;
    /**
     * 收藏
     */
    private TextView tv_fav;
    private Thread thread1;
    private DbFav dbFav;

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mrecyclerView.setLayoutManager(linearLayoutManager);
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

                if (CheckAppUtil.isAvilible(getApplicationContext(), CheckAppUtil.XLPackget)) {
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
        /* if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        //返回键
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        //upArrow.setColorFilter(getResources().getColor(R.color.black_90), PorterDuff.Mode.SRC_ATOP);
        mtoolbar.setNavigationIcon(upArrow);
        mtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*//设置title颜色
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
        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                final PageDetailInfo pageDetailInfo = JsoupApi.NewInstans().GetPageDetail(pageDetailurl);
                if (pageDetailInfo == null) {
                    return;
                }
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
        });
        thread1.start();

    }

    /**
     * 更新view
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updataview(PageDetailInfo pageDetailInfo) {
        if (pageDetailInfo == null) {
            return;
        }
        //取消progressbar
        if (rlprogress != null) {
            rlprogress.setVisibility(View.GONE);
        }
        //header
        updateheaer(pageDetailInfo, pageDetailInfo.getSmalltext(), pageDetailInfo.getAlltext(), pageDetailInfo.getActor());
        //appbar
        if (pageDetailInfo.getTitle() != null && mcollToolbarLayout != null) {
            mcollToolbarLayout.setTitle(pageDetailInfo.getTitle());
        }
        if (!this.isDestroyed()) {
            Glide.with(this)
                    .load(pageDetailInfo.getCover())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .into(detailImage);
        }
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
        tv_expend = ButterKnife.findById(view, R.id.tv_expend);
        tv_fav = ButterKnife.findById(view, R.id.tv_fav);
        return view;
    }

    /**
     * 更新headerview数据
     *
     * @param smalltext 部分简介
     * @param alltext   所有简介
     * @param actor     演员等信息
     */
    private void updateheaer(final PageDetailInfo pageDetailInfo, final String smalltext, final String alltext, String actor) {
        tv_content.setText(Html.fromHtml(smalltext));
        tv_actor.setText(Html.fromHtml(actor));
        //展开收缩简介
        tv_expend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getString(R.string.textcontent3).equals(tv_expend.getText())) {//展开全部
                    tv_expend.setText(getResources().getString(R.string.textcontent4));
                    tv_content.setText(Html.fromHtml(alltext));
                } else if (getResources().getString(R.string.textcontent4).equals(tv_expend.getText())) {//收起简介
                    tv_expend.setText(getResources().getString(R.string.textcontent3));
                    tv_content.setText(Html.fromHtml(smalltext));
                }
            }
        });
        //收藏状态
        dbFav = DbFav.Builder(this, DbConstant.DbFavrite);
        if (dbFav.FindIsExit(pageDetailurl)) {//已收藏
            tv_fav.setText(getResources().getString(R.string.textfav2));
        } else {//未收藏
            tv_fav.setText(getResources().getString(R.string.textfav1));
        }
        tv_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbFav.FindIsExit(pageDetailurl)) {//已收藏
                    if (dbFav.DeleteItem(pageDetailurl)) {
                        Snackbar.make(mrecyclerView, "取消收藏成功", Snackbar.LENGTH_SHORT).show();
                        tv_fav.setText(getResources().getString(R.string.textfav1));
                    } else {
                        Snackbar.make(mrecyclerView, "取消收藏失败", Snackbar.LENGTH_SHORT).show();
                    }
                } else {//未收藏
                    if (dbFav.Insert(pageDetailInfo)) {
                        Snackbar.make(mrecyclerView, "添加收藏成功", Snackbar.LENGTH_SHORT).show();
                        tv_fav.setText(getResources().getString(R.string.textfav2));
                    } else {
                        Snackbar.make(mrecyclerView, "添加收藏失败", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

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
        if (dbFav!=null){
            dbFav.Close();
        }
        mhandler.removeCallbacksAndMessages(null);
        if (thread1.isAlive()) {
            thread1.interrupt();
        }
    }
}
