package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.edit_expenses_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.ui.ExpenseFragment.Companion.EXTRA_EXPENSE
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class EditExpensesActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var currency: String = ""
    private var expense: String = ""
    private var summa: Float = 0F
    private var dateTimeRecord = Date()
    private var idRecord: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.edit_expenses_activity)
        setSupportActionBar(editExpensesToolbar).apply {
            title = getString(R.string.expenses_edit_title)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val currencySpinner = currencySpinner
        val currencyAdapter = ArrayAdapter<Any>(this, android.R.layout.simple_spinner_item)
        currencySpinner.adapter = currencyAdapter
        currencySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (currency != currencySpinner.selectedItem.toString()) {
                    viewModel.resetDef()
                    viewModel.setDefCurrency(currencySpinner.selectedItem.toString())
                }
            }
        }

        val idSelectedExpense = intent.getStringExtra(TravelExpensesFragment.EXTRA_MESSAGE)
        idSelectedExpense?.let { viewModel.setQueryExpensesId(it.toLong()) }
        viewModel.expensesById.observe(this, Observer { expenses ->
            idRecord = expenses.id
            dateTimeRecord = expenses.dateTime
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", dateTimeRecord)
            expenseTextView.text = expenses.expense
            sumTextInputEdit.setText(expenses.sum.toString())
            noteTextInputEdit.setText(expenses.note)
            currency = expenses.currency
            expense = expenses.expense
            //currencySpinner.setSelection(currencyId.toInt()-1)
        })
        viewModel.currencyList.observe(
            this,
            Observer { currencyList ->
                currencyAdapter.clear()
                currencyList?.forEachIndexed { count, value ->
                    currencyAdapter.add(value.name)
                    if (value.defCurrency == 1) {
                        currency = value.name
                        currencySpinner.setSelection(count)
                    }
                }
            }
        )
        viewModel.expenseById.observe(this, Observer {
            expenseTextView.text = it.name
            expense = it.name
        })
        expenseEditImageButton.setOnClickListener {
            startActivityForResult(
                Intent(this, ExpenseActivity::class.java),
                PICK_EXPENSE_REQUEST_EDIT
            )
        }
        saveEditExpensesButton.setOnClickListener {
            summa = sumTextInputEdit.text.toString().toFloat()
            if (expense != "" && summa != 0F && !noteTextInputEdit.text.isNullOrBlank()) {
                val expensesSave = Expenses(
                    id = idRecord,
                    dateTime = dateTimeRecord,
                    sum = summa,
                    currency = currency,
                    expense = expense,
                    note = noteTextInputEdit.text.toString()
                )
                viewModel.updateExpenses(expensesSave)
                finish()
            } else Snackbar.make(
                newExpensesActivityLayout, getString(R.string.addNewRepairError),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_EXPENSE_REQUEST_EDIT -> data?.extras?.getString(EXTRA_EXPENSE)
                    ?.let { _expense ->
                        viewModel.setQueryExpense(_expense)
                        expense = _expense
                    }
            }
        }
    }

    companion object {
        const val PICK_EXPENSE_REQUEST_EDIT = 1
    }
}