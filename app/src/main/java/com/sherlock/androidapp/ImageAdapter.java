package com.sherlock.androidapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ItemData> itemDataArrayList;

    public ImageAdapter(Context context, ArrayList<ItemData> itemDataArrayList) {
        mContext = context;
        this.itemDataArrayList = itemDataArrayList;
    }

    @Override
    public int getCount() {
        return itemDataArrayList.size();
    }

    @Override
    public ItemData getItem(int position) {
        return itemDataArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_image, parent, false);

        ItemData itemData = itemDataArrayList.get(position);

        ImageView itemImage = listItem.findViewById(R.id.itemImage);
        CommonUtils.loadImage(mContext, itemData.getItemPhoto(), new RequestOptions(), itemImage);

        TextView itemName = listItem.findViewById(R.id.itemName);
        itemName.setText(itemData.getItemName());

        TextView timestamp = listItem.findViewById(R.id.itemTime);

        DateTime time = DateTime.parse(itemData.getTimestamp());
        if (CommonUtils.isToday(time)) {
            DateTimeFormatter build = DateTimeFormat.forPattern("hh:mm a");
            timestamp.setText(build.print(time));
        } else if (CommonUtils.isYesterday(time)) {
            timestamp.setText("YESTERDAY");
        } else {
            DateTimeFormatter build = DateTimeFormat.forPattern("dd/MM/yy");
            timestamp.setText(build.print(time));
        }

        return listItem;
    }
}
