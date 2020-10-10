package m.kampukter.travelexpenses.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.travelexpenses.NetworkLiveData
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.switchStatusGPS
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return

        // Set up Action Bar
        val navController = host.navController

        appBarConfiguration =
            if (drawer_layout != null) AppBarConfiguration(navController.graph, drawer_layout)
            else AppBarConfiguration(navController.graph)

        setupActionBar(navController, appBarConfiguration)

        navigation_view?.let {
            NavigationUI.setupWithNavController(it, navController)
        }

        viewModel.savedSettings.observe(this, Observer {
            navigation_view?.menu?.findItem(R.id.currentExchangeFragment)?.isVisible =
                it?.defCurrency != 0
            if (switchStatusGPS != it?.statusGPS && it?.statusGPS != null) switchStatusGPS = it.statusGPS
            navigation_view?.menu?.findItem(R.id.mapExpensesFragment)?.isVisible =
                switchStatusGPS == 1

        })
        NetworkLiveData.observe(this, Observer {
            navigation_view?.menu?.findItem(R.id.currentExchangeFragment)?.isVisible =
                it and (mainApplication.getActiveCurrencySession() != null)
            navigation_view?.menu?.findItem(R.id.mapExpensesFragment)?.isVisible =
                it && (switchStatusGPS == 1)
        })

    }

    private fun setupActionBar(navController: NavController, appBarConfig: AppBarConfiguration) {
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.my_nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}