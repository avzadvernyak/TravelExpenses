package m.kampukter.travelexpenses.ui.expenses

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.add_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.MyLocation
import m.kampukter.travelexpenses.ui.MyArrayAdapter
import m.kampukter.travelexpenses.ui.STATUS_GPS_OFF
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.ui.permissionsForLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AddMainExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var myDropdownAdapter: MyArrayAdapter? = null

    private lateinit var navController: NavController

    /*
    Work with Location
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            accuracyTextView.visibility = View.VISIBLE
            gpsImageView.visibility = View.VISIBLE
            accuracyTextView.text =
                getString(R.string.accuracy_value, locationResult.locations.last().accuracy.toInt())

            locationResult.locations.last()?.let { lastLocation ->
                viewModel.setLastLocation(
                    MyLocation(
                        accuracy = lastLocation.accuracy,
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude
                    )
                )
            }
        }
    }
    private val locationRequest = LocationRequest.create()?.apply {
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
        return inflater.inflate(R.layout.add_expenses_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        /*
        Work with Location
        */
        context?.let { context ->
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context.applicationContext)
            viewModel.savedSettingsLiveData.observe(viewLifecycleOwner, { settings ->
                if (settings.statusGPS == STATUS_GPS_ON) {

                    locationRequest?.let {
                        LocationSettingsRequest.Builder().addLocationRequest(it)
                    }
                    val isLocationPermission = permissionsForLocation.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    if (isLocationPermission) fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    ) else {
                        viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                        navController.navigate(R.id.toLocationPermissionsDialogFragment)
                    }

                } else {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        //Реализация изменения ФАБ в зависимости от взаиморасположения вьющек на экране
        val saveFab = activity?.findViewById<ExtendedFloatingActionButton>(R.id.saveExpensesFAB)
        val tabLayout = activity?.findViewById<TabLayout>(R.id.tab_layout)
        saveFab?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            noteTextInputLayout?.bottom?.let {
                if (tabLayout?.height != null) {
                    if (it > saveFab.top.minus(saveFab.marginBottom + tabLayout.height)) saveFab.shrink()
                    else saveFab.extend()
                }
            }
        }
        saveFab?.isEnabled = false

        myDropdownAdapter =
            context?.let {
                MyArrayAdapter(it, android.R.layout.simple_list_item_1, mutableListOf())
            }
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)

        viewModel.addExpensesLiveData.observe(viewLifecycleOwner) { (expenses, expenseList, currencyList) ->
            val expenseName = expenseList.find { it.id == expenses.expense_id }?.name
            if (expenseTextInputEdit.text.toString() != expenseName) expenseTextInputEdit.setText(
                expenseName
            )
            when {
                expenses.sum == 0.0 ||
                        expenseName.isNullOrBlank() ||
                        expenses.currency.isBlank() ||
                        expenses.note.isBlank() -> {
                    saveFab?.isEnabled = false
                }
                else -> saveFab?.isEnabled = true
            }

            myDropdownAdapter?.addAll(currencyList.map { it.name })
            val currencyName =
                if (expenses.currency.isBlank()) currencyList.find { it.defCurrency == 1 }?.name
                else expenses.currency
            myDropdownAdapter?.getPosition(currencyName)?.let {
                if (currencyTextInputEdit.text.toString() != currencyName)
                    currencyTextInputEdit?.setText(currencyName)
            }
        }
        expenseTextInputEdit.setOnClickListener {
            navController.navigate(R.id.toChoiceExpenseForAddFragment)
        }
        currencyTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setLastCurrency(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        currencyTextInputEdit.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
            if (p1) {
                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        sumTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val inputString = p0.toString()
                if (inputString.length == 1 && inputString == ".") {
                    sumTextInputEdit.setText("0.")
                } else {
                    viewModel.setLastSum(
                        if (inputString.isNotBlank()) inputString.toDouble()
                        else 0.0
                    )
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        noteTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setLastNote(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}