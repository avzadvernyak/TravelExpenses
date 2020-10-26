package m.kampukter.travelexpenses.ui

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.add_expenses_fragment.*
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.MyLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class AddExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var myDropdownAdapter: MyArrayAdapter? = null

    private lateinit var  navController: NavController
    /*
    Work with Location
     */
    private var lastLocationResult: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            accuracyTextView.visibility = View.VISIBLE
            gpsImageView.visibility = View.VISIBLE
            accuracyTextView.text = getString(R.string.accuracy_value,locationResult.locations.last().accuracy.toInt())

            //gpsButton.visibility = View.VISIBLE

            //gpsButton.text = locationResult.locations.last().accuracy.toString()
            lastLocationResult = locationResult.locations.last()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = findNavController()

        /*
        Work with Location
        */
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(view.context.applicationContext)
        viewModel.savedSettingsLiveData.observe(viewLifecycleOwner, Observer { settings ->
            if (settings.statusGPS == STATUS_GPS_ON) {

                locationRequest?.let { LocationSettingsRequest.Builder().addLocationRequest(it) }
                val isLocationPermission = permissionsForLocation.all {
                    ContextCompat.checkSelfPermission(
                        view.context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }
                if (isLocationPermission) fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                ) else {
                    //Log.d("blabla", "Permission no granted")
                    viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                    navController.navigate(R.id.toLocationPermissionsDialogFragment)
                }
                /*saveNewExpensesButton.icon = resources.getDrawable(R.drawable.ic_gps_fixed_24, null)
                gpsButton.setOnClickListener {
                    gpsButton.visibility = View.INVISIBLE
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    lastLocationResult = null
                    //gpsOffButton.visibility = View.VISIBLE
                }
                gpsOffButton.setOnClickListener {
                    gpsOffButton.visibility = View.INVISIBLE
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                    //gpsButton.visibility = View.VISIBLE
                }*/
            } else {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                lastLocationResult = null
                //gpsButton.visibility = View.INVISIBLE
                //gpsOffButton.visibility = View.INVISIBLE

            }
        })



        myDropdownAdapter =
            context?.let {
                MyArrayAdapter(it, android.R.layout.simple_list_item_1, mutableListOf())
            }
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)
        sumTextInputEdit.setText("")
        viewModel.bufferExpensesMediatorLiveData.observe(viewLifecycleOwner, Observer { value ->

            value.second?.let { list -> myDropdownAdapter?.addAll(list.map { it.name }) }

            val tempExpenses = value.first

            if (tempExpenses == null) {
                val currencyList = value.second
                if (!currencyList.isNullOrEmpty()) {
                    val defCurrencyName =
                        currencyList.find { currency -> currency.defCurrency == 1 }?.name
                    if (defCurrencyName != null) {
                        val currencyPosition = myDropdownAdapter?.getPosition(defCurrencyName)
                        if (currencyPosition != null && currencyPosition >= 0) {
                            currencyTextInputEdit?.setText(
                                myDropdownAdapter?.getItem(currencyPosition).toString(), false
                            )
                        }
                        viewModel.setBufferExpenses(
                            Expenses(
                                dateTime = Calendar.getInstance().time,
                                sum = 0.0,
                                currency = defCurrencyName,
                                expense = "",
                                note = "",
                                location = null
                            )
                        )
                    } else {
                        val defaultCurrency = when (mainApplication.getActiveCurrencySession()) {
                            DEFAULT_CURRENCY_CONST_RUB -> "RUB"
                            DEFAULT_CURRENCY_CONST_BYN -> "BYN"
                            else -> "UAH"
                        }
                        viewModel.setBufferExpenses(
                            Expenses(
                                dateTime = Calendar.getInstance().time,
                                sum = 0.0,
                                currency = defaultCurrency,
                                expense = "",
                                note = "",
                                location = null
                            )
                        )
                    }
                }
            } else {
                expenseTextInputEdit.setText(tempExpenses.expense)
                if (noteTextInputEdit.text.toString() != tempExpenses.note) noteTextInputEdit.setText(
                    tempExpenses.note
                )

            }
            noteTextInputEdit.doOnTextChanged { text, _, _, _ ->
                viewModel.setBufferExpenses(
                    tempExpenses?.copy(
                        note = text.toString()
                    )
                )
            }

            sumTextInputEdit.doOnTextChanged { text, _, _, _ ->
                val inputString = text.toString()
                if (inputString.length == 1 && inputString == ".") {
                    sumTextInputEdit.setText("0.")
                } else {
                    if (!inputString.isBlank()) viewModel.setBufferExpenses(
                        tempExpenses?.copy(sum = inputString.toDouble())
                    ) else viewModel.setBufferExpenses(tempExpenses?.copy(sum = 0.0))
                }
            }

            currencyTextInputEdit.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
                if (p1) {
                    hideSystemKeyboard()
                } else {
                    val newValue = currencyTextInputEdit.text.toString()
                    if (tempExpenses?.currency != newValue) {
                        viewModel.resetDef()
                        viewModel.setDefCurrency(newValue)
                        viewModel.setBufferExpenses(tempExpenses?.copy(currency = newValue))
                    } //else Log.d("blablabla", "Не сохраняем")
                }
            }
            tempExpenses?.currency.let {
                val currencyPosition = myDropdownAdapter?.getPosition(it)
                if (currencyPosition != null && currencyPosition >= 0) {
                    currencyTextInputEdit?.setText(
                        myDropdownAdapter?.getItem(currencyPosition).toString(), false
                    )
                }
            }

        })
        /*viewModel.isSavingAllowed.observe(viewLifecycleOwner, Observer { _isSavingAllowed ->
            _isSavingAllowed?.let { saveNewExpensesButton.isEnabled = it }
        })
        saveNewExpensesButton.setOnClickListener {
            lastLocationResult?.let { lastLocation ->
                viewModel.setBufferExpensesLocation(
                    location = MyLocation(
                        accuracy = lastLocation.accuracy,
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude
                    )
                )
            }
            viewModel.saveNewExpenses()
            //сброс временной переменной
            viewModel.setBufferExpenses(null)
            //установка сохраняемой валюты как по умолчанию
            viewModel.resetDef()
            viewModel.setDefCurrency(currencyTextInputEdit.text.toString())
            hideSystemKeyboard()
            navController.navigate(R.id.next_action)
        }*/
        expenseTextInputEdit.setOnClickListener {
            navController.navigate(R.id.toChoiceExpenseForAddFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_expenses, menu)

        viewModel.isSavingAllowed.observe(viewLifecycleOwner, Observer { _isSavingAllowed ->
            _isSavingAllowed?.let { menu.findItem(R.id.saveExpenses).isEnabled = it }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveExpenses){
            lastLocationResult?.let { lastLocation ->
                viewModel.setBufferExpensesLocation(
                    location = MyLocation(
                        accuracy = lastLocation.accuracy,
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude
                    )
                )
            }
            viewModel.saveNewExpenses()
            //сброс временной переменной
            viewModel.setBufferExpenses(null)
            //установка сохраняемой валюты как по умолчанию
            viewModel.resetDef()
            viewModel.setDefCurrency(currencyTextInputEdit.text.toString())
            hideSystemKeyboard()
            navController.navigate(R.id.next_action)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun hideSystemKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
