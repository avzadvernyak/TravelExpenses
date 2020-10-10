package m.kampukter.travelexpenses.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.settings_fragment.*
import m.kampukter.travelexpenses.*
import m.kampukter.travelexpenses.data.Periodic
import m.kampukter.travelexpenses.data.Settings
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SettingsFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var programId: String

    private val permissionsForLocation = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                Log.d("blabla", "${it.key} = ${it.value}")
            }
        }

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
        var oldGpsStatus: Int? = null

        viewModel.savedSettings.observe(viewLifecycleOwner, Observer { settings ->
            if (settings != null) {
                programId = settings.userName
                tempBackupPeriod = settings.backupPeriod
                tempDefaultCurrency = settings.defCurrency
                oldBackupPeriod = settings.backupPeriod
                oldDefaultCurrency = settings.defCurrency
                oldGpsStatus = settings.statusGPS

                gpsSwitch.isChecked = (settings.statusGPS == 1)

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
                                    lastBackupTextView.text =
                                        resources.getString(R.string.last_backup, dateStr)
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
        gpsSwitch.setOnClickListener {
            /*
            Проверяем на необходимость получения разрешений для локации
            */
            if (gpsSwitch.isChecked && (oldGpsStatus == 0)) {
                gpsPermissionRequest(view)
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


            var tempGpsStatus = if (gpsSwitch.isChecked) 1 else 0
            if (tempGpsStatus != oldGpsStatus) {
                isSave = true

                if ((tempGpsStatus == 1) && !hasPermissions(view.context, permissionsForLocation)) {
                    Snackbar.make(view, "Включить локацию не возможно", Snackbar.LENGTH_SHORT)
                        .show()
                    tempGpsStatus = 0
                }
            }

            if (isSave) {
                viewModel.saveSettings(
                    Settings(
                        userName = programId,
                        defCurrency = tempDefaultCurrency,
                        backupPeriod = tempBackupPeriod,
                        statusGPS = tempGpsStatus
                    )
                )
            }

            //findNavController().navigate(R.id.next_action)
        }


    }

    /*
   Проверяем на необходимость получения разрешений для локации
   Manifest.permission.ACCESS_FINE_LOCATION
   Manifest.permission.ACCESS_COARSE_LOCATION
   Manifest.permission.WRITE_EXTERNAL_STORAGE
   */
    private fun gpsPermissionRequest(view: View) {
        if (!hasPermissions(view.context, permissionsForLocation)) {
            // Пообщатся на вопрос получения разрешений
            Snackbar.make(
                view,
                "Получить разрешения для доступа к геолокации?",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Да") { requestPermissionLauncher.launch(permissionsForLocation) }
                .show()


        } else Snackbar.make(
            view,
            "Разрешения для работы с локацией предоставлены. Опция доступна.",
            Snackbar.LENGTH_SHORT
        )
            .show()

    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
}