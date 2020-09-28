package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.new_expenses_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.ui.ExpenseFragment.Companion.EXTRA_EXPENSE
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class NewExpensesActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    private var expense: String = ""
    private var currency: String = ""
    private var summa: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.new_expenses_activity)
        setSupportActionBar(newExpensesToolbar).apply {
            title = getString(R.string.add_record_title)
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

        expenseImageButton.setOnClickListener {
            startActivityForResult(
                Intent(this, ExpenseActivity::class.java),
                PICK_EXPENSE_REQUEST
            )
        }
        saveExpensesButton.setOnClickListener {
            summa = sumTextInputEdit.text.toString().toDouble()
            if (expense != "" && summa != 0.0 && !noteTextInputEdit.text.isNullOrBlank()) {
                //Log.d("blablabla", "Save summa= $summa - currency=$currency type =$expense")
                val expensesSave = Expenses(
                    //dateTime = Date( System.currentTimeMillis() ),
                    dateTime = Calendar.getInstance().time,
                    /*dateTime =Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -2)
                    }.time,*/
                    sum = summa,
                    currency = currency,
                    expense = expense,
                    note = noteTextInputEdit.text.toString()
                )
                viewModel.addExpenses(expensesSave)
                finish()
            } else Snackbar.make(
                newExpensesActivityLayout, getString(R.string.addNewRepairError),
                Snackbar.LENGTH_LONG
            ).show()
        }

        viewModel.currencyTableList.observe(
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
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_EXPENSE_REQUEST -> data?.extras?.getString(EXTRA_EXPENSE)?.let { _expense ->
                    viewModel.setQueryExpense(_expense)
                    expense = _expense
                }
            }
        }
    }

    companion object {
        const val PICK_EXPENSE_REQUEST = 1
    }
}