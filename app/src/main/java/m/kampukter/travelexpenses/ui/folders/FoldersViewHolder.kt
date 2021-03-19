package m.kampukter.travelexpenses.ui.folders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.folders_item.view.*
import m.kampukter.travelexpenses.data.FoldersExtendedView
import m.kampukter.travelexpenses.ui.ClickEventDelegate

class FoldersViewHolder(
    itemView: View,
    private val clickEventDelegate: ClickEventDelegate<FoldersExtendedView>
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: FoldersExtendedView) {
        with(itemView) {
            shortNameTextView.text = item.shortName
            descriptionTextView.text = item.description

            item.countRecords.let {
                countRecordTextView.text = if (it < 999) it.toString() else "999+"
            }
            setOnClickListener {
                clickEventDelegate.onClick(item)
            }
            setOnLongClickListener {
                clickEventDelegate.onLongClick(item)
                return@setOnLongClickListener true
            }
        }
    }
}
