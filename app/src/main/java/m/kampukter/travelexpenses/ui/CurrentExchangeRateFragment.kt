package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.exchange_rate_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ResultCurrentExchangeRate
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class CurrentExchangeRateFragment : Fragment() {
    private val viewModel by viewModel<MyViewModel>()
    private var exchangeRateAdapter: ExchangeRateAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.exchange_rate_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val defaultProgramCurrency = mainApplication.getActiveCurrencySession()

        (activity as? AppCompatActivity)?.setSupportActionBar(exchangeRateToolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "Курсы валют"
            subtitle = "Подключение к серверу банка..."
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        progressBar.visibility = View.VISIBLE
        exchangeRateAdapter = ExchangeRateAdapter()
        with(exchangeRateRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = exchangeRateAdapter
        }
        viewModel.startReloadCurrentRate()
        viewModel.currentExchangeRate.observe(
            viewLifecycleOwner,
            Observer { resultCurrentExchangeRate ->
                when (resultCurrentExchangeRate) {
                    is ResultCurrentExchangeRate.ErrorAPI -> {
                        progressBar.visibility = View.GONE
                        errorApiTextView.visibility = View.VISIBLE
                        reloadRateButton.visibility = View.VISIBLE
                    }
                    is ResultCurrentExchangeRate.Success -> {
                        progressBar.visibility = View.GONE
                        exchangeRateAdapter?.setList(resultCurrentExchangeRate.currentExchangeRate)
                        val _exchangeDate =
                            resultCurrentExchangeRate.currentExchangeRate.first().exchangeDate
                        val mySubtitle = when (defaultProgramCurrency) {
                            // Гривна по умолчанию
                            1 -> getString(R.string.oneUah, _exchangeDate)
                            // Рубль по умолчению
                            2 -> getString(R.string.oneRub, _exchangeDate)
                            // Беларуский Рубль по умолчению
                            3 -> getString(R.string.oneByn, _exchangeDate)
                            else -> getString(R.string.noCurrency)
                        }

                        (activity as AppCompatActivity).supportActionBar?.subtitle = mySubtitle
                    }
                }
            })
        reloadRateButton.setOnClickListener {
            errorApiTextView.visibility = View.INVISIBLE
            reloadRateButton.visibility = View.INVISIBLE
            viewModel.startReloadCurrentRate()
            progressBar.visibility = View.VISIBLE
        }
    }

}
