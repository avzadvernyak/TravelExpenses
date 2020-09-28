package m.kampukter.travelexpenses.ui.fragments

import android.R
import android.content.ClipData.Item
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable

//не дописан, вместо написан MyArrayAdapter

class TestArrayAdapter(val context: Context, val resource: Int) : BaseAdapter(), Filterable {
    private var myList = listOf<String>()

    override fun getCount(): Int {
        return myList.size
    }

    override fun getItem(position: Int): String {
        return myList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {

        return LayoutInflater.from(context).inflate(resource, parent, false);
    }

    fun setList(list: List<String>) {
        this.myList = list
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                return FilterResults()
            }
        }
    }
}
