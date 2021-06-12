package m.kampukter.travelexpenses.ui.map

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.map_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FilterForExpensesMap
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*


class MapExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var firstStart = true

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.map_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance()
            .load(view.context, PreferenceManager.getDefaultSharedPreferences(view.context))

        with( mapMapView ) {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            isTilesScaledToDpi = true
        }
        val mapController = mapMapView.controller

        viewModel.paramMapViewLiveData.observe(viewLifecycleOwner, {
            firstStart = false
            mapController.setZoom(it.first)
            mapController.setCenter(it.second)
        })
        if (firstStart) {
            mapController.setZoom(8.5)
            mapController.setCenter(GeoPoint(48.0154, 37.8647))
        }
        viewModel.expensesInFolderForMap.observe(viewLifecycleOwner) { (lastExpenses, filter) ->

            mapMapView.overlays.clear()

            var pointsLongitudeMax: Double = -90.0
            var pointsLongitudeMin = 90.0
            var pointsLatitudeMax: Double = -180.0
            var pointsLatitudeMin = 180.0

            val expenses = when (filter) {
                is FilterForExpensesMap.DateRangeFilter -> {
                    val filteredExpenses =
                        lastExpenses.filter { it.dateTime.time in filter.startPeriod..filter.endPeriod && it.location != null }
                    if (filteredExpenses.isNotEmpty()) {
                        val actionMode =
                            (context as AppCompatActivity).startSupportActionMode(actionModeCallback)
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
                itemExpenses.location?.let { location ->
                    if (location.latitude > pointsLatitudeMax) pointsLatitudeMax = location.latitude
                    if (location.latitude < pointsLatitudeMin) pointsLatitudeMin = location.latitude
                    if (location.longitude > pointsLongitudeMax) pointsLongitudeMax =
                        location.longitude
                    if (location.longitude < pointsLongitudeMin) pointsLongitudeMin =
                        location.longitude

                    val myMarker = Marker(mapMapView)
                    myMarker.position = GeoPoint(location.latitude, location.longitude)

                    myMarker.title = "${
                        DateFormat.format("dd/MM/yyyy HH:mm", itemExpenses.dateTime)
                    } ${itemExpenses.sum} ${itemExpenses.currency}"

                    myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    myMarker.snippet = itemExpenses.expense
                    myMarker.subDescription = itemExpenses.note

                    mapMapView.overlays.add(myMarker)
                }
            }
            if (expenses.isNotEmpty()) {
                val centerLatitude =
                    pointsLatitudeMin + ((pointsLatitudeMax - pointsLatitudeMin) / 2.0)
                val centerLongitude =
                    pointsLongitudeMin + ((pointsLongitudeMax - pointsLongitudeMin) / 2.0)


                if (pointsLatitudeMax - pointsLatitudeMin == -360.0) {
                    Snackbar.make(view, getString(R.string.points_is_empty), Snackbar.LENGTH_SHORT)
                        .show()
                    if (filter !is FilterForExpensesMap.All) viewModel.setFilterForExpensesMap(FilterForExpensesMap.All)
                }

                when  {
                    pointsLatitudeMax - pointsLatitudeMin > 360 -> mapController.setZoom(0.1)
                    pointsLatitudeMax - pointsLatitudeMin > 180 -> mapController.setZoom(1.0)
                    pointsLatitudeMax - pointsLatitudeMin > 90 -> mapController.setZoom(2.0)
                    pointsLatitudeMax - pointsLatitudeMin > 45 -> mapController.setZoom(3.0)
                    pointsLatitudeMax - pointsLatitudeMin > 22.5 -> mapController.setZoom(4.0)
                    pointsLatitudeMax - pointsLatitudeMin > 11.25 -> mapController.setZoom(5.0)
                    pointsLatitudeMax - pointsLatitudeMin > 5.625 -> mapController.setZoom(6.0)
                    pointsLatitudeMax - pointsLatitudeMin > 2.813 -> mapController.setZoom(7.0)
                    pointsLatitudeMax - pointsLatitudeMin > 1.406 -> mapController.setZoom(8.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.703 -> mapController.setZoom(9.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.352 -> mapController.setZoom(10.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.176 -> mapController.setZoom(11.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.088 -> mapController.setZoom(12.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.044 -> mapController.setZoom(13.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.022 -> mapController.setZoom(14.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.011 -> mapController.setZoom(15.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.0055 -> mapController.setZoom(16.0)
                    pointsLatitudeMax - pointsLatitudeMin > 0.00255 -> mapController.setZoom(17.0)
                    else -> mapController.setZoom(18.0)
                }
                mapController.setCenter(GeoPoint(centerLatitude, centerLongitude))
            } else {
                if (filter !is FilterForExpensesMap.All ) {
                    viewModel.setFilterForExpensesMap(FilterForExpensesMap.All)
                    Snackbar.make(mapMapView, getString(R.string.points_is_empty), Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

    override fun onResume() {
        mapMapView.onResume()
        super.onResume()

    }

    override fun onPause() {
        mapMapView.onPause()
        viewModel.setParamMapView(
            Pair(mapMapView.zoomLevelDouble, (mapMapView.mapCenter as GeoPoint))
        )
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_top_app_bar, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pointsDateFilter -> {
                //Log.d("blabla", "DateFilter")
                val pickerRange = MaterialDatePicker.Builder.dateRangePicker().build()
                pickerRange.show(parentFragmentManager, "Picker")
                pickerRange.addOnPositiveButtonClickListener { dateSelected ->
                    val start = DateFormat.format("yyyyMMdd", dateSelected.first).toString()
                    val startLong =
                        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(start)?.time
                    val end = DateFormat.format("yyyyMMdd", dateSelected.second).toString()
                    val endLong = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(end)?.time
                    if (startLong != null && endLong != null)
                        viewModel.setFilterForExpensesMap(
                            FilterForExpensesMap.DateRangeFilter(
                                startLong,
                                endLong + (24 * 60 * 60 * 1000) - 1000
                            )
                        )
                }
                firstStart = true
            }
            R.id.pointsExpenseFilter -> {
                findNavController().navigate(R.id.toChoiceExpenseForMapFragment)
                firstStart = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}