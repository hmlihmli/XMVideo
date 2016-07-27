package cn.xm.xmvideoplayer.data.jsoup;

import android.graphics.pdf.PdfDocument;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.xm.xmvideoplayer.entity.PageDetailInfo;
import cn.xm.xmvideoplayer.entity.PageInfo;

/**
 * Created by WANG on 2016/7/27.
 */
public class JsoupApi {

    /**
     * url编码
     */
    public static final String urlencodde = "gb2312";
    /**
     * useragent
     */
    public static final String useragent = "Mozilla";
    public static final int timeout = 10000;
    /**
     * 主链接
     */
    private static String BaseUrl = "http://www.dytt.com";

    /**
     * 搜索链接
     */
    private static String BaseUrlSearch = "http://www.dytt.com/search.asp?searchword=";

    /**
     * 最新更新链接
     */
    //private static final String BaseUrlUpdate = "http://www.dytt.com/top/lastupdate";
    private static final String BaseUrlUpdate = "http://www.dytt.com/top/toplist";
    public static final String typeUpdate = "typeUpdate";

    /**
     * 最新电影链接http://www.dytt.com/top/toplist_15.html
     */
    //private static String BaseUrlFilm = "http://www.dytt.com/top/lastupdate_15";
    private static String BaseUrlFilm = "http://www.dytt.com/top/toplist_15";
    public static final String typeFilm = "typeFilm";

    /**
     * 最新电视剧链接http://www.dytt.com/top/toplist_16.html
     */
    // private static String BaseUrlTv = "http://www.dytt.com/top/lastupdate_16";
    private static String BaseUrlTv = "http://www.dytt.com/top/toplist_16";
    public static final String typeTv = "typeTv";

    /**
     * 最新动漫链接http://www.dytt.com/top/toplist_7.html
     */
    //private static String BaseUrlCartoon = "http://www.dytt.com/top/lastupdate_7";
    private static String BaseUrlCartoon = "http://www.dytt.com/top/toplist_7";
    public static final String typeCartoon = "typeCartoon";

    /**
     * 最新综艺链接http://www.dytt.com/top/toplist_8.html
     */
    //private static String BaseUrlVariety = "http://www.dytt.com/top/lastupdate_8";
    private static String BaseUrlVariety = "http://www.dytt.com/top/toplist_8";
    public static final String typeVariety = "typeVariety";

    /**
     * 获得对象
     *
     * @return jsoup对象
     */
    public static JsoupApi NewInstans() {
        return new JsoupApi();
    }

