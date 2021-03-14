package m.kampukter.travelexpenses.ui.gallery

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.gallery_fragment.*
import kotlinx.android.synthetic.main.gallery_item.*
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.DecimalFormat

class GalleryFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var pageAdapter: GalleryPageAdapter
    private var isFullScreen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gallery_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout)

        pageAdapter = GalleryPageAdapter()

        photoPager.adapter = pageAdapter
        photoPager.setPageTransformer { _, _ ->
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            imageInGalleryToggleButton.visibility = View.VISIBLE
        }
        pageAdapter.onClickCallback = { actionId ->
            when (actionId) {
                ACTION_FULL_SCREEN -> {
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        imageInGalleryToggleButton.visibility = View.VISIBLE
                    } else {
                        with(activity as AppCompatActivity) {
                            isFullScreen = if (isFullScreen) {
                                window.decorView.systemUiVisibility = View.VISIBLE
                                supportActionBar?.show()
                                val param = mainAppBarLayout.layoutParams
                                param.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                mainAppBarLayout.layoutParams = param
                                imageInGalleryToggleButton.visibility = View.VISIBLE
                                itemPhotoViewLayout.setBackgroundColor(Color.WHITE)
                                photoPager.isUserInputEnabled = true
                                false
                            } else {
                                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                                supportActionBar?.hide()
                                val param = mainAppBarLayout.layoutParams
                                param.height = 0
                                mainAppBarLayout.layoutParams = param
                                imageInGalleryToggleButton.visibility = View.INVISIBLE
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                itemPhotoViewLayout.setBackgroundColor(Color.BLACK)
                                photoPager.isUserInputEnabled = false
                                true
                            }
                        }
                    }
                }
                ACTION_ZOOM_ON -> {
                    if (!isFullScreen) photoPager?.isUserInputEnabled = true
                }
                ACTION_ZOOM_OFF -> {
                    if (!isFullScreen) photoPager?.isUserInputEnabled = false
                }
            }

        }

        viewModel.expensesWithRate.observe(viewLifecycleOwner, { list ->
            val collection = list.filter { it.imageUri != null }
            pageAdapter.setList(collection)
            photoPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    imageInGalleryToggleButton.addOnButtonCheckedListener { _, checkedId, _ ->
                        when (checkedId) {
                            infoButton.id -> {
                                bottomSheetInit(collection[position])
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                                imageInGalleryToggleButton.visibility = View.INVISIBLE
                            }
                            delButton.id -> {
                                viewModel.setQueryExpensesId(collection[position].id)
                                navController.navigate(R.id.delPhotoFromGalleryDialogFragment)
                            }
                            shareButton.id -> {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    val date = DateFormat.format(
                                        "dd/MM/yyyy HH:mm",
                                        collection[position].dateTime
                                    ).toString()
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        getString(
                                            R.string.msg_sent_to,
                                            collection[position].expense_id.toString(),
                                            collection[position].note,
                                            collection[position].sum,
                                            collection[position].currency,
                                           date
                                        )
                                    )
                                    putExtra(
                                        Intent.EXTRA_STREAM,
                                        Uri.parse(collection[position].imageUri)
                                    )
                                    type = "image/*"
                                }
                                startActivity(Intent.createChooser(sendIntent, "Share photo"))
                            }
                        }
                    }
                }

            })
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFullScreen) with(requireActivity() as AppCompatActivity) {
            window.decorView.systemUiVisibility = View.VISIBLE
            supportActionBar?.show()
            val param = mainAppBarLayout.layoutParams
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mainAppBarLayout.layoutParams = param
        }
    }

    private fun bottomSheetInit(item: ExpensesWithRate) {
        sumTextView.text = item.sum.toString()
        expenseTextView.text = item.expense_id.toString()
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

    companion object {
        const val ACTION_ZOOM_ON = 4
        const val ACTION_ZOOM_OFF = 3
        const val ACTION_FULL_SCREEN = 2
    }
}
