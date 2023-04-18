package wongxd.common.recycleview.flow;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {


    /**
     * @param ctx
     * @param row 竖直间距 dp
     * @param clo 水平间距 dp
     */
    public SpaceItemDecoration(Context ctx, int row, int clo) {
        this.r = dp2px(ctx, row);
        this.c = dp2px(ctx, clo);
    }

    private int c;
    private int r;

    /**
     * @param row 竖直间距  px
     * @param clo 水平间距  px
     */
    public SpaceItemDecoration(int row, int clo) {
        this.r = row;
        this.c = clo;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = r;
        outRect.left = c;
        outRect.right = c;
        outRect.bottom = r;
    }

    private int dp2px(Context ctx, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, ctx.getResources().getDisplayMetrics());
    }
}