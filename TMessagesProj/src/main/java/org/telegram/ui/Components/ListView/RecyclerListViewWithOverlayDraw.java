package org.telegram.ui.Components.ListView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

import org.telegram.ui.Components.RecyclerListView;

public class RecyclerListViewWithOverlayDraw extends RecyclerListView {

    boolean invalidated;
    public RecyclerListViewWithOverlayDraw(Context context) {
        super(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        invalidated = false;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof OverlayView) {
                OverlayView view = (OverlayView) getChildAt(i);
                canvas.save();
                canvas.translate(view.getX(), view.getY());
                view.preDraw(this, canvas);
                canvas.restore();
            }
        }
        super.dispatchDraw(canvas);
    }

    @Override
    public void capture(Canvas canvas, RectF position) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof OverlayView) {
                float left = child.getX();
                float top = child.getY();
                float right = left + child.getWidth();
                float bottom = top + child.getHeight();
                if (position.intersects(left, top, right, bottom)) {
                    canvas.save();
                    canvas.clipRect(left, top, right, bottom);
                    canvas.translate(left, top);
                    ((OverlayView) child).preDraw(this, canvas);
                    canvas.restore();
                }
            }
        }
        super.capture(canvas, position);
    }

    @Override
    public void invalidate() {
        if (invalidated) {
            return;
        }
        super.invalidate();
        invalidated = true;
    }

    public interface OverlayView {
        void preDraw(View view, Canvas canvas);
        float getX();
        float getY();
    }
}
