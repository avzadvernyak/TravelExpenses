package m.kampukter.travelexpenses.ui.test

import android.content.Context
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class MyMarker(mapView: MapView) : Marker(mapView) {

    override fun setInfoWindow(infoWindow: MarkerInfoWindow?) {
        Log.d("blabla", "setInfoWindow ${this.mInfoWindow}**${this.infoWindow}*********")
//val idLa = findViewById()
        infoWindow?.let {
                //val myInfoWindow = MyMarkerInfoWindow( id, it.mapView)
                super.setInfoWindow(infoWindow)
        }
    }


}

class MyMarkerInfoWindow(layoutResId: Int, mapView: MapView?) :
    MarkerInfoWindow(layoutResId, mapView) {
    init {
        Log.d("blabla", "******** INIT ***********")
    }
    override fun open(`object`: Any?, position: GeoPoint?, offsetX: Int, offsetY: Int) {
        super.open(`object`, position, offsetX, offsetY)
    }
    override fun close() {
        Log.d("blabla", "******** CLOSE ***********")
        super.close()
    }

}