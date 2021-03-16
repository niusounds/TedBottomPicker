package gun0912.tedbottompicker.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import gun0912.tedbottompicker.R
import gun0912.tedbottompicker.view.TedSquareFrameLayout
import gun0912.tedbottompicker.view.TedSquareImageView

class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val root: TedSquareFrameLayout = view.findViewById(R.id.root)
    val thumbnail: TedSquareImageView = view.findViewById(R.id.iv_thumbnail)
}