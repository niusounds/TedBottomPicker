package gun0912.tedbottompicker;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by TedPark on 2016. 8. 30..
 */

/**
 * https://gist.github.com/liangzhitao/e57df3c3232ee446d464
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private int extraBottomPadding;
    private boolean includeEdge;


    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, int extraBottomPadding) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
        this.extraBottomPadding = extraBottomPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position

        if (position >= 0) {
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }

            // Add extra bottom padding for last elements
            int lastCount = (state.getItemCount() % spanCount) == 0 ? spanCount : state.getItemCount() % spanCount;
            if (position >= state.getItemCount() - lastCount) {
                outRect.bottom = extraBottomPadding;
            }
        } else {
            outRect.left = 0;
            outRect.right = 0;
            outRect.top = 0;
            outRect.bottom = 0;
        }
    }
}
