package cn.xm.xmvideoplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import cn.xm.xmvideoplayer.R;
import cn.xm.xmvideoplayer.adapter.SearchHisAdapter;
import cn.xm.xmvideoplayer.adapter.TabHomeAdapter;
import cn.xm.xmvideoplayer.constant.DbConstant;
import cn.xm.xmvideoplayer.constant.IntenConstant;
import cn.xm.xmvideoplayer.core.BaseSwipeBackActivity;
import cn.xm.xmvideoplayer.data.jsoup.JsoupApi;
import cn.xm.xmvideoplayer.data.realm.DbSearchHis;
import cn.xm.xmvideoplayer.entity.PageInfo;
import cn.xm.xmvideoplayer.entity.SearchHistroy;
import cn.xm.xmvideoplayer.utils.SnackbarUtil;
import io.realm.RealmResults;

public class act_searchpage extends BaseSwipeBackActivity implements RecyclerArrayAdapter.OnLoadMoreListener {

    @Bind(R.id.et_search)
    EditText etsearch;

    @Bind(R.id.tv_clearhis)
    TextView tvclearhis;

    @Bind(R.id.tv_search)
    TextView tesearch;

    @Bind(R.id.ll_history)
    LinearLayout llhistory;

    @Bind(R.id.ll_result)
    LinearLayout llresult;

    @Bind(R.id.recycler_his)
    EasyRecyclerView recyclerhis;

    @Bind(R.id.recycler_result)
    EasyRecyclerView mrecyclerView;

    /**
     * 数据库
     */
    private DbSearchHis dbSearchHis;
    private SearchHisAdapter searchHisAdapter;
    private Handler handler = new Handler();
    private TabHomeAdapter mAdapter;
    private String keyword;
    /**
     * 当前页数
     */
    private int currentpage = 1;
    private Thread thread;

    @Override
    public int getLayoutId() {
        return R.layout.act_search_page;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        initSearchHis();
    }

