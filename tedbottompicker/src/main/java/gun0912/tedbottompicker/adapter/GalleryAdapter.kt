package gun0912.tedbottompicker.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import gun0912.tedbottompicker.Content
import gun0912.tedbottompicker.R
import gun0912.tedbottompicker.TedBottomSheetDialogFragment.BaseBuilder
import gun0912.tedbottompicker.Type
import java.io.File

/**
 * Created by TedPark on 2016. 8. 30..
 */
class GalleryAdapter(private val context: Context, private val builder: BaseBuilder<*>) :
    RecyclerView.Adapter<GalleryViewHolder>() {
    private val pickerTiles = mutableListOf<Content>()
    private var onItemClickListener: OnItemClickListener? = null
    private var selected: List<Content> = emptyList()

    fun setSelectedUriList(selected: List<Content>, content: Content) {
        this.selected = selected
        val position = pickerTiles.indexOfFirst { content == it }
        if (position >= 0) {
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = View.inflate(context, R.layout.tedbottompicker_grid_item, null)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val content = getItem(position)

        if (builder.imageProvider == null) {
            Glide.with(context)
                .load(content.uri)
                .thumbnail(0.1f)
                .apply(
                    RequestOptions().centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                )
                .into(holder.thumbnail)
        } else {
            builder.imageProvider.onProvideImage(holder.thumbnail, content.uri)
        }

        val isSelected: Boolean = selected.contains(content)

        val foregroundDrawable: Drawable? = if (builder.selectedForegroundDrawable != null) {
            builder.selectedForegroundDrawable
        } else {
            ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected)
        }

        holder.root.foreground = if (isSelected) foregroundDrawable else null

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener {
                onItemClickListener!!.onItemClick(
                    holder.itemView,
                    holder.adapterPosition
                )
            }
        }
    }

    fun getItem(position: Int): Content {
        return pickerTiles[position]
    }

    override fun getItemCount(): Int {
        return pickerTiles.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    init {
        try {
            val selections = mutableListOf<String>()
            if (builder.filterType.contains(Type.Image)) {
                selections += "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
            }
            if (builder.filterType.contains(Type.Video)) {
                selections += "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
            }
            val selection = selections.joinToString(" OR ")
            val columns = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )
            val orderBy = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            val uri = MediaStore.Files.getContentUri("external")
            context.applicationContext.contentResolver.query(
                uri,
                columns,
                selection,
                null,
                orderBy
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val dataIndex = MediaStore.Files.FileColumns.DATA
                    val imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex))
                    val mediaType =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE))
                    val imageFile = File(imageLocation)
                    val type =
                        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) Type.Image else Type.Video
                    pickerTiles.add(Content(Uri.fromFile(imageFile), type))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}