package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.settings_activity.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Periodic
import m.kampukter.travelexpenses.data.Settings
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()
    lateinit var programId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(settingsToolbar).apply { title = "Settings" }

        var tempDefaultCurrency = 0
        var tempBackupPeriod = 0
        var oldBackupPeriod: Int? = null
        var oldDefaultCurrency: Int? = null

        viewModel.savedSettings.observe(this, Observer { settings ->
            if (settings != null) {
                programId = settings.userName
                tempBackupPeriod = settings.backupPeriod
                tempDefaultCurrency = settings.defCurrency
                oldBackupPeriod = settings.backupPeriod
                oldDefaultCurrency = settings.defCurrency

                when (settings.backupPeriod) {
                    0 -> withoutBackupRadioButton.isChecked = true
                    1 -> {
                        halfDayRadioButton.isChecked = true
                        viewModel.setIdProgram(programId)
                        viewModel.restoreBackupLiveData.observe(
                            this,
                            Observer { restoreBackup ->
                                if (restoreBackup != null) {
                                    val dateStr = DateFormat.format(
                                        "dd/MM/yyyy HH:mm",
                                        restoreBackup.backupTime
                                    ).toString()
                                    lastBackupTextView.text = "Last backup in " + dateStr
                                }
                            })
                    }
                    2 -> dayRadioButton.isChecked = true
                    3 -> weekRadioButton.isChecked = true
                }
                when (oldDefaultCurrency) {
                    // Гривна по умолчанию
                    1 -> uahRadioButton.isChecked = true
                    // Рубль по умолчению
                    2 -> rubRadioButton.isChecked = true
                    // Бел рубль по умолчению
                    3 -> bynRadioButton.isChecked = true
                    // Off
                    else -> offRadioButton.isChecked = true
                }
            }
        })

        currencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            tempDefaultCurrency = when (checkedId) {
                R.id.uahRadioButton -> 1
                R.id.rubRadioButton -> 2
                R.id.bynRadioButton -> 5
                else -> 0
            }
        }

        controlBackupRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            tempBackupPeriod = when (checkedId) {
                R.id.halfDayRadioButton -> 1
                R.id.dayRadioButton -> 2
                R.id.weekRadioButton -> 3
                else -> 0
            }
        }
        saveSettingButton.setOnClickListener {

            var isSave = false

            if (tempBackupPeriod != oldBackupPeriod) {
                when (tempBackupPeriod) {
                    1 -> viewModel.startBackup(Periodic.HalfDayBackup)
                    2 -> viewModel.startBackup(Periodic.DayBackup)
                    3 -> viewModel.startBackup(Periodic.WeekBackup)
                    else -> viewModel.stopBackup()
                }
                isSave = true
            }

            if (tempDefaultCurrency != oldDefaultCurrency) {
                viewModel.deleteRate()
                mainApplication.changeActiveCurrency(tempDefaultCurrency)
                isSave = true
            }

            if (isSave) {

                viewModel.saveSettings(
                    Settings(
                        userName = programId,
                        defCurrency = tempDefaultCurrency,
                        backupPeriod = tempBackupPeriod
                    )
                )
            }

            startActivity(Intent(this, MainActivityWithNavigation::class.java))
        }
        cancelButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}

