package m.kampukter.travelexpenses.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.current_exchange_fragment.*
import kotlinx.android.synthetic.main.exchange_rate_fragment.errorApiTextView
import kotlinx.android.synthetic.main.exchange_rate_fragment.exchangeRateRecyclerView
import kotlinx.android.synthetic.main.exchange_rate_fragment.progressBar
import kotlinx.android.synthetic.main.exchange_rate_fragment.reloadRateButton
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.data.ResultCurrentExchangeRate
import m.kampukter.travelexpenses.ui.ExchangeRateAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CurrentExchangeFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var exchangeRateAdapter: ExchangeRateAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.current_exchange_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val defaultProgramCurrency = mainApplication.getActiveCurrencySession()

        progressBar?.visibility = View.VISIBLE
        exchangeRateAdapter = ExchangeRateAdapter()
        with(exchangeRateRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = exchangeRateAdapter
        }

        viewModel.setDateForCurrencyExchange(null)
        viewModel.exchangeRateLiveDate.observe(
            viewLifecycleOwner,
            Observer { resultCurrentExchangeRate ->
                when (resultCurrentExchangeRate) {
                    is ResultCurrentExchangeRate.ErrorAPI -> {
                        progressBar?.visibility = View.GONE
                        errorApiTextView?.visibility = View.VISIBLE
                        reloadRateButton?.visibility = View.VISIBLE
                    }
                    is ResultCurrentExchangeRate.Success -> {
                        progressBar?.visibility = View.GONE
                        exchangeRateAdapter?.setList(resultCurrentExchangeRate.currentExchangeRate)
                        val exchangeDateString =
                            resultCurrentExchangeRate.currentExchangeRate.first().exchangeDate
                        val myHint = when (defaultProgramCurrency) {
                            // Гривна по умолчанию
                            DEFAULT_CURRENCY_CONST_UAH -> getString(R.string.oneUah)
                            // Рубль по умолчению
                            DEFAULT_CURRENCY_CONST_RUB -> getString(R.string.oneRub)
                            // Беларуский Рубль по умолчению
                            DEFAULT_CURRENCY_CONST_BYN -> getString(R.string.oneByn)
                            else -> getString(R.string.noCurrency)
                        }
                        dateTextInputLayout.hint = myHint
                        dateTextInputEdit.setText(exchangeDateString)
                    }
                }
            })
        reloadRateButton?.setOnClickListener {
            errorApiTextView.visibility = View.INVISIBLE
            reloadRateButton.visibility = View.INVISIBLE
            viewModel.setDateForCurrencyExchange(null)
            progressBar.visibility = View.VISIBLE
        }
        dateTextInputEdit.setOnClickListener {
            findNavController().navigate(R.id.toDatePickerDialogFragment)
        }
    }
}