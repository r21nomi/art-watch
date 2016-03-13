package com.nomi.artwatch.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.nomi.artwatch.R;
import com.nomi.artwatch.di.component.ActivityComponent;
import com.nomi.artwatch.model.LoginModel;
import com.nomi.artwatch.model.PostModel;
import com.nomi.artwatch.model.UserModel;
import com.nomi.artwatch.ui.view.ArtView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/04.
 */
public class MainActivity extends DrawerActivity {

    private static final String PATH_OF_GIF = "/gif";
    private static final String KEY_GIF = "gif";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Inject
    UserModel mUserModel;
    @Inject
    PostModel mPostModel;
    @Inject
    LoginModel mLoginModel;

    @Bind(R.id.artView)
    ArtView mArtView;

    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle connectionHint) {
            if (mPeerId != null) {
                Timber.d("Connected to wear.");
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            // TODO：Handling
        }
    };

    private GoogleApiClient.OnConnectionFailedListener mGoogleConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // TODO：Error handling
        }
    };

    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void injectDependency(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mGoogleConnectionCallback)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .addApi(Wearable.API)
                .build();

        if (mLoginModel.isAuthorized()) {
            // Already authorized.
            fetchGifPosts();

        } else {
            // Authorize now.
            authorize();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Authorize to Tumblr.
     */
    private void authorize() {
        mLoginModel
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(authUrl -> {
                    Timber.d("Auth URL : " + authUrl);
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse(authUrl)));

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                });
    }

    /**
     * Fetch Gif posts from Tumblr.
     */
    private void fetchGifPosts() {
        mPostModel
                .getPhotoPost("ryotaniinomi")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    mArtView.init(items, this::onGifSelected);

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                });
    }

    /**
     * Send an request to change gif image with selected one.
     *
     * @param url
     */
    private void onGifSelected(String url) {
        if (mPeerId != null) {
            Glide.with(this)
                    .load(url)
                    .asGif()
                    .into(new SimpleTarget<GifDrawable>() {
                        @Override
                        public void onResourceReady(GifDrawable resource, GlideAnimation<? super GifDrawable> glideAnimation) {
                            // Convert GifDrawable to Asset.
                            Asset asset = createAssetFromDrawable(resource);

                            PutDataMapRequest dataMap = PutDataMapRequest.create(PATH_OF_GIF);
                            dataMap.getDataMap().putAsset(KEY_GIF, asset);
                            PutDataRequest request = dataMap.asPutDataRequest();

                            // Send the request.
                            Wearable.DataApi.putDataItem(mGoogleApiClient, request);

                            Toast.makeText(MainActivity.this, "changed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Create asset for sending the gif data to wear.
     *
     * @param drawable
     * @return
     */
    private Asset createAssetFromDrawable(GifDrawable drawable) {
        byte[] byteArray = drawable.getData();
        return Asset.createFromBytes(byteArray);
    }
}
