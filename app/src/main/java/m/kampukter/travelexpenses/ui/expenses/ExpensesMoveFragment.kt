package m.kampukter.travelexpenses.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_move_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FoldersExtendedView
import m.kampukter.travelexpenses.ui.ClickEventDelegate
import m.kampukter.travelexpenses.ui.folders.FoldersAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExpensesMoveFragment : Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var foldersAdapter: FoldersAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.expenses_move_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        val ids = arguments?.getLongArray("Ids")

        val clickEventDelegate: ClickEventDelegate<FoldersExtendedView> =
            object : ClickEventDelegate<FoldersExtendedView> {
                override fun onClick(item: FoldersExtendedView) {
                    ids?.let { viewModel.moveSelectedExpenses( it.toSet(), item.id)}
                    navController.navigateUp()
                }

                override fun onLongClick(item: FoldersExtendedView) {}
            }
        foldersAdapter = FoldersAdapter(clickEventDelegate)
        with(foldersCandidateRecyclerView) {
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
    }
}