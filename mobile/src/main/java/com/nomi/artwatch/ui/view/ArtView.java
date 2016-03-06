package com.nomi.artwatch.ui.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.nomi.artwatch.ui.adapter.binder.ArtBinder;
import com.tumblr.jumblr.types.Photo;
import com.yqritc.recyclerviewmultipleviewtypesadapter.ListBindAdapter;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by Ryota Niinomi on 2015/11/08.
 */
public class ArtView extends RecyclerView {

    private ListBindAdapter mAdapter = new ListBindAdapter();
    private Action1<String> mOnSelect;

    public ArtView(Context context) {
        this(context, null);
    }

    public ArtView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArtView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(List<Photo> photos, Action1<String> onSelect) {
        mOnSelect = onSelect;

        initAdapter(photos);
    }

    private void initAdapter(List<Photo> photos) {
        ArtBinder artBinder = new ArtBinder(mAdapter, photos, mOnSelect);

        mAdapter.addBinder(artBinder);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);
        setHasFixedSize(false);
        setLayoutManager(layoutManager);
        setAdapter(mAdapter);
    }
}
