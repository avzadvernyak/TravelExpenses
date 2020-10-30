package m.kampukter.travelexpenses.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.attachment_photo_view_fragment.*
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

        val navController = findNavController()

        viewModel.expenseMediatorLiveData.observe(viewLifecycleOwner, { value ->
            value.first?.let { expenses ->

                Glide.with(view).load(Uri.parse(expenses.imageUri))
                    .placeholder(R.drawable.ic_photo_24)
                    .into(attachmentImageView)

                delFab.setOnClickListener {
                    navController.navigate(R.id.delAttachmentPhotoDialogFragment)
                }
            }
        })
        backFab.setOnClickListener { navController.navigate(R.id.next_action) }
    }
}