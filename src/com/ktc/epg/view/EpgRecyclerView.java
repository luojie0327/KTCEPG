package com.ktc.epg.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.internal.widget.RecyclerView;

public class EpgRecyclerView extends RecyclerView {
    private static final int DIR_UP = 0;
    private static final int DIR_DOWN = 1;

    public EpgRecyclerView(Context context) {
        super(context);
    }

    public EpgRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EpgRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 事件分发处理流程：
     * 首先在RecycleView中处理上下按键，针对乱获取焦点进行处理。
     */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP
                    && decideFocus(DIR_UP)) {
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                    && decideFocus(DIR_DOWN)) {
                return true;

            }
        }
        return super.dispatchKeyEvent(event);
    }

    boolean decideFocus(int direction) {
        View focusedView = getFocusedChild();
        if (focusedView == null) {
            Log.d("Ktcepg", "decideFocus: 111");
            return false;
        }
        View searchView = null;
        if (direction == DIR_UP) {
            searchView = focusedView.focusSearch(FOCUS_UP);
        } else if (direction == DIR_DOWN) {
            searchView = focusedView.focusSearch(FOCUS_DOWN);
        }
        if (searchView == null) {
            Log.d("Ktcepg", "decideFocus: 222");
            return true;
        }
        int pos = indexOfChild(searchView);
        Log.d("Ktcepg", "decideFocus: "+pos+" "+searchView.toString());
        return pos == NO_POSITION;
    }


}
