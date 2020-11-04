package m.kampukter.travelexpenses.ui.gallery

import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.gallery_item.view.*
import kotlinx.android.synthetic.main.gallery_item.view.currencyTextView
import kotlinx.android.synthetic.main.gallery_item.view.dateTimeTextView
import kotlinx.android.synthetic.main.gallery_item.view.expenseTextView
import kotlinx.android.synthetic.main.gallery_item.view.noteTextView
import kotlinx.android.synthetic.main.gallery_item.view.rateTextView
import kotlinx.android.synthetic.main.gallery_item.view.sumTextView
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.data.ExpensesWithRate
import java.text.DecimalFormat

class GalleryViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    fun bind(item: ExpensesWithRate){
        with(itemView) {
            Glide.with(this).load(Uri.parse(item.imageUri)).placeholder(R.drawable.ic_photo_24)
                .into(photoImageView)



            val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
           /* bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    Log.d("blabla", "onStateChanged -> $newState" )
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    Log.d("blabla", "onSlide $slideOffset")

                }
            })*/
            photoImageView.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }


            sumTextView.text = item.sum.toString()
            expenseTextView.text = item.expense
            currencyTextView.text = item.currency
            noteTextView.text = item.note
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", item.dateTime)
            val pattern = when (mainApplication.getActiveCurrencySession()) {
                // Гривна по умолчанию
                DEFAULT_CURRENCY_CONST_UAH -> "######.## UAH"
                // Рубль по умолчению
                DEFAULT_CURRENCY_CONST_RUB -> "######.## RUB"
                // Белорусский Рубль по умолчению
                DEFAULT_CURRENCY_CONST_BYN -> "####.#### BYN"
                else -> "######.##"
            }
            item.rate?.let { rateTextView.text = DecimalFormat(pattern).format(item.sum * it) }

        }
    }

}
