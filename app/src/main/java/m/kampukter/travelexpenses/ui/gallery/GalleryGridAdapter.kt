package m.kampukter.travelexpenses.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesExtendedView

class GalleryGridAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mediaList = emptyList<ExpensesExtendedView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder  = GalleryGridViewHolder( LayoutInflater
        .from(parent.context)
        .inflate(R.layout.gallery_grid_item, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GalleryGridViewHolder).bind( mediaList[position] )
    }

    override fun getItemCount(): Int = mediaList.size

    fun setList(newMediaList: List<ExpensesExtendedView>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                mediaList[oldItemPosition].id == newMediaList[newItemPosition].id

            override fun getOldListSize(): Int = mediaList.size

            override fun getNewListSize(): Int = newMediaList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                mediaList[oldItemPosition].imageUri == newMediaList[newItemPosition].imageUri

        })
        mediaList = newMediaList
        diff.dispatchUpdatesTo(this)

    }

}
