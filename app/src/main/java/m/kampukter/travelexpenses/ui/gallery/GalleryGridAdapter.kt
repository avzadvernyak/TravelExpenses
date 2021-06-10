package m.kampukter.travelexpenses.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.ui.ClickEventDelegate
import m.kampukter.travelexpenses.ui.gallery.GalleryGridFragment.Companion.GALLERY_GRID_TYPE_ITEM_PHOTO
import m.kampukter.travelexpenses.ui.gallery.GalleryGridFragment.Companion.GALLERY_GRID_TYPE_ITEM_TITLE

class GalleryGridAdapter(private var clickListener: ClickEventDelegate<ViewItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mediaList = emptyList<ViewItem>()

    override fun getItemCount(): Int = mediaList.size

    override fun getItemViewType(position: Int): Int = when (mediaList[position]) {
        is ViewItem.DateItem -> GALLERY_GRID_TYPE_ITEM_TITLE
        is ViewItem.ImageItem -> GALLERY_GRID_TYPE_ITEM_PHOTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            1 -> GalleryGridHeaderViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.gallery_grid_item_date, parent, false)
            )
            else -> GalleryGridViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.gallery_grid_item, parent, false),
                clickListener

            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GalleryGridViewHolder -> {
                val item = mediaList[position]
                holder.bind(item)
            }
            is GalleryGridHeaderViewHolder -> holder.bind(mediaList[position])
        }
    }

    fun setList(newMediaList: List<ViewItem>) {

        this.mediaList = newMediaList
        notifyDataSetChanged()

    }

}
