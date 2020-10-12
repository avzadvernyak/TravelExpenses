package m.kampukter.travelexpenses.ui

import android.Manifest
import android.content.pm.PackageManager
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.travelexpenses.NetworkLiveData
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

const val STATUS_GPS_ON = 1
const val STATUS_GPS_OFF = 0

const val PERMISSION_REQUEST_GPS = 1

// Разрешения для работы с локацией
//    пока не реализовано Manifest.permission.WRITE_EXTERNAL_STORAGE
val permissionsForLocation = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

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

        viewModel.savedSettings.observe(this, Observer { settings ->
            // Изменение текущей валюты в зависимости от настроек программы
            settings?.let {
                if (it.defCurrency != mainApplication.getActiveCurrencySession())
                    mainApplication.changeActiveCurrency(it.defCurrency)
            }

            // Скрыть в меня Курсы так как не выбрана валюта
            navigation_view?.menu?.findItem(R.id.currentExchangeFragment)?.isVisible =
                settings?.defCurrency != 0
        })
        NetworkLiveData.observe(this, Observer {
            navigation_view?.menu?.findItem(R.id.currentExchangeFragment)?.isVisible =
                it and (mainApplication.getActiveCurrencySession() != null)
            navigation_view?.menu?.findItem(R.id.mapExpensesFragment)?.isVisible = it
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.my_nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_GPS -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED })
                    viewModel.setSettingStatusGPS(STATUS_GPS_ON)
                else {
                    Snackbar.make(
                        drawer_layout,
                        "Запрошенные разрешения не получены. Опция отключена",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                    viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                }
            }
        }

    }

    private fun setupActionBar(navController: NavController, appBarConfig: AppBarConfiguration) {
        setupActionBarWithNavController(navController, appBarConfig)
    }
}