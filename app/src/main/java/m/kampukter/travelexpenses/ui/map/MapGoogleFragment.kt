package m.kampukter.travelexpenses.ui.map

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.map_google_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FilterForExpensesMap
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*


class MapGoogleFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

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
        with(googleMap) {
            viewModel.lastMapTypeLiveData.observe(viewLifecycleOwner){
                mapType = when (it){
                    2 -> GoogleMap.MAP_TYPE_HYBRID
                    else -> GoogleMap.MAP_TYPE_NORMAL
                }
            }
            //
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_google_app_bar, menu)
        viewModel.lastMapTypeLiveData.observe(viewLifecycleOwner){
            when (it){
                2 -> menu.findItem(R.id.action_maps_type_hybrid).isChecked = true
                else -> menu.findItem(R.id.action_maps_type_normal).isChecked = true
            }
        }

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
            R.id.action_maps_type_hybrid -> {
                item.isChecked = !item.isChecked
                viewModel.setMapType( 2 )
            }
            R.id.action_maps_type_normal -> {
                item.isChecked = !item.isChecked
                viewModel.setMapType( 1 )
            }
        }
        return super.onOptionsItemSelected(item)
    }
}