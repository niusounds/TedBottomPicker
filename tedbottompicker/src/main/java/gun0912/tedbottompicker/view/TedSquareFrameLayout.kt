package gun0912.tedbottompicker.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import gun0912.tedbottompicker.R

/**
 * Created by Gil on 09/06/2014.
 */
class TedSquareFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var mMatchHeightToWidth = false
    private var mMatchWidthToHeight = false

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TedBottomPickerSquareView,
            0, 0
        )
        try {
            mMatchHeightToWidth =
                a.getBoolean(R.styleable.TedBottomPickerSquareView_matchHeightToWidth, false)
            mMatchWidthToHeight =
                a.getBoolean(R.styleable.TedBottomPickerSquareView_matchWidthToHeight, false)
        } finally {
            a.recycle()
        }
    }

    //Squares the thumbnail
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mMatchHeightToWidth) {
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
        } else if (mMatchWidthToHeight) {
            setMeasuredDimension(heightMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mMatchHeightToWidth) {
            super.onSizeChanged(w, w, oldw, oldh)
        } else if (mMatchWidthToHeight) {
            super.onSizeChanged(h, h, oldw, oldh)
        }
    }
}