package com.potd.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.provider.CalendarContract;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.potd.GlobalResources;
import com.potd.ImageDownloaderTask;
import com.potd.R;
import com.potd.models.PicDetailTable;
import com.potd.models.PicDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class PicDetailsAdapter extends ArrayAdapter<PicDetailTable> {

    private Context context;
    private int resource;
    private List<PicDetailTable> picDetailTableList;

    private static final Logger logger = Logger.getLogger("PicDetailsAdapter");

    public PicDetailsAdapter(Context context, int resource, List<PicDetailTable> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.picDetailTableList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(resource, parent, false);
        try {
            final PicDetailTable picDetailTable = picDetailTableList.get(position);

            TextView description = (TextView) row.findViewById(R.id.description);
            description.setText(picDetailTable.getDescription());

            TextView subject = (TextView) row.findViewById(R.id.subject);
            subject.setText(picDetailTable.getSubject());

            TextView date = (TextView) row.findViewById(R.id.date);
            DateFormat dateFormat = new SimpleDateFormat("EEEE d MMMM");
            date.setText(dateFormat.format(picDetailTable.getDate()));
            final ImageView image = (ImageView) row.findViewById(R.id.picofday);
            image.setBackgroundColor(Color.WHITE);

            final ImageView loadingImage = (ImageView) row.findViewById(R.id.loading_image);
            loadingImage.setBackgroundResource(R.drawable.loading_animation);
            AnimationDrawable animation = (AnimationDrawable) loadingImage.getBackground();
            animation.start();

            LruCache<String, Bitmap> images = GlobalResources.getImages();
            Bitmap bitmap = images.get(picDetailTable.getLink());
            if (bitmap != null) {
                logger.info("Found image in cache");
                animation.stop();
                loadingImage.setBackgroundResource(0);
                image.setImageBitmap(bitmap);
            } else {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ImageDownloaderTask(image, loadingImage).execute(picDetailTable.getLink());
                    }
                });
                thread.start();
            }
        } catch (Exception e) {
            logger.info("Failed to load image : " + e.getMessage());
        }
        return row;
    }
}
