package m.kampukter.travelexpenses.ui.expenses

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.edit_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.CurrencyTable
import m.kampukter.travelexpenses.data.EditedExpensesField
import m.kampukter.travelexpenses.ui.MyArrayAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class EditExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private lateinit var myDropdownAdapter: MyArrayAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.edit_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sumTextInputEdit.setText("0,0")

        val navController = findNavController()
        var currentIdExpenses: Long? = null

        myDropdownAdapter =
            MyArrayAdapter(view.context, android.R.layout.simple_list_item_1, mutableListOf())
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)

        arguments?.getLong("expensesId")?.let {
            viewModel.expensesIdEdit(it)
            currentIdExpenses = it
        }
        viewModel.expensesEdit.observe(viewLifecycleOwner) { (expenses, currencyList) ->

            locationChip.visibility = if ( expenses.location ==null ) View.INVISIBLE else View.VISIBLE

            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", expenses.dateTime)

            if (expenseTextInputEdit.text.toString() != expenses.expense) expenseTextInputEdit.setText(
                expenses.expense
            )
            if (currencyTextInputEdit.text.toString() != expenses.currency) currencyTextInputEdit.setText(
                expenses.currency
            )
            if (sumTextInputEdit.text.toString() != expenses.sum.toString()) sumTextInputEdit.setText(
                expenses.sum.toString()
            )
            if (noteTextInputEdit.text.toString() != expenses.note) noteTextInputEdit.setText(
                expenses.note
            )

            if (expenses.imageUri != null) {
                attachmentImageView.visibility = View.VISIBLE
                Glide.with(view).load(Uri.parse(expenses.imageUri))
                    .placeholder(R.drawable.ic_photo_24)
                    .into(attachmentImageView)
            } else {
                attachmentImageView.visibility = View.INVISIBLE
            }

            myDropdownAdapter.addAll(currencyList.map { it.name })
            val currencyName =
                if (expenses.currency.isBlank()) currencyList.find { it.defCurrency == 1 }?.name
                else expenses.currency
            myDropdownAdapter.getPosition(currencyName).let {
                if (currencyTextInputEdit.text.toString() != currencyName)
                    currencyTextInputEdit?.setText(currencyName)
            }
            expenseTextInputEdit.setOnClickListener {
                val bundle = bundleOf("expensesIdForEdit" to expenses.id)
                navController.navigate(R.id.toChoiceExpenseForEditFragment, bundle)
            }
        }
        locationChip.setOnClickListener {
            navController.navigate(R.id.toMapPointFragment)
        }
        currencyTextInputEdit.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
            if (p1) {
                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        currencyTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                currentIdExpenses?.let { id ->
                    viewModel.updateExpenses(
                        EditedExpensesField.CurrencyField(id, CurrencyTable(name = p0.toString()))
                    )
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        sumTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                currentIdExpenses?.let { id ->
                    viewModel.updateExpenses(
                        EditedExpensesField.SumField(id, p0.toString().toDouble())
                    )
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        noteTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                currentIdExpenses?.let { id ->
                    viewModel.updateExpenses(EditedExpensesField.NoteField(id, p0.toString()))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        attachmentImageView.setOnClickListener {
            navController.navigate(R.id.toAttachmentPhotoViewFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_expenses, menu)
        super.onCreateOptionsMenu(menu, inflater)
        viewModel.expensesEdit.observe(viewLifecycleOwner) { (expenses, _) ->
            menu.findItem(R.id.addPhoto).isVisible = expenses.imageUri == null
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addPhoto) {
            findNavController().navigate(R.id.toTakePhotoForEditFragment)
        }
        return super.onOptionsItemSelected(item)
    }
}