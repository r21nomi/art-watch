package com.nomi.artwatch.ui.adapter.binder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nomi.artwatch.R;
import com.nomi.artwatch.util.WindowUtil;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoSize;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Ryota Niinomi on 15/11/8.
 */
public class ArtBinder extends DataBinder<ArtBinder.ViewHolder> {

    private List<Photo> mDataSet;

    public ArtBinder(DataBindAdapter dataBindAdapter, List<Photo> dataSet) {
        super(dataBindAdapter);

        mDataSet = dataSet;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_art, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, int position) {
        PhotoSize photoSize = mDataSet.get(position).getOriginalSize();

        Glide.with(holder.mGifView.getContext())
                .load(photoSize.getUrl())
                .asGif()
                .into(new SimpleTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(GifDrawable resource, GlideAnimation<? super GifDrawable> glideAnimation) {
                        int width = WindowUtil.getWidth(holder.mGifView.getContext());
                        int height = width * resource.getIntrinsicHeight() / resource.getIntrinsicWidth();

                        ViewGroup.LayoutParams params = holder.mGifView.getLayoutParams();
                        params.width = width;
                        params.height = height;
                        holder.mGifView.setBackground(resource);

                        resource.start();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.gifView)
        GifImageView mGifView;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
