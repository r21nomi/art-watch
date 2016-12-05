package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import butterknife.bindView
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import net.yslibrary.licenseadapter.LicenseAdapter
import net.yslibrary.licenseadapter.LicenseEntry
import net.yslibrary.licenseadapter.Licenses
import java.util.*

class OssActivity : DrawerActivity() {

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, OssActivity::class.java)
    }

    val mRecyclerView: RecyclerView by bindView(R.id.recyclerView)

    override val layout: Int = R.layout.activity_oss
    override val toolbarName: Int = R.string.oss_license
    override val shouldShowSpinner: Boolean = false

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataset: MutableList<LicenseEntry> = ArrayList()

        dataset.add(Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"))
        dataset.add(Licenses.fromGitHub("google/dagger"))
        dataset.add(Licenses.fromGitHub("square/okhttp"))
        dataset.add(Licenses.fromGitHub("square/retrofit"))
        dataset.add(Licenses.fromGitHub("square/sqlbrite"))
        dataset.add(Licenses.fromGitHub("square/leakcanary"))
        dataset.add(Licenses.fromGitHub("JakeWharton/kotterknife"))
        dataset.add(Licenses.fromGitHub("JakeWharton/timber"))
        dataset.add(Licenses.fromGitHub("mttkay/signpost", Licenses.LICENSE_APACHE_V2))
        dataset.add(Licenses.fromGitHub("reactivex/rxandroid", Licenses.FILE_NO_EXTENSION))
        dataset.add(Licenses.fromGitHub("orfjackal/retrolambda"))
        dataset.add(Licenses.fromGitHub("yqritc/RecyclerView-MultipleViewTypesAdapter", Licenses.FILE_NO_EXTENSION))
        dataset.add(Licenses.fromGitHub("bumptech/glide", Licenses.FILE_NO_EXTENSION))
        dataset.add(Licenses.fromGitHub("wasabeef/glide-transformations", Licenses.FILE_NO_EXTENSION))
        dataset.add(Licenses.fromGitHub("facebook/stetho", Licenses.FILE_NO_EXTENSION))
        dataset.add(Licenses.fromGitHub("JetBrains/kotlin", Licenses.LICENSE_APACHE_V2))
        dataset.add(Licenses.fromGitHub("yshrsmz/LicenseAdapter", Licenses.FILE_NO_EXTENSION))

        val adapter = LicenseAdapter(dataset)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = adapter

        Licenses.load(dataset)
    }
}
