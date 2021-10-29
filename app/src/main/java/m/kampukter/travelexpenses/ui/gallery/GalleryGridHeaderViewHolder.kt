package m.kampukter.travelexpenses.ui.gallery

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.gallery_grid_item_date.view.*

class GalleryGridHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: ViewItem) {
        with(itemView) {
            val data = (item as ViewItem.DateItem).dateString
            datePhotoTextView.text = data
        }
    }
}
