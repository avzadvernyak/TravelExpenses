package m.kampukter.travelexpenses.ui.test

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.map_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem


class MapFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /*// BS
        val bottomSheetBehavior = BottomSheetBehavior.from(map_bottom_sheet_layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        // end BS*/
        val mapController = mapMapView.controller

        val mapItemizedOverlay = object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                Log.d("blabla", "LongPress")

                return true
            }

            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                Log.d("blabla", "SingleTapUp")
                /*bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                titleTextView.text = "Информация"
                field1TextView.text = item?.title
                field2TextView.text = item?.snippet*/
                mapController.animateTo(item?.point)
                return true
            }
        }

        Configuration.getInstance()
            .load(view.context, PreferenceManager.getDefaultSharedPreferences(view.context))

        val startPoint = GeoPoint(48.0154, 37.8647)

        mapMapView.setTileSource(TileSourceFactory.MAPNIK)
        mapMapView.setMultiTouchControls(true)
        mapMapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapController.setCenter(startPoint)
        mapController.setZoom(9.5)
        mapMapView.overlayManager.add(object : Overlay() {

            override fun onTouchEvent(event: MotionEvent?, mapView: MapView?): Boolean {
                //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                return false
            }
        })
        viewModel.expenses.observe(viewLifecycleOwner, Observer { expenses ->
            val itemsOverlayItem = mutableListOf<OverlayItem>()
            val itemsOverlayItem1 = mutableListOf<OverlayItem>()

            expenses.forEach { itemExpenses ->
                if (itemExpenses.location != null) {
                    val myMarker = MyMarker(mapMapView)
                    myMarker.position =
                        GeoPoint(itemExpenses.location.latitude, itemExpenses.location.longitude)
                    myMarker.title = "${DateFormat.format("dd/MM/yyyy HH:mm", itemExpenses.dateTime)} ${itemExpenses.sum} ${itemExpenses.currency}"
                    myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    myMarker.snippet = itemExpenses.expense
                    myMarker.subDescription = itemExpenses.note

                    myMarker.setOnMarkerClickListener { marker, mapView ->
                        Log.d("blabla", "P")
                        marker?.showInfoWindow()

                        marker?.let { mapController.animateTo(it.position) }

                    /*    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        titleTextView.text = itemExpenses.expense
                        field1TextView.text = itemExpenses.dateTime.toString()
                        field2TextView.text = itemExpenses.note*/
                        false
                    }
                    mapMapView.overlays.add(myMarker)

                    /*val itemOverlay = OverlayItem(
                        "${itemExpenses.sum} ${itemExpenses.currency}",
                        itemExpenses.expense,
                        GeoPoint(
                            itemExpenses.location.latitude,
                            itemExpenses.location.longitude
                        )
                    )
                    //itemOverlay.setMarker()
                    itemsOverlayItem.add(itemOverlay)*/


                }
            }
            /* val mapOverlays = ItemizedOverlayWithFocus<OverlayItem>(
                 itemsOverlayItem,
                 mapItemizedOverlay,
                 view.context
             )*/

            //mapMapView.overlays.add(mapOverlays)

        }
        )
    }

    override fun onResume() {
        super.onResume()
        mapMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapMapView.onPause()
    }
}