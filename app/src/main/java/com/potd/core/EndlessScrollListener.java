package com.potd.core;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by sanjay.rajput on 01/05/15.
 */
public class EndlessScrollListener implements AbsListView.OnScrollListener {

    public int visibleThreshold = 1;
    public int currentPage = 0;
    public int previousTotal = 0;
    public boolean loading = true;
    private Context applicationContext;
    private ListView listView;
    private Context activityContext;

    public EndlessScrollListener(Context applicationContext, Context activityContext, ListView listView) {
        this.applicationContext = applicationContext;
        this.listView = listView;
        this.activityContext = activityContext;
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
            new InitApplication(listView, applicationContext, activityContext).execute(currentPage, false);
            loading = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
