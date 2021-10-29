package m.kampukter.travelexpenses.ui.gallery

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.gallery_grid_item.view.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.ui.ClickEventDelegate

class GalleryGridViewHolder(
    itemView: View,
    private val clickEventDelegate: ClickEventDelegate<ViewItem>
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: ViewItem) {
        with(itemView) {
            itemGridPhotoView.setOnClickListener {
                clickEventDelegate.onClick(item)
            }
            itemGridPhotoView.setOnLongClickListener {
                clickEventDelegate.onLongClick(item)
                return@setOnLongClickListener true
            }
            val data = (item as ViewItem.ImageItem).expensesItem
            Glide.with(this).load(Uri.parse(data.imageUri)).placeholder(R.drawable.ic_photo_24)
                .fitCenter().into(itemGridPhotoView)

        }
    }
}