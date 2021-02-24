package m.kampukter.travelexpenses.ui.folders

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.FoldersExtendedView
import m.kampukter.travelexpenses.ui.ClickEventDelegate

class FoldersAdapter(private val clickEventDelegate: ClickEventDelegate<FoldersExtendedView>) :
    RecyclerView.Adapter<FoldersViewHolder>() {

    private var profileList = listOf<FoldersExtendedView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolder {
        return FoldersViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.folders_item, parent, false),
            clickEventDelegate
        )
    }

    override fun getItemCount(): Int = profileList.size

    override fun onBindViewHolder(holder: FoldersViewHolder, position: Int) {
        holder.bind(profileList[position])
    }

    fun setList(list: List<FoldersExtendedView>) {
        this.profileList = list
        Log.d("blabla","$list")
        notifyDataSetChanged()
    }
}