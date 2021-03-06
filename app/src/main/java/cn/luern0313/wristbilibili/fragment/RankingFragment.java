package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.RankingAdapter;
import cn.luern0313.wristbilibili.api.DynamicApi;
import cn.luern0313.wristbilibili.api.RankingApi;
import cn.luern0313.wristbilibili.api.VideoApi;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.ui.SelectPartActivity;
import cn.luern0313.wristbilibili.ui.TextActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class RankingFragment extends Fragment
{
    private static final String ARG_RID = "ridArg";

    private Context ctx;
    private View rootLayout;
    private RankingAdapter rankingAdapter;
    private VideoApi videoDetailsApi;
    private VideoModel videoModel;
    private TitleView.TitleViewListener titleViewListener;

    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View uiToolbarView, uiLoadingView;

    private RankingApi rankingApi;
    private final ArrayList<RankingModel> rankingVideoArrayList = new ArrayList<>();
    private LinkedHashMap<Integer, String> pickUpHashMap = new LinkedHashMap<>();
    private String pickupDay;
    private int pn = 1;
    private boolean isLoading = true;
    private Bitmap bitmapPickUpUpFace;
    private Bitmap bitmapPickUpVideoCover;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableNoMore, runnableMoreNoWeb, runnablePick, runnablePickUi;

    private final int RESULT_RANKING_REGION = 101;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_ranking, container, false);
        rankingApi = new RankingApi();

        uiPickUpView = inflater.inflate(R.layout.widget_ranking_pickup, null, false);
        uiToolbarView = inflater.inflate(R.layout.widget_ranking_toolbar, null, false);
        uiLoadingView = inflater.inflate(R.layout.widget_loading, null, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.rk_exception);
        listView = rootLayout.findViewById(R.id.rk_listview);
        listView.addFooterView(uiLoadingView);
        listView.addHeaderView(uiToolbarView);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.rk_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            listView.setVisibility(View.GONE);
            getRanking();
        }));

        RankingAdapter.RankingAdapterListener adapterListener = this::onViewClick;

        rankingAdapter = new RankingAdapter(inflater, rankingVideoArrayList, uiListView, adapterListener);
        uiListView.setAdapter(rankingAdapter);

        runnableUi = () -> {
            rootLayout.findViewById(R.id.rk_noweb).setVisibility(View.GONE);
            uiListView.setVisibility(View.VISIBLE);
            uiWaveSwipeRefreshLayout.setRefreshing(false);
            isLoading = false;
            rankingAdapter.notifyDataSetChanged();
        };

        runnableNoWeb = () -> {
            uiWaveSwipeRefreshLayout.setRefreshing(false);
            rootLayout.findViewById(R.id.rk_noweb).setVisibility(View.VISIBLE);
            isLoading = false;
        };

        runnableMoreNoWeb = () -> {
            ((TextView) uiLoadingView.findViewById(R.id.wid_load_button)).setText(ctx.getString(R.string.main_tip_no_more_web));
            uiLoadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableNoMore = () -> ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data));

        runnablePick = () -> {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            int today_int = Integer.parseInt(format.format(new Date(System.currentTimeMillis())));
            ArrayList<Integer> dates = new ArrayList<>(pickUpHashMap.keySet());
            Collections.sort(dates);
            for(int i = dates.size() - 1; i >= 0; i--)
            {
                if(dates.get(i) <= today_int)
                {
                    pickupDay = String.valueOf(dates.get(i));
                    videoDetailsApi = new VideoApi(pickUpHashMap.get(dates.get(i)), "");
                    new Thread(() -> {
                        try
                        {
                            videoModel = videoDetailsApi.getVideoDetails();
                            if(videoModel != null)
                                handler.post(runnablePickUi);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }).start();
                    break;
                }
            }
        };

        runnablePickUi = () -> {
            try
            {
                uiPickUpView.findViewById(R.id.rk_pu_lay).setVisibility(View.VISIBLE);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_title)).setText(videoModel.getTitle());
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_up_name)).setText(videoModel.getUpName());
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_date_date)).setText(String.format(ctx.getString(R.string.ranking_pickup_date), pickupDay
                        .substring(4, 6), pickupDay.substring(6, 8)));
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_play)).setText(videoModel.getPlay());
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_danmaku)).setText(videoModel.getDanmaku());
                if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.firstPickUp, true))
                    uiPickUpView.findViewById(R.id.rk_pu_date_click).setVisibility(View.VISIBLE);

                Drawable playNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_number_play);
                Drawable danmakuNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_number_danmu);
                playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
                danmakuNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_play)).setCompoundDrawables(playNumDrawable,null, null,null);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_danmaku)).setCompoundDrawables(danmakuNumDrawable,null, null,null);

                uiPickUpView.findViewById(R.id.rk_pu_date).setOnClickListener(v -> {
                    uiPickUpView.findViewById(R.id.rk_pu_date_click).setVisibility(View.GONE);
                    SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.firstPickUp, false);
                    Intent intent = new Intent(ctx, TextActivity.class);
                    intent.putExtra("title", "说明");
                    intent.putExtra("text", ("Pick Up视频说明\n" + "每天由用户推荐并投票选出一个精选视频，在排行榜的上方Pick Up栏推广展示一天\n" + "目的是让一些制作精良但播放不高的视频获得更多的曝光\n" + "（相关规则和投票系统正在制作中，目前推荐视频为手动设置，你可以在qq或b站私聊开发者推荐你喜欢的视频）\n" + "（相关要求：连续五天不能推荐同一名up主的视频，上榜视频非引战或有争议视频，播放量少的视频优先等）").replaceAll("\n", "<br/><br/>"));
                    startActivity(intent);
                });

                uiPickUpView.findViewById(R.id.rk_pu_video_up).setOnClickListener(v -> {
                    Intent intent = new Intent(ctx, UserActivity.class);
                    intent.putExtra("mid", videoModel.getUpMid());
                    startActivity(intent);
                });

                uiPickUpView.findViewById(R.id.rk_pu_lay).setOnClickListener(v -> {
                    new Thread(() -> rankingApi.clickPickUpVideo()).start();
                    startActivity(VideoActivity.getActivityIntent(ctx, videoModel.getAid(), ""));
                });

                new Thread(() -> {
                    try
                    {
                        bitmapPickUpUpFace = ImageDownloaderUtil.downloadImage(videoModel.getUpFace());
                        bitmapPickUpVideoCover = ImageDownloaderUtil.downloadImage(videoModel.getCover());
                        handler.post(runnablePickImg);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }).start();
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
            }
        };

        runnablePickImg = () -> {
            ((ImageView) uiPickUpView.findViewById(R.id.rk_pu_video_up_head)).setImageBitmap(bitmapPickUpUpFace);
            ((ImageView) uiPickUpView.findViewById(R.id.rk_pu_video_img)).setImageBitmap(bitmapPickUpVideoCover);
        };

        sendDynamicView.findViewById(R.id.wid_dy_title).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, SelectPartActivity.class);
            intent.putExtra("title", getString(R.string.dynamic_filter_title));
            intent.putExtra("options_name", getResources().getStringArray(R.array.region_list_title));
            intent.putExtra("options_id", DynamicApi.DYNAMIC_TYPES);
            startActivityForResult(intent, RESULT_RANKING_REGION);
        });

        uiListView.setOnTouchListener(new ViewTouchListener(uiListView, titleViewListener));

        uiListView.setVisibility(View.GONE);
        uiWaveSwipeRefreshLayout.setRefreshing(true);
        getRanking();

        return rootLayout;
    }

    private void getRanking()
    {
        uiPickUpView.findViewById(R.id.rk_pu_lay).setVisibility(View.GONE);
        new Thread(() -> {
            try
            {
                ArrayList<RankingModel> rankingModelArrayList = rankingApi.getRankingVideo(pn);
                pickUpHashMap = rankingApi.getPickUpVideo();
                if(rankingModelArrayList != null && rankingModelArrayList.size() != 0)
                {
                    handler.post(runnableUi);
                    if(pickUpHashMap != null && pickUpHashMap.size() != 0)
                        handler.post(runnablePick);
                }
                else
                    exceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                exceptionHandlerView.noWeb();
                e.printStackTrace();
            }
        }).start();
    }

    private void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.rk_video_lay)
        {
            if(rankingVideoArrayList.get(position).getIdentification().equals("av"))
            {
                Intent intent = VideoActivity.getActivityIntent(ctx, rankingVideoArrayList.get(position).getAid(), "");
                startActivity(intent);
            }
        }
        else if(viewId == R.id.rk_video_video_up)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", rankingVideoArrayList.get(position).getMid());
            startActivity(intent);
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
