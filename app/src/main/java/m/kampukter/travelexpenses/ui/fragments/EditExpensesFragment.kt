package m.kampukter.travelexpenses.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.edit_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.expensesById.observe(viewLifecycleOwner, Observer { expenses ->

            currencyTextInputEdit.setText( expenses.currency)
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", expenses.dateTime)
            expenseTextInputEdit.setText(expenses.expense)

            var isNoteEdited = false
            var isSumEdited = false
            noteTextInputEdit.setText(expenses.note)
            noteTextInputEdit.onFocusChangeListener =
                View.OnFocusChangeListener { _, p1 ->
                    if (isNoteEdited and !p1) viewModel.addExpenses(expenses.copy(note = noteTextInputEdit.text.toString()))
                }
            noteTextInputEdit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    isNoteEdited = true
                }
            })

            sumTextInputEdit.setText(expenses.sum.toString())
            sumTextInputEdit.onFocusChangeListener =
                View.OnFocusChangeListener { _, p1 ->
                    if (isSumEdited and !p1) viewModel.addExpenses(
                        expenses.copy(
                            sum = sumTextInputEdit.text.toString().toFloat()
                        )
                    )
                }
            sumTextInputEdit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    isSumEdited = true
                }
            })

            expenseTextInputEdit.setOnClickListener {
                val navController = findNavController()
                navController.navigate(R.id.toChoiceExpenseFragment)
            }
            currencyTextInputEdit.setOnClickListener {
                val navController = findNavController()
                navController.navigate(R.id.toChoiceCurrencyFragment)
            }
            val adapter = context?.let { ArrayAdapter(it,R.layout.list_item , listOf("1 USD","1 RUB")) }
            (textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)
//            currencyTextInputLayout.setEndIconOnClickListener { Log.d("blablabla", "setEndIconOnClickListener") }

        })
    }
}