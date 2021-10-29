package m.kampukter.travelexpenses.ui.gallery

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.gallery_grid_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesExtendedView
import m.kampukter.travelexpenses.ui.ClickEventDelegate
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

        val clickEventDelegate: ClickEventDelegate<ViewItem> =
            object : ClickEventDelegate<ViewItem> {

                override fun onClick(item: ViewItem) {
                    val bundle = bundleOf("galleryItemId" to (item as ViewItem.ImageItem).expensesItem.id )
                    findNavController().navigate(R.id.galleryFragment, bundle)
                }

                override fun onLongClick(item: ViewItem) {
                    //onLongClick
                }
            }

        val layoutManager = GridLayoutManager(context, 3)

        galleryGridAdapter = GalleryGridAdapter(clickEventDelegate)
        galleryGridRecyclerView.layoutManager = layoutManager
        galleryGridRecyclerView.adapter = galleryGridAdapter

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ( galleryGridAdapter?.getItemViewType(position) ) {
                    GALLERY_GRID_TYPE_ITEM_TITLE -> 3
                    else -> 1
                }
            }
        }

        viewModel.expensesInFolder.observe(viewLifecycleOwner) { (_, expenses) ->
            //val collection = expenses.filter { it.imageUri != null }

            val newCollection = expenses.filter { it.imageUri != null }
                .groupBy { DateFormat.format("dd/MM/yyyy", it.dateTime) }
            val items = mutableListOf<ViewItem>()
            newCollection.forEach {
                items.add(ViewItem.DateItem(dateString = it.key.toString()))
                it.value.forEach { element -> items.add(ViewItem.ImageItem(expensesItem = element)) }
            }
            galleryGridAdapter?.setList(items)
        }
    }
    companion object {
        const val GALLERY_GRID_TYPE_ITEM_TITLE = 1
        const val GALLERY_GRID_TYPE_ITEM_PHOTO = 2
    }
}

sealed class ViewItem {
    class DateItem(val dateString: String) : ViewItem()
    class ImageItem(val expensesItem: ExpensesExtendedView) : ViewItem()
}