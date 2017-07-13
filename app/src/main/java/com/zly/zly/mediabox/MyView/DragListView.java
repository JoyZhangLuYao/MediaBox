package com.zly.zly.mediabox.MyView;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.zly.zly.mediabox.Adapter.DragListAdapter;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Utils.DragItemInfo;
import com.zly.zly.mediabox.bean.FileInfo;


/**
 * �Զ�����϶�Item����ListView
 *
 * @author zihao
 */
@SuppressLint({"NewApi", "HandlerLeak"})
public class DragListView extends ListView {

    private ImageView mDragImageView;// ����ק����(item)����ʵ����һ��ImageView
    private int mStartPosition;// ��ָ�϶���ԭʼ���б��е�λ��
    private int mDragPosition;// ��ָ���׼���϶���ʱ��,��ǰ�϶������б��е�λ��
    private int mLastPosition;// ��ָ���׼���϶���ʱ��,��ǰ�϶������б��е�λ��
    private int mDragPoint;// �ڵ�ǰ�������е�λ��
    private int mDragOffset;// ��ǰ��ͼ����Ļ�ľ���(����ֻʹ����y������)
    private int mUpScrollBounce;// �϶���ʱ�򣬿�ʼ���Ϲ����ı߽�
    private int mDownScrollBounce;// �϶���ʱ�򣬿�ʼ���¹����ı߽�
    private final static int mStep = 1;// ListView ��������
    private int mCurrentStep;// ��ǰ����
    private DragItemInfo mDragItemInfo;// ���ڴ��Item��Ϣ�Ķ���
    private int mItemVerticalSpacing = 0;// Item��ֱ����ռ�
    private int mHoldPosition;// ������ͣ����Position

