package gun0912.tedbottompicker.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import gun0912.tedbottompicker.Content
import gun0912.tedbottompicker.R
import gun0912.tedbottompicker.TedBottomPicker
import gun0912.tedbottompicker.Type
import gun0912.tedbottompicker.databinding.TedbottompickerGridItemBinding

/**
 * Created by TedPark on 2016. 8. 30..
 */
class GalleryAdapter(
    private val context: Context,
    private val builder: TedBottomPicker.Builder
) : ListAdapter<GalleryAdapter.GridItem, GalleryViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<GalleryAdapter.GridItem>() {
            override fun areItemsTheSame(oldItem: GridItem, newItem: GridItem): Boolean =
                oldItem.content.uri == newItem.content.uri

            override fun areContentsTheSame(oldItem: GridItem, newItem: GridItem): Boolean =
                oldItem == newItem
        }
    }

    data class GridItem(val content: Content, val selected: Boolean, val disabled: Boolean)

    private var onItemClickListener: OnItemClickListener? = null
    private var selected: List<Content> = emptyList()

    fun setSelectedUriList(selectedContents: List<Content>, content: Content) {
        this.selected = selectedContents

        val reachedToSelectMaxCount = selectedContents.size == builder.selectMaxCount
        val reachedToSelectMaxImageCount =
            selectedContents.filter { it.type == Type.Image }.size == builder.selectMaxImageCount
        val reachedToSelectMaxVideoCount =
            selectedContents.filter { it.type == Type.Video }.size == builder.selectMaxVideoCount

        val newList = currentList.map {
            val selected = selectedContents.contains(it.content)
            val reachedToMaxSelectCountForType = when (it.content.type) {
                Type.Image -> reachedToSelectMaxImageCount
                Type.Video -> reachedToSelectMaxVideoCount
            }

            it.copy(
                selected = selected,
                disabled = !selected && (reachedToSelectMaxCount || reachedToMaxSelectCountForType),
            )
        }
        super.submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(TedbottompickerGridItemBinding.inflate(LayoutInflater.from(context)))
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val item = getItem(position)

        Glide.with(context)
            .load(item.content.uri)
            .thumbnail(0.1f)
            .apply(
                RequestOptions().centerCrop()
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.img_error)
            )
            .into(holder.thumbnail)

        val isSelected: Boolean = item.selected

        val foregroundDrawable: Drawable? = builder.selectedForegroundDrawable
            ?: ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected)

        holder.root.foreground = if (isSelected) foregroundDrawable else null
        holder.disableOverlay.isVisible = item.disabled

        onItemClickListener?.let { listener ->
            holder.itemView.setOnClickListener {
                listener.onItemClick(item.content)
            }
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun submitContentList(list: List<Content>, commitCallback: Runnable? = null) {
        super.submitList(list.map { GridItem(it, false, false) }, commitCallback)
    }

    override fun submitList(list: MutableList<GridItem>?) {
        error("use submitContentList")
    }

    override fun submitList(list: MutableList<GridItem>?, commitCallback: Runnable?) {
        error("use submitContentList")
    }

    fun interface OnItemClickListener {
        fun onItemClick(content: Content)
    }
}
