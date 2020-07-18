package com.example.note_gabru_android;


public class ListViewAdaptor extends ArrayAdapter {

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
