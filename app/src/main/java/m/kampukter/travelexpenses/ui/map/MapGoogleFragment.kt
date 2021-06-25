package m.kampukter.travelexpenses.ui.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.add_expenses_fragment.*
import kotlinx.android.synthetic.main.map_google_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FilterForExpensesMap
import m.kampukter.travelexpenses.ui.STATUS_GPS_OFF
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.ui.permissionsForLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*


class MapGoogleFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private var map: GoogleMap? = null

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            locationResult.locations.last()?.let {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            }
        }
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.customView
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModel.setFilterForExpensesMap(FilterForExpensesMap.All)

        }
    }

    private val myOnMapReadyCallback = OnMapReadyCallback { googleMap ->
        googleMap ?: return@OnMapReadyCallback
        this.map = googleMap

        with(googleMap) {
            viewModel.lastMapTypeLiveData.observe(viewLifecycleOwner) { googleMapType ->
                googleMapType?.let{ mapType = it }
                controlMapTypes(googleMap)
            }
            val builder = LatLngBounds.Builder()
            viewModel.expensesInFolderForMap.observe(viewLifecycleOwner) { (lastExpenses, filter) ->
                clear()
                val expenses = when (filter) {
                    is FilterForExpensesMap.DateRangeFilter -> {
                        val filteredExpenses =
                            lastExpenses.filter { it.dateTime.time in filter.startPeriod..filter.endPeriod && it.location != null }
                        if (filteredExpenses.isNotEmpty()) {
                            val actionMode =
                                (context as AppCompatActivity).startSupportActionMode(
                                    actionModeCallback
                                )
                            actionMode?.title = "Найдено записей: ${filteredExpenses.size}"
                            val startDate =
                                DateFormat.format("dd/MM/yyyy", filter.startPeriod).toString()
                            val endDate =
                                DateFormat.format("dd/MM/yyyy", filter.endPeriod).toString()
                            actionMode?.subtitle = "c $startDate по $endDate"
                        }
                        filteredExpenses
                    }
                    is FilterForExpensesMap.ExpenseFilter -> {
                        val filteredExpenses =
                            lastExpenses.filter { it.expense_id == filter.expense.id && it.location != null }
                        val actionMode =
                            (context as AppCompatActivity).startSupportActionMode(actionModeCallback)
                        actionMode?.title = "Найдено записей: ${filteredExpenses.size}"
                        actionMode?.subtitle = filter.expense.name
                        filteredExpenses
                    }
                    else -> lastExpenses.filter { it.location != null }
                }
                expenses.forEach { itemExpenses ->
                    itemExpenses.location?.let { item ->
                        val latLng = LatLng(item.latitude, item.longitude)
                        addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("${itemExpenses.expense}; ${itemExpenses.sum}${itemExpenses.currency}")
                                .snippet(itemExpenses.note)
                        )
                        builder.include(latLng)
                    }
                }
                val padding = 128 // offset from edges of the map in pixels
                val cu = CameraUpdateFactory.newLatLngBounds(builder.build(), padding)
                moveCamera(cu)
            }
        }
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
                Handler().postDelayed({
                    kotlin.run {
                        mapTypeFAB.visibility = View.VISIBLE
                    }
                }, 100)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.map_google_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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
                            /*val locationResult = fusedLocationProviderClient.lastLocation
                            locationResult.addOnCompleteListener(activity as AppCompatActivity) { task ->
                                Log.w("blabla", "Listener ")
                                if (task.isSuccessful) {
                                    Log.w("blabla", "Listener isSuccessful true")
                                    // Set the map's camera position to the current location of the device.
                                    task.result?.let {
                                        Log.w("blabla", "Location W ")
                                        map?.isMyLocationEnabled = true
                                        map?.uiSettings?.isMyLocationButtonEnabled = true
                                    }
                                } else {
                                    Log.w("blabla", "Listener isSuccessful false")
                                    map?.isMyLocationEnabled = false
                                    map?.uiSettings?.isMyLocationButtonEnabled = false
                                }
                            }*/
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_google_app_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pointsDateFilter -> {
                val pickerRange = MaterialDatePicker.Builder.dateRangePicker().build()
                pickerRange.show(parentFragmentManager, "Picker")
                pickerRange.addOnPositiveButtonClickListener { dateSelected ->
                    val start = DateFormat.format("yyyyMMdd", dateSelected.first).toString()
                    val startLong =
                        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(start)?.time
                    val end = DateFormat.format("yyyyMMdd", dateSelected.second).toString()
                    val endLong =
                        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(end)?.time
                    if (startLong != null && endLong != null)
                        viewModel.setFilterForExpensesMap(
                            FilterForExpensesMap.DateRangeFilter(
                                startLong,
                                endLong + (24 * 60 * 60 * 1000) - 1000
                            )
                        )
                }
            }
            R.id.pointsExpenseFilter -> {
                findNavController().navigate(R.id.toChoiceExpenseForMapFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}