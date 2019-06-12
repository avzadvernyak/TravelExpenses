package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import m.kampukter.travelexpenses.data.TravelExpensesView
import m.kampukter.travelexpenses.data.repository.CurrencyRepository
import m.kampukter.travelexpenses.data.repository.ExpenseRepository
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.TravelExpensesRepository

class MyViewModel(
    currencyRepository: CurrencyRepository,
    expenseRepository: ExpenseRepository,
    expensesRepository: ExpensesRepository,
    travelExpensesRepository: TravelExpensesRepository
) : ViewModel() {

    val expenses: LiveData<List<TravelExpensesView>> = travelExpensesRepository.getAll()

}