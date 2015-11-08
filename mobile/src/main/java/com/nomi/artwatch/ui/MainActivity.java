package com.nomi.artwatch.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.nomi.artwatch.R;
import com.nomi.artwatch.di.component.ActivityComponent;
import com.nomi.artwatch.model.PostModel;
import com.nomi.artwatch.model.UserModel;
import com.nomi.artwatch.ui.view.ArtView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/04.
 */
public class MainActivity extends InjectActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {

    private static final String KEY_COLOR = "COLOR";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    private CharSequence mTitle;
    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Inject
    UserModel mUserModel;
    @Inject
    PostModel mPostModel;

    @Bind(R.id.artView)
    ArtView mArtView;
    @Bind(R.id.gif)
    GifView gifImageView;

    @Override
    protected void injectDependency(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mTitle = getTitle();

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        mPostModel
                .getPhotoPost("ryotaniinomi")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mArtView.init(result);
//                    gifImageView.setGif("https://49.media.tumblr.com/4697092aaddaac4f70fa56d415b27cf9/tumblr_nv9mpn23Gp1rt67t1o1_400.gif");

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
//            displayNoConnectedDeviceDialog();
        }
    }

    @Override
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            setUpAllPickers(null);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @OnClick(R.id.btn1)
    void onBtn1Click() {
        sendConfigUpdateMessage(Color.parseColor("Red"));
    }

    @OnClick(R.id.btn2)
    void onBtn2Click() {
        sendConfigUpdateMessage(Color.parseColor("Blue"));
    }

    private void sendConfigUpdateMessage(int color) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(KEY_COLOR, color);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);
        }
    }

    private void setUpAllPickers(DataMap config) {
        setUpColorPickerSelection(config);
    }

    private void setUpColorPickerSelection(DataMap config) {
        String defaultColorName = "Black";
        int defaultColor = Color.parseColor(defaultColorName);
        int color;
        if (config != null) {
            color = config.getInt(KEY_COLOR, defaultColor);
        } else {
            color = defaultColor;
        }
        String[] colorNames = getResources().getStringArray(R.array.progress_spinner_sequence);
        for (int i = 0; i < colorNames.length; i++) {
            if (Color.parseColor(colorNames[i]) == color) {
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
