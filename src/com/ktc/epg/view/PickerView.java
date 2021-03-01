package com.ktc.epg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.ktc.epg.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class PickerView extends View {

    public static final String TAG = "PickerView";
    /**
     * text之间间距和minTextSize之比
     */
    public static final float MARGIN_ALPHA = 2.8f;
    /**
     * 自动回滚到中间的速度
     */
    public static final float SPEED = 2;

    private List<String> mDataList;
    /**
     * 选中的位置，这个位置是mDataList的中心位置，一直不变
     */
    private int mCurrentSelected;
    private Paint mPaint, mCenterPaint;

    private float mMaxTextSize = 80;
    private float mMinTextSize = 62;

    private float mCenterTextSize = 42;
    private float mTextSize = 30;

    private float mMaxTextAlpha = 255;
    private float mMinTextAlpha = 120;

    private int mColorText = 0xfffffff;
    private int mCenterColorText = 0xfefefe;
    private int mFocusedTextColor = 0x30f3ff;

    private int mViewHeight;
    private int mViewWidth;

    private float mLastDownY;
    /**
     * 滑动的距离
     */
    private float mMoveLen = 0;

    private onSelectListener mSelectListener;
    private Timer timer;
    private MyTimerTask mTask;

    private UpdateHandler updateHandler = new UpdateHandler(this);

    static class UpdateHandler extends Handler {
        WeakReference<PickerView> mPickerNumberViewWeakReference;
        PickerView mPickerView;

        UpdateHandler(PickerView pickerView) {
            mPickerNumberViewWeakReference = new WeakReference<>(pickerView);
            mPickerView = mPickerNumberViewWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (Math.abs(mPickerView.mMoveLen) < SPEED) {
                mPickerView.mMoveLen = 0;
                if (mPickerView.mTask != null) {
                    mPickerView.mTask.cancel();
                    mPickerView.mTask = null;
                    mPickerView.performSelect();
                }
            } else
                // 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                mPickerView.mMoveLen = mPickerView.mMoveLen - mPickerView.mMoveLen
                        / Math.abs(mPickerView.mMoveLen) * SPEED;
            mPickerView.invalidate();
        }
    }

    public PickerView(Context context) {
        super(context);
        init(context, null);
    }

    public PickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setOnSelectListener(onSelectListener listener) {
        mSelectListener = listener;
    }

    private void performSelect() {
        if (mSelectListener != null)
            mSelectListener.onSelect(mDataList.get(mCurrentSelected));
    }

    public void setData(List<String> datas) {
        mDataList = datas;
        invalidate();
    }

    public void setSelected(int selected) {
        mCurrentSelected = selected;
        int distance = mDataList.size() / 2 - mCurrentSelected;
        if (distance < 0) {
            for (int i = 0; i < -distance; i++) {
                moveHeadToTail();
                mCurrentSelected--;
            }
        } else if (distance > 0) {
            for (int i = 0; i < distance; i++) {
                moveTailToHead();
                mCurrentSelected++;
            }
        }

    }

    private void moveHeadToTail() {
        String head = mDataList.get(0);
        mDataList.remove(0);
        mDataList.add(head);
    }

    private void moveTailToHead() {
        String tail = mDataList.get(mDataList.size() - 1);
        mDataList.remove(mDataList.size() - 1);
        mDataList.add(0, tail);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();
        // 按照View的高度计算字体大小
        mMaxTextSize = mViewHeight / 4.0f;
        mMinTextSize = mMaxTextSize / 2f;
        invalidate();
    }

    private void init(Context mContext, AttributeSet attrs) {
        timer = new Timer();
        mDataList = new ArrayList<>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAlpha(255);
        if (attrs != null) {
            TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.PickerView);
            mMaxTextSize = ta.getDimensionPixelSize(R.styleable.PickerView_mMaxTextSize, Integer.MAX_VALUE);
            mMinTextSize = ta.getDimensionPixelSize(R.styleable.PickerView_mMinTextSize, Integer.MAX_VALUE);
            mCenterTextSize = ta.getDimensionPixelSize(R.styleable.PickerView_mCenterTextSize, Integer.MAX_VALUE);
            mTextSize = ta.getDimensionPixelSize(R.styleable.PickerView_mTextSize, Integer.MAX_VALUE);
            mMaxTextAlpha = ta.getFloat(R.styleable.PickerView_mMaxTextAlpha, 0);
            mMinTextAlpha = ta.getFloat(R.styleable.PickerView_mMinTextAlpha, 0);
            ta.recycle();

        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 根据index绘制view
        drawData(canvas);
    }

    private void drawData(Canvas canvas) {
        // 先绘制选中的text再往上往下绘制其余的text
        float scale = parabola(mViewHeight / 2.0f, mMoveLen);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mPaint.setTextSize(mCenterTextSize);
        mPaint.setColor(mCenterColorText);
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        float x = (float) (mViewWidth / 2.0);
        float y = (float) (mViewHeight / 2.0 + mMoveLen);
        Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        float bitMapBaseline = (float) (mViewHeight / 2.0);

        Log.d(TAG, "drawData: " + baseline + "  " + bitMapBaseline + "  " + ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));

        canvas.drawText(mDataList.get(mCurrentSelected), x, baseline, mPaint);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.picker_line), 0, bitMapBaseline + 44, mPaint);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.picker_line), 0, bitMapBaseline - 44, mPaint);
        // 绘制上方data
        for (int i = 1; (mCurrentSelected - i) >= 0; i++) {
            drawOtherText(canvas, i, -1);
        }
        // 绘制下方data
        for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++) {
            drawOtherText(canvas, i, 1);
        }

    }


    /**
     * @param canvas
     * @param position 距离mCurrentSelected的差值
     * @param type     1表示向下绘制，-1表示向上绘制
     */
    private void drawOtherText(Canvas canvas, int position, int type) {
        float d = (MARGIN_ALPHA * mTextSize * position + type
                * mMoveLen);
        float scale = parabola(mViewHeight / 2.0f, d);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mColorText);
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
        float y = (float) (mViewHeight / 2.0 + type * d);
        Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        Log.d(TAG, "drawOtherText: base" + baseline);
        canvas.drawText(mDataList.get(mCurrentSelected + type * position),
                (float) (mViewWidth / 2.0), baseline, mPaint);
    }

    /**
     * 抛物线
     *
     * @param zero 零点坐标
     * @param x    偏移量
     * @return scale
     */
    private float parabola(float zero, float x) {
        float f = (float) (1 - Math.pow(x / zero, 2));
        return f < 0 ? 0 : f;
    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        Log.d(TAG, "onFocusChanged: " + gainFocus);
        mCenterColorText = gainFocus ? mFocusedTextColor : mColorText;
        invalidate();
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP: {
                if (mTask != null) {
                    return true;
                }
                moveTailToHead();
                mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
                mTask = new MyTimerTask(updateHandler);
                timer.schedule(mTask, 0, 10);
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                if (mTask != null) {
                    return true;
                }
                moveHeadToTail();
                mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
                mTask = new MyTimerTask(updateHandler);
                timer.schedule(mTask, 0, 10);
                return true;
            }

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (mSelectListener != null)
                    mSelectListener.onClick();
                return true;
            default:
        }


        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                doDown(event);
                if (mSelectListener != null) {
                    mSelectListener.onClick();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                doMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (doUp(event)) {
                    mCurrentSelected = (mCurrentSelected - 1 + mDataList.size()) % mDataList.size();
                    performSelect();
                    invalidate();
                }
                break;
        }
        return true;
    }


    private void doDown(MotionEvent event) {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mLastDownY = event.getY();
    }

    private void doMove(MotionEvent event) {

        mMoveLen += (event.getY() - mLastDownY);

        if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2) {
            // 往下滑超过离开距离
            moveTailToHead();
            mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
        } else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2) {
            // 往上滑超过离开距离
            moveHeadToTail();
            mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
        }

        mLastDownY = event.getY();
        invalidate();
    }


    private boolean doUp(MotionEvent event) {
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(mMoveLen) < 0.0001) {
            mMoveLen = 0;
            return true;
        } else {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
            mTask = new MyTimerTask(updateHandler);
            timer.schedule(mTask, 0, 10);
            return false;
        }
    }


    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }

    }

    public interface onSelectListener {
        void onSelect(String text);

        void onClick();


    }
}
