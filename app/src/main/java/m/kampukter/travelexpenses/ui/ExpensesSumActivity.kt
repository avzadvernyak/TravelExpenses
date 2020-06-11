package m.kampukter.travelexpenses.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.expenses_sum_activity.*
import m.kampukter.travelexpenses.R

class ExpensesSumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.expenses_sum_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.expenses_sum_title)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        viewPager.adapter = ReportPagerAdapter(supportFragmentManager)
        /*
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            android.R.id.content,
            ExpensesSumFragment()
        ).commit()
         */
    }
}