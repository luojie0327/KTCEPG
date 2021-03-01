package com.ktc.epg.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.android.internal.widget.LinearLayoutManager;

public class FocusLinerLayoutManager extends LinearLayoutManager {
    public FocusLinerLayoutManager(Context context) {
        super(context);
    }

    public FocusLinerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public FocusLinerLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        int count = getItemCount();
        int fromPos = getPosition(focused);
        int lastVisibleItemPos = findLastVisibleItemPosition();
        switch (direction) {
            case View.FOCUS_DOWN:
                fromPos++;
                break;
            case View.FOCUS_UP:
                fromPos--;
                break;
            default:
        }
        if (fromPos > count) {
            return focused;
        } else {
            if (fromPos > lastVisibleItemPos) {
                scrollToPosition(fromPos);
            }
        }
        return super.onInterceptFocusSearch(focused, direction);
    }
}
