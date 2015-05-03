package com.potd.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.potd.FullScreen;
import com.potd.GlobalResources;
import com.potd.ImageDBHelper;
import com.potd.core.ImageDownloaderTask;
import com.potd.R;
import com.potd.models.PicDetailTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by sanjay.rajput on 27/04/15.
 */
public class PicDetailsAdapter extends ArrayAdapter<PicDetailTable> {

    private Context applicationContext;
    private Context activityContext;
    private int resource;
    private List<PicDetailTable> picDetailTableList;

    private static final Logger logger = Logger.getLogger("PicDetailsAdapter");

    public PicDetailsAdapter(Context applicationContext, Context activityContext, int resource, List<PicDetailTable> objects) {
        super(applicationContext, resource, objects);
        this.applicationContext = applicationContext;
        this.resource = resource;
        this.picDetailTableList = objects;
        this.activityContext = activityContext;
    }

    @Override
    public void add(PicDetailTable object) {
        super.add(object);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(resource, parent, false);
        try {
            final PicDetailTable picDetailTable = picDetailTableList.get(position);

            TextView description = (TextView) row.findViewById(R.id.description);
            description.setText(picDetailTable.getDescription());

            TextView subject = (TextView) row.findViewById(R.id.subject);
            subject.setText(picDetailTable.getSubject());

            if (picDetailTable.getPhotographer() != null) {
                TextView photographer = (TextView) row.findViewById(R.id.photographer);
                String html = "<html>" +
                        "<p>Photography by - <i>" + picDetailTable.getPhotographer() + "</i><p>" +
                        "</html>";
                photographer.setText(Html.fromHtml(html));

            }

            TextView date = (TextView) row.findViewById(R.id.date);
            DateFormat dateFormat = new SimpleDateFormat("EEEE d MMMM");
            date.setText(dateFormat.format(picDetailTable.getDate()));
            final ImageView image = (ImageView) row.findViewById(R.id.picofday);
            image.setBackgroundColor(Color.WHITE);

            final ImageView loadingImage = (ImageView) row.findViewById(R.id.loading_image);
            loadingImage.setBackgroundResource(R.drawable.loading_animation);
            AnimationDrawable animation = (AnimationDrawable) loadingImage.getBackground();
            animation.start();

            ProgressDialog d = GlobalResources.getLoadingDialog();
            if (d != null)
                d.hide();

            Bitmap bitmap = ImageDBHelper.getImage(picDetailTable.getLink());
            if (bitmap != null) {
                animation.stop();
                loadingImage.setBackgroundResource(0);
                image.setImageBitmap(bitmap);
                picDetailTable.setBitmap(bitmap);

                ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                image.setLayoutParams(layoutParams);
                image.requestLayout();

            } else {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ImageDownloaderTask(image, loadingImage, "Home", picDetailTable).execute();
                    }
                });
                thread.start();
//                Picasso.with(context).load(picDetailTable.getLink()).into(image); TODO : Explore Picasso
            }

            image.setOnClickListener(new ImageOnClickListener(position));
            loadingImage.setOnClickListener(new ImageOnClickListener(position));
        } catch (Exception e) {
            logger.info("Failed to load image : " + e.getMessage());
        }
        return row;
    }



    public class ImageOnClickListener implements View.OnClickListener {
        int position;

        public ImageOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(applicationContext, FullScreen.class);
            intent.putExtra("position", position);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Activity ac = (Activity) activityContext;
            ac.startActivity(intent);
            ac.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
