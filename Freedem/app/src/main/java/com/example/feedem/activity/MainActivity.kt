package com.example.feedem.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.sharedPref.SharedPreference
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var vm: ViewModelFeedem

    private val dl: DrawerLayout by lazy { findViewById(R.id.dlMain) }
    private lateinit var actionBarDrawer: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //View Model
        vm = ViewModelProvider(this)[ViewModelFeedem::class.java]

        //SharedPreference(this).save("baseUrl", vm.baseUrl)

        //Notification
        val intent = intent.getStringExtra("url")
        val dbI = ReportDatabaseBuilder.getInstance(this).ReportDao()

        if (intent != null) {
            val savedDocument = runBlocking { dbI.getReportFromUrl(intent.toString()) }
            if (savedDocument != null) {
                savedDocument.isRead = true
                CoroutineScope(Dispatchers.IO).launch {
                    dbI.updateReport(savedDocument)
                }
            }
            //val i = Intent(this, ViewActivity::class.java)
            //i.putExtra("urlView", intent)
            //startActivity(i)
        }

        //Nav Controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcvMain) as NavHostFragment?
        val navController = navHostFragment!!.navController

        val nv = findViewById<NavigationView>(R.id.nvMain)
        nv.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.title) {
                "Home" -> navController.navigate(R.id.homeFragment)
                "Settings" -> navController.navigate(R.id.settingsFragment)
                "About" -> navController.navigate(R.id.aboutFragment)
                "Logout" -> logOut()
            }

            //This is for maintaining the behavior of the Navigation view
            NavigationUI.onNavDestinationSelected(menuItem, navController)
            dl.closeDrawer(GravityCompat.START)
            true
        }
        actionBarDrawer = ActionBarDrawerToggle(
            this,
            dl,
            R.string.nav_open,
            R.string.nav_close
        )
        dl.addDrawerListener(actionBarDrawer)
        actionBarDrawer.syncState()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            hide()
        }
        val w = window
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        w.statusBarColor = ContextCompat.getColor(this, R.color.feedemBlack)
    }

    private fun logOut() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcvMain) as NavHostFragment?
        val navCon = navHostFragment!!.navController
        val sp = SharedPreference(this)
        vm.setAppTokenKeyV("")
        vm.setUserTokenKeyV("")
        sp.removeValue("userName")
        sp.removeValue("userPassword")
        navCon.navigate(R.id.loginFragment)
        Toast.makeText(this, R.string.log_out, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawer.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}