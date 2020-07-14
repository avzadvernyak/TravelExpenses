package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.settings_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Settings
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(settingsToolbar).apply { title = "Settings" }

        var tempDefaultCurrency: Int? = null
        val defaultProgramCurrency = mainApplication.getActiveCurrencySession()
        when (defaultProgramCurrency) {
            // Гривна по умолчанию
            1 -> uahRadioButton.isChecked = true
            // Рубль по умолчению
            2 -> rubRadioButton.isChecked = true
            // Бел рубль по умолчению
            3 -> bynRadioButton.isChecked = true
        }
        uahRadioButton.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId) tempDefaultCurrency = 1
        }
        rubRadioButton.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId) tempDefaultCurrency = 2
        }
        bynRadioButton.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId) tempDefaultCurrency = 3
        }

        saveSettingButton.setOnClickListener {
            if (tempDefaultCurrency != defaultProgramCurrency) tempDefaultCurrency?.let {
                tempDefaultCurrency?.let {
                    mainApplication.changeActiveCurrency(it)
                    viewModel.deleteRate()
                    viewModel.saveSettings(Settings("default", it))
                    //viewModel.testRate()
                }

            }
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}