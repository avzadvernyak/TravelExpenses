package m.kampukter.travelexpenses.ui.gallery

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.gallery_item.view.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.gallery.GalleryFragment.Companion.ACTION_FULL_SCREEN
import m.kampukter.travelexpenses.ui.gallery.GalleryFragment.Companion.ACTION_ZOOM_OFF
import m.kampukter.travelexpenses.ui.gallery.GalleryFragment.Companion.ACTION_ZOOM_ON


class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: ExpensesWithRate, onClickCallback: ((Int) -> Unit)?) {
        with(itemView) {

            Glide.with(this).load(Uri.parse(item.imageUri)).placeholder(R.drawable.ic_photo_24)
                .fitCenter().into(itemPhotoView)

            itemPhotoView.setOnClickListener { onClickCallback?.invoke(ACTION_FULL_SCREEN) }
            itemPhotoView.setOnMatrixChangeListener {
                if (itemPhotoView.scale == 1F) {
                    onClickCallback?.invoke(ACTION_ZOOM_ON)
                } else {
                    onClickCallback?.invoke(ACTION_ZOOM_OFF)
                }
            }
        }
    }

}
