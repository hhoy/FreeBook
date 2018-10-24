package com.ouyang.freebook.ui.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ouyang.freebook.R;
import com.ouyang.freebook.modle.RequestConfig;
import com.ouyang.freebook.modle.bean.Book;
import com.ouyang.freebook.modle.bean.BookList;
import com.ouyang.freebook.modle.bean.ResponseData;
import com.ouyang.freebook.modle.request.RankRequest;
import com.ouyang.freebook.ui.activity.BookDetailsActivity;
import com.ouyang.freebook.ui.activity.MainActivity;
import com.ouyang.freebook.ui.adapter.BookListAdapter;
import com.ouyang.freebook.util.RequestUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankFragment extends BaseFragment {
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    private RankRequest rankRequest;
    private String sexType;
    private String sortType;
    private String timeType;
    private int index;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.sex)
    TabLayout sex;
    @BindView(R.id.sort)
    TabLayout sort;
    @BindView(R.id.time)
    TabLayout time;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    Unbinder unbinder;

    BookListAdapter bookListAdapter;

    public RankFragment() {

        // Required empty public constructor
    }

    @Override
    public void init() {
        sexType = RequestConfig.SEX_TYPE_MAN;
        sortType = RequestConfig.SORT_TYPE_HOT;
        timeType = RequestConfig.TIME_TYPE_TOTAL;
        index = 1;
        rankRequest = RequestUtil.get(RankRequest.class);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbar(toolbar, true);
        sex.addTab(sex.newTab().setText("男生"));
        sex.addTab(sex.newTab().setText("女生"));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false));
        bookListAdapter = new BookListAdapter();
        recyclerView.setAdapter(bookListAdapter);
        bookListAdapter.setOnItemClickListener(new BookListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                Book book = bookListAdapter.getBookList().get(position);
                Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
                intent.putExtra("id", book.getId());
                startActivity(intent);
            }
        });
        refresh.setColorSchemeColors(Color.RED);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(true);
            }
        });
        getData(false);
    }

    public void getData(final boolean isUpdate) {
        int i;
        if(isUpdate){
            i=1;
        }else {
            i=index++;
        }
        rankRequest.getRankList(sexType, sortType, timeType, i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseData<BookList>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                            refresh.setRefreshing(true);
                    }

                    @Override
                    public void onNext(ResponseData<BookList> bookListResponseData) {
                        if(isUpdate){
                            bookListAdapter.getBookList().clear();
                            bookListAdapter.getBookList().addAll(bookListResponseData.getData().getBookList());
                            index=1;
                        }else {
                            bookListAdapter.addBookList(bookListResponseData.getData().getBookList());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("请求网络失败", e.getMessage());
                        Snackbar.make(getView(), "请求失败了", 3000).setAction("重试", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getData(isUpdate);
                            }
                        }).show();
                    }

                    @Override
                    public void onComplete() {
                            refresh.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onVisibleAgain() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rank, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}