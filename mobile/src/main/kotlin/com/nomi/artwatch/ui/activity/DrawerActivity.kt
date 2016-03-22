package com.nomi.artwatch.ui.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.nomi.artwatch.R
import com.nomi.artwatch.model.BlogModel
import com.nomi.artwatch.model.LoginModel
import com.nomi.artwatch.model.UserModel
import jp.wasabeef.glide.transformations.CropCircleTransformation
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 15/12/06.
 */
abstract class DrawerActivity : InjectActivity() {

    private var mDrawerToggle: ActionBarDrawerToggle? = null

    @Inject
    lateinit var mLoginModel: LoginModel
    @Inject
    lateinit var mUserModel: UserModel
    @Inject
    lateinit var mBlogModel: BlogModel

    val mDrawerLayout: DrawerLayout by bindView(R.id.drawer_layout)
    val mUserThumb: ImageView by bindView(R.id.userThumb)
    val mUserName: TextView by bindView( R.id.userName)
    val mLogoutBtn: TextView by bindView(R.id.logoutButton)

    protected abstract val layout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        initDrawer()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initDrawer() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mDrawerToggle = object : ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.app_name,
                R.string.app_name) {
            override fun onDrawerClosed(view: View?) {
                super.onDrawerClosed(view)
            }

            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
            }
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle)
        mDrawerToggle!!.isDrawerIndicatorEnabled = true

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        mLogoutBtn.setOnClickListener({
            AlertDialog.Builder(this, R.style.DefaultDialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", {dialogInterface, i ->
                        mLoginModel.logout().subscribe({aVoid -> startActivity()})
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show()
        })

        // User info
        mUserModel
                .user
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({user ->
                    setUserThumb()
                    mUserName.text = user.name
                }, {throwable ->
                    Timber.e(throwable.message, throwable)
                })
    }

    private fun setUserThumb() {
        mBlogModel
                .getAvatar("ryotaniinomi")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({url ->
                    Glide.with(this)
                            .load(url)
                            .bitmapTransform(CropCircleTransformation(this))
                            .placeholder(R.drawable.__leak_canary_icon)
                            .into(mUserThumb)
                });
    }

    private fun startActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent);
        finish();
    }
}
