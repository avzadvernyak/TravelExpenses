package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.new_expenses_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.ui.ExpenseFragment.Companion.EXTRA_EXPENSE_ID
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewExpensesActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var expenseId: Long = 0L
    private var currencyId: Long = 0L
    private var summa: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.new_expenses_activity)
        setSupportActionBar(newExpensesToolbar).apply { title = getString(R.string.add_expenses_title) }
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
                    viewModel.resetDef()
                    viewModel.setDefCurrency(pos)
                }
            }
        }

        expenseImageButton.setOnClickListener {
            startActivityForResult(
                Intent(this, ExpenseActivity::class.java),
                PICK_EXPENSE_REQUEST
            )
        }
        saveExpensesButton.setOnClickListener {
            summa = sumTextInputEdit.text.toString().toFloat()
            if (expenseId != 0L && summa != 0F) {
                Log.d("blablabla", "Save summa= $summa - currencyId=$currencyId type =$expenseId")
                val expensesSave = Expenses(
                    dateTime = System.currentTimeMillis(),
                    sum = summa,
                    currency = currencyId,
                    expense = expenseId,
                    note = noteTextInputEdit.text.toString()
                )
                viewModel.addExpenses(expensesSave)
                finish()
            } else Snackbar.make(
                newExpensesActivityLayout, getString(R.string.addNewRepairError),
                Snackbar.LENGTH_LONG
            ).show()
        }

        viewModel.defCurrency.observe(
            this,
            Observer { defCurrency ->
                currencyId = if (defCurrency != null) defCurrency.id else 1L
                currencySpinner.setSelection(currencyId.toInt() - 1)
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
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_EXPENSE_REQUEST -> data?.extras?.getString(EXTRA_EXPENSE_ID)?.let { _expenseId ->
                    viewModel.setQueryExpenseId(_expenseId)
                    expenseId = _expenseId.toLong()
                }
            }
        }
    }

    companion object {
        const val PICK_EXPENSE_REQUEST = 1
    }
}