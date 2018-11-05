package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private ArrayList<ListViewItem> listViewItemList=new ArrayList<ListViewItem>();

    public ListViewAdapter(Context context) {
        this.mContext = context;
    }
    @Override
    public int getCount() {
        return listViewItemList.size();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos=position;
        final Context context=parent.getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);


        if(convertView==null) {
            LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(!sharedPref.getBoolean("blackTheme",false))
                convertView=inflater.inflate(R.layout.listview_layout,parent,false);
            else
                convertView=inflater.inflate(R.layout.listview_layout_dark,parent,false);
        }
        ImageView iconImageView=(ImageView) convertView.findViewById(R.id.image1);
        TextView text1=(TextView) convertView.findViewById(R.id.text1);
        TextView text2=(TextView) convertView.findViewById(R.id.text2);

        ListViewItem listViewItem=listViewItemList.get(position);

        if(!listViewItem.getIcon().isEmpty()) {
            Picasso.with(mContext)
                    .load(listViewItem.getIcon())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .resize(300, 450)
                    .into(iconImageView);
        }
        //iconImageView.setImageDrawable(listViewItem.getIcon());
        text1.setText(listViewItem.getText1());
        text2.setText(listViewItem.getText2());

        return convertView;
    }
    @Override
    public long getItemId(int position)
    {
        return position;
    }
    @Override
    public Object getItem(int position)
    {
        return listViewItemList.get(position);
    }

    public void addItem(String icon,String text1,String text2,String url) {
        ListViewItem item=new ListViewItem();

        item.setIcon(icon);
        item.setText1(text1);
        item.setText2(text2);
        item.setUrl(url);

        listViewItemList.add(item);
    }
}
