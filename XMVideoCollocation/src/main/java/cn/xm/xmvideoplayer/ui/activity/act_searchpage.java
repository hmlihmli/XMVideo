package cn.xm.xmvideoplayer.ui.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import cn.xm.xmvideoplayer.R;
import cn.xm.xmvideoplayer.core.BaseSwipeBackActivity;

public class act_searchpage extends BaseSwipeBackActivity {

    @Bind(R.id.et_search)
    EditText etsearch;

    @Bind(R.id.tv_search)
    TextView tesearch;

    @Override
    public int getLayoutId() {
        return R.layout.act_search_page;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

        //键盘
        initinput();
    }

    /**
     * 弹出键盘
     */
    private void initinput() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
