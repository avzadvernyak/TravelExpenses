package m.kampukter.travelexpenses.ui.gallery

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.gallery_item.view.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesExtendedView

class GalleryGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: ExpensesExtendedView) {
        with(itemView) {

            Glide.with(this).load(Uri.parse(item.imageUri)).placeholder(R.drawable.ic_photo_24)
                .fitCenter().into(itemGridPhotoView)
        }
    }
}