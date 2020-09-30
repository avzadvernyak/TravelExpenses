package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.settings_fragment.*
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.data.Periodic
import m.kampukter.travelexpenses.data.Settings
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SettingsFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var programId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var tempDefaultCurrency = 0
        var tempBackupPeriod = 0
        var oldBackupPeriod: Int? = null
        var oldDefaultCurrency: Int? = null

        viewModel.savedSettings.observe(viewLifecycleOwner, Observer { settings ->
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
                            viewLifecycleOwner,
                            Observer { restoreBackup ->
                                if (restoreBackup != null) {
                                    val dateStr = DateFormat.format(
                                        "dd/MM/yyyy HH:mm",
                                        restoreBackup.backupTime
                                    ).toString()
                                    lastBackupTextView.text = resources.getString( R.string.last_backup, dateStr)
                                }
                            })
                    }
                    2 -> dayRadioButton.isChecked = true
                    3 -> weekRadioButton.isChecked = true
                }
                when (oldDefaultCurrency) {
                    // Гривна по умолчанию
                    DEFAULT_CURRENCY_CONST_UAH -> uahRadioButton.isChecked = true
                    // Рубль по умолчению
                    DEFAULT_CURRENCY_CONST_RUB -> rubRadioButton.isChecked = true
                    // Бел рубль по умолчению
                    DEFAULT_CURRENCY_CONST_BYN -> bynRadioButton.isChecked = true
                    // Off
                    else -> offRadioButton.isChecked = true
                }
            }
        })
        currencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            tempDefaultCurrency = when (checkedId) {
                R.id.uahRadioButton -> DEFAULT_CURRENCY_CONST_UAH
                R.id.rubRadioButton -> DEFAULT_CURRENCY_CONST_RUB
                R.id.bynRadioButton -> DEFAULT_CURRENCY_CONST_BYN
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

            findNavController().navigate(R.id.next_action)
        }
    }
}