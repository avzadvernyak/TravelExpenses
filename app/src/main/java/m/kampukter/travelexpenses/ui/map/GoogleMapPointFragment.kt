package m.kampukter.travelexpenses.ui.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.google_map_point_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.ui.STATUS_GPS_OFF
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.ui.permissionsForLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.concurrent.schedule

class GoogleMapPointFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var map: GoogleMap? = null
    private val myOnMapReadyCallback = OnMapReadyCallback { googleMap ->
        googleMap ?: return@OnMapReadyCallback
        this.map = googleMap

        with(googleMap) {
            uiSettings.isCompassEnabled = true

            viewModel.lastMapTypeLiveData.observe(viewLifecycleOwner) { googleMapType ->
                googleMapType?.let { mapType = it }
                controlMapTypes(googleMap)
            }
            viewModel.expensesEdit.observe(viewLifecycleOwner) { (expenses, _) ->
                expenses.location?.let {
                    val position = LatLng(it.latitude, it.longitude)
                    clear()
                    val title = "${
                        DateFormat.format(
                            "dd/MM/yyyy HH:mm",
                            expenses.dateTime
                        )
                    } ${expenses.sum} ${expenses.currency}"

                    val snippet = "${expenses.expense}(${expenses.note})"

                    addMarker(
                        MarkerOptions().position(position).title(title).snippet(snippet)
                    )
                    moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))

                    markerFAB.setOnClickListener {
                        moveCamera(CameraUpdateFactory.newLatLng(position))
                    }
                }
            }
        }
    }

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            locationResult.locations.last()?.let {

                map?.uiSettings?.isMyLocationButtonEnabled = true
                map?.uiSettings?.isMapToolbarEnabled = false
            }
        }
    }
    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.google_map_point_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragmentContainerView) as SupportMapFragment
        mapFragment.getMapAsync(myOnMapReadyCallback)


    }

    override fun onResume() {
        super.onResume()

        context?.let { context ->

            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context.applicationContext)

            viewModel.savedSettingsLiveData.observe(viewLifecycleOwner, { settings ->
                if (settings.statusGPS == STATUS_GPS_ON) {
                    locationRequest.let {
                        LocationSettingsRequest.Builder().addLocationRequest(it)
                    }
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
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper()
                            )
                            map?.isMyLocationEnabled = true
                        }
                    } else {
                        viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                        findNavController().navigate(R.id.toLocationPermissionsDialogFragment)
                    }
                } else {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun controlMapTypes(googleMap: GoogleMap) {

        // When map is initially loaded, determine which map type option to 'select'
        when (googleMap.mapType) {
            GoogleMap.MAP_TYPE_HYBRID -> {
                hybridTypeBackgroundView.visibility = View.VISIBLE
                hybridTypeTextView.setTextColor(Color.BLUE)
            }
            else -> {
                defaultTypeBackgroundView.visibility = View.VISIBLE
                defaultTypeTextView.setTextColor(Color.BLUE)
            }
        }

        // Set click listener on FAB to open the map type selection view
        mapTypeFAB.setOnClickListener {

            // Start animator to reveal the selection view, starting from the FAB itself

            // 8 is android:layout_marginBottom="8dp"
            val markerFabY = mapTypeSelectionLayout.height - mapTypeFAB.height - 8
            ObjectAnimator.ofFloat(markerFAB, View.TRANSLATION_Y, -markerFabY.toFloat())
                .apply {
                    duration = 200
                    start()
                }
            val anim = ViewAnimationUtils.createCircularReveal(
                mapTypeSelectionLayout,
                mapTypeSelectionLayout.width - (mapTypeFAB.width / 2),
                mapTypeSelectionLayout.height - (mapTypeFAB.height / 2),
                mapTypeFAB.width / 2f,
                mapTypeSelectionLayout.width.toFloat()
            )
            anim.duration = 200
            anim.interpolator = AccelerateDecelerateInterpolator()

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mapTypeSelectionLayout.visibility = View.VISIBLE
                }
            })
            anim.start()
            mapTypeFAB.visibility = View.INVISIBLE
        }
        // Set click listener on the map to close the map type selection view
        googleMap.setOnMapClickListener {

            // Conduct the animation if the FAB is invisible (window open)
            if (mapTypeFAB.visibility == View.INVISIBLE) {

                // Start animator close and finish at the FAB position
                ObjectAnimator.ofFloat(markerFAB, View.TRANSLATION_Y, 0f).apply {
                    duration = 200
                    start()
                }
                val anim = ViewAnimationUtils.createCircularReveal(
                    mapTypeSelectionLayout,
                    mapTypeSelectionLayout.width - (mapTypeFAB.width / 2),
                    mapTypeSelectionLayout.height - (mapTypeFAB.height / 2),
                    mapTypeSelectionLayout.width.toFloat(),
                    mapTypeFAB.width / 2f
                )
                anim.duration = 200
                anim.interpolator = AccelerateDecelerateInterpolator()

                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mapTypeSelectionLayout.visibility = View.INVISIBLE
                    }
                })

                // Set a delay to reveal the FAB. Looks better than revealing at end of animation
                Timer().schedule(100) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mapTypeFAB.visibility = View.VISIBLE
                    }
                }

                anim.start()
            }
        }

        // Handle selection of the Default map type
        defaultTypeImageButton.setOnClickListener {
            defaultTypeBackgroundView.visibility = View.VISIBLE
            hybridTypeBackgroundView.visibility = View.INVISIBLE
            defaultTypeTextView.setTextColor(Color.BLUE)
            hybridTypeTextView.setTextColor(Color.parseColor("#808080"))
            viewModel.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        }

        // Handle selection of the Satellite map type
        hybridTypeImageButton.setOnClickListener {
            defaultTypeBackgroundView.visibility = View.INVISIBLE
            hybridTypeBackgroundView.visibility = View.VISIBLE
            defaultTypeTextView.setTextColor(Color.parseColor("#808080"))
            hybridTypeTextView.setTextColor(Color.BLUE)
            viewModel.setMapType(GoogleMap.MAP_TYPE_HYBRID)
        }

    }
}