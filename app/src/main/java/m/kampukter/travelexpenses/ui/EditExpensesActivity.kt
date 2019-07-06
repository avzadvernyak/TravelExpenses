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
import kotlinx.android.synthetic.main.edit_expenses_activity.currencySpinner
import kotlinx.android.synthetic.main.edit_expenses_activity.expenseTextView
import kotlinx.android.synthetic.main.edit_expenses_activity.noteTextInputEdit
import kotlinx.android.synthetic.main.edit_expenses_activity.sumTextInputEdit
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.ui.ExpenseFragment.Companion.EXTRA_EXPENSE_ID
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditExpensesActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var currencyId: Long = 0L
    private var expenseId: Long = 0L
    private var summa: Float = 0F
    private var dateTimeRecord: Long = 0L
    private var idRecord: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.edit_expenses_activity)
        setSupportActionBar(editExpensesToolbar).apply { title = getString(R.string.expenses_edit_title) }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val currencySpinner = currencySpinner
        val currencyAdapter = ArrayAdapter<Any>(this, android.R.layout.simple_spinner_item)
        currencySpinner.adapter = currencyAdapter
        currencySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val pos = (position + 1).toLong()
                if (currencyId != pos) {
                    currencyId = pos
                    currencySpinner.setSelection(currencyId.toInt()-1)
                }
            }
        }

        val idSelectedExpense = intent.getStringExtra(TravelExpensesFragment.EXTRA_MESSAGE)
        viewModel.setQueryTravelExpensesId(idSelectedExpense.toLong())
        viewModel.expensesById.observe(this, Observer { expenses ->
            idRecord = expenses.id
            dateTimeRecord = expenses.dateTime
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", dateTimeRecord)
            expenseTextView.text = expenses.expenseName
            sumTextInputEdit.setText(expenses.sum.toString())
            noteTextInputEdit.setText(expenses.note)
            currencyId = expenses.currencyId
            expenseId = expenses.expenseId
            currencySpinner.setSelection(currencyId.toInt()-1)
        })
        viewModel.currencyList.observe(
            this,
            Observer { currency ->
                currencyAdapter.clear()
                currency?.forEach { currencyAdapter.add(it.name) }
            }
        )
        viewModel.expenseById.observe(this, Observer {
            expenseTextView.text = it.name
            expenseId = it.id
        })
        expenseEditImageButton.setOnClickListener {
            startActivityForResult(
                Intent(this, ExpenseActivity::class.java),
                PICK_EXPENSE_REQUEST_EDIT
            )
        }
        saveEditExpensesButton.setOnClickListener {
            summa = sumTextInputEdit.text.toString().toFloat()
            if (expenseId != 0L && summa != 0F) {
                val expensesSave = Expenses(
                    id = idRecord,
                    dateTime = dateTimeRecord,
                    sum = summa,
                    currency = currencyId,
                    expense = expenseId,
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
                PICK_EXPENSE_REQUEST_EDIT -> data?.extras?.getString(EXTRA_EXPENSE_ID)?.let { _expenseId ->
                    viewModel.setQueryExpenseId(_expenseId)
                    expenseId = _expenseId.toLong()
                }
            }
        }
    }
    companion object {
        const val PICK_EXPENSE_REQUEST_EDIT = 1
    }
}