package cn.xm.xmvideoplayer.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.xm.xmvideoplayer.R;
import cn.xm.xmvideoplayer.entity.PageDetailInfo;
import cn.xm.xmvideoplayer.entity.SeasonDetailBean;

/**
 * Created by gaohailong on 2016/5/17.
 */
public class SeasonDetailHolder extends BaseViewHolder<String> {

    private TextView tv_title;
    private List<String> list=new ArrayList<>();

    public SeasonDetailHolder(ViewGroup parent, List<String> list) {
        super(parent, R.layout.item_seasonlist);
        tv_title = $(R.id.tv_title);
        this.list=list;
    }

    @Override
    public void setData(String data) {
        super.setData(data);
        //
        tv_title.setText(data);
    }

}