    /**
     * windows���ڿ�����
     */
    private WindowManager mWindowManager;
    /**
     * ���ڿ�����ק�����ʾ�Ĳ���
     */
    private WindowManager.LayoutParams mWindowParams;
    /**
     * ֹͣ״̬
     */
    public static final int MSG_DRAG_STOP = 0x1001;
    /**
     * �ƶ�״̬
     */
    public static final int MSG_DRAG_MOVE = 0x1002;
    /**
     * ����ʱ��(һ�������ĺ�ʱ)
     */
    private static final int ANIMATION_DURATION = 200;
    /**
     * ��ʶ�Ƿ�����
     */
    private boolean isLock;
    /**
     * ��ʶ�Ƿ����ƶ�״̬
     */
    private boolean isMoving = false;
    /**
     * �Ƿ��϶�Item
     */
    private boolean isDragItemMoving = false;
    /**
     * ��ʶ�Ƿ��ȡ�����
     */
    private boolean bHasGetSapcing = false;
    //�϶���ɼ���
    private DragEndListener dragEndListener;

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mDragItemInfo = new DragItemInfo();
        init();
    }

    /**
     * ��ʼ��
     */
    private void init() {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * ������Ϣ����ɶ�Ӧ����
     */
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case MSG_DRAG_STOP:// ֹͣ
                    stopDrag();
                    onDrop(msg.arg1);
                    break;
                case MSG_DRAG_MOVE:// �ƶ�
                    onDrag(msg.arg1);
                    break;
            }

        }

        ;
    };

    /**
     * ��ȡ���--��ȡ���¹������
     */
    private void getSpacing() {
        bHasGetSapcing = true;

        mUpScrollBounce = getHeight() / 3;// ȡ�����Ϲ����ı߼ʣ����Ϊ�ÿؼ���1/3
        mDownScrollBounce = getHeight() * 2 / 3;// ȡ�����¹����ı߼ʣ����Ϊ�ÿؼ���2/3

        int[] firstTempLocation = new int[2];
        int[] secondTempLocation = new int[2];

        ViewGroup firstItemView = (ViewGroup) getChildAt(0);// ��һ��
        ViewGroup secondItemView = (ViewGroup) getChildAt(1);// �ڶ���

        if (firstItemView != null) {
            firstItemView.getLocationOnScreen(firstTempLocation);
        } else {
            return;
        }

        if (secondItemView != null) {
            secondItemView.getLocationOnScreen(secondTempLocation);
            mItemVerticalSpacing = Math.abs(secondTempLocation[1]
                    - firstTempLocation[1]);
        } else {
            return;
        }
    }

    /***
     * touch�¼����� �������ҽ�����Ӧ���أ�
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // ����
        if (ev.getAction() == MotionEvent.ACTION_DOWN && !isLock && !isMoving
                && !isDragItemMoving) {

            int x = (int) ev.getX();// ��ȡ�����ListView��x����
            int y = (int) ev.getY();// ��ȡ��Ӧ��ListView��y����
            mLastPosition = mStartPosition = mDragPosition = pointToPosition(x,
                    y);

            // ��Ч�����д���
            if (mDragPosition == AdapterView.INVALID_POSITION) {
                return super.onInterceptTouchEvent(ev);
            }

            if (false == bHasGetSapcing) {
                getSpacing();
            }

            // ��ȡ��ǰλ�õ���ͼ(�ɼ�״̬)
            ViewGroup dragger = (ViewGroup) getChildAt(mDragPosition
                    - getFirstVisiblePosition());

            DragListAdapter adapter = (DragListAdapter) getAdapter();

            mDragItemInfo.obj = adapter.getItem(mDragPosition
                    - getFirstVisiblePosition());

            // ��ȡ����dragPoint��ʵ����������ָ��item���еĸ߶�.
            mDragPoint = y - dragger.getTop();
            // ���ֵ�ǹ̶���:��ʵ����ListView����ؼ�����Ļ����ľ��루һ��Ϊ������+״̬����.
            mDragOffset = (int) (ev.getRawY() - y);

            // ��ȡ����ק��ͼ��
            View draggerIcon = dragger.findViewById(R.id.drag_item_image);
            if (draggerIcon.getVisibility() == View.VISIBLE) {// ֻ���ڰ�ťΪ�ɼ�������²������ƶ�

                // x > dragger.getLeft() - 20��仰Ϊ�˸��õĴ�����-20����ʡ�ԣ�
                if (draggerIcon != null && x > draggerIcon.getLeft() - 20) {

                    dragger.destroyDrawingCache();
                    dragger.setDrawingCacheEnabled(true);// ����cache.
                    dragger.setBackgroundResource(R.color.color_w_drag);
                    Bitmap bm = Bitmap.createBitmap(dragger.getDrawingCache(true));// ����cache����һ���µ�bitmap����.
                    hideDropItem();
                    adapter.setInvisiblePosition(mStartPosition);
                    adapter.notifyDataSetChanged();
                    startDrag(bm, y);// ��ʼ��Ӱ��
                    isMoving = false;

                    adapter.copyList();
                }

            }

            return false;

        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * ��ȡ�������Ŷ���
     *
     * @return
     */
    public Animation getScaleAnimation() {
        Animation scaleAnimation = new ScaleAnimation(0.0f, 0.0f, 0.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnimation.setFillAfter(true);
        return scaleAnimation;
    }

    /**
     * �����½���Item
     */
    private void hideDropItem() {
        final DragListAdapter adapter = (DragListAdapter) this.getAdapter();
        adapter.showDropItem(false);
    }

    /**
     * �����¼�����
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // item��view��Ϊ�գ��һ�ȡ��dragPosition��Ч
        if (mDragImageView != null && mDragPosition != INVALID_POSITION
                && !isLock) {

            int action = ev.getAction();
            switch (action) {

                case MotionEvent.ACTION_UP:
                    int upY = (int) ev.getY();
                    stopDrag();
                    onDrop(upY);

                    //TODO
                    final DragListAdapter adapter = (DragListAdapter) this.getAdapter();
                    List<FileInfo> list = adapter.getFinalList();

                    if (dragEndListener != null) dragEndListener.onDragEnd(list);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int moveY = (int) ev.getY();
                    onDrag(moveY);
                    itemMoveAnimation(moveY);
                    break;
                case MotionEvent.ACTION_DOWN:
                    break;

            }

            return true;// ȡ��ListView����.

        }

        return super.onTouchEvent(ev);
    }

    /**
     * �Ƿ�Ϊ��ͬ�����϶��ı��
     */
    private boolean isSameDragDirection = true;
    /**
     * �ƶ�����ı�ǣ�-1ΪĬ��ֵ��0��ʾ�����ƶ���1��ʾ�����ƶ�
     */
    private int lastFlag = -1;
    private int mFirstVisiblePosition, mLastVisiblePosition;// ��һ�������һ����λ��
    private int turnUpPosition, turnDownPosition;// ���ϡ��µ�λ��

    /**
     * ��̬�ı�Item����
     *
     * @param last    // ���һ���λ��
     * @param current // ��ǰλ��
     */
    private void onChangeCopy(int last, int current) {

        DragListAdapter adapter = (DragListAdapter) getAdapter();
        if (last != current) {// �ж��Ƿ��ƶ������һ��
            adapter.exchangeCopy(last, current);
        }

    }

    /**
     * Item�ƶ�����
     *
     * @param y
     */
    private void itemMoveAnimation(int y) {

        final DragListAdapter adapter = (DragListAdapter) getAdapter();
        int tempPosition = pointToPosition(0, y);

        if (tempPosition == INVALID_POSITION || tempPosition == mLastPosition) {
            return;
        }

        mFirstVisiblePosition = getFirstVisiblePosition();
        mDragPosition = tempPosition;
        onChangeCopy(mLastPosition, mDragPosition);
        int MoveNum = tempPosition - mLastPosition;// �����ƶ���--�ƶ�����
        int count = Math.abs(MoveNum);

        for (int i = 1; i <= count; i++) {
            int xAbsOffset, yAbsOffset;
            // �����϶�
            if (MoveNum > 0) {

                if (lastFlag == -1) {
                    lastFlag = 0;
                    isSameDragDirection = true;
                }

                if (lastFlag == 1) {
                    turnUpPosition = tempPosition;
                    lastFlag = 0;
                    isSameDragDirection = !isSameDragDirection;
                }

                if (isSameDragDirection) {
                    mHoldPosition = mLastPosition + 1;
                } else {
                    if (mStartPosition < tempPosition) {
                        mHoldPosition = mLastPosition + 1;
                        isSameDragDirection = !isSameDragDirection;
                    } else {
                        mHoldPosition = mLastPosition;
                    }
                }

                xAbsOffset = 0;
                yAbsOffset = -mItemVerticalSpacing;
                mLastPosition++;

            } else {// �����϶�

                if (lastFlag == -1) {
                    lastFlag = 1;
                    isSameDragDirection = true;
                }

                if (lastFlag == 0) {
                    turnDownPosition = tempPosition;
                    lastFlag = 1;
                    isSameDragDirection = !isSameDragDirection;
                }

                if (isSameDragDirection) {
                    mHoldPosition = mLastPosition - 1;
                } else {

                    if (mStartPosition > tempPosition) {
                        mHoldPosition = mLastPosition - 1;
                        isSameDragDirection = !isSameDragDirection;
                    } else {
                        mHoldPosition = mLastPosition;
                    }

                }

                xAbsOffset = 0;
                yAbsOffset = mItemVerticalSpacing;
                mLastPosition--;

            }

            adapter.setHeight(mItemVerticalSpacing);
            adapter.setIsSameDragDirection(isSameDragDirection);
            adapter.setLastFlag(lastFlag);

            ViewGroup moveView = (ViewGroup) getChildAt(mHoldPosition
                    - getFirstVisiblePosition());

            Animation animation;
            if (isSameDragDirection) {// ��ͬ�����϶�
                animation = getFromSelfAnimation(xAbsOffset, yAbsOffset);
            } else {// ����ͬ�����϶�
                animation = getToSelfAnimation(xAbsOffset, -yAbsOffset);
            }
            // ���ö�Ӧ�Ķ���
            moveView.startAnimation(animation);

        }
    }

    private void onDrop(int x, int y) {
        final DragListAdapter adapter = (DragListAdapter) getAdapter();
        adapter.setInvisiblePosition(-1);
        adapter.showDropItem(true);
        adapter.notifyDataSetChanged();
    }

    /**
     * ׼���϶�����ʼ���϶����ͼ��
     *
     * @param bm
     * @param y
     */
    private void startDrag(Bitmap bm, int y) {
        /***
         * ��ʼ��window.
         */
        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPoint + mDragOffset;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE// �����ȡ����
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE// ������ܴ����¼�
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON// �����豸���������������Ȳ��䡣
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;// ����ռ��������Ļ��������Χ��װ�α߿�����״̬�������˴����迼�ǵ�װ�α߿�����ݡ�

        // windowParams.format = PixelFormat.TRANSLUCENT;// Ĭ��Ϊ��͸�����������͸��Ч��.
        mWindowParams.windowAnimations = 0;// ������ʹ�õĶ�������

        mWindowParams.alpha = 0.8f;
        mWindowParams.format = PixelFormat.TRANSLUCENT;

        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bm);

        mWindowManager.addView(imageView, mWindowParams);
        mDragImageView = imageView;
    }

    /**
     * �϶�ִ�У���Move������ִ��
     *
     * @param y
     */
    public void onDrag(int y) {
        int drag_top = y - mDragPoint;// ��קview��topֵ���ܣ�0�����������.
        if (mDragImageView != null && drag_top >= 0) {
            mWindowParams.alpha = 1.0f;
            mWindowParams.y = y - mDragPoint + mDragOffset;
            mWindowManager.updateViewLayout(mDragImageView, mWindowParams);// ʱʱ�ƶ�.
        }

        doScroller(y);// listview�ƶ�.
    }

    /***
     * ListView���ƶ�.
     * Ҫ�����ƶ�ԭ�������ƶ����¶˵�ʱ��ListView���ϻ����������ƶ����϶˵�ʱ��ListViewҪ���»��������ú�ʵ�ʵ��෴.
     *
     */
    public void doScroller(int y) {
        // ListView��Ҫ�»�
        if (y < mUpScrollBounce) {
            mCurrentStep = mStep + (mUpScrollBounce - y) / 10;// ʱʱ����
        }// ListView��Ҫ�ϻ�
        else if (y > mDownScrollBounce) {
            mCurrentStep = -(mStep + (y - mDownScrollBounce)) / 10;// ʱʱ����
        } else {
            mCurrentStep = 0;
        }

        // ��ȡ����ק������λ�ü���ʾitem��Ӧ��view�ϣ�ע������ʾ���֣���position��
        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        // ���������ķ���setSelectionFromTop()
        setSelectionFromTop(mDragPosition, view.getTop() + mCurrentStep);

    }

    /**
     * ֹͣ�϶���ɾ��Ӱ��
     */
    public void stopDrag() {
        isMoving = false;

        if (mDragImageView != null) {
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }

        isSameDragDirection = true;
        lastFlag = -1;
        DragListAdapter adapter = (DragListAdapter) getAdapter();
        adapter.setLastFlag(lastFlag);
        adapter.pastList();
    }

    /**
     * �϶����µ�ʱ��
     *
     * @param y
     */
    public void onDrop(int y) {
        onDrop(0, y);
    }

    /**
     * ��ȡ������ֵĶ���
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
        translateAnimation.setDuration(ANIMATION_DURATION);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        return translateAnimation;
    }

    /**
     * ��ȡ�����뿪�Ķ���
     *
     * @param x
     * @param y
     * @return
     */
    private Animation getToSelfAnimation(int x, int y) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0,
                Animation.ABSOLUTE, y, Animation.RELATIVE_TO_SELF, 0);
        translateAnimation
                .setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(ANIMATION_DURATION);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        return translateAnimation;
    }

    public interface DragEndListener {
        void onDragEnd(List<FileInfo> list);
    }

    public void setDragEndListener(DragEndListener dragEndListener) {
        this.dragEndListener = dragEndListener;
    }

}