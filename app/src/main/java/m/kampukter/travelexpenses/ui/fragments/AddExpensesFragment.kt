package m.kampukter.travelexpenses.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.add_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.math.log

class AddExpensesFragment : Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()
    private var myDropdownAdapter: MyArrayAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navController = findNavController()

        myDropdownAdapter =
            context?.let {
                MyArrayAdapter(it, android.R.layout.simple_list_item_1, mutableListOf())
            }
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)
        sumTextInputEdit.setText("")
        viewModel.bufferExpensesMediatorLiveData.observe(viewLifecycleOwner, Observer { value ->

            value.second?.let { list -> myDropdownAdapter?.addAll(list.map { it.name }) }

            val tempExpenses = value.first

            if (tempExpenses == null) {
                val currencyList = value.second
                if (!currencyList.isNullOrEmpty()) {
                    val defCurrencyName =
                        currencyList.find { currency -> currency.defCurrency == 1 }?.name
                    if (defCurrencyName != null) {
                        val currencyPosition = myDropdownAdapter?.getPosition(defCurrencyName)
                        if (currencyPosition != null && currencyPosition >= 0) {
                            currencyTextInputEdit?.setText(
                                myDropdownAdapter?.getItem(currencyPosition).toString(), false
                            )
                        }
                        viewModel.setBufferExpenses(
                            Expenses(
                                dateTime = Calendar.getInstance().time,
                                sum = 0.0,
                                currency = defCurrencyName,
                                expense = "",
                                note = ""
                            )
                        )
                    }
                }
            } else {
                expenseTextInputEdit.setText(tempExpenses.expense)
                if (noteTextInputEdit.text.toString() != tempExpenses.note) noteTextInputEdit.setText(
                    tempExpenses.note
                )

                /*if (sumTextInputEdit.text.toString() != tempExpenses.sum.toString()) {
                    val tempString = tempExpenses.sum.toString()
                    sumTextInputEdit.setText(tempString)
                }*/


            }
            noteTextInputEdit.doOnTextChanged { text, start, before, count ->
                viewModel.setBufferExpenses(
                    tempExpenses?.copy(
                        note = text.toString()
                    )
                )
            }

            sumTextInputEdit.doOnTextChanged { text, start, before, count ->
                val inputString = text.toString()
                if (!inputString.isBlank()) viewModel.setBufferExpenses(
                    tempExpenses?.copy(sum = inputString.toDouble())
                ) else viewModel.setBufferExpenses(tempExpenses?.copy(sum = 0.0))
            }

            currencyTextInputEdit.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
                if (p1) {
                    hideSystemKeyboard()
                } else {
                    val newValue = currencyTextInputEdit.text.toString()
                    if (tempExpenses?.currency != newValue) {
                        viewModel.resetDef()
                        viewModel.setDefCurrency(newValue)
                        viewModel.setBufferExpenses(tempExpenses?.copy(currency = newValue))
                    } else Log.d("blablabla", "Не сохраняем")
                }
            }
            tempExpenses?.currency.let {
                val currencyPosition = myDropdownAdapter?.getPosition(it)
                if (currencyPosition != null && currencyPosition >= 0) {
                    currencyTextInputEdit?.setText(
                        myDropdownAdapter?.getItem(currencyPosition).toString(), false
                    )
                }
            }

        })
        viewModel.isSavingAllowed.observe(viewLifecycleOwner, Observer { _isSavingAllowed ->
            _isSavingAllowed?.let { saveNewExpensesButton.isEnabled = it }
        })
        saveNewExpensesButton.setOnClickListener {
            viewModel.saveNewExpenses()
            //сброс временной переменной
            viewModel.setBufferExpenses(null)
            //установка сохраняемой валюты как по умолчанию
            viewModel.resetDef()
            viewModel.setDefCurrency(currencyTextInputEdit.text.toString())
            hideSystemKeyboard()
            navController.navigate(R.id.next_action)
        }
        expenseTextInputEdit.setOnClickListener {
            navController.navigate(R.id.toChoiceExpenseForAddFragment)
        }

    }

    private fun hideSystemKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
