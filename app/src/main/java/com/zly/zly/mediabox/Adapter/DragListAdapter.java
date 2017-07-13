package com.zly.zly.mediabox.Adapter;

import java.util.ArrayList;
import java.util.List;


import com.anye.greendao.gen.FileInfoDao;
import com.anye.greendao.gen.MusicFileInfoDao;
import com.anye.greendao.gen.PhotoFileInfoDao;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.MusicFileInfo;
import com.zly.zly.mediabox.bean.PhotoFileInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/***
 * �Զ������קListView������
 *
 * @author zihao
 *
 */
public class DragListAdapter extends BaseAdapter {
    private List<FileInfo> mDataList;// ��������
    private Context mContext;
    boolean isEdit = false;
    private int currentMain = -1;
    MyApplication myApplication;

    public List<FileInfo> getmDataList() {
        return mDataList;
    }

    public void setmDataList(List<FileInfo> mDataList) {
        this.mDataList = mDataList;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    /**
     * DragListAdapter���췽��
     *
     * @param context  // �����Ķ���
     * @param dataList // ���ݼ���
     */


    public DragListAdapter(Context context, List<FileInfo> dataList, int currentMain, MyApplication myApplication) {
        this.mContext = context;
        this.mDataList = dataList;
        this.currentMain = currentMain;
        this.myApplication = myApplication;
    }

    /**
     * �����Ƿ���ʾ�½���Item
     *
     * @param showItem
     */
    public void showDropItem(boolean showItem) {
        this.mShowItem = showItem;
    }

    /**
     * ���ò��ɼ����λ�ñ��
     *
     * @param position
     */
    public void setInvisiblePosition(int position) {
        mInvisilePosition = position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        /***
         * �����ﾡ����ÿ�ζ�����ʵ�����µģ���������קListView��ʱ�򲻻���ִ���.
         * ����ԭ���������������������ԣ�Ŀǰû�з��ִ��ҡ���˵Ч�ʲ��ߣ���������קLisView�㹻�ˡ�
         */
        convertView = LayoutInflater.from(mContext).inflate(
                R.layout.love_item_layout, null);

        initItemView(position, convertView);

        TextView titleTv = (TextView) convertView.findViewById(R.id.love_name);
        ImageView delete = (ImageView) convertView.findViewById(R.id.delete_love);
        ImageView playMark = (ImageView) convertView.findViewById(R.id.play_mark);
        RelativeLayout playBg = (RelativeLayout) convertView.findViewById(R.id.play_bg);
        ImageView drag = (ImageView) convertView.findViewById(R.id.drag_item_image);

        //删除Item
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileInfo fileInfo = mDataList.get(position);
                mDataList.remove(fileInfo);
                notifyDataSetChanged();
                switch (currentMain) {
                    case 1:
                        FileInfoDao fileInfoDao = myApplication.getFileInfoDao();
                        FileInfo file = fileInfoDao.queryBuilder().where(FileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                        if (file != null) {
                            file.setLove(false);
                            fileInfoDao.update(file);
                        }
                        break;
                    case 2:
                        MusicFileInfoDao musicFileInfoDao = myApplication.getMusicFileInfoDao();
                        MusicFileInfo musicFileInfo = musicFileInfoDao.queryBuilder().where(MusicFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                        if (musicFileInfo != null) {
                            musicFileInfo.setLove(false);
                            musicFileInfoDao.update(musicFileInfo);
                        }
                        break;
                    case 3:
                        PhotoFileInfoDao photoFileInfoDao = myApplication.getPhotoFileInfoDao();
                        PhotoFileInfo photoFileInfo = photoFileInfoDao.queryBuilder().where(PhotoFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                        if (photoFileInfo != null) {
                            photoFileInfo.setLove(false);
                            photoFileInfoDao.update(photoFileInfo);
                        }

                        break;
                }
            }
        });


        titleTv.setText(mDataList.get(position).getName());


        if (mDataList.get(position).getClickMark()) {
            convertView.setBackgroundResource(R.color.play_bg);
            playMark.setVisibility(View.VISIBLE);
        } else {
            convertView.setBackgroundResource(R.color.color_w);
            playMark.setVisibility(View.INVISIBLE);
        }
        if (isEdit) {
            drag.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        } else {
            drag.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }

        if (isChanged) {// �ж��Ƿ����˸ı�

            if (position == mInvisilePosition) {

                if (!mShowItem) {// ����ק���̲�������ʾ��״̬�£�����Item��������

                    // ��Ϊitem����Ϊ��ɫ���ʶ�������Ҫ����Ϊȫ͸��ɫ��ֹ�а�ɫ�ڵ����⣨������ק��
                    convertView.findViewById(R.id.drag_item_layout)
                            .setBackgroundColor(0x0000000000);

                    // ����Item���������
                    int vis = View.INVISIBLE;
                    convertView.findViewById(R.id.drag_item_image)
                            .setVisibility(vis);
                    /*convertView.findViewById(R.id.drag_item_close_layout)
                            .setVisibility(vis);*/
                    titleTv.setVisibility(vis);

                }

            }

            if (mLastFlag != -1) {

                if (mLastFlag == 1) {

                    if (position > mInvisilePosition) {
                        Animation animation;
                        animation = getFromSelfAnimation(0, -mHeight);
                        convertView.startAnimation(animation);
                    }

                } else if (mLastFlag == 0) {

                    if (position < mInvisilePosition) {
                        Animation animation;
                        animation = getFromSelfAnimation(0, mHeight);
                        convertView.startAnimation(animation);
                    }

                }

            }
        }

        return convertView;
    }

    /**
     * ��ʼ��Item��ͼ
     *
     * @param convertView
     */
    private void initItemView(final int position, final View convertView) {

        if (convertView != null) {
            // ���ö�Ӧ�ļ���
			/*convertView.findViewById(R.id.drag_item_close_layout)
					.setOnClickListener(new OnClickListener() {// ɾ��

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									removeItem(position);
								}
							});*/
        }

    }

    private int mInvisilePosition = -1;// ������ǲ��ɼ�Item��λ��
    private boolean isChanged = true;// ��ʶ�Ƿ����ı�
    private boolean mShowItem = false;// ��ʶ�Ƿ���ʾ��קItem������

    /***
     * ��̬�޸�ListView�ķ�λ.
     *
     * @param startPosition
     *            ����ƶ���position
     * @param endPosition
     *            �ɿ�ʱ���position
     */
	/*public void exchange(int startPosition, int endPosition) {
		Object startObject = getItem(startPosition);

		if (startPosition < endPosition) {
			mDataList.add(endPosition + 1, (String) startObject);
			mDataList.remove(startPosition);
		} else {
			mDataList.add(endPosition, (String) startObject);
			mDataList.remove(startPosition + 1);
		}

		isChanged = true;
	}*/

    /**
     * ��̬�޸�Item����
     *
     * @param startPosition // ��ʼ��λ��
     * @param endPosition   // ��ǰͣ����λ��
     */
    public void exchangeCopy(int startPosition, int endPosition) {
        Object startObject = getCopyItem(startPosition);

        if (startPosition < endPosition) {// �����ƶ�
            mCopyList.add(endPosition + 1, (FileInfo) startObject);
            mCopyList.remove(startPosition);
        } else {// �����϶����߲���
            mCopyList.add(endPosition, (FileInfo) startObject);
            mCopyList.remove(startPosition + 1);
        }

        isChanged = true;
    }

    /**
     * ɾ��ָ����Item
     *
     * @param pos // Ҫɾ�����±�
     */
    private void removeItem(int pos) {
        if (mDataList != null && mDataList.size() > pos) {
            mDataList.remove(pos);
            this.notifyDataSetChanged();
        }
    }

    /**
     * ��ȡ����(��ק)Item��
     *
     * @param position
     * @return
     */
    public Object getCopyItem(int position) {
        return mCopyList.get(position);
    }

    /**
     * ��ȡItem����
     */
    @Override
    public int getCount() {
        return mDataList.size();
    }

    /**
     * ��ȡListView��Item��
     */
    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * ����϶���
     *
     * @param start // Ҫ������ӵ�λ��
     * @param obj
     */
    public void addDragItem(int start, Object obj) {
        mDataList.remove(start);// ɾ������
        mDataList.add(start, (FileInfo) obj);// ���ɾ����
    }

    private ArrayList<FileInfo> mCopyList = new ArrayList<FileInfo>();

    //获取修改后的List
    public List<FileInfo> getFinalList() {
        //return mCopyList;
        return mDataList;
    }

    public void copyList() {
        mCopyList.clear();
        for (FileInfo str : mDataList) {
            mCopyList.add(str);
        }
    }

    public void pastList() {
        mDataList.clear();
        for (FileInfo str : mCopyList) {
            mDataList.add(str);
        }
    }

    private boolean isSameDragDirection = true;// �Ƿ�Ϊ��ͬ�����϶��ı��
    private int mLastFlag = -1;
    private int mHeight;
    private int mDragPosition = -1;

    /**
     * �����Ƿ�Ϊ��ͬ�����϶��ı��
     *
     * @param value
     */
    public void setIsSameDragDirection(boolean value) {
        isSameDragDirection = value;
    }

    /**
     * �����϶�������
     *
     * @param flag
     */
    public void setLastFlag(int flag) {
        mLastFlag = flag;
    }

    /**
     * ���ø߶�
     *
     * @param value
     */
    public void setHeight(int value) {
        mHeight = value;
    }

    /**
     * ���õ�ǰ�϶�λ��
     *
     * @param position
     */
    public void setCurrentDragPosition(int position) {
        mDragPosition = position;
    }

    /**
     * ��������ֵĶ���
     *
     * @param x
     * @param y
     * @return
     */
    private Animation getFromSelfAnimation(int x, int y) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, x,
                Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y);
        translateAnimation
                .setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(100);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        return translateAnimation;
    }


    public void upDateItem(FileInfo iteminfo) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getFileNumber() == iteminfo.getFileNumber()) {
                mDataList.remove(mDataList.get(i));
                mDataList.add(i, iteminfo);
            } else {
                mDataList.get(i).setClickMark(false);
            }

        }
    }

}