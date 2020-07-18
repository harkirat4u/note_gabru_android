package com.example.note_gabru_android;


public class ListViewAdaptor extends ArrayAdapter {


import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

    Context mContext;
    int layoutRes;
    List<String> categories;
    public ListViewAdaptor(@NonNull Context context, int resource,List<String> category) {
        super(context, resource,category);
        this.mContext = context;
        this.layoutRes = resource;
        this.categories = category;
    }
}