    /**
     * 搜索记录
     */
    private void initSearchHis() {
        //隐藏搜索结果
        llresult.setVisibility(View.INVISIBLE);
        //搜索记录显示
        dbSearchHis = DbSearchHis.Builder(this, DbConstant.DbSearchHis);
        if (dbSearchHis.FindIsAllExit()) {//数据存在，显示
            llhistory.setVisibility(View.VISIBLE);
            //搜索记录
            initRecycleSearchHis();
        } else {//数据不存在，
            llhistory.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 搜索记录
     */
    private void initRecycleSearchHis() {
        RealmResults<SearchHistroy> searchHistroys = dbSearchHis.FindAll();
        ArrayList<String> datalistSearchHis = new ArrayList<>();
        for (SearchHistroy obj : searchHistroys) {
            datalistSearchHis.add(obj.getField());
        }
        //倒序
        Collections.reverse(datalistSearchHis);
        //recycle
        searchHisAdapter = new SearchHisAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayout.VERTICAL);
        recyclerhis.setLayoutManager(linearLayoutManager);
     /*   DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, DensityUtil.dip2px(this, 0.5f), 0,0);//颜色 & 高度 & 左边距 & 右边距
        itemDecoration.setDrawLastItem(true);//有时候你不想让最后一个item有分割线,默认true.
        itemDecoration.setDrawHeaderFooter(false);//是否对Header于Footer有效,默认false.
        recyclerhis.addItemDecoration(itemDecoration);*/
        recyclerhis.setAdapter(searchHisAdapter);
        //添加数据
        searchHisAdapter.addAll(datalistSearchHis);
        searchHisAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String field = searchHisAdapter.getAllData().get(position).toString();
                //et设置text，启动搜索
                etsearch.setText(field);
                etsearch.setSelection(field.length());
                searchAction();
            }
        });
    }

    /**
     * 搜索键
     *
     * @param view
     */
    @OnClick(R.id.tv_search)
    public void click1(View view) {
        searchAction();
    }

    /**
     * 键盘确定键搜索
     *
     * @return
     */
    @OnEditorAction(R.id.et_search)
    public boolean OnEditorAction1() {
        return searchAction();
    }

    /**
     * 搜索按键
     */
    private boolean searchAction() {
        String filed = etsearch.getText().toString();
        //搜索不为空
        if (filed.length() <= 0) {
            SnackbarUtil.SnackBarShort(recyclerhis, "请输入关键字");
            return false;
        }
        //隐藏历史记录，显示搜索布局，关键词加入数据库
        llhistory.setVisibility(View.INVISIBLE);
        llresult.setVisibility(View.VISIBLE);
        if (dbSearchHis.DeleteItem(filed)) {//不存在
            dbSearchHis.Insert(new SearchHistroy(filed));
        }
        //访问网络，获取结果
        initRecycleSearchList();
        return true;
    }

    /**
     * 搜索结果
     */
    private void initRecycleSearchList() {
        String filed = etsearch.getText().toString();
        //搜索不为空
        if (filed.length() <= 0) {
            SnackbarUtil.SnackBarShort(recyclerhis, "请输入关键字");
            return;
        }
        //设置recycleview
        mAdapter = new TabHomeAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mrecyclerView.setLayoutManager(linearLayoutManager);
        //
        mAdapter.setMore(R.layout.load_more_layout, this);
        mAdapter.setError(R.layout.error_layout);
        mAdapter.setNoMore(R.layout.no_more_layout);
        mrecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //跳转到电视剧
                Intent intent = new Intent(act_searchpage.this, act_seasondetail.class);
                intent.putExtra(IntenConstant.pagedetailurl, mAdapter.getItem(position).getAhref());
                act_searchpage.this.startActivity(intent);
            }
        });
        //填充数据
        keyword = filed;
        startSearch();
    }

    /**
     * 搜索框监听
     */
    @OnTextChanged(value = R.id.et_search, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void textchange1() {
        String filed = etsearch.getText().toString();
        if (filed.length() <= 0) {//输入框0字符，显示搜索记录
            initSearchHis();
        }
    }

    /**
     * 清空记录
     *
     * @param view
     */
    @OnClick(R.id.tv_clearhis)
    public void click2(View view) {
        if (!dbSearchHis.FindIsAllExit()) {
            return;
        }
        if (dbSearchHis.DeleteAll()) {//删除成功，刷新Recycleview
            searchHisAdapter.clear();
            searchHisAdapter.notifyDataSetChanged();
            SnackbarUtil.SnackBarShort(recyclerhis, "记录已清空");
        }
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMore() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                currentpage++;
                getData(false, currentpage, keyword);
            }
        });
    }

    /**
     * 设置搜索
     */
    private void startSearch() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getData(true, 1, keyword);
            }
        });
    }

    /**
     * 获取数据
     *
     * @param page  页码
     * @param filed 关键词
     */
    private void getData(final boolean isclear, final int page, final String filed) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<PageInfo> pageInfos = JsoupApi.NewInstans().GetPageSearch(page, filed);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (pageInfos == null) {
                            return;
                        }
                        //添加数据
                        if (isclear) {
                            mAdapter.clear();
                        }
                        mAdapter.addAll(pageInfos);
                        //需要添加数据后面
                        if (pageInfos.size() == 0) {
                            mAdapter.pauseMore();
                        } else if (pageInfos.size() > 0 && pageInfos.size() < 20) {
                            mAdapter.stopMore();
                        } else if (pageInfos.size() >= 20) {
                            mAdapter.resumeMore();
                        }
                    }
                });
                if (pageInfos != null) {
                    Log.i("msg", pageInfos.size() + "  " + pageInfos.toString() + "  page:" + page);
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (thread.isAlive()) {
            thread.interrupt();
        }
        if (dbSearchHis != null) {
            dbSearchHis.Close();
        }
    }
}
