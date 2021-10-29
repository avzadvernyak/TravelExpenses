package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.exchange_fragment.*
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.data.ResultCurrentExchangeRate
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExchangeFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var exchangeRateAdapter: ExchangeRateAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.exchange_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navController = findNavController()
        val defaultProgramCurrency = mainApplication.getActiveCurrencySession()

        exchangeRateAdapter = ExchangeRateAdapter()
        with(exchangeRateRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = exchangeRateAdapter
        }
        viewModel.currencyRateLiveDate.observe(viewLifecycleOwner) { (resultExchangeCurrentRate, queryString) ->
            when (resultExchangeCurrentRate) {
                is ResultCurrentExchangeRate.Loading -> {
                    progressBar?.visibility = View.VISIBLE
                    errorApiTextView.visibility = View.INVISIBLE
                    reloadRateButton.visibility = View.INVISIBLE
                    dateTextInputLayout.visibility = View.INVISIBLE
                }
                is ResultCurrentExchangeRate.ErrorAPI -> {
                    progressBar?.visibility = View.GONE
                    errorApiTextView?.visibility = View.VISIBLE
                    reloadRateButton?.visibility = View.VISIBLE
                    dateTextInputLayout.visibility = View.INVISIBLE
                }
                is ResultCurrentExchangeRate.Success -> {
                    progressBar?.visibility = View.GONE

                    val currencyRate = if (queryString != null) {
                        resultExchangeCurrentRate.exchangeCurrentRate.filter { item ->
                            item.currencyName.indexOf(
                                queryString,
                                0,
                                true
                            ) != -1 || item.currencyCode.indexOf(queryString, 0, true) != -1
                        }
                    } else resultExchangeCurrentRate.exchangeCurrentRate

                    exchangeRateAdapter?.setList(currencyRate)

                    if (currencyRate.isNotEmpty()) {
                        dateTextInputLayout.visibility = View.VISIBLE

                        dateTextInputLayout.hint = when (defaultProgramCurrency) {
                            // Гривна по умолчанию
                            DEFAULT_CURRENCY_CONST_UAH -> getString(R.string.oneUah)
                            // Рубль по умолчению
                            DEFAULT_CURRENCY_CONST_RUB -> getString(R.string.oneRub)
                            // Беларуский Рубль по умолчению
                            DEFAULT_CURRENCY_CONST_BYN -> getString(R.string.oneByn)
                            else -> getString(R.string.noCurrency)
                        }
                        dateTextInputEdit.setText( currencyRate.first().exchangeDate )
                    } else {
                        dateTextInputLayout.visibility = View.INVISIBLE
                        Snackbar.make(
                            view,
                            getString(R.string.dialog_exchange_is_empty),
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        reloadRateButton?.setOnClickListener {

            if (navController.currentDestination?.id == R.id.exchangeFragment)
                navController.navigate(R.id.toDatePickerDialogFragment)
        }
        dateTextInputEdit.setOnClickListener {
            if (navController.currentDestination?.id == R.id.exchangeFragment) {
                navController.navigate(R.id.toDatePickerDialogFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.exchange_search_menu, menu)

        val mySearchView = (menu.findItem(R.id.exchangeSearch)?.actionView as? SearchView)

        viewModel.currencyRateLiveDate.observe(viewLifecycleOwner) { ( _, queryString) ->
            val searchString: CharSequence? = queryString
            mySearchView?.setQuery(searchString, false)
            if (!queryString.isNullOrEmpty()) {
                mySearchView?.isIconified = false
            }
        }

        mySearchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) viewModel.setQueryInExchangeCurrentRate("")
                    else viewModel.setQueryInExchangeCurrentRate(newText)
                    return true
                }
            })
    }
}