    /**
     * 获取doc对象
     *
     * @param URL    链接
     * @param method 访问的方法MethodGet MethodPost
     * @return doc对象
     */
    private Document GetDoc(String URL, Connection.Method method) {
        Document mdoc = null;
        try {
            mdoc = Jsoup.connect(URL)
                    .timeout(timeout)
                    .method(method)
                    .userAgent(useragent)
                    .execute()
                    .parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mdoc;
    }


    /**
     * 获取信息详情数据
     *
     * @param URL 页面链接
     * @return
     */
    public PageDetailInfo GetPageDetail(String URL) {

        Document doc = GetDoc(URL, Connection.Method.GET);
        //标题
        String title = doc.select("div.movie h1").first().html();
        //封面链接
        String cover = doc.select("div.pic").first().getElementsByTag("img").first().select("img").attr("src").trim();
        //剧情介绍
        String smalltext = doc.select("div.smalltext").first().html();
        String alltext = doc.select("div.alltext").first().html();
        //主演,更新时间
        String actor = doc.select("div.movie ul").first().outerHtml().replaceAll("li", "br");
        //下载地址
        Elements downlist = doc.select("div.downlist script");
        //
        List<List> listsdownload = new ArrayList<List>();
        for (Element et : downlist) {
            List<String> lists = new ArrayList<>();
            if (et.html().contains("GvodUrls")) {
                Pattern pat = Pattern.compile("\"(.*?)\"");
                Matcher mat = pat.matcher(et.html());
                if (mat.find()) {
                    //下载地址
                    String urlDeCode = null;
                    try {
                        urlDeCode = URLDecoder.decode(mat.group(1), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //
                    String[] split = urlDeCode.split("###");
                    for (int i = 0; i < split.length; i++) {
                        if (!"".equals(split[i])) {
                            lists.add(split[i]);
                        }
                    }
                    Collections.reverse(lists);
                    listsdownload.add(lists);
                }
            }

        }
        return new PageDetailInfo(title, cover, smalltext, alltext, actor, listsdownload);

    }

    /**
     * 获取信息封面链接
     *
     * @param URL 页面链接
     * @return
     */
    public String GetPageDetailCover(String URL) {
        Document doc = GetDoc(URL, Connection.Method.GET);
        //封面链接
        String cover = doc.select("div.pic").first().getElementsByTag("img").first().select("img").attr("src").trim();
        return cover;
    }

    /**
     * 获取搜索结果数据
     *
     * @param kw 关键字
     * @return list
     */
    public List<PageInfo> GetPageSearch(String kw) {
        Document doc = GetDoc(DealUrl(kw), Connection.Method.POST);
        return GetPageDeal(doc);
    }

    /**
     * 获取信息展示数据
     *
     * @param Page 页码
     * @param type 类型
     */
    public List<PageInfo> GetPage(int Page, String type) {
        Document doc = GetDoc(DealUrl(Page, type), Connection.Method.GET);
        return GetPageDeal(doc);
    }

    /**
     * 处理信息展示数据
     *
     * @param doc doc对象
     * @return list
     */
    private List<PageInfo> GetPageDeal(Document doc) {
        ArrayList<PageInfo> pageInfos = new ArrayList<>();
        Elements movielist = doc.select("div.movielist li");
        for (Element et : movielist) {
            String score = et.select("p.s4").first().html();
            String type = et.select("p.s5").first().html();
            String actor = et.select("p.s6").first().html();
            String updatetime = et.select("p.s7").first().html();
            String ahref = BaseUrl + et.select("p.s1").first().select("a").attr("href");
            String title = et.select("p.s1").first().select("a").html();
            String year = et.select("p").first().nextElementSibling().html();
            String addr = et.select("p").first().nextElementSibling().nextElementSibling().html().replace("&nbsp;", "");
            pageInfos.add(new PageInfo(score, type, actor, updatetime, ahref, title, year, addr));
            //Log.i("msg", "  评分：" + score + "  网址:" + ahref + "  标题:" + title + "  类型:" + type + "  主演:" + actor + "  更新时间:" + updatetime + "  年代:" + year + "  地区:" + addr);
        }
        return pageInfos;
    }

    /**
     * 处理链接格式,加上页码
     *
     * @param Page 页面
     * @return 处理后的结果
     */
    private String DealUrl(int Page, String type) {
        //处理页码
        String html = ".html";
        String mPage = Page + "";
        if (Page <= 1) {
            mPage = "";
        }
        //处理链接
        switch (type) {
            case typeUpdate:
                return BaseUrlUpdate + mPage + html;
            case typeFilm:
                return BaseUrlFilm + mPage + html;
            case typeTv:
                return BaseUrlTv + mPage + html;
            case typeCartoon:
                return BaseUrlCartoon + mPage + html;
            case typeVariety:
                return BaseUrlVariety + mPage + html;
        }
        return BaseUrl;
    }

    /**
     * 处理链接格式，搜索页面
     *
     * @param kw 关键词（不需要编码转换）
     * @return 处理后的结果
     */
    private String DealUrl(String kw) {
        String kwgb2312 = "";
        try {
            kwgb2312 = URLEncoder.encode(kw, urlencodde);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return BaseUrlSearch + kwgb2312;
    }
}
