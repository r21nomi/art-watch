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

class OssActivity : DrawerActivity() {

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, OssActivity::class.java)
    }

    private val mRecyclerView: RecyclerView by bindView(R.id.recyclerView)

    override val layout: Int = R.layout.activity_oss
    override val toolbarName: Int = R.string.oss_license
    override val shouldShowSpinner: Boolean = false

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSet: List<LicenseEntry> = listOf(
                Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"),
                Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"),
                Licenses.fromGitHubApacheV2("google/dagger"),
                Licenses.fromGitHubApacheV2("square/okhttp"),
                Licenses.fromGitHubApacheV2("square/retrofit"),
                Licenses.fromGitHubApacheV2("square/sqlbrite"),
                Licenses.fromGitHubApacheV2("square/leakcanary"),
                Licenses.fromGitHubApacheV2("JakeWharton/kotterknife"),
                Licenses.fromGitHubApacheV2("JakeWharton/timber"),
                Licenses.fromGitHubApacheV2("mttkay/signpost"),
                Licenses.fromGitHubApacheV2("reactivex/rxandroid", Licenses.FILE_NO_EXTENSION),
                Licenses.fromGitHubApacheV2("orfjackal/retrolambda"),
                Licenses.fromGitHubApacheV2("yqritc/RecyclerView-MultipleViewTypesAdapter", Licenses.FILE_NO_EXTENSION),
                Licenses.fromGitHubApacheV2("bumptech/glide", Licenses.FILE_NO_EXTENSION),
                Licenses.fromGitHubApacheV2("wasabeef/glide-transformations", Licenses.FILE_NO_EXTENSION),
                Licenses.fromGitHubApacheV2("facebook/stetho", Licenses.FILE_NO_EXTENSION),
                Licenses.fromGitHubApacheV2("JetBrains/kotlin"),
                Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter", Licenses.FILE_NO_EXTENSION)
        )

        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = LicenseAdapter(dataSet)

        Licenses.load(dataSet)
    }
}
