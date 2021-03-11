package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.Content;
import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import gun0912.tedbottompicker.Type;
import gun0912.tedbottompicker.util.StringUtil;
import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {


    private ArrayList<Content> pickerTiles;
    private Context context;
    private TedBottomSheetDialogFragment.BaseBuilder builder;
    private OnItemClickListener onItemClickListener;
    private List<Content> selectedUriList;


    public GalleryAdapter(Context context, TedBottomSheetDialogFragment.BaseBuilder builder) {

        this.context = context;
        this.builder = builder;

        pickerTiles = new ArrayList<>();
        selectedUriList = new ArrayList<>();

        Cursor cursor = null;
        try {
            List<String> selections = new ArrayList<>();
            if (builder.filterType.contains(Type.Image)) {
                selections.add(MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            }
            if (builder.filterType.contains(Type.Video)) {
                selections.add(MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
            }
            String selection = StringUtil.join(selections, " OR ");
            String[] columns = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
            };
            String orderBy = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
            Uri uri = MediaStore.Files.getContentUri("external");

            cursor = context.getApplicationContext().getContentResolver().query(uri, columns, selection, null, orderBy);
            //imageCursor = sContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);


            if (cursor != null) {

                int count = 0;
                while (cursor.moveToNext() && count < builder.previewMaxCount) {

                    String dataIndex = MediaStore.Files.FileColumns.DATA;
                    String imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex));
                    int mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
                    File imageFile = new File(imageLocation);
                    final Type type = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ? Type.Image : Type.Video;
                    pickerTiles.add(new Content(Uri.fromFile(imageFile), type));
                    count++;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


    }

    public void setSelectedUriList(List<Content> selectedUriList, @NonNull Content uri) {
        this.selectedUriList = selectedUriList;

        int position = -1;


        Content pickerTile;
        for (int i = 0; i < pickerTiles.size(); i++) {
            pickerTile = pickerTiles.get(i);
            if (uri.equals(pickerTile)) {
                position = i;
                break;
            }
        }


        if (position >= 0) {
            notifyItemChanged(position);
        }


    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.tedbottompicker_grid_item, null);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, int position) {

        Content pickerTile = getItem(position);


        boolean isSelected = false;

        Uri uri = pickerTile.getUri();
        if (builder.imageProvider == null) {
            Glide.with(context)
                .load(uri)
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop()
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.img_error))
                .into(holder.iv_thumbnail);
        } else {
            builder.imageProvider.onProvideImage(holder.iv_thumbnail, uri);
        }


        isSelected = selectedUriList.contains(uri);


        if (holder.root != null) {

            Drawable foregroundDrawable;

            if (builder.selectedForegroundDrawable != null) {
                foregroundDrawable = builder.selectedForegroundDrawable;
            } else {
                foregroundDrawable = ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected);
            }

            holder.root.setForeground(isSelected ? foregroundDrawable : null);
        }


        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
                }
            });
        }
    }

    public Content getItem(int position) {
        return pickerTiles.get(position);
    }

    @Override
    public int getItemCount() {
        return pickerTiles.size();
    }

    public void setOnItemClickListener(
        OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    class GalleryViewHolder extends RecyclerView.ViewHolder {

        TedSquareFrameLayout root;


        TedSquareImageView iv_thumbnail;

        private GalleryViewHolder(View view) {
            super(view);
            root = view.findViewById(R.id.root);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);

        }

    }


}
