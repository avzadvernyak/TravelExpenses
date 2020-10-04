package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.ui.AppBarConfiguration
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.map_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker


class MapExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var firstStart = true
        Configuration.getInstance()
            .load(view.context, PreferenceManager.getDefaultSharedPreferences(view.context))

        mapMapView.setTileSource(TileSourceFactory.MAPNIK)
        mapMapView.setMultiTouchControls(true)
        mapMapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        val mapController = mapMapView.controller

        viewModel.paramMapViewLiveData.observe(viewLifecycleOwner, Observer {
            firstStart = false
            mapController.setCenter(it.second)
            mapController.setZoom(it.first)
        })
        if (firstStart) {
            mapController.setCenter(GeoPoint(48.0154, 37.8647))
            mapController.setZoom(9.5)
        }
        viewModel.expenses.observe(viewLifecycleOwner, Observer { expenses ->

            val pointsLatitude = mutableListOf<Double>()
            val pointsLongitude = mutableListOf<Double>()

            expenses.forEach { itemExpenses ->
                if (itemExpenses.location != null) {
                    pointsLatitude += itemExpenses.location.latitude
                    pointsLongitude += itemExpenses.location.longitude

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


            val boundingBox = if (pointsLatitude.size != 0 && pointsLongitude.size != 0) {

                BoundingBox(
                    pointsLatitude.max()!! + ((pointsLatitude.max()!! - pointsLatitude.min()!!) * 0.2),
                    pointsLongitude.max()!! - ((pointsLongitude.max()!! - pointsLongitude.min()!!) * 0.2),
                    pointsLatitude.min()!! - ((pointsLatitude.max()!! - pointsLatitude.min()!!) * 0.2),
                    pointsLongitude.min()!! + ((pointsLongitude.max()!! - pointsLongitude.min()!!) * 0.2)
                )
            } else null
            if (firstStart) boundingBox?.let { mapMapView.zoomToBoundingBox(it, false) }

            /* allPointsButton.setOnClickListener {
                boundingBox?.let {
                    mapMapView.zoomToBoundingBox(
                        it,
                        false
                    )
                }
            }*/
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
        inflater.inflate(R.menu.map_top_app_bar, menu)
    }
}