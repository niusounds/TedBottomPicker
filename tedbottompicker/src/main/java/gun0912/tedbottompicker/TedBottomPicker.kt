package gun0912.tedbottompicker

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import gun0912.tedbottompicker.adapter.GalleryAdapter
import gun0912.tedbottompicker.databinding.TedbottompickerContentViewBinding
import gun0912.tedbottompicker.databinding.TedbottompickerSelectedItemBinding
import java.io.File

class TedBottomPicker : BottomSheetDialogFragment() {
    private lateinit var binding: TedbottompickerContentViewBinding
    private lateinit var builder: Builder
    private lateinit var imageGalleryAdapter: GalleryAdapter
    private lateinit var selectedContentList: MutableList<Content>

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Simulate fixed bottom button
            if (slideOffset >= 0) {
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                binding.btnDone.translationY =
                    -(bottomSheet.top - behavior.expandedOffset).toFloat()
            }
        }
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding = TedbottompickerContentViewBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        val layoutParams =
            (binding.root.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        if (behavior is BottomSheetBehavior<*>) {
            behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
            if (builder.peekHeight > 0) {
                behavior.peekHeight = builder.peekHeight
            }
        }

        // Simulate fixed bottom button
        dialog.setOnShowListener { dialog1: DialogInterface? ->
            mBottomSheetBehaviorCallback.onSlide((binding.root.parent as View), 0f)
        }
        setTitle()
        setRecyclerView()
        setSelectionView()
        selectedContentList = mutableListOf()
        setDoneButton()
        checkMultiMode()
    }

    private fun setSelectionView() {
        if (builder.emptySelectionText != null) {
            binding.selectedPhotosEmpty.text = builder.emptySelectionText
        }
    }

    private fun setDoneButton() {
        if (builder.completeButtonText != null) {
            binding.btnDone.text = builder.completeButtonText
        }
        builder.buttonColor?.let {
            binding.btnDone.backgroundTintList = ColorStateList.valueOf(it)
        }
        builder.buttonTextColor?.let {
            binding.btnDone.setTextColor(it)
        }
        binding.btnDone.setOnClickListener { onMultiSelectComplete() }
    }

    private fun onMultiSelectComplete() {
        if (selectedContentList.size < builder.selectMinCount) {
            val message: String? = if (builder.selectMinCountErrorText != null) {
                builder.selectMinCountErrorText
            } else {
                String.format(
                    resources.getString(R.string.select_min_count),
                    builder.selectMinCount
                )
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            return
        }
        builder.onMultiImageSelectedListener?.onImagesSelected(selectedContentList)
        dismissAllowingStateLoss()
    }

    private fun checkMultiMode() {
        if (!isMultiSelect) {
            binding.btnDone.visibility = View.GONE
            binding.selectedPhotosContainerFrame.visibility = View.GONE
        }
    }

    private fun setRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.rcGallery.layoutManager = gridLayoutManager
        binding.rcGallery.addItemDecoration(
            GridSpacingItemDecoration(
                gridLayoutManager.spanCount,
                builder.spacing,
                builder.includeEdgeSpacing,
                if (isMultiSelect) builder.extraBottomPadding else 0
            )
        )
        updateAdapter()
    }

    private fun updateAdapter() {
        imageGalleryAdapter = GalleryAdapter(requireContext(), builder)
        initAdapter()
        binding.rcGallery.adapter = imageGalleryAdapter
        imageGalleryAdapter.setOnItemClickListener(object : GalleryAdapter.OnItemClickListener {
            override fun onItemClick(content: Content) {
                complete(content)
            }
        })
    }

    private fun initAdapter() {
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
            requireContext().applicationContext.contentResolver.query(
                uri,
                columns,
                selection,
                null,
                orderBy
            )?.use { cursor ->
                val pickerTiles = mutableListOf<Content>()
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
                imageGalleryAdapter.submitContentList(pickerTiles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun complete(content: Content) {
        if (isMultiSelect) {
            if (selectedContentList.contains(content)) {
                removeSelected(content)
            } else {
                addSelected(content)
            }
        } else {
            builder.onImageSelectedListener?.onImageSelected(content)
            dismissAllowingStateLoss()
        }
    }

    private fun addSelected(content: Content) {
        if (selectedContentList.size == builder.selectMaxCount) {
            val message: String = builder.selectMaxCountErrorText ?: String.format(
                resources.getString(R.string.select_max_count),
                builder.selectMaxCount
            )
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            return
        }

        when (content.type) {
            Type.Image -> {
                if (selectedContentList.filter { it.type == Type.Image }.size == builder.selectMaxImageCount) {
                    val message: String = builder.selectMaxImageCountErrorText ?: String.format(
                        resources.getString(R.string.select_max_images_count),
                        builder.selectMaxImageCount
                    )
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Type.Video -> {
                if (selectedContentList.filter { it.type == Type.Video }.size == builder.selectMaxVideoCount) {
                    val message: String = builder.selectMaxVideoCountErrorText ?: String.format(
                        resources.getString(R.string.select_max_videos_count),
                        builder.selectMaxVideoCount
                    )
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        selectedContentList.add(content)
        val itemBinding =
            TedbottompickerSelectedItemBinding.inflate(LayoutInflater.from(requireContext()))
        itemBinding.root.tag = content
        binding.selectedPhotosContainer.addView(itemBinding.root, 0)
        val px = resources.getDimension(R.dimen.tedbottompicker_selected_image_height).toInt()
        itemBinding.selectedPhoto.layoutParams = FrameLayout.LayoutParams(px, px)
        val imageProvider = builder.imageProvider
        if (imageProvider == null) {
            Glide.with(requireActivity())
                .load(content.uri)
                .thumbnail(0.1f)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                )
                .into(itemBinding.selectedPhoto)
        } else {
            imageProvider.onProvideImage(itemBinding.selectedPhoto, content.uri)
        }
        if (builder.deSelectIconDrawable != null) {
            itemBinding.ivClose.setImageDrawable(builder.deSelectIconDrawable)
        }
        itemBinding.ivClose.setOnClickListener { removeSelected(content) }
        updateSelectedView()
        imageGalleryAdapter.setSelectedUriList(selectedContentList, content)
    }

    private fun removeSelected(content: Content) {
        selectedContentList.remove(content)
        for (i in 0 until binding.selectedPhotosContainer.childCount) {
            val childView = binding.selectedPhotosContainer.getChildAt(i)
            if (childView.tag == content) {
                binding.selectedPhotosContainer.removeViewAt(i)
                break
            }
        }
        updateSelectedView()
        imageGalleryAdapter.setSelectedUriList(selectedContentList, content)
    }

    private fun updateSelectedView() {
        if (selectedContentList.size == 0) {
            binding.selectedPhotosEmpty.visibility = View.VISIBLE
            binding.selectedPhotosContainer.visibility = View.GONE
        } else {
            binding.selectedPhotosEmpty.visibility = View.GONE
            binding.selectedPhotosContainer.visibility = View.VISIBLE
        }
    }

    private fun errorMessage(message: String? = null) {
        val errorMessage = message ?: "Something wrong."
        val onErrorListener = builder.onErrorListener
        if (onErrorListener == null) {
            Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
        } else {
            onErrorListener.onError(errorMessage)
        }
    }

    private fun setTitle() {
        if (!builder.showTitle) {
            binding.tvTitle.visibility = View.GONE
            if (!isMultiSelect) {
                binding.viewTitleContainer.visibility = View.GONE
            }
            return
        }
        if (!TextUtils.isEmpty(builder.title)) {
            binding.tvTitle.text = builder.title
        }
        if (builder.titleBackgroundResId > 0) {
            binding.tvTitle.setBackgroundResource(builder.titleBackgroundResId)
        }
    }

    private val isMultiSelect: Boolean
        private get() = builder.onMultiImageSelectedListener != null

    interface OnMultiImageSelectedListener {
        fun onImagesSelected(contents: List<Content>)
    }

    interface OnImageSelectedListener {
        fun onImageSelected(content: Content)
    }

    interface OnErrorListener {
        fun onError(message: String)
    }

    interface ImageProvider {
        fun onProvideImage(imageView: ImageView, imageUri: Uri)
    }

    data class Builder @JvmOverloads constructor(
        private val fragmentActivity: FragmentActivity,
        val filterType: Set<Type> = setOf(Type.Image, Type.Video),
        val selectedForegroundDrawable: Drawable? = null,
        val imageProvider: ImageProvider? = null,
        val cameraTileBackgroundResId: Int = R.color.tedbottompicker_camera,
        val galleryTileBackgroundResId: Int = R.color.tedbottompicker_gallery,
        val onImageSelectedListener: OnImageSelectedListener? = null,
        val onMultiImageSelectedListener: OnMultiImageSelectedListener? = null,
        val onErrorListener: OnErrorListener? = null,
        val title: String? = null,
        val showTitle: Boolean = true,
        val deSelectIconDrawable: Drawable? = null,
        val spacing: Int = fragmentActivity.resources.getDimensionPixelSize(R.dimen.tedbottompicker_grid_layout_margin),
        val includeEdgeSpacing: Boolean = false,
        val peekHeight: Int = -1,
        val extraBottomPadding: Int = fragmentActivity.resources.getDimensionPixelSize(R.dimen.tedbottompicker_grid_padding_bottom),
        val titleBackgroundResId: Int = 0,
        val selectMaxCount: Int = Int.MAX_VALUE,
        val selectMinCount: Int = 0,
        val selectMaxImageCount: Int = Int.MAX_VALUE,
        val selectMaxVideoCount: Int = Int.MAX_VALUE,
        val completeButtonText: String? = null,
        val emptySelectionText: String? = null,
        val selectMaxCountErrorText: String? = null,
        val selectMaxImageCountErrorText: String? = null,
        val selectMaxVideoCountErrorText: String? = null,
        val selectMinCountErrorText: String? = null,
        val buttonTextColor: Int? = null,
        val buttonColor: Int? = null,
    ) {
        fun setFilterType(vararg types: Type): Builder {
            return copy(filterType = setOf(*types))
        }

        fun setSpacingResId(@DimenRes dimenResId: Int): Builder {
            return copy(spacing = fragmentActivity.resources.getDimensionPixelSize(dimenResId))
        }

        fun setExtraBottomPaddingId(@DimenRes dimenResId: Int): Builder {
            return copy(
                extraBottomPadding = fragmentActivity.resources.getDimensionPixelSize(
                    dimenResId
                )
            )
        }

        fun setDeSelectIcon(@DrawableRes deSelectIconResId: Int): Builder {
            return setDeSelectIcon(ContextCompat.getDrawable(fragmentActivity, deSelectIconResId))
        }

        fun setDeSelectIcon(deSelectIconDrawable: Drawable?): Builder {
            return copy(deSelectIconDrawable = deSelectIconDrawable)
        }

        fun setSelectedForeground(@DrawableRes selectedForegroundResId: Int): Builder {
            setSelectedForeground(
                ContextCompat.getDrawable(
                    fragmentActivity,
                    selectedForegroundResId
                )
            )
            return this
        }

        fun setSelectedForeground(selectedForegroundDrawable: Drawable?): Builder {
            return copy(selectedForegroundDrawable = selectedForegroundDrawable)
        }

        fun setSelectMaxCount(selectMaxCount: Int): Builder {
            return copy(selectMaxCount = selectMaxCount)
        }

        fun setSelectMaxImageCount(selectMaxImageCount: Int): Builder {
            return copy(selectMaxImageCount = selectMaxImageCount)
        }

        fun setSelectMaxVideoCount(selectMaxVideoCount: Int): Builder {
            return copy(selectMaxVideoCount = selectMaxVideoCount)
        }

        fun setSelectMinCount(selectMinCount: Int): Builder {
            return copy(selectMinCount = selectMinCount)
        }

        fun setOnImageSelectedListener(onImageSelectedListener: OnImageSelectedListener?): Builder {
            return copy(onImageSelectedListener = onImageSelectedListener)
        }

        fun setOnMultiImageSelectedListener(onMultiImageSelectedListener: OnMultiImageSelectedListener?): Builder {
            return copy(onMultiImageSelectedListener = onMultiImageSelectedListener)
        }

        fun setOnErrorListener(onErrorListener: OnErrorListener?): Builder {
            return copy(onErrorListener = onErrorListener)
        }

        fun setSpacing(spacing: Int): Builder {
            return copy(spacing = spacing)
        }

        fun setIncludeEdgeSpacing(includeEdgeSpacing: Boolean): Builder {
            return copy(includeEdgeSpacing = includeEdgeSpacing)
        }

        fun setPeekHeight(peekHeight: Int): Builder {
            return copy(peekHeight = peekHeight)
        }

        fun setPeekHeightResId(@DimenRes dimenResId: Int): Builder {
            return copy(peekHeight = fragmentActivity.resources.getDimensionPixelSize(dimenResId))
        }

        fun setCameraTileBackgroundResId(@ColorRes colorResId: Int): Builder {
            return copy(cameraTileBackgroundResId = colorResId)
        }

        fun setGalleryTileBackgroundResId(@ColorRes colorResId: Int): Builder {
            return copy(galleryTileBackgroundResId = colorResId)
        }

        fun setTitle(title: String?): Builder {
            return copy(title = title)
        }

        fun setTitle(@StringRes stringResId: Int): Builder {
            return copy(title = fragmentActivity.resources.getString(stringResId))
        }

        fun showTitle(showTitle: Boolean): Builder {
            return copy(showTitle = showTitle)
        }

        fun setCompleteButtonText(completeButtonText: String?): Builder {
            return copy(completeButtonText = completeButtonText)
        }

        fun setCompleteButtonText(@StringRes completeButtonResId: Int): Builder {
            return copy(
                completeButtonText = fragmentActivity.resources.getString(
                    completeButtonResId
                )
            )
        }

        fun setEmptySelectionText(emptySelectionText: String?): Builder {
            return copy(emptySelectionText = emptySelectionText)
        }

        fun setEmptySelectionText(@StringRes emptySelectionResId: Int): Builder {
            return copy(
                emptySelectionText = fragmentActivity.resources.getString(
                    emptySelectionResId
                )
            )
        }

        fun setSelectMaxCountErrorText(selectMaxCountErrorText: String?): Builder {
            return copy(selectMaxCountErrorText = selectMaxCountErrorText)
        }

        fun setSelectMaxCountErrorText(@StringRes selectMaxCountErrorResId: Int): Builder {
            return copy(
                selectMaxCountErrorText = fragmentActivity.resources.getString(
                    selectMaxCountErrorResId
                )
            )
        }

        fun setSelectMinCountErrorText(selectMinCountErrorText: String?): Builder {
            return copy(selectMinCountErrorText = selectMinCountErrorText)
        }

        fun setSelectMinCountErrorText(@StringRes selectMinCountErrorResId: Int): Builder {
            return copy(
                selectMinCountErrorText = fragmentActivity.resources.getString(
                    selectMinCountErrorResId
                )
            )
        }

        fun setTitleBackgroundResId(@ColorRes colorResId: Int): Builder {
            return copy(titleBackgroundResId = colorResId)
        }

        fun setImageProvider(imageProvider: ImageProvider?): Builder {
            return copy(imageProvider = imageProvider)
        }

        fun setButtonColor(@ColorInt color: Int): Builder {
            return copy(buttonColor = color)
        }

        fun setButtonTextColor(@ColorInt color: Int): Builder {
            return copy(buttonTextColor = color)
        }

        fun create(): TedBottomPicker {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                ContextCompat.checkSelfPermission(
                        fragmentActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?")
            }
            if (onImageSelectedListener == null && onMultiImageSelectedListener == null) {
                throw RuntimeException("You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener() for receive selected Uri")
            }
            val customBottomSheetDialogFragment = TedBottomPicker()
            customBottomSheetDialogFragment.builder = this
            return customBottomSheetDialogFragment
        }

        fun show(onImageSelectedListener: OnImageSelectedListener?) {
            copy(onImageSelectedListener = onImageSelectedListener)
                .create()
                .show(fragmentActivity.supportFragmentManager, null)
        }

        fun showMultiImage(onMultiImageSelectedListener: OnMultiImageSelectedListener?) {
            copy(onMultiImageSelectedListener = onMultiImageSelectedListener)
                .create()
                .show(fragmentActivity.supportFragmentManager, null)
        }
    }

    companion object {
        @JvmStatic
        fun with(fragmentActivity: FragmentActivity): Builder {
            return Builder(fragmentActivity)
        }
    }
}
