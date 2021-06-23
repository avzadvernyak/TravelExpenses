package m.kampukter.travelexpenses.ui.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.map_point_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.ui.STATUS_GPS_OFF
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.ui.permissionsForLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapPointFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var myLocationNewOverlay: MyLocationNewOverlay? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.map_point_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentLocationFAB.visibility = View.INVISIBLE

        Configuration.getInstance()
            .load(view.context, PreferenceManager.getDefaultSharedPreferences(view.context))


        with(pointMapView) {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            isTilesScaledToDpi = true
        }
        val mapController = pointMapView.controller
        viewModel.expensesEdit.observe(viewLifecycleOwner) { (expenses, _) ->

            expenses.location?.let {
                pointMapView.overlays.clear()
                val location = GeoPoint(it.latitude, it.longitude)

                mapController.setZoom(12.0)
                mapController.setCenter(location)

                val startMarker = Marker(pointMapView).apply {
                    position = location
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    title = "${
                        DateFormat.format("dd/MM/yyyy HH:mm", expenses.dateTime)
                    } ${expenses.sum} ${expenses.currency}"
                    snippet = expenses.expense
                    subDescription = expenses.note
                }

                pointMapView.overlays.add(startMarker)
                whereFAB.setOnClickListener {
                    mapController.animateTo(location)
                }
            }

        }

    }

    override fun onResume() {
        super.onResume()

        currentLocationFAB.visibility = View.INVISIBLE
        pointMapView.onResume()
        context?.let { context ->
            viewModel.savedSettingsLiveData.observe(viewLifecycleOwner, { settings ->
                if (settings.statusGPS == STATUS_GPS_ON) {
                    val isLocationPermission = permissionsForLocation.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    if (isLocationPermission) {
                        val manager =
                            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } else {

                            myLocationNewOverlay = MyLocationNewOverlay(
                                GpsMyLocationProvider(context),
                                pointMapView
                            )
                            myLocationNewOverlay?.enableMyLocation()

                            myLocationNewOverlay?.runOnFirstFix {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    pointMapView.overlays.add(myLocationNewOverlay)
                                    currentLocationFAB.visibility = View.VISIBLE
                                    currentLocationFAB.setOnClickListener {
                                        pointMapView.controller.animateTo(myLocationNewOverlay?.myLocation)
                                    }
                                }
                            }
                        }
                    } else {
                        viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                        findNavController().navigate(R.id.toLocationPermissionsDialogFragment)
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()

        myLocationNewOverlay?.disableMyLocation()
        currentLocationFAB.visibility = View.INVISIBLE
        pointMapView.onPause()
    }
}