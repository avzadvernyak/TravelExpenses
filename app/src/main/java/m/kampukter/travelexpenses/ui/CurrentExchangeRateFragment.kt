package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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


    }

}
