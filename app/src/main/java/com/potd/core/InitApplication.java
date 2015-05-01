package com.potd.core;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import com.potd.ApiException;
import com.potd.Configuration;
import com.potd.GlobalResources;
import com.potd.R;
import com.potd.adapters.PicDetailsAdapter;
import com.potd.core.DBManager;
import com.potd.models.PicDetailTable;

import java.util.Collections;
import java.util.Comparator;
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
            PicDetailsAdapter adapter = (PicDetailsAdapter) listView.getAdapter();
            if(adapter == null) {
                adapter = new PicDetailsAdapter(context, R.layout.main_list2, picDetailTables);
                listView.setAdapter(adapter);
            } else {
                for (PicDetailTable p : picDetailTables) {
                    adapter.add(p);
                }
            }
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }
    }

    @Override
    protected List<PicDetailTable> doInBackground(Object[] params) {
        try {
            DBManager dbManager = GlobalResources.getDbManager();
            int currentPage = (int) params[0];
            if (dbManager == null) {
                dbManager = new DBManager();
                dbManager.init();
                GlobalResources.setDbManager(dbManager);
            }

            if (isCurrentPageExistInCache(currentPage))
                return GlobalResources.getPicDetailList();

            List<PicDetailTable> list = dbManager.getAllImages(currentPage * Configuration.chunkSize, Configuration.chunkSize);
            logger.info("Total Images fetched from DB : " + list.size());
            GlobalResources.getPicDetailList().addAll(list);
            return list;
        } catch (ApiException e) {
            logger.log(Level.ALL, "ApiException : " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }
        return null;
    }

    public boolean isCurrentPageExistInCache(int page) {
        if (GlobalResources.getPicDetailList() != null &&
                !GlobalResources.getPicDetailList().isEmpty() &&
                (GlobalResources.getPicDetailList().size() >= ((page + 1) * Configuration.chunkSize)) &&
                (Configuration.maxPhotoCount > ((page + 1) * Configuration.chunkSize))) {
            return true;
        }
        return false;
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
