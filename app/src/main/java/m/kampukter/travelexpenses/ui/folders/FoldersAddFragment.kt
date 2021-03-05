package m.kampukter.travelexpenses.ui.folders

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.folders_add_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class FoldersAddFragment : Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.folders_add_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveNewFolderFAB.isEnabled = false
        viewModel.isFolderSavingAllowed.observe(viewLifecycleOwner, { _isFolderSavingAllowed ->
            _isFolderSavingAllowed.let { saveNewFolderFAB.isEnabled = it }
        })

        // Set filter folderShortNameTextInputEdit len string max 8
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(20)
        folderShortNameTextInputEdit.filters = filterArray
        folderShortNameTextInputEdit.filterTouchesWhenObscured

        viewModel.inputShortNameError.observe(viewLifecycleOwner, { msg ->
            msg?.let {
                folderShortNameTextInputEdit.error = when(it){
                    FolderNameValidateMsg.FOLDER_NAME_OK -> null
                    FolderNameValidateMsg.FOLDER_NAME_DUPLICATE -> getString(R.string.folder_name_validate_msg_duplicate)
                    FolderNameValidateMsg.FOLDER_NAME_EMPTY -> getString(R.string.folder_name_validate_msg_empty)
                }
            }

        })


        folderShortNameTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setNewFolderName(p0.toString())
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        folderDescriptionTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setNewFolderDescription(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })

        saveNewFolderFAB.setOnClickListener {
            viewModel.saveNewFolder()
            findNavController().navigate(R.id.toFoldersFragment)
        }
    }
}