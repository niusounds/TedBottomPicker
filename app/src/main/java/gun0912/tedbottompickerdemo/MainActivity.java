package gun0912.tedbottompickerdemo;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.Content;
import gun0912.tedbottompicker.TedBottomPicker;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_image;
    private List<Content> selectedUriList;
    private Uri selectedUri;
    private ViewGroup mSelectedImagesContainer;
    private RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_image = findViewById(R.id.iv_image);
        mSelectedImagesContainer = findViewById(R.id.selected_photos_container);
        requestManager = Glide.with(this);
        setMultiShowButton();
    }

    private void setMultiShowButton() {

        Button btnMultiShow = findViewById(R.id.btn_multi_show);
        btnMultiShow.setOnClickListener(view -> {

            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {

                    TedBottomPicker.with(MainActivity.this)
                        //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
//                        .setFilterType(Type.Image) // for image only
//                        .setFilterType(Type.Video) // for video only
                        .setTitle("Pick photos")
                        .setCompleteButtonText("Done")
                        .setEmptySelectionText("No Select")
                        .setSelectMaxCount(4)
                        .setSelectMaxImageCount(4)
                        .setSelectMaxVideoCount(1)
                        .showMultiImage(contentList -> {
                            selectedUriList = contentList;
                            final List<Uri> uriList = new ArrayList<>();
                            for (Content content : contentList) {
                                uriList.add(content.getUri());
                            }
                            showUriList(uriList);
                        });


                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                }


            };

            checkPermission(permissionlistener);

        });

    }


    private void checkPermission(PermissionListener permissionlistener) {
        TedPermission.with(MainActivity.this)
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check();
    }

    private void showUriList(List<Uri> uriList) {
        // Remove all views before
        // adding the new ones.
        mSelectedImagesContainer.removeAllViews();

        iv_image.setVisibility(View.GONE);
        mSelectedImagesContainer.setVisibility(View.VISIBLE);

        int widthPixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int heightPixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());


        for (Uri uri : uriList) {

            View imageHolder = LayoutInflater.from(this).inflate(R.layout.image_item, null);
            ImageView thumbnail = imageHolder.findViewById(R.id.media_image);

            requestManager
                .load(uri.toString())
                .apply(new RequestOptions().fitCenter())
                .into(thumbnail);

            mSelectedImagesContainer.addView(imageHolder);

            thumbnail.setLayoutParams(new FrameLayout.LayoutParams(widthPixel, heightPixel));

        }

    }
}
