package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExportFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.export_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getExpensesCSV(true)
        viewModel.expensesCSVForExport.observe(viewLifecycleOwner, { expensesCSV ->
            if (expensesCSV.isNullOrEmpty()) {
                Snackbar.make(view, "Нет данных для экспорта", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, expensesCSV)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "My Send"))
                findNavController().navigate(R.id.next_action)
            }
        })
    }
}