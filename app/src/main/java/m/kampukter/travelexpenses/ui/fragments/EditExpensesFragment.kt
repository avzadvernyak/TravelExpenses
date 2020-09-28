package m.kampukter.travelexpenses.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.edit_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private var myDropdownAdapter: MyArrayAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // (currencyTextInputLayout.editText as? AutoCompleteTextView)?.

        myDropdownAdapter =
            context?.let {
                MyArrayAdapter(it, android.R.layout.simple_list_item_1, mutableListOf())
            }
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)

        viewModel.expenseMediatorLiveData.observe(viewLifecycleOwner, Observer { value ->
            value.first?.let { expenses ->
                sumTextInputEdit.setText(expenses.sum.toString())
                dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", expenses.dateTime)
                expenseTextInputEdit.setText(expenses.expense)
                noteTextInputEdit.setText(expenses.note)
                noteTextInputEdit.onFocusChangeListener =
                    View.OnFocusChangeListener { _, p1 ->
                        if (!p1) viewModel.addExpenses(expenses.copy(note = noteTextInputEdit.text.toString()))
                    }
                sumTextInputEdit.onFocusChangeListener =
                    View.OnFocusChangeListener { _, p1 ->
                        if (!p1) viewModel.addExpenses(
                            expenses.copy(
                                sum = sumTextInputEdit.text.toString().toDouble()
                            )
                        )
                    }
                currencyTextInputEdit.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
                    if (!p1) {
                        val newValue = currencyTextInputEdit.text.toString()
                        if (expenses.currency != newValue) {
                            viewModel.resetDef()
                            viewModel.setDefCurrency(newValue)
                            viewModel.addExpenses(expenses.copy(currency = newValue))
                        } else Log.d("blablabla", "Не сохраняем")
                    } else {
                        val imm =
                            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }
            value.second?.let { list ->
                myDropdownAdapter?.addAll(list.map { it.name })
            }

            value.first?.currency.let {
                val currencyPosition = myDropdownAdapter?.getPosition(it)
                if (currencyPosition != null && currencyPosition >= 0) {
                    currencyTextInputEdit?.setText(
                        myDropdownAdapter?.getItem(currencyPosition).toString(), false
                    )
                }
            }

        })
        expenseTextInputEdit.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.toChoiceExpenseForEditFragment)
        }
    }
}