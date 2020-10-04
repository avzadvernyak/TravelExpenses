package m.kampukter.travelexpenses.ui.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.test_add_expenses_fragment.*
import kotlinx.android.synthetic.main.test_add_expenses_fragment.currencyTextInputEdit
import kotlinx.android.synthetic.main.test_add_expenses_fragment.expenseTextInputEdit
import kotlinx.android.synthetic.main.test_add_expenses_fragment.noteTextInputEdit
import kotlinx.android.synthetic.main.test_add_expenses_fragment.saveNewExpensesButton
import kotlinx.android.synthetic.main.test_add_expenses_fragment.sumTextInputEdit
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.MyLocation
import m.kampukter.travelexpenses.ui.MyArrayAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class TestAddExpenseFragment : Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()
    private var myDropdownAdapter: MyArrayAdapter? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                viewModel.setBufferExpensesLocation(
                    location = MyLocation(
                        accuracy = location.accuracy,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
                accuracyTextView.text = "Точность ${location.accuracy} м."
                latitudeTextView.text = "Широта ${location.latitude}"
                longitudeTextView.text = "Долгота ${location.longitude}"
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
        return inflater.inflate(R.layout.test_add_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(view.context.applicationContext)

        locationRequest?.let { LocationSettingsRequest.Builder().addLocationRequest(it) }
        startLocationUpdates()
        // BS
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        // end BS
        val navController = findNavController()

        myDropdownAdapter =
            context?.let {
                MyArrayAdapter(it, android.R.layout.simple_list_item_1, mutableListOf())
            }
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)
        sumTextInputEdit.setText("")
        viewModel.bufferExpensesMediatorLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { value ->

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

                sumTextInputEdit.doOnTextChanged { text, p1, p2, p3 ->
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
                        } else Log.d("blablabla", "Не сохраняем")
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
        viewModel.isSavingAllowed.observe(viewLifecycleOwner, Observer { _isSavingAllowed ->
            _isSavingAllowed?.let { saveNewExpensesButton.isEnabled = it }
        })
        saveNewExpensesButton.setOnClickListener {
            viewModel.saveNewExpenses()
            //сброс временной переменной
            viewModel.setBufferExpenses(null)
            //установка сохраняемой валюты как по умолчанию
            viewModel.resetDef()
            viewModel.setDefCurrency(currencyTextInputEdit.text.toString())
            hideSystemKeyboard()
            navController.navigate(R.id.next_action)
        }
        expenseTextInputEdit.setOnClickListener {
            navController.navigate(R.id.toChoiceExpenseForAddFragment)
        }


    }

    override fun onResume() {
        super.onResume()
        locationRequest?.let { startLocationUpdates() }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        val context = requireContext()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("blabla", "Permission no granted")
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun hideSystemKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}