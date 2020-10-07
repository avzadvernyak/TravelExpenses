package m.kampukter.travelexpenses.ui.map

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.map_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FilterForExpensesMap
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*


class MapExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var firstStart = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        Configuration.getInstance()
            .load(view.context, PreferenceManager.getDefaultSharedPreferences(view.context))

        mapMapView.setTileSource(TileSourceFactory.MAPNIK)
        mapMapView.setMultiTouchControls(true)
        mapMapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)

        val mapController = mapMapView.controller

        viewModel.paramMapViewLiveData.observe(viewLifecycleOwner, Observer {
            firstStart = false
            mapController.setZoom(it.first)
            mapController.setCenter(it.second)
        })
        if (firstStart) {
            mapController.setZoom(8.5)
            mapController.setCenter(GeoPoint(48.0154, 37.8647))
        }
        viewModel.expensesForMapMutableLiveData.observe(viewLifecycleOwner, Observer { expenses ->

            mapMapView.overlays.clear()

            var pointsLongitudeMax: Double = -90.0
            var pointsLongitudeMin: Double = 90.0
            var pointsLatitudeMax: Double = -180.0
            var pointsLatitudeMin: Double = 180.0

            expenses.forEach { itemExpenses ->

                if (itemExpenses.location != null) {

                    Log.d("blabla", "${itemExpenses.location} ")

                    if (itemExpenses.location.latitude > pointsLatitudeMax) pointsLatitudeMax =
                        itemExpenses.location.latitude
                    if (itemExpenses.location.latitude < pointsLatitudeMin) pointsLatitudeMin =
                        itemExpenses.location.latitude
                    if (itemExpenses.location.longitude > pointsLongitudeMax) pointsLongitudeMax =
                        itemExpenses.location.longitude
                    if (itemExpenses.location.longitude < pointsLongitudeMin) pointsLongitudeMin =
                        itemExpenses.location.longitude

                    val myMarker = Marker(mapMapView)
                    myMarker.position =
                        GeoPoint(itemExpenses.location.latitude, itemExpenses.location.longitude)

                    myMarker.title = "${
                        DateFormat.format(
                            "dd/MM/yyyy HH:mm",
                            itemExpenses.dateTime
                        )
                    } ${itemExpenses.sum} ${itemExpenses.currency}"

                    myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    myMarker.snippet = itemExpenses.expense
                    myMarker.subDescription = itemExpenses.note

                    mapMapView.overlays.add(myMarker)
                }
            }

            /* val boundingBox = BoundingBox(
                 pointsLatitudeMax + ((pointsLatitudeMax - pointsLatitudeMin) * 0.15),
                 pointsLongitudeMax + ((pointsLongitudeMax - pointsLongitudeMin) * 0.05),
                 pointsLatitudeMin - ((pointsLatitudeMax - pointsLatitudeMin) * 0.05),
                 pointsLongitudeMin - ((pointsLongitudeMax - pointsLongitudeMin) * 0.05)
             )*/

            val centerLatitude = pointsLatitudeMin + ((pointsLatitudeMax - pointsLatitudeMin) / 2.0)
            val centerLongitude =
                pointsLongitudeMin + ((pointsLongitudeMax - pointsLongitudeMin) / 2.0)

            Log.d(
                "blabla",
                "${pointsLatitudeMax - pointsLatitudeMin} $centerLatitude $centerLongitude"
            )

            when {
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
                else -> mapController.setZoom(15.0)
            }
            mapController.setCenter(GeoPoint(centerLatitude, centerLongitude))

            //mapMapView.zoomToBoundingBox(boundingBox, false)
        })
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
            R.id.allPoints -> {
                firstStart = true
                viewModel.setFilterForExpensesMap(FilterForExpensesMap.All)
            }
            R.id.pointsDateFilter -> {
                Log.d("blabla", "DateFilter")
                val pickerRange = MaterialDatePicker.Builder.dateRangePicker().build()
                fragmentManager?.let { pickerRange.show(it, "Picker") }
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
                                endLong + (24 * 60 * 60 * 1000)
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