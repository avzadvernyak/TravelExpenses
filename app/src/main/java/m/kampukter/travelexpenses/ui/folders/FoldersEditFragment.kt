package m.kampukter.travelexpenses.ui.folders

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
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

        viewModel.currentFolder.observe(viewLifecycleOwner) {
            if (folderShortNameTextInputEdit.text.toString() != it.shortName) {
                folderShortNameTextInputEdit.setText(it.shortName)
            }
            if (folderDescriptionTextInputEdit.text.toString() != it.description) {
                folderDescriptionTextInputEdit.setText(it.description)
            }
        }
        viewModel.editFolderLiveData.observe(viewLifecycleOwner) { (currentFolder, candidate, folders) ->
            folderShortNameTextInputEdit.error = when {
                folders.map { it.shortName }
                    .contains(candidate.shortName) -> getString(R.string.folder_name_validate_msg_duplicate)
                candidate.shortName.isBlank() -> getString(R.string.folder_name_validate_msg_empty)
                else -> {
                    if (currentFolder != candidate) viewModel.updateFolder(candidate)

                    null
                }
            }
        }
        folderDescriptionTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setFolderDescriptionForUpd(p0.toString())

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
        folderShortNameTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setFolderNameForUpd(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}