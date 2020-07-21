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
        viewModel.currentExchangeRate.observe(viewLifecycleOwner, Observer {
            exchangeRateAdapter?.setList(it)
            val mySubtitle = when (defaultProgramCurrency) {
                // Гривна по умолчанию
                1 -> getString(R.string.oneUah, it.first().exchangeDate)
                // Рубль по умолчению
                2 -> getString(R.string.oneRub, it.first().exchangeDate)
                // Беларуский Рубль по умолчению
                3 -> getString(R.string.oneByn, it.first().exchangeDate)
                else -> getString(R.string.noCurrency)
            }

            (activity as AppCompatActivity).supportActionBar?.subtitle = mySubtitle

            progressBar.visibility = View.GONE
        })

    }

}
