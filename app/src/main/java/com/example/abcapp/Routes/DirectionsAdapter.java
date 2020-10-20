package com.example.abcapp.Routes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.abcapp.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsAdapter extends BaseExpandableListAdapter {
    Context context;
    ArrayList<String> listNames; // the list of keys for the hashmap
    HashMap<String, List<String>> itemMap;
    int currentDirection = -1;

    public DirectionsAdapter(Context context,
                             HashMap<String, List<String>> itemMap,
                             int currentDirection) {
        this.context = context;
        this.itemMap = itemMap;
        this.currentDirection = currentDirection;
        this.listNames = new ArrayList<String>();

        for (String listName: itemMap.keySet()) {
            this.listNames.add(listName);
        }
    }

    public int getCurrentDirection() {
        return this.currentDirection;
    }

    public void setCurrentDirection(int newDirection) {
        this.currentDirection = newDirection;
    }

    @Override
    public int getGroupCount() {
        return this.listNames.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.itemMap.get(this.listNames.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.itemMap.get(this.listNames.get(i));
    }

    @Override
    public String getChild(int groupIndex, int childIndex) {
        List<String> group = this.itemMap.get(this.listNames.get(groupIndex));
        return group.get(childIndex);
    }

    @Override
    public long getGroupId(int groupIndex) {
        if (groupIndex > 0 && groupIndex < this.listNames.size()) {
            return groupIndex;
        } else {
            return -1;
        }
    }

    @Override
    public long getChildId(int groupIndex, int childIndex) {
        if (groupIndex > 0 && groupIndex < this.listNames.size()) {
            List<String> group = this.itemMap.get(this.listNames.get(groupIndex));
            if (childIndex > 0 && childIndex < group.size()) {
                return childIndex;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupIndex, boolean b, View view, ViewGroup viewGroup) {
        String groupName = this.listNames.get(groupIndex);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.group_layout, null);
        }

        TextView textView = view.findViewById(R.id.group_parent);
        textView.setText(groupName);
        return view;
    }

    @Override
    public View getChildView(int groupIndex, int childIndex, boolean b, View view, ViewGroup viewGroup) {
        String childName = getChild(groupIndex, childIndex);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_layout, null);
        }

        TextView textView = view.findViewById(R.id.group_child);
        textView.setText(childName);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
