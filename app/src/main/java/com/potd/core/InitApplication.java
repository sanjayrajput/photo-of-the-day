package com.potd.core;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import com.potd.ApiException;
import com.potd.Configuration;
import com.potd.GlobalResources;
import com.potd.R;
import com.potd.Utils;
import com.potd.adapters.PicDetailsAdapter;
import com.potd.models.PicDetailTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private Context applicationContext;
    private Context activityContext;
    private boolean isRefresh;
    public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public InitApplication(ListView listView, Context applicationContext, Context context) {
        this.listView = listView;
        this.applicationContext = applicationContext;
        this.activityContext = context;
        isRefresh = false;
    }

    @Override
    protected void onPostExecute(List<PicDetailTable> picDetailTables) {
        try {
            PicDetailsAdapter adapter = (PicDetailsAdapter) listView.getAdapter();
            if(adapter == null) {
                if (picDetailTables != null) {
                    adapter = new PicDetailsAdapter(applicationContext, activityContext, R.layout.main_list, picDetailTables);
                    listView.setAdapter(adapter);
                }
            } else {
                if (picDetailTables != null) {
                    for (PicDetailTable p : picDetailTables) {
                        adapter.add(p);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }
    }

    @Override
    protected List<PicDetailTable> doInBackground(Object[] params) {
        try {
            List<PicDetailTable> list = new ArrayList<>();
            int currentPage = (int) params[0];
            isRefresh = (boolean) params[1];
            if ((Configuration.maxPhotoCount < ((currentPage + 1) * Configuration.chunkSize)) || isCurrentPageExistInCache(currentPage))
                return null;

            int start = currentPage * Configuration.chunkSize;
            int size = Configuration.chunkSize;
            Date date = Utils.getDateBeforeDays(new Date(), start);
            List<PicDetailTable> l1 = GlobalResources.getInternalDBHelper().getByDate(date, size);
            logger.info("Total fetched : " + l1.size());
            List<PicDetailTable> l2 = getMaxContinuous(l1, date);
            logger.info("Max Continuous : " + l2.size());
            if (l2.size() == size) {
                GlobalResources.getPicDetailList().addAll(l2);
                return l2;
            }

            if (GlobalResources.isNetworkConnected(applicationContext)) {
                list.addAll(l2);
                start += l2.size();
                size -= l2.size();

                DataBaseManager dataBaseManager = GlobalResources.getDataBaseManager();
                if (dataBaseManager == null) {
                    dataBaseManager = new DataBaseManager();
                    GlobalResources.setDataBaseManager(dataBaseManager);
                }
                List<PicDetailTable> l3 = dataBaseManager.getAllImages(start, size);
                if (l3 != null)
                    logger.info("Total Images fetched from Server : " + l3.size());
                list.addAll(l3);
            } else {
                list.addAll(l1);
//                list = GlobalResources.getInternalDBHelper().getAll(start, size);
            }
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
                (GlobalResources.getPicDetailList().size() >= ((page + 1) * Configuration.chunkSize))) {
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

    public static List<PicDetailTable> getMaxContinuous(List<PicDetailTable> list, Date date) {
        List<PicDetailTable> filterList = new ArrayList<>();
        try {
            for (PicDetailTable p : list) {
                if (p.getDate() != null && df.format(p.getDate()).equals(df.format(date))) {
                    filterList.add(p);
                    date = Utils.getDateBeforeDays(date, 1);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.log(Level.ALL, "Exception : " + e.getMessage());
        }
        return filterList;
    }
}
