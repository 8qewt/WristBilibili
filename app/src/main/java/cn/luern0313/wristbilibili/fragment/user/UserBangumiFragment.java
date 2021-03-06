package cn.luern0313.wristbilibili.fragment.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListBangumiAdapter;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;

public class UserBangumiFragment extends Fragment
{
    private static final String ARG_USER_BANGUMI_MID = "argUserBangumiMid";
    private static final String ARG_USER_BANGUMI_MODE = "argUserBangumiMode";

    private Context ctx;
    private String mid;
    private int mode;
    private UserApi userApi;
    private final ArrayList<ListBangumiModel> listBangumiModelArrayList = new ArrayList<>();
    private int page = 1;
    private ListBangumiAdapter listBangumiAdapter;
    private ListBangumiAdapter.ListBangumiAdapterListener listBangumiAdapterListener;
    private TitleView.TitleViewListener titleViewListener;

    private View rootLayout;
    private View layoutLoading;
    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableMore, runnableMoreNoWeb, runnableMoreNothing;

    private boolean isLoading = true;

    public UserBangumiFragment() {}

    public static UserBangumiFragment newInstance(String mid, int mode)
    {
        UserBangumiFragment fragment = new UserBangumiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_BANGUMI_MID, mid);
        args.putInt(ARG_USER_BANGUMI_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mid = getArguments().getString(ARG_USER_BANGUMI_MID);
            mode = getArguments().getInt(ARG_USER_BANGUMI_MODE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_user_bangumi, container, false);
        userApi = new UserApi(mid);

        listBangumiAdapterListener = this::onViewClick;

        layoutLoading = inflater.inflate(R.layout.widget_loading, null, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.user_bangumi_exception);
        listView = rootLayout.findViewById(R.id.user_bangumi_listview);

        listView.addFooterView(layoutLoading);

        runnableUi = () -> {
            exceptionHandlerView.hideAllView();
            listBangumiAdapter = new ListBangumiAdapter(inflater, listView, listBangumiModelArrayList, listBangumiAdapterListener);
            listView.setAdapter(listBangumiAdapter);
        };

        runnableMore = () -> listBangumiAdapter.notifyDataSetChanged();

        runnableMoreNoWeb = () -> {
            ((TextView) layoutLoading.findViewById(R.id.wid_load_button)).setText(getString(R.string.main_tip_no_more_web));
            layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableMoreNothing = () -> ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_data));

        layoutLoading.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) layoutLoading.findViewById(R.id.wid_load_button)).setText(getString(R.string.main_tip_no_more_data_loading));
            layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getMoreBangumi();
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isLoading)
                {
                    isLoading = true;
                    getMoreBangumi();
                }
            }
        });

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        new Thread(() -> {
            try
            {
                ArrayList<ListBangumiModel> b = userApi.getUserBangumi(page, mode);
                isLoading = false;
                if(b != null && b.size() != 0)
                {
                    listBangumiModelArrayList.addAll(b);
                    handler.post(runnableUi);
                }
                else
                    exceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                exceptionHandlerView.noWeb();
            }
        }).start();

        return rootLayout;
    }

    private void getMoreBangumi()
    {
        new Thread(() -> {
            try
            {
                page++;
                ArrayList<ListBangumiModel> b = userApi.getUserBangumi(page, mode);
                isLoading = false;
                if(b != null && b.size() != 0)
                {
                    listBangumiModelArrayList.addAll(b);
                    handler.post(runnableMore);
                }
                else
                    handler.post(runnableMoreNothing);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handler.post(runnableMoreNoWeb);
            }
        }).start();
    }

    private void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.item_list_bangumi_lay)
        {
            Intent intent = new Intent(ctx, BangumiActivity.class);
            intent.putExtra("season_id", listBangumiModelArrayList.get(position).getSeasonId());
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
