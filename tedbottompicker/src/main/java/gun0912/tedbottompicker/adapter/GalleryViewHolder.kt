package gun0912.tedbottompicker.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gun0912.tedbottompicker.databinding.TedbottompickerGridItemBinding
import gun0912.tedbottompicker.view.TedSquareFrameLayout
import gun0912.tedbottompicker.view.TedSquareImageView

class GalleryViewHolder(binding: TedbottompickerGridItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val root: TedSquareFrameLayout = binding.root
    val thumbnail: TedSquareImageView = binding.ivThumbnail
    val disableOverlay: View = binding.disableOverlay
    val duration: TextView = binding.duration
}
