package m.kampukter.travelexpenses.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.gallery_grid_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class GalleryGridFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var galleryGridAdapter: GalleryGridAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gallery_grid_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryGridAdapter = GalleryGridAdapter()
        galleryGridRecyclerView.layoutManager = GridLayoutManager(context, 3)
        galleryGridRecyclerView.adapter = galleryGridAdapter

        viewModel.expensesInFolder.observe(viewLifecycleOwner) { (_, expenses) ->
            val collection = expenses.filter { it.imageUri != null }
            galleryGridAdapter?.setList(collection)
        }
    }
}