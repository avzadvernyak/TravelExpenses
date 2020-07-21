package m.kampukter.travelexpenses.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CurrentExchangeRateActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            android.R.id.content,
            CurrentExchangeRateFragment()
        ).commit()
    }
}