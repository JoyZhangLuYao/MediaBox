package com.zly.zly.mediabox.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.bean.MyDevice;

import java.util.List;

/**
 * Created by Tim on 2017/3/30.
 */

public class DeviceAdapter extends BaseAdapter {
    List<MyDevice> list;
    Context context;
    LayoutInflater inflater;

    public DeviceAdapter(List<MyDevice> list, Context context) {
        this.list = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public List<MyDevice> getList() {
        return list;
    }

    public void setList(List<MyDevice> list) {
        this.list = list;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MyDevice getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;
        MyDevice myDevice = list.get(i);
        if (view == null) {
            view = inflater.inflate(R.layout.device_item, viewGroup, false);
            viewHolder = new ViewHolder((TextView) view.findViewById(R.id.device_name), (ImageView) view.findViewById(R.id.link_ok));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvName.setText(myDevice.getBluetoothDevice().getName());
        if (myDevice.isLink()) {
            viewHolder.islink.setVisibility(View.VISIBLE);
        } else {
            viewHolder.islink.setVisibility(View.GONE);
        }


        return view;
    }

    class ViewHolder {
        TextView tvName;
        ImageView islink;

        public ViewHolder(TextView tvName, ImageView islink) {
            this.tvName = tvName;
            this.islink = islink;
        }
    }
}
