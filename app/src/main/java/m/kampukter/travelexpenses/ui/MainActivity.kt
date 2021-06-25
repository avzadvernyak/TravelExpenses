package m.kampukter.travelexpenses.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
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
const val PERMISSION_REQUEST_CAMERA = 2

val permissionsForCamera = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

// Разрешения для работы с локацией
//    не реализовано Manifest.permission.WRITE_EXTERNAL_STORAGE
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainAppBarLayout.setExpanded(true)
            addExpenseFab.hide()
            addExpensesExtendedFab.hide()
            toolbar.setOnClickListener(null)
            when (destination.id) {
                R.id.homeExpensesFragment -> {
                    cardViewToolbarCardView.visibility = View.VISIBLE
                    enableLayoutBehaviour()
                    addExpensesExtendedFab.show()

                }
                R.id.expenseFragment -> {
                    enableLayoutBehaviour()
                    addExpenseFab.show()
                    cardViewToolbarCardView.visibility = View.VISIBLE
                }
                R.id.foldersFragment -> {
                    enableLayoutBehaviour()
                    addExpenseFab.show()
                    cardViewToolbarCardView.visibility = View.VISIBLE
                }
                R.id.exchangeFragment -> {
                    enableLayoutBehaviour()
                    cardViewToolbarCardView.visibility = View.VISIBLE
                }

                R.id.mapPointFragment -> {
                    disableLayoutBehaviour()
                    cardViewToolbarCardView.visibility = View.GONE
                }

                R.id.mapGoogleFragment -> {
                    disableLayoutBehaviour()
                    cardViewToolbarCardView.visibility = View.GONE
                }

                R.id.cameraXFragment,
                R.id.attachmentPhotoViewFragment,
                R.id.delAttachmentPhotoDialogFragment -> cardViewToolbarCardView.visibility =
                    View.GONE

                R.id.foldersAddFragment,
                R.id.mapExpensesFragment,
                R.id.galleryFragment,
                R.id.editExpensesFragment,
                R.id.addExpensesFragment -> {
                    disableLayoutBehaviour()
                    cardViewToolbarCardView.visibility = View.VISIBLE
                }
                /*R.id.expensesMoveFragment -> {
                    toolbar.visibility = View.VISIBLE
                }*/
                else -> cardViewToolbarCardView.visibility = View.VISIBLE
            }
        }

        appBarConfiguration =
            if (drawer_layout != null) AppBarConfiguration(navController.graph, drawer_layout)
            else AppBarConfiguration(navController.graph)

        setupActionBar(navController, appBarConfiguration)

        toolbar.setupWithNavController(navController, appBarConfiguration)

        navigation_view?.let {
            NavigationUI.setupWithNavController(it, navController)
        }

        viewModel.savedSettings.observe(this, { settings ->
            // Изменение текущей валюты в зависимости от настроек программы
            settings?.let {
                if (it.defCurrency != mainApplication.getActiveCurrencySession())
                    mainApplication.changeActiveCurrency(it.defCurrency)
            }

            // Скрыть в меня Курсы так как не выбрана валюта
            navigation_view?.menu?.findItem(R.id.exchangeFragment)?.isVisible =
                settings?.defCurrency != 0
        })
        NetworkLiveData.observe(this, {
            navigation_view?.menu?.findItem(R.id.exchangeFragment)?.isVisible =
                it and (mainApplication.getActiveCurrencySession() != null)
            navigation_view?.menu?.findItem(R.id.mapExpensesFragment)?.isVisible = it
        })

    }

    // заменил на toolbar.setupWithNavController(navController, appBarConfiguration)
    /*  override fun onSupportNavigateUp(): Boolean {
          return findNavController(R.id.my_nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
      }*/

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
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.all { it != PackageManager.PERMISSION_GRANTED }) {

                    Snackbar.make(
                        drawer_layout,
                        "Запрошенные разрешения не получены.",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

    }

    private fun setupActionBar(navController: NavController, appBarConfig: AppBarConfiguration) {
        setupActionBarWithNavController(navController, appBarConfig)
    }

    private fun enableLayoutBehaviour() {
        val param = cardViewToolbarCardView.layoutParams as AppBarLayout.LayoutParams
        param.scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
        cardViewToolbarCardView.requestLayout()

    }

    private fun disableLayoutBehaviour() {
        val param = cardViewToolbarCardView.layoutParams as AppBarLayout.LayoutParams
        param.scrollFlags = 0
    }

}