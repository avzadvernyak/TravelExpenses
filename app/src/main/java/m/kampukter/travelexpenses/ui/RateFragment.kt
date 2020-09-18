package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.expenses_fragment.*
import kotlinx.android.synthetic.main.rate_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RateFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var rateAdapter: RateAdapter? = null

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
        return inflater.inflate(R.layout.rate_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val defaultProgramCurrency = mainApplication.getActiveCurrencySession()
        (activity as? AppCompatActivity)?.setSupportActionBar(rateToolbar)


        rateAdapter = RateAdapter()
        with(rateRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = rateAdapter
        }
        viewModel.allRate.observe( viewLifecycleOwner, Observer { list ->
            //Log.d("blablabla", " ***************** $list")
            rateAdapter?.setList(list)
            val mySubtitle = when (defaultProgramCurrency) {
                // Гривна по умолчанию
                1 -> getString(R.string.oneUah, "")
                // Рубль по умолчению
                2 -> getString(R.string.oneRub, "")
                // Беларуский Рубль по умолчению
                3 -> getString(R.string.oneByn, "")
                else -> getString(R.string.noCurrency)
            }
            (activity as AppCompatActivity).supportActionBar?.apply {
                title = "Архив курсов валют"
                subtitle = mySubtitle
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
            /*currencyTextView.text = when (defaultProgramCurrency ){
                // Гривна по умолчанию
                1 -> "1 UAH ="
                // Рубль по умолчению
                2 -> "1 RUB ="
                // Беларуский Рубль по умолчению
                3 -> "1 BYN ="
                else -> "######.##"
            }*/
        })
    }
}
