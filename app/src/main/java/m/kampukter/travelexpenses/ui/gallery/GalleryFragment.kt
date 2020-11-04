package m.kampukter.travelexpenses.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.gallery_fragment.*
import kotlinx.android.synthetic.main.gallery_item.view.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class GalleryFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var pageAdapter: GalleryPageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gallery_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageAdapter = GalleryPageAdapter()
        photoPager.adapter = pageAdapter
        photoPager.setPageTransformer { page, _ ->
            BottomSheetBehavior.from(page.bottom_sheet_layout).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }

        viewModel.expensesWithRate.observe(viewLifecycleOwner, { expenses ->
            pageAdapter.setList(expenses.filter { it.imageUri != null })

        })
    }
}