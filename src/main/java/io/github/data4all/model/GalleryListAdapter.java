/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.model;

import io.github.data4all.R;
import io.github.data4all.activity.AbstractActivity;
import io.github.data4all.activity.ShowPictureActivity;
import io.github.data4all.util.Gallery;
import io.github.data4all.util.Gallery.Informations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * @author tbrose
 *
 */
public class GalleryListAdapter implements ListAdapter {

    private Gallery gallery;

    private List<DataSetObserver> observer = new ArrayList<DataSetObserver>();

    private AbstractActivity context;

    /**
     * @param context
     */
    public GalleryListAdapter(AbstractActivity context) {
        this.context = context;
        gallery = new Gallery(context);
    }

    public void removeImage(long id) {
        gallery.deleteImage(id);
        this.invalidate();
    }

    public void tagImage(long id) {
        try {
            Informations infos = gallery.getImageInformations(id);
            File imageFile = gallery.getImageFile(id);

            ShowPictureActivity.startActivity(context, imageFile,
                    infos.getParameters(), infos.getOrientation(),
                    infos.getDimension());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return gallery.getImageFiles().length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public File getItem(int position) {
        if (position < 0) {
            return null;
        } else {
            File[] files = gallery.getImageFiles();
            if (files.length > position) {
                return files[position];
            } else {
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        if (position < 0) {
            return -1;
        } else {
            long[] files = gallery.getImages();
            if (files.length > position) {
                return files[position];
            } else {
                return -1;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        Layout layout;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            result = inflater.inflate(R.layout.view_listitem, null);
            layout =
                    new Layout(result.findViewById(R.id.gallery_date_text),
                            result.findViewById(R.id.gallery_thumbnail));
            result.setTag(layout);
        } else {
            layout = (Layout) convertView.getTag();
        }

        layout.text.setText(getDateText(getItemId(position)));
        try {
            Options options = new Options();
            options.inSampleSize = 8;
            Bitmap bitmap =
                    BitmapFactory.decodeFile(
                            gallery.getImageFile(getItemId(position))
                                    .getAbsolutePath(), options);
            layout.image.setImageBitmap(bitmap);
            if (bitmap.getWidth() > bitmap.getHeight()) {
                layout.image.setRotation(90);
            } else {
                layout.image.setRotation(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * @param itemId
     * @return
     */
    private String getDateText(long timestamp) {
        final long now = System.currentTimeMillis();
        final long diff = (now / 1000) - (timestamp / 1000);

        String result = null;

        if (diff < 60) {
            result = context.getString(R.string.gallery_sec, "🕒");
        } else if (diff < 60 * 60) {
            final int min = (int) (diff / 60);
            if (min == 1) {
                result = context.getString(R.string.gallery_min, "🕒");
            } else {
                result = context.getString(R.string.gallery_mins, "🕒 " + min);
            }
        } else if (diff < 60 * 60 * 24) {
            final int hour = (int) (diff / 3600);
            if (hour == 1) {
                result = context.getString(R.string.gallery_hour, "🕒");
            } else {
                result =
                        context.getString(R.string.gallery_hours, "🕒 " + hour);
            }
        } else if (isYesterday(timestamp)) {
            result =
                    context.getString(R.string.gallery_yesterday, "🕒 ",
                            DateFormat.format("HH:mm", timestamp));
        } else {
            result = "🕒 " + DateFormat.format("dd.MM HH:mm", timestamp);
        }
        return result;
    }

    private static boolean isYesterday(long timestamp) {
        Calendar cal = Calendar.getInstance();
        int currentDayNumber = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(timestamp);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        int givenDayNumber = cal.get(Calendar.DAY_OF_MONTH);
        return givenDayNumber == currentDayNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return gallery.getImageFiles().length == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#registerDataSetObserver(android.database.
     * DataSetObserver)
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (!this.observer.contains(observer)) {
            this.observer.add(observer);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#unregisterDataSetObserver(android.database.
     * DataSetObserver)
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.observer.remove(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.ListAdapter#areAllItemsEnabled()
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.ListAdapter#isEnabled(int)
     */
    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    /**
     * 
     */
    public void invalidate() {
        for (DataSetObserver o : this.observer) {
            o.onChanged();
        }
    }

    /**
     * 
     * 
     * @author tbrose
     */
    private static final class Layout {
        private final TextView text;
        private final ImageView image;

        private Layout(View text, View image) {
            super();
            this.text = (TextView) text;
            this.image = (ImageView) image;
        }
    }
}
