package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.settings_fragment.*
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_BYN
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_RUB
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_UAH
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SettingsFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controlBackupRadioGroup.visibility = View.INVISIBLE
        viewModel.savedSettings.observe(viewLifecycleOwner, { settings ->
            if (settings != null) {
                gpsSwitch.isChecked = (settings.statusGPS == STATUS_GPS_ON)

                when (settings.backupPeriod) {
                    0 -> withoutBackupRadioButton.isChecked = true
                    1 -> halfDayRadioButton.isChecked = true
                    2 -> dayRadioButton.isChecked = true
                    3 -> weekRadioButton.isChecked = true
                }
                when (settings.defCurrency) {
                    // Гривна по умолчанию
                    DEFAULT_CURRENCY_CONST_UAH -> uahRadioButton.isChecked = true
                    // Рубль по умолчению
                    DEFAULT_CURRENCY_CONST_RUB -> rubRadioButton.isChecked = true
                    // Бел рубль по умолчению
                    DEFAULT_CURRENCY_CONST_BYN -> bynRadioButton.isChecked = true
                    // Off
                    else -> offRadioButton.isChecked = true
                }
                currencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                    val defaultCurrency = when (checkedId) {
                        R.id.uahRadioButton -> DEFAULT_CURRENCY_CONST_UAH
                        R.id.rubRadioButton -> DEFAULT_CURRENCY_CONST_RUB
                        R.id.bynRadioButton -> DEFAULT_CURRENCY_CONST_BYN
                        else -> 0
                    }
                    viewModel.deleteRate()
                    viewModel.saveSettings(
                        settings.copy(defCurrency = defaultCurrency)
                    )
                }
                controlBackupRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                    val backupPeriod = when (checkedId) {
                        R.id.halfDayRadioButton -> 1
                        R.id.dayRadioButton -> 2
                        R.id.weekRadioButton -> 3
                        else -> 0
                    }
                    viewModel.saveSettings(
                        settings.copy(backupPeriod = backupPeriod)
                    )
                }
                gpsSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.saveSettings(
                        settings.copy(statusGPS = if(isChecked) STATUS_GPS_ON else STATUS_GPS_OFF)
                    )
                }

            }
        })
    }
}