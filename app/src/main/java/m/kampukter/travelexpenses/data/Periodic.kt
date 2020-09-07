package m.kampukter.travelexpenses.data

import java.util.concurrent.TimeUnit

sealed class Periodic( val timeUnit: TimeUnit , val value: Long  ) {
    object HalfDayBackup: Periodic( TimeUnit.HOURS, 12L )
    object DayBackup: Periodic( TimeUnit.DAYS, 1L )
    object WeekBackup: Periodic( TimeUnit.DAYS, 7L)
}