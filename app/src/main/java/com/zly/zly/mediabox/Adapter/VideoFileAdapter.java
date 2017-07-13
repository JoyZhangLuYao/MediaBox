package com.zly.zly.mediabox.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.bean.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 2017/3/31.
 */

public class VideoFileAdapter extends BaseAdapter {
    Context context;
    MyApplication myApplication;
    LayoutInflater layoutInflater;
    List<FileInfo> list = new ArrayList<>();
    static onLove onLove;
    int playNumber = -1;
    int currentP;

    public VideoFileAdapter(Context context, List<FileInfo> list, MyApplication myApplication) {
        this.context = context;
        this.myApplication = myApplication;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public int getCurrentP() {
        return currentP;
    }

    public void setCurrentP(int currentP) {
        this.currentP = currentP;
    }

    public List<FileInfo> getList() {
        return list;
    }

    public void setList(List<FileInfo> list) {
        this.list = list;
    }

    public int getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(int playNumber) {
        this.playNumber = playNumber;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public FileInfo getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public VideoFileAdapter.onLove getOnLove() {
        return onLove;
    }

    public void setOnLove(VideoFileAdapter.onLove onLove) {
        this.onLove = onLove;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.file_item, viewGroup, false);
            viewHolder = new ViewHolder((ImageView) view.findViewById(R.id.play_mark), (TextView) view.findViewById(R.id.name), (ImageView) view.findViewById(R.id.love), (RelativeLayout) view.findViewById(R.id.play_bg));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final FileInfo fileInfo = getItem(i);
        viewHolder.name.setText(fileInfo.getName().trim());
        if (fileInfo.getClickMark() || playNumber == fileInfo.getFileNumber()) {
            viewHolder.playMark.setVisibility(View.VISIBLE);
            viewHolder.bg.setBackgroundColor(context.getResources().getColor(R.color.play_bg));
            currentP=i;
        } else {
            viewHolder.playMark.setVisibility(View.INVISIBLE);
            viewHolder.bg.setBackgroundColor(context.getResources().getColor(R.color.color_w));
        }
        if (fileInfo.getLove()) {
           // viewHolder.love.setBackgroundResource(R.mipmap.uncollect);
            viewHolder.love.setImageResource(R.mipmap.uncollect);
            viewHolder.love.setTag(R.mipmap.love_on);
        } else {
          //  viewHolder.love.setBackgroundResource(R.mipmap.collect);
            viewHolder.love.setImageResource(R.mipmap.collect);
            viewHolder.love.setTag(R.mipmap.love);
        }
        viewHolder.love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onLove != null) {
                    if (myApplication.isLoveOK()) {

                        if ((int) viewHolder.love.getTag() == R.mipmap.love_on) {
                            // viewHolder.love.setBackgroundResource(R.mipmap.love);
                            // viewHolder.love.setTag(R.mipmap.love);
                            // VideoFileAdapter.onLove.love(fileInfo, false);
                        } else if ((int) viewHolder.love.getTag() == R.mipmap.love) {
                            //  viewHolder.love.setBackgroundResource(R.mipmap.love_on);
                            viewHolder.love.setTag(R.mipmap.love_on);
                            VideoFileAdapter.onLove.love(fileInfo, true);
                        }
                    } else {
                        MyToast.makeToast(context, -1, "当前设备模式不支持此操作", 800);
                    }
                }
            }
        });


        return view;
    }

    class ViewHolder {
        public ViewHolder(ImageView playMark, TextView name, ImageView love, RelativeLayout bg) {
            this.playMark = playMark;
            this.name = name;
            this.love = love;
            this.bg = bg;
        }

        ImageView playMark;
        TextView name;
        ImageView love;
        RelativeLayout bg;
    }

    public interface onLove {
        void love(FileInfo fileInfo, boolean isLove);
    }
}
