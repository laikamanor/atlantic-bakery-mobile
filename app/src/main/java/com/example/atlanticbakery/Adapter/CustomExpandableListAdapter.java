package com.example.atlanticbakery.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.atlanticbakery.R;

import java.util.List;
import java.util.Map;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listTitle;
    private Map <String,List<String>> listItem;
    private int totalCount = 0,
            sapCount = 0, transferCount = 0,
            offlineCount = 0;

    public CustomExpandableListAdapter(Context context, List<String> listTitle, Map<String, List<String>> listItem, int totalCount, int sapCount, int transferCount, int offlineCount) {
        this.context = context;
        this.listTitle = listTitle;
        this.listItem = listItem;
        this.totalCount = totalCount;
        this.transferCount = transferCount;
        this.sapCount = sapCount;
        this.offlineCount = offlineCount;
    }

    @Override
    public int getGroupCount() {
        return listTitle.size();

    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listItem.get(this.listTitle.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listTitle.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listItem.get(listTitle.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String title = (String)getGroup(groupPosition);
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group,null);
        }
        TextView txtTitle = (TextView)convertView.findViewById(R.id.listTitle);
        txtTitle.setTypeface(null, Typeface.BOLD);

//        if(title.contains("Inventory")) {
//            txtTitle.setText(title + " (" + (sapCount + transferCount) + ")");
//        }
         if(title.equals("Settings")){
            txtTitle.setText(title + " (" + offlineCount + ")");
        }
        else {
            txtTitle.setText(title);
        }
        return convertView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String title = (String)getChild(groupPosition,childPosition);
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item,null);
        }
        TextView txtChild = (TextView)convertView.findViewById(R.id.expandabledListItem);
        txtChild.setTypeface(null, Typeface.BOLD);
//        if(title.contains("Received from System Transfer Item")) {
//            txtChild.setText(title + " (" + transferCount + ")");
//        }
//        else if(title.contains("Received from SAP")) {
//            txtChild.setText(title + " (" + sapCount + ")");
//        }
         if(title.contains("Offline Pending Transactions")) {
            txtChild.setText(title + " (" + offlineCount + ")");
        }
        else {
            txtChild.setText(title);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
