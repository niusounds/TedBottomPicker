package gun0912.tedbottompicker;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gun0912.tedbottompicker.adapter.GalleryAdapter;

public class TedBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public BaseBuilder builder;
    private GalleryAdapter imageGalleryAdapter;
    private View view_title_container;
    private TextView tv_title;
    private MaterialButton btn_done;

    private FrameLayout selected_photos_container_frame;
    private LinearLayout selected_photos_container;

    private TextView selected_photos_empty;
    private List<Content> selectedUriList;
    private List<Content> tempUriList;
    private RecyclerView rc_gallery;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {


        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }


        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            // Simulate fixed bottom button
            if (slideOffset >= 0) {
                final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                btn_done.setTranslationY(-(bottomSheet.getTop() - behavior.getExpandedOffset()));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSavedInstanceState(savedInstanceState);
    }

    private void setupSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            tempUriList = builder.selectedUriList;
        }
    }

    public void show(FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, getTag());
        ft.commitAllowingStateLoss();
    }


    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.tedbottompicker_content_view, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
            (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).addBottomSheetCallback(mBottomSheetBehaviorCallback);
            if (builder != null && builder.peekHeight > 0) {
                ((BottomSheetBehavior) behavior).setPeekHeight(builder.peekHeight);
            }

        }

        // Simulate fixed bottom button
        dialog.setOnShowListener(dialog1 -> mBottomSheetBehaviorCallback.onSlide((View) contentView.getParent(), 0));

        if (builder == null) {
            dismissAllowingStateLoss();
            return;
        }
        initView(contentView);

        setTitle();
        setRecyclerView();
        setSelectionView();

        selectedUriList = new ArrayList<>();


        if (builder.onMultiImageSelectedListener != null && tempUriList != null) {
            for (Content uri : tempUriList) {
                addUri(uri);
            }
        }

        setDoneButton();
        checkMultiMode();
    }

    private void setSelectionView() {

        if (builder.emptySelectionText != null) {
            selected_photos_empty.setText(builder.emptySelectionText);
        }


    }

    private void setDoneButton() {

        if (builder.completeButtonText != null) {
            btn_done.setText(builder.completeButtonText);
        }

        if (builder.buttonColor != null) {
            btn_done.setBackgroundTintList(ColorStateList.valueOf(builder.buttonColor));
        }

        if (builder.buttonTextColor != null) {
            btn_done.setTextColor(builder.buttonTextColor);
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onMultiSelectComplete();


            }
        });
    }

    private void onMultiSelectComplete() {

        if (selectedUriList.size() < builder.selectMinCount) {
            String message;
            if (builder.selectMinCountErrorText != null) {
                message = builder.selectMinCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_min_count), builder.selectMinCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }


        builder.onMultiImageSelectedListener.onImagesSelected(selectedUriList);
        dismissAllowingStateLoss();
    }

    private void checkMultiMode() {
        if (!isMultiSelect()) {
            btn_done.setVisibility(View.GONE);
            selected_photos_container_frame.setVisibility(View.GONE);
        }

    }

    private void initView(View contentView) {

        view_title_container = contentView.findViewById(R.id.view_title_container);
        rc_gallery = contentView.findViewById(R.id.rc_gallery);
        tv_title = contentView.findViewById(R.id.tv_title);
        btn_done = contentView.findViewById(R.id.btn_done);

        selected_photos_container_frame = contentView.findViewById(R.id.selected_photos_container_frame);
        selected_photos_container = contentView.findViewById(R.id.selected_photos_container);
        selected_photos_empty = contentView.findViewById(R.id.selected_photos_empty);
    }

    private void setRecyclerView() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rc_gallery.setLayoutManager(gridLayoutManager);
        rc_gallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), builder.spacing, builder.includeEdgeSpacing, isMultiSelect() ? builder.extraBottomPadding : 0));
        updateAdapter();
    }

    private void updateAdapter() {

        imageGalleryAdapter = new GalleryAdapter(getActivity(), builder);
        rc_gallery.setAdapter(imageGalleryAdapter);
        imageGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Content pickerTile = imageGalleryAdapter.getItem(position);
                complete(pickerTile);
            }
        });
    }

    private void complete(final Content content) {
        if (isMultiSelect()) {
            if (selectedUriList.contains(content)) {
                removeImage(content);
            } else {
                addUri(content);
            }

        } else {
            builder.onImageSelectedListener.onImageSelected(content);
            dismissAllowingStateLoss();
        }

    }

    private void addUri(final Content content) {
        if (selectedUriList.size() == builder.selectMaxCount) {
            String message;
            if (builder.selectMaxCountErrorText != null) {
                message = builder.selectMaxCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_max_count), builder.selectMaxCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }


        selectedUriList.add(content);

        final View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.tedbottompicker_selected_item, null);
        ImageView thumbnail = rootView.findViewById(R.id.selected_photo);
        ImageView iv_close = rootView.findViewById(R.id.iv_close);
        rootView.setTag(content);

        selected_photos_container.addView(rootView, 0);


        int px = (int) getResources().getDimension(R.dimen.tedbottompicker_selected_image_height);
        thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));

        if (builder.imageProvider == null) {
            Glide.with(getActivity())
                .load(content.getUri())
                .thumbnail(0.1f)
                .apply(new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.img_error))
                .into(thumbnail);
        } else {
            builder.imageProvider.onProvideImage(thumbnail, content.getUri());
        }


        if (builder.deSelectIconDrawable != null) {
            iv_close.setImageDrawable(builder.deSelectIconDrawable);
        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImage(content);

            }
        });


        updateSelectedView();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, content);

    }

    private void removeImage(Content uri) {

        selectedUriList.remove(uri);


        for (int i = 0; i < selected_photos_container.getChildCount(); i++) {
            View childView = selected_photos_container.getChildAt(i);


            if (childView.getTag().equals(uri)) {
                selected_photos_container.removeViewAt(i);
                break;
            }
        }

        updateSelectedView();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);
    }

    private void updateSelectedView() {

        if (selectedUriList == null || selectedUriList.size() == 0) {
            selected_photos_empty.setVisibility(View.VISIBLE);
            selected_photos_container.setVisibility(View.GONE);
        } else {
            selected_photos_empty.setVisibility(View.GONE);
            selected_photos_container.setVisibility(View.VISIBLE);
        }

    }

    private void errorMessage(String message) {
        String errorMessage = message == null ? "Something wrong." : message;

        if (builder.onErrorListener == null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            builder.onErrorListener.onError(errorMessage);
        }
    }

    private void errorMessage() {
        errorMessage(null);
    }

    private void setTitle() {

        if (!builder.showTitle) {
            tv_title.setVisibility(View.GONE);

            if (!isMultiSelect()) {
                view_title_container.setVisibility(View.GONE);
            }

            return;
        }

        if (!TextUtils.isEmpty(builder.title)) {
            tv_title.setText(builder.title);
        }

        if (builder.titleBackgroundResId > 0) {
            tv_title.setBackgroundResource(builder.titleBackgroundResId);
        }

    }

    private boolean isMultiSelect() {
        return builder.onMultiImageSelectedListener != null;
    }

    public interface OnMultiImageSelectedListener {
        void onImagesSelected(List<Content> uriList);
    }

    public interface OnImageSelectedListener {
        void onImageSelected(Content uri);
    }

    public interface OnErrorListener {
        void onError(String message);
    }

    public interface ImageProvider {
        void onProvideImage(ImageView imageView, Uri imageUri);
    }

    public abstract static class BaseBuilder<T extends BaseBuilder> {

        public Set<Type> filterType = new HashSet<>(Arrays.asList(Type.Image, Type.Video));
        public int previewMaxCount = 25;
        public Drawable selectedForegroundDrawable;
        public ImageProvider imageProvider;
        public int cameraTileBackgroundResId = R.color.tedbottompicker_camera;
        public int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;
        protected FragmentActivity fragmentActivity;
        OnImageSelectedListener onImageSelectedListener;
        OnMultiImageSelectedListener onMultiImageSelectedListener;
        OnErrorListener onErrorListener;
        private String title;
        private boolean showTitle = true;
        private List<Uri> selectedUriList;
        private Uri selectedUri;
        private Drawable deSelectIconDrawable;
        private int spacing = 1;
        private boolean includeEdgeSpacing = false;
        private int peekHeight = -1;
        private int extraBottomPadding = -1;
        private int titleBackgroundResId;
        private int selectMaxCount = Integer.MAX_VALUE;
        private int selectMinCount = 0;
        private String completeButtonText;
        private String emptySelectionText;
        private String selectMaxCountErrorText;
        private String selectMinCountErrorText;
        private Integer buttonTextColor;
        private Integer buttonColor;

        public BaseBuilder(@NonNull FragmentActivity fragmentActivity) {

            this.fragmentActivity = fragmentActivity;

            setSpacingResId(R.dimen.tedbottompicker_grid_layout_margin);
            setExtraBottomPaddingId(R.dimen.tedbottompicker_grid_padding_bottom);
        }

        public T setFilterType(Type... types) {
            this.filterType = new HashSet<>(Arrays.asList(types));
            return (T) this;
        }

        public T setSpacingResId(@DimenRes int dimenResId) {
            this.spacing = fragmentActivity.getResources().getDimensionPixelSize(dimenResId);
            return (T) this;
        }

        public T setExtraBottomPaddingId(@DimenRes int dimenResId) {
            this.extraBottomPadding = fragmentActivity.getResources().getDimensionPixelSize(dimenResId);
            return (T) this;
        }

        public T setDeSelectIcon(@DrawableRes int deSelectIconResId) {
            setDeSelectIcon(ContextCompat.getDrawable(fragmentActivity, deSelectIconResId));
            return (T) this;
        }

        public T setDeSelectIcon(Drawable deSelectIconDrawable) {
            this.deSelectIconDrawable = deSelectIconDrawable;
            return (T) this;
        }

        public T setSelectedForeground(@DrawableRes int selectedForegroundResId) {
            setSelectedForeground(ContextCompat.getDrawable(fragmentActivity, selectedForegroundResId));
            return (T) this;
        }

        public T setSelectedForeground(Drawable selectedForegroundDrawable) {
            this.selectedForegroundDrawable = selectedForegroundDrawable;
            return (T) this;
        }

        public T setPreviewMaxCount(int previewMaxCount) {
            this.previewMaxCount = previewMaxCount;
            return (T) this;
        }

        public T setSelectMaxCount(int selectMaxCount) {
            this.selectMaxCount = selectMaxCount;
            return (T) this;
        }

        public T setSelectMinCount(int selectMinCount) {
            this.selectMinCount = selectMinCount;
            return (T) this;
        }

        public T setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            return (T) this;
        }

        public T setOnMultiImageSelectedListener(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            this.onMultiImageSelectedListener = onMultiImageSelectedListener;
            return (T) this;
        }

        public T setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return (T) this;
        }

        public T setSpacing(int spacing) {
            this.spacing = spacing;
            return (T) this;
        }

        public T setIncludeEdgeSpacing(boolean includeEdgeSpacing) {
            this.includeEdgeSpacing = includeEdgeSpacing;
            return (T) this;
        }

        public T setPeekHeight(int peekHeight) {
            this.peekHeight = peekHeight;
            return (T) this;
        }

        public T setPeekHeightResId(@DimenRes int dimenResId) {
            this.peekHeight = fragmentActivity.getResources().getDimensionPixelSize(dimenResId);
            return (T) this;
        }

        public T setCameraTileBackgroundResId(@ColorRes int colorResId) {
            this.cameraTileBackgroundResId = colorResId;
            return (T) this;
        }

        public T setGalleryTileBackgroundResId(@ColorRes int colorResId) {
            this.galleryTileBackgroundResId = colorResId;
            return (T) this;
        }

        public T setTitle(String title) {
            this.title = title;
            return (T) this;
        }

        public T setTitle(@StringRes int stringResId) {
            this.title = fragmentActivity.getResources().getString(stringResId);
            return (T) this;
        }

        public T showTitle(boolean showTitle) {
            this.showTitle = showTitle;
            return (T) this;
        }

        public T setCompleteButtonText(String completeButtonText) {
            this.completeButtonText = completeButtonText;
            return (T) this;
        }

        public T setCompleteButtonText(@StringRes int completeButtonResId) {
            this.completeButtonText = fragmentActivity.getResources().getString(completeButtonResId);
            return (T) this;
        }

        public T setEmptySelectionText(String emptySelectionText) {
            this.emptySelectionText = emptySelectionText;
            return (T) this;
        }

        public T setEmptySelectionText(@StringRes int emptySelectionResId) {
            this.emptySelectionText = fragmentActivity.getResources().getString(emptySelectionResId);
            return (T) this;
        }

        public T setSelectMaxCountErrorText(String selectMaxCountErrorText) {
            this.selectMaxCountErrorText = selectMaxCountErrorText;
            return (T) this;
        }

        public T setSelectMaxCountErrorText(@StringRes int selectMaxCountErrorResId) {
            this.selectMaxCountErrorText = fragmentActivity.getResources().getString(selectMaxCountErrorResId);
            return (T) this;
        }

        public T setSelectMinCountErrorText(String selectMinCountErrorText) {
            this.selectMinCountErrorText = selectMinCountErrorText;
            return (T) this;
        }

        public T setSelectMinCountErrorText(@StringRes int selectMinCountErrorResId) {
            this.selectMinCountErrorText = fragmentActivity.getResources().getString(selectMinCountErrorResId);
            return (T) this;
        }

        public T setTitleBackgroundResId(@ColorRes int colorResId) {
            this.titleBackgroundResId = colorResId;
            return (T) this;
        }

        public T setImageProvider(ImageProvider imageProvider) {
            this.imageProvider = imageProvider;
            return (T) this;
        }

        public T setSelectedUri(Uri selectedUri) {
            this.selectedUri = selectedUri;
            return (T) this;
        }

        public T setButtonColor(@ColorInt int color) {
            this.buttonColor = color;
            return (T) this;
        }

        public T setButtonTextColor(@ColorInt int color) {
            this.buttonTextColor = color;
            return (T) this;
        }

        public TedBottomSheetDialogFragment create() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ContextCompat.checkSelfPermission(fragmentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
            }

            if (onImageSelectedListener == null && onMultiImageSelectedListener == null) {
                throw new RuntimeException("You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener() for receive selected Uri");
            }

            TedBottomSheetDialogFragment customBottomSheetDialogFragment = new TedBottomSheetDialogFragment();
            customBottomSheetDialogFragment.builder = (T) this;
            return customBottomSheetDialogFragment;
        }


    }


}
