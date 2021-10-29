package m.kampukter.travelexpenses.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesExtendedView

class GalleryPageAdapter : RecyclerView.Adapter<GalleryViewHolder>() {

    private var mediaList = emptyList<ExpensesExtendedView>()

    var onClickCallback: ((Int) -> Unit)? = null

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(mediaList[position], onClickCallback)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GalleryViewHolder = GalleryViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
    )

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