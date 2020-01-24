package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 被 luern0313 创建于 2020/1/21.
 */

public class BangumiModel
{
    public String bangumi_title;
    public String bangumi_score;
    public String bangumi_play;
    public String bangumi_like;
    public String bangumi_series;
    public String bangumi_needvip;
    public ArrayList<BangumiEpisodeModel> bangumi_episodes = new ArrayList<>();
    public ArrayList<BangumiSeasonModel> bangumi_seasons = new ArrayList<>();
    public BangumiModel(JSONObject bangumi)
    {
        bangumi_title = bangumi.optString("season_title");
        bangumi_score = bangumi.has("rating") ? String.valueOf(bangumi.optJSONObject("rating").optDouble("score")) + "分" : "";
        bangumi_play = getView(bangumi.optJSONObject("stat").optInt("views"));
        bangumi_like = getView(bangumi.optJSONObject("stat").optInt("favorites"));
        bangumi_series = bangumi.optJSONObject("publish").optString("time_length_show");
        bangumi_needvip = bangumi.optString("badge");

        JSONArray episodes = bangumi.optJSONArray("episodes");
        for(int i = 0; i < episodes.length(); i++)
            bangumi_episodes.add(new BangumiEpisodeModel(episodes.optJSONObject(i), i));

        JSONArray seasons = bangumi.optJSONArray("seasons");
        for(int i = 0; i < seasons.length(); i++)
            bangumi_seasons.add(new BangumiSeasonModel(episodes.optJSONObject(i)));
    }

    public class BangumiEpisodeModel
    {
        public String bangumi_episode_title;
        public String bangumi_episode_title_long;
        public String bangumi_episode_aid;
        public String bangumi_episode_cid;
        public int position;
        BangumiEpisodeModel(JSONObject episode, int position)
        {
            bangumi_episode_title = episode.optString("title");
            bangumi_episode_title_long = episode.optString("long_title");
            bangumi_episode_aid = String.valueOf(episode.optInt("aid"));
            bangumi_episode_cid = String.valueOf(episode.optInt("cid"));
            this.position = position;
        }
    }

    public class BangumiSeasonModel
    {
        public String bangumi_season_id;
        public String bangumi_season_title;
        BangumiSeasonModel(JSONObject season)
        {
            bangumi_season_id = String.valueOf(season.optInt("season_id"));
            bangumi_season_title = season.optString("season_title");
        }
    }

    private static String getView(int view)
    {
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }
}