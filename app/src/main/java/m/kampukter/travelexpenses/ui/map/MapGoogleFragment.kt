package m.kampukter.travelexpenses.ui.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.map_google_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FilterForExpensesMap
import m.kampukter.travelexpenses.ui.STATUS_GPS_OFF
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.ui.permissionsForLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule


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
                map?.uiSettings?.isMapToolbarEnabled = false
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
            filtersLayout?.visibility = View.INVISIBLE
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
            filtersLayout?.visibility = View.VISIBLE
        }
    }

    private val myOnMapReadyCallback = OnMapReadyCallback { googleMap ->
        googleMap ?: return@OnMapReadyCallback
        this.map = googleMap

        with(googleMap) {

            uiSettings.isCompassEnabled = true

            viewModel.lastMapTypeLiveData.observe(viewLifecycleOwner) { googleMapType ->
                googleMapType?.let { mapType = it }
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
                var isFoundPoint = false
                expenses.forEach { itemExpenses ->
                    itemExpenses.location?.let { item ->
                        isFoundPoint = true
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
                if (isFoundPoint) {
                    val padding = 256 // offset from edges of the map in pixels
                    val cu = CameraUpdateFactory.newLatLngBounds(builder.build(), padding)
                    moveCamera(cu)
                } else {
                    if (filter !is FilterForExpensesMap.All) {
                        viewModel.setFilterForExpensesMap(FilterForExpensesMap.All)
                        Snackbar.make(
                            googleMapLayout,
                            getString(R.string.points_is_empty),
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        dateFilterButton.setOnClickListener {
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
        expenseFilterButton.setOnClickListener {
            findNavController().navigate(R.id.toChoiceExpenseForMapFragment)
        }
    }

    private fun controlMapTypes(googleMap: GoogleMap) {
        var delta: Float? = null
        context?.let { context ->
            delta =
                context.resources.displayMetrics.density * (filtersLayout.marginTop + filtersLayout.height)
        }

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

            when {
                mapTypeFAB.visibility == View.INVISIBLE -> {

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
                    Timer().schedule(100) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mapTypeFAB.visibility = View.VISIBLE
                        }
                    }

                    anim.start()
                }
                filtersLayout.visibility == View.INVISIBLE -> {
                    delta?.let {
                        ObjectAnimator.ofFloat(filtersLayout, "translationY", 0F).apply {
                            duration = 200
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationStart(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    filtersLayout.visibility = View.VISIBLE

                                }
                            })
                            start()

                        }
                    }

                    /* val anim = ViewAnimationUtils.createCircularReveal(
                         filtersLayout,
                         filtersLayout.width / 2,
                         filtersLayout.height / 2,
                         0f,
                         filtersLayout.width.toFloat()
                     )
                     anim.duration = 200
                     anim.interpolator = AccelerateDecelerateInterpolator()

                     anim.addListener(object : AnimatorListenerAdapter() {
                         override fun onAnimationStart(animation: Animator?) {
                             super.onAnimationEnd(animation)
                             filtersLayout.visibility = View.VISIBLE
                         }
                     })
                     anim.start()*/
                }
                else -> {
                    delta?.let {
                        ObjectAnimator.ofFloat(filtersLayout, "translationY", -it).apply {
                            duration = 200
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    filtersLayout.visibility = View.INVISIBLE
                                }
                            })
                            start()
                        }
                    }
                    /*val path = Path().apply {
                        arcTo(0f, 0f, 1000f, 1000f, 270f, -180f, true)
                    }
                    val pathInterpolator = PathInterpolator(path)
                    val animation = ObjectAnimator.ofFloat(filtersLayout, "translationX", 100f).apply {
                        interpolator = pathInterpolator
                        start()
                    }*/
                }
                /*val anim = ViewAnimationUtils.createCircularReveal(
                    filtersLayout,
                    filtersLayout.width / 2 ,
                    filtersLayout.height / 2 ,
                    filtersLayout.width.toFloat(),
                    0F
                )
                anim.duration = 200
                anim.interpolator = AccelerateDecelerateInterpolator()

                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        filtersLayout.visibility = View.INVISIBLE
                    }
                })
                anim.start()
            }*/
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