package m.kampukter.travelexpenses.ui.expenses

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.attachment_photo_view_fragment.*
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AttachmentPhotoViewFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.attachment_photo_view_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(activity as AppCompatActivity) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            supportActionBar?.hide()
            val param = mainAppBarLayout.layoutParams
            param.height = 0
            mainAppBarLayout.layoutParams = param
        }

        val navController = findNavController()

        viewModel.expensesEdit.observe(viewLifecycleOwner) { (expenses, _) ->

            Glide.with(view).load(Uri.parse(expenses.imageUri))
                .placeholder(R.drawable.ic_photo_24)
                .into(attachmentImageView)

            delButton.setOnClickListener {
                navController.navigate(R.id.delAttachmentPhotoDialogFragment)
            }
        }
        backButton.setOnClickListener { navController.navigate(R.id.next_action) }
    }
    override fun onDestroy() {
        super.onDestroy()
        with(requireActivity() as AppCompatActivity) {
            window.decorView.systemUiVisibility = View.VISIBLE
            supportActionBar?.show()
            val param = mainAppBarLayout.layoutParams
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mainAppBarLayout.layoutParams = param
        }
    }
}