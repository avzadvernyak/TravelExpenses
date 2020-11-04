package m.kampukter.travelexpenses.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate

class GalleryPageAdapter : RecyclerView.Adapter<GalleryViewHolder>() {

    private var mediaList = emptyList<ExpensesWithRate>()

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) =
        holder.bind(mediaList[position])

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GalleryViewHolder = GalleryViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
    )

    fun setList(newMediaList: List<ExpensesWithRate>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                mediaList[oldItemPosition].rate == newMediaList[newItemPosition].rate

            override fun getOldListSize(): Int = mediaList.size

            override fun getNewListSize(): Int = newMediaList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                mediaList[oldItemPosition] == newMediaList[newItemPosition]

        })
        mediaList = newMediaList
        diff.dispatchUpdatesTo(this)
    }
}