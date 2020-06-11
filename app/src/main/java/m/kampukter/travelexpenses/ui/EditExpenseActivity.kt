package m.kampukter.travelexpenses.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EditExpenseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(
                android.R.id.content,
                EditExpenseFragment()
            ).commit()
            /*supportFragmentManager.commit {
                replace(android.R.id.content, EditExpenseFragment.create())
            }*/
        }
    }
}