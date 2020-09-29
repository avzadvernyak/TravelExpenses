package m.kampukter.travelexpenses.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class DatePickerDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private var datePickerListener =
        DatePickerDialog.OnDateSetListener { _, p1, p2, p3 ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("$p1-${p2 +1}-$p3")
            viewModel.setDateForCurrencyExchange(date)
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val myDialog = DatePickerDialog(requireContext(), datePickerListener, year, month, day)
        myDialog.datePicker.maxDate = Date().time
        return myDialog
    }
}
