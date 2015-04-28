package com.potd;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.potd.adapters.PicDetailsAdapter;
import com.potd.models.PicDetailTable;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class InitApplication extends AsyncTask<Object, Void, List<PicDetailTable>> {
    private static final Logger logger = Logger.getLogger("InitApplication");
    private ListView listView;
    private Context context;

    public InitApplication(ListView listView, Context context) {
        this.listView = listView;
        this.context = context;
    }

    @Override
    protected void onPostExecute(List<PicDetailTable> picDetailTables) {
        try {
            PicDetailsAdapter adapter = new PicDetailsAdapter(context, R.layout.main_list2, picDetailTables);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }
    }

    @Override
    protected List<PicDetailTable> doInBackground(Object[] params) {
        try {
            DBManager dbManager = new DBManager();
            dbManager.init();
            List<PicDetailTable> list = dbManager.getAllImages();
            sortResults(list);
            logger.info("Total Images : " + list.size());
            GlobalResources.setPicDetailList(list);
            return list;
        } catch (ApiException e) {
            logger.log(Level.ALL, "ApiException : " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }
        return null;
    }

    public void sortResults(List<PicDetailTable> list) {
        if (list.size() > 0) {
            Collections.sort(list, new Comparator<PicDetailTable>() {
                @Override
                public int compare(final PicDetailTable object1, final PicDetailTable object2) {
                    return object2.getDate().compareTo(object1.getDate());
                }
            });
        }
    }
}
