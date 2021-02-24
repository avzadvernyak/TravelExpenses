package m.kampukter.travelexpenses.ui.folders

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.folders_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FolderDeletionResult
import m.kampukter.travelexpenses.data.FoldersExtendedView
import m.kampukter.travelexpenses.ui.ClickEventDelegate
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class FoldersFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var foldersAdapter: FoldersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.folders_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        // Hide Soft Input
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            0
        )

        viewModel.currentFolder.observe(viewLifecycleOwner, { folder ->
            shortNameFolderTextView.text = folder?.shortName
            descriptionFolderTextView.text = folder?.description
        })

        viewModel.folderDeletionResult.observe(viewLifecycleOwner, { result ->
            when (result) {
                is FolderDeletionResult.Warning -> {
                    val bundle = bundleOf(
                        "folderPhaseSecond" to resources.getString(
                            R.string.expense_del_warning,
                            result.folderName,
                            result.countRecords
                        )
                    )
                    navController.navigate(R.id.toDelFolderPhaseSecondDialogFragment, bundle)
                }
                is FolderDeletionResult.Success -> Snackbar.make(
                    view,
                    "Success",
                    Snackbar.LENGTH_SHORT
                ).show()

            }
        })

        val clickEventDelegate: ClickEventDelegate<FoldersExtendedView> =
            object : ClickEventDelegate<FoldersExtendedView> {
                override fun onClick(item: FoldersExtendedView) {

                    viewModel.setSettingNewFolder(item.shortName)
                }

                override fun onLongClick(item: FoldersExtendedView) {
                    viewModel.deleteFolderName(item.shortName)
                    val bundle = bundleOf("folderPhaseOne" to item.shortName)
                    navController.navigate(R.id.toDelFolderPhaseOneDialogFragment, bundle)
                }
            }
        foldersAdapter = FoldersAdapter(clickEventDelegate)
        with(foldersRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = foldersAdapter
        }
        viewModel.folderCandidates.observe(viewLifecycleOwner, { listFolders ->
            foldersAdapter.setList(listFolders)
        })

        val addFAB = activity?.findViewById<ExtendedFloatingActionButton>(R.id.addExpenseFab)
        addFAB?.setOnClickListener {
            navController.navigate(R.id.toFoldersAddFragment)
        }
        foldersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                addFAB?.let {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && !addFAB.isExtended
                        && recyclerView.computeVerticalScrollOffset() == 0
                    ) {
                        addFAB.extend()
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                addFAB?.let {
                    if (dy != 0 && addFAB.isExtended) {
                        addFAB.shrink()
                    }
                }
                super.onScrolled(recyclerView, dx + 16, dy + 16)
            }
        })
        iconEditCheckBox.setOnClickListener {
            navController.navigate(R.id.toFoldersEditFragment)
        }
    }
}