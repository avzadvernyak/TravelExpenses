package m.kampukter.travelexpenses

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.edit_expenses_fragment.*

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val items = listOf("одын", "джва", "трэ")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        (textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }
}