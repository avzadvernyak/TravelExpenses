package m.kampukter.travelexpenses.ui.folders

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.folders_add_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class FoldersEditFragment : Fragment() {

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

        saveNewFolderFAB.visibility = View.GONE

        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(20)
        folderShortNameTextInputEdit.filters = filterArray
        folderShortNameTextInputEdit.filterTouchesWhenObscured

        folderDescriptionTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val shortName = folderShortNameTextInputEdit.text.toString()
                viewModel.updateFolderDescription(shortName, p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        folderShortNameTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.updateFolderShortName(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        viewModel.editFolderMsg.observe(viewLifecycleOwner, { msg ->
            msg?.let {
                folderShortNameTextInputEdit.error = when (it) {
                    FolderNameValidateMsg.FOLDER_NAME_OK -> null
                    FolderNameValidateMsg.FOLDER_NAME_DUPLICATE -> getString(R.string.folder_name_validate_msg_duplicate)
                    FolderNameValidateMsg.FOLDER_NAME_EMPTY -> getString(R.string.folder_name_validate_msg_empty)
                }
            }

        })

        viewModel.currentFolder.observe(viewLifecycleOwner, { folder ->
            folder?.let {
                if (folderShortNameTextInputEdit.text.toString() != it.shortName)
                    folderShortNameTextInputEdit.setText(it.shortName)
                if (folderDescriptionTextInputEdit.text.toString() != it.description)
                    folderDescriptionTextInputEdit.setText(it.description)
            }
        })
    }
}