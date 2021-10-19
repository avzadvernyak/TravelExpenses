package m.kampukter.travelexpenses.ui.map

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import m.kampukter.travelexpenses.R

class MapGooglePlaceFragment : Fragment() {

    private var map: GoogleMap? = null
    private val myOnMapReadyCallback = OnMapReadyCallback { googleMap ->
        googleMap ?: return@OnMapReadyCallback
        this.map = googleMap

        with(googleMap) {
            uiSettings.isCompassEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.map_google_place_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the SDK
        if (!Places.isInitialized()) {
            val applicationInfo: ApplicationInfo = view.context.packageManager
                .getApplicationInfo(view.context.packageName, PackageManager.GET_META_DATA)
            val apiKey = applicationInfo.metaData["com.google.android.geo.API_KEY"].toString()
            Places.initialize(view.context, apiKey)
        }


        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragmentContainerView) as SupportMapFragment
        mapFragment.getMapAsync(myOnMapReadyCallback)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.VIEWPORT))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                Log.i("blabla", "An error occurred: $status")
            }

            override fun onPlaceSelected(place: Place) {
                Log.i("blabla", "Place: ${place.viewport}")
                place.viewport?.let { latLng ->
                    val padding = 0 // offset from edges of the map in pixels
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(latLng, padding)
                    )
                }
            }
        })
    }
}