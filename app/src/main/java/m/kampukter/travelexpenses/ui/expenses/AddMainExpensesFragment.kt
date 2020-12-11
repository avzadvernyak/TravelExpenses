package m.kampukter.travelexpenses.ui.expenses

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.add_expenses_fragment.*
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_BYN
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_RUB
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.MyLocation
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.ui.MyArrayAdapter
import m.kampukter.travelexpenses.ui.STATUS_GPS_OFF
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.ui.permissionsForLocation
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


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
                viewModel.setBufferExpensesLocation(
                    location = MyLocation(
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
                        //Log.d("blabla", "Permission no granted")
                        viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                        navController.navigate(R.id.toLocationPermissionsDialogFragment)
                    }

                } else {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            })
        }

        /*
        //Не совсем успешная попытка менять ФАБ в зависимости от взаиморасположения вьющек на экране
        val saveFab = activity?.findViewById<ExtendedFloatingActionButton>(R.id.saveExpensesFAB)
         val tabLayout = activity?.findViewById<TabLayout>(R.id.tab_layout)
          addExpensesFragmentLayout.viewTreeObserver?.addOnGlobalLayoutListener {
              Log.d("blabla", "addOnGlobalLayoutListener")
              noteTextInputLayout?.bottom?.let {
                  if (saveFab?.top != null && tabLayout?.height != null) {
                      if (it > saveFab.top.minus(saveFab.marginBottom + tabLayout.height)) saveFab.shrink()
                      else saveFab.extend()
                  }
              }
          }*/
        /*addExpensesFragmentLayout.viewTreeObserver?.addOnGlobalLayoutListener(object :
           ViewTreeObserver.OnGlobalLayoutListener {
           override fun onGlobalLayout() {
               Log.d("blabla", "removeOnGlobalLayoutListener")
               addExpensesFragmentLayout.viewTreeObserver?.removeOnGlobalLayoutListener(this)
           }
       })*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        myDropdownAdapter =
            context?.let {
                MyArrayAdapter(it, android.R.layout.simple_list_item_1, mutableListOf())
            }
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)

        sumTextInputEdit.setText("")

        viewModel.bufferExpensesMediatorLiveData.observe(viewLifecycleOwner, { value ->

            value.second?.let { list -> myDropdownAdapter?.addAll(list.map { it.name }) }

            val tempExpenses = value.first

            if (tempExpenses == null) {
                val currencyList = value.second
                if (!currencyList.isNullOrEmpty()) {

                    val defCurrencyName =
                        currencyList.find { currency -> currency.defCurrency == 1 }?.name

                    val currency = if (defCurrencyName != null) {
                        val currencyPosition = myDropdownAdapter?.getPosition(defCurrencyName)
                        if (currencyPosition != null && currencyPosition >= 0) {
                            currencyTextInputEdit?.setText(
                                myDropdownAdapter?.getItem(currencyPosition).toString(), false
                            )
                        }
                        defCurrencyName
                    } else {
                        when (mainApplication.getActiveCurrencySession()) {
                            DEFAULT_CURRENCY_CONST_RUB -> "RUB"
                            DEFAULT_CURRENCY_CONST_BYN -> "BYN"
                            else -> "UAH"
                        }
                    }
                    viewModel.setBufferExpenses(
                        Expenses(
                            dateTime = Calendar.getInstance().time,
                            sum = 0.0,
                            currency = currency,
                            expense = "",
                            note = "",
                            location = null,
                            imageUri = null
                        )
                    )
                }
            } else {

              /*  if (sumTextInputEdit.text.toString()
                        .toDoubleOrNull() != tempExpenses.sum
                ) sumTextInputEdit.setText(tempExpenses.sum.toString())*/

                expenseTextInputEdit.setText(tempExpenses.expense)
                if (noteTextInputEdit.text.toString() != tempExpenses.note) noteTextInputEdit.setText(
                    tempExpenses.note
                )

            }
            noteTextInputEdit.doOnTextChanged { text, _, _, _ ->
                viewModel.setBufferExpenses(
                    tempExpenses?.copy(note = text.toString())
                )
            }

            sumTextInputEdit.doOnTextChanged { text, _, _, _ ->
                val inputString = text.toString()
                if (inputString.length == 1 && inputString == ".") {
                    sumTextInputEdit.setText("0.")
                } else {
                    if (inputString.isNotBlank()) viewModel.setBufferExpenses(
                        tempExpenses?.copy(sum = inputString.toDouble())
                    ) else viewModel.setBufferExpenses(tempExpenses?.copy(sum = 0.0))
                }
            }

            currencyTextInputEdit.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    val newValue = currencyTextInputEdit.text.toString()
                    if (tempExpenses?.currency != newValue) {
                        viewModel.resetDef()
                        viewModel.setDefCurrency(newValue)
                        viewModel.setBufferExpenses(tempExpenses?.copy(currency = newValue))
                    } //else Log.d("blablabla", "Не сохраняем")
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
        currencyTextInputEdit.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
            if (p1) {
                hideSystemKeyboard()
            }
        }
        expenseTextInputEdit.setOnClickListener {
            navController.navigate(R.id.toChoiceExpenseForAddFragment)
        }

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

    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun hideSystemKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}