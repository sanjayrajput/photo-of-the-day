package com.potd.core;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.ListView;

import com.potd.Configuration;

/**
 * Created by sanjay.rajput on 01/05/15.
 */
public class EndlessScrollListener implements AbsListView.OnScrollListener {

    private int visibleThreshold = 1;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private Context context;
    private ListView listView;

    public EndlessScrollListener(Context context, ListView listView) {
        this.context = context;
        this.listView = listView;
    }

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of gigs using a background task,
            // but you can call any function here.
            currentPage++;
            new InitApplication(listView, context).execute(currentPage);
            loading = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
