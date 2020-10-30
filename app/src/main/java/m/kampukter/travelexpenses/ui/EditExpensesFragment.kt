package m.kampukter.travelexpenses.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.edit_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

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

        val navController = findNavController()

        myDropdownAdapter =
            MyArrayAdapter(view.context, android.R.layout.simple_list_item_1, mutableListOf())
        currencyTextInputEdit?.setAdapter(myDropdownAdapter)

        viewModel.expenseMediatorLiveData.observe(viewLifecycleOwner, { value ->
            value.first?.let { expenses ->

                if (expenses.imageUri != null) {
                    attachmentImageView.visibility = View.VISIBLE
                    Glide.with(view).load(Uri.parse(expenses.imageUri))
                        .placeholder(R.drawable.ic_photo_24)
                        .into(attachmentImageView)
                } else {
                    attachmentImageView.visibility = View.INVISIBLE
                }


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
                myDropdownAdapter.addAll(list.map { it.name })
            }

            value.first?.currency.let {
                val currencyPosition = myDropdownAdapter.getPosition(it)
                if (currencyPosition >= 0) {
                    currencyTextInputEdit?.setText(
                        myDropdownAdapter.getItem(currencyPosition).toString(), false
                    )
                }
            }

        })
        expenseTextInputEdit.setOnClickListener {
            navController.navigate(R.id.toChoiceExpenseForEditFragment)
        }
        attachmentImageView.setOnClickListener {
            navController.navigate(R.id.toAttachmentPhotoViewFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_expenses, menu)
        super.onCreateOptionsMenu(menu, inflater)
        viewModel.expenseMediatorLiveData.observe(viewLifecycleOwner, { value ->
            value.first?.let { expenses ->
                menu.findItem(R.id.addPhoto).isVisible = expenses.imageUri == null
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addPhoto) {
            findNavController().navigate(R.id.toTakePhotoForEditFragment)
        }
        return super.onOptionsItemSelected(item)
    }
}