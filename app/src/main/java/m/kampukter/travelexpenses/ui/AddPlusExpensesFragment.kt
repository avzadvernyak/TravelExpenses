package m.kampukter.travelexpenses.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.add_additional_data_expenses.*
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import java.net.URI

class AddPlusExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.add_additional_data_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bufferExpensesMediatorLiveData.observe(viewLifecycleOwner, { value ->
            val tempExpenses = value.first
            if (tempExpenses?.imageUri != null) {
                //Glide.with(view).load(tempExpenses.imageUri).into(photoImageView)
                addPhotoFab?.hide()
                delPhotoFab?.show()
                delPhotoFab?.setOnClickListener {
                    viewModel.setBufferExpensesPhoto(null)
                    viewModel.deleteFile(File(URI(tempExpenses.imageUri)))
                }
            } else {
                addPhotoFab?.show()
                delPhotoFab?.hide()
                addPhotoFab?.setOnClickListener {
                    findNavController().navigate(R.id.toCameraXFragment)
                }
            }
            Glide.with(view).load(tempExpenses?.imageUri).placeholder(R.drawable.ic_photo_24)
                .into(photoImageView)
        })

    }

    override fun onResume() {
        super.onResume()
        hideSystemKeyboard()
    }


    private fun hideSystemKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}