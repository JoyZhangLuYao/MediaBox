package com.zly.zly.mediabox.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Utils.MusicLoader;

import java.util.List;


public class PhoneMusicAdapter extends BaseAdapter {
    /**
     * 上下文对象
     */
    private Context mContext = null;

    /**
     *
     */
    private int mRightWidth = 0;

    /**
     * 单击事件监听器
     */


    private List<MusicLoader.MusicInfo> musicList;

    private int clickPosition = -1;
    private long clickId;


    public PhoneMusicAdapter(Context ctx, List<MusicLoader.MusicInfo> musicList) {
        this.mContext = ctx;
        this.musicList = musicList;

    }

    public long getClickId() {
        return clickId;
    }

    public void setClickId(long clickId) {
        this.clickId = clickId;
    }

    public List<MusicLoader.MusicInfo> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<MusicLoader.MusicInfo> musicList) {
        this.musicList = musicList;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    public void setClickPosition(int clickPosition) {
        this.clickPosition = clickPosition;
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return musicList.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        String title;
        //   String artist;

        final int thisPosition = position;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.file_item, parent, false);

            viewHolder = new ViewHolder((ImageView) view.findViewById(R.id.play_mark), (TextView) view.findViewById(R.id.name), (ImageView) view.findViewById(R.id.love), (RelativeLayout) view.findViewById(R.id.play_bg));
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        //      viewHolder.titleId.setText("" + (position + 1) + ".");
        viewHolder.love.setVisibility(View.GONE);
        if (musicList.get(position).getTitle().length() >= 15) {
            title = musicList.get(position).getTitle().substring(0, 10) + "...";
        } else {
            title = musicList.get(position).getTitle();
        }
        viewHolder.name.setText(title);

       /* viewHolder.duration.setText(FormatHelper.formatDuration(musicList.get(
                position).getDuration()));*/

        Log.e("PhoneMusicAdapter", "clickPosition:" + clickPosition + "=============position:" + position);
        if (position == clickPosition) {
            viewHolder.playMark.setVisibility(View.VISIBLE);
            viewHolder.bg.setBackgroundColor(mContext.getResources().getColor(R.color.play_bg));
        } else {
            viewHolder.playMark.setVisibility(View.INVISIBLE);
            viewHolder.bg.setBackgroundColor(mContext.getResources().getColor(R.color.color_w));
        }
        /*if (musicList.get(position).getId()==clickId) {
            viewHolder.playMark.setVisibility(View.VISIBLE);
            viewHolder.bg.setBackgroundColor(mContext.getResources().getColor(R.color.play_bg));
        } else {
            viewHolder.playMark.setVisibility(View.INVISIBLE);
            viewHolder.bg.setBackgroundColor(mContext.getResources().getColor(R.color.color_w));
        }*/
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
}
