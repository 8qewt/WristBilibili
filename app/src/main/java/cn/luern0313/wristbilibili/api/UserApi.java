package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.models.UserListPeopleModel;
import cn.luern0313.wristbilibili.models.UserModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * Created by liupe on 2018/11/13.
 * 好像用不到了。。
 * 谁说的！！
 */

public class UserApi
{
    private String csrf;
    private String access_key;
    private String mid;
    private ArrayList<String> webHeaders;

    private UserModel userModel;

    public UserApi(final String cookie, String csrf, String access_key, String mid)
    {
        this.csrf = csrf;
        this.access_key = access_key;
        this.mid = mid;

        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public UserModel getUserInfo() throws IOException
    {
        try
        {
            String url = "http://app.bilibili.com/x/v2/space";
            String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                    "&build=" + ConfInfoApi.getConf("build") + "&mobi_app=" + ConfInfoApi.getConf("mobi_app") +
                    "&platform=" + ConfInfoApi.getConf("platform") + "&ps=20&ts=" + (int) (System.currentTimeMillis() / 1000) +
                    "&vmid=" + mid;
            String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, webHeaders).body().string());
            if(result.optInt("code") == 0)
                userModel = new UserModel(result.optJSONObject("data"));
            return userModel;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<ListVideoModel> getUserVideo(int page) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/space/arc/search";
            String arg = "mid=" + mid + "&ps=30&tid=0&pn=" + page + "&keyword=&order=pubdate&jsonp=jsonp";
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            ArrayList<ListVideoModel> v = new ArrayList<>();
            if(result.optInt("code") == 0)
            {
                JSONArray videoJsonArray = result.getJSONObject("data").getJSONObject("list").getJSONArray("vlist");
                for(int i = 0; i < videoJsonArray.length(); i++)
                    v.add(new ListVideoModel(videoJsonArray.optJSONObject(i), 1));
                return v;
            }
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<ListBangumiModel> getUserBangumi(int page, int mode) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/space/bangumi/follow/list";
            String arg = "type=" + mode + "&pn=" + page + "&ps=20&vmid=" + mid;
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            ArrayList<ListBangumiModel> b = new ArrayList<>();
            if(result.optInt("code") == 0)
            {
                JSONArray bangumiJsonArray = result.getJSONObject("data").getJSONArray("list");
                for(int i = 0; i < bangumiJsonArray.length(); i++)
                    b.add(new ListBangumiModel(bangumiJsonArray.optJSONObject(i)));
                return b;
            }
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<UserListPeopleModel> getUserFollow(int page) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/followings";
            String arg = "vmid=" + mid + "&pn=" + page + "&ps=20&order=desc";
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            ArrayList<UserListPeopleModel> u = new ArrayList<>();
            if(result.optInt("code") == 0)
            {
                JSONArray peopleJsonArray = result.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < peopleJsonArray.length(); i++)
                {
                    JSONObject p = peopleJsonArray.optJSONObject(i);
                    u.add(new UserListPeopleModel(p));
                }
                return u;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<UserListPeopleModel> getUserFans(int page) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/followers";
            String arg = "vmid=" + mid + "&pn=" + page + "&ps=20&order=desc";
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            ArrayList<UserListPeopleModel> u = new ArrayList<>();
            if(result.optInt("code") == 0)
            {
                JSONArray peopleJsonArray = result.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < peopleJsonArray.length(); i++)
                {
                    JSONObject p = peopleJsonArray.optJSONObject(i);
                    u.add(new UserListPeopleModel(p));
                }
                return u;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String follow() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/modify";
            String arg = "fid=" + mid + "&act=1&re_src=11&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch(JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String unfollow() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/modify";
            String arg = "fid=" + mid + "&act=2&re_src=11&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }
}
