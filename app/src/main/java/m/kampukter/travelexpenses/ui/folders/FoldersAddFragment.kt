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
import m.kampukter.travelexpenses.data.Folders
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


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

        // Set filter folderShortNameTextInputEdit len string max 20
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(20)
        folderShortNameTextInputEdit.filters = filterArray
        folderShortNameTextInputEdit.filterTouchesWhenObscured

        folderShortNameTextInputEdit.setText(
            getString(
                R.string.expenses,
                UUID.randomUUID().toString()
            )
        )


        viewModel.lastFolderLiveData.observe(viewLifecycleOwner) { (candidate, folders) ->
            if (folderShortNameTextInputEdit.text.toString() != candidate.shortName) {
                folderShortNameTextInputEdit.setText(candidate.shortName)
                folderDescriptionTextInputEdit.setText(candidate.description)
            }
            folderShortNameTextInputEdit.error = when {
                candidate.shortName.isBlank() -> {
                    saveNewFolderFAB.isEnabled = false
                    getString(R.string.folder_name_validate_msg_empty)
                }
                folders.map { it.shortName }.contains(candidate.shortName) -> {
                    saveNewFolderFAB.isEnabled = false
                    getString(R.string.folder_name_validate_msg_duplicate)
                }
                else -> {
                    saveNewFolderFAB.isEnabled = true
                    null
                }
            }

        }

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
            viewModel.saveNewFolder(
                Folders(
                    shortName = folderShortNameTextInputEdit.text.toString(),
                    description = folderDescriptionTextInputEdit.text.toString()
                )
            )
            findNavController().navigate(R.id.toFoldersFragment)
        }

    }
}