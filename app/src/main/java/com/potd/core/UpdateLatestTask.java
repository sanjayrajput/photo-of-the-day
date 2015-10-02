package com.potd.core;

import android.os.AsyncTask;
import android.widget.ListView;

import com.potd.ApiException;
import com.potd.GlobalResources;
import com.potd.adapters.PicDetailsAdapter;
import com.potd.models.PicDetailTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 11/05/15.
 */
public class UpdateLatestTask extends AsyncTask<Object, Void, List<PicDetailTable>> {

    private static final Logger logger = Logger.getLogger("UpdateLatestTask");
    private ListView listView;

    public UpdateLatestTask(ListView listView) {
        this.listView = listView;
    }

    @Override
    protected void onPostExecute(List<PicDetailTable> list) {
        List<PicDetailTable> currentList = GlobalResources.getPicDetailList();
        PicDetailsAdapter adapter = (PicDetailsAdapter) listView.getAdapter();
        if (list.size() > 0) {
            Collections.reverse(list);
            for (PicDetailTable pdt : list) {
                currentList.add(0, pdt);
                adapter.updateList(currentList);
                adapter.notifyDataSetChanged();
            }
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(0);
                }
            });
        }
    }

    @Override
    protected List<PicDetailTable> doInBackground(Object... params) {
        List<PicDetailTable> currentList = GlobalResources.getPicDetailList();
        PicDetailTable topItem = currentList.get(0);
        List<PicDetailTable> latestImages = new ArrayList<>();
        try {
            DataBaseManager dataBaseManager = GlobalResources.getDataBaseManager();
            if (dataBaseManager == null) {
                dataBaseManager = new DataBaseManager();
                GlobalResources.setDataBaseManager(dataBaseManager);
            }
            latestImages = dataBaseManager.getLatestImages(topItem.getDate());
            logger.info("Latest Images : " + latestImages.size());

        } catch (ApiException e) {
            logger.info("Failed to connect to Mongo database " + e.getMessage());
        } catch (Exception e) {
            logger.info("Failed to update to latest content " + e.getMessage());
        }
        return latestImages;
    }
}
