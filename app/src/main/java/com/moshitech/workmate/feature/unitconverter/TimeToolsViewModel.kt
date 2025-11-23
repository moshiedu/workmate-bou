package com.moshitech.workmate.feature.unitconverter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TimeToolsViewModel : ViewModel() {

    // --- Date Calculator State ---
    private val _calcStartDate = MutableStateFlow(LocalDate.now())
    val calcStartDate: StateFlow<LocalDate> = _calcStartDate.asStateFlow()

    private val _calcYears = MutableStateFlow("0")
    val calcYears: StateFlow<String> = _calcYears.asStateFlow()

    private val _calcMonths = MutableStateFlow("0")
    val calcMonths: StateFlow<String> = _calcMonths.asStateFlow()

    private val _calcDays = MutableStateFlow("0")
    val calcDays: StateFlow<String> = _calcDays.asStateFlow()

    private val _calcOperation = MutableStateFlow(DateOperation.ADD)
    val calcOperation: StateFlow<DateOperation> = _calcOperation.asStateFlow()

    private val _calcResultDate = MutableStateFlow(LocalDate.now())
    val calcResultDate: StateFlow<LocalDate> = _calcResultDate.asStateFlow()

    // --- Time Difference State ---
    private val _diffStartDate = MutableStateFlow(LocalDate.now())
    val diffStartDate: StateFlow<LocalDate> = _diffStartDate.asStateFlow()

    private val _diffEndDate = MutableStateFlow(LocalDate.now())
    val diffEndDate: StateFlow<LocalDate> = _diffEndDate.asStateFlow()

    private val _diffResult = MutableStateFlow("")
    val diffResult: StateFlow<String> = _diffResult.asStateFlow()

    // --- Timestamp Converter State ---
    private val _timestampInput = MutableStateFlow(System.currentTimeMillis().toString())
    val timestampInput: StateFlow<String> = _timestampInput.asStateFlow()

    private val _timestampResult = MutableStateFlow("")
    val timestampResult: StateFlow<String> = _timestampResult.asStateFlow()

    // --- Date Calculator Logic ---
    fun onCalcStartDateChanged(date: LocalDate) {
        _calcStartDate.value = date
        calculateDate()
    }

    fun onCalcDurationChanged(years: String, months: String, days: String) {
        _calcYears.value = years
        _calcMonths.value = months
        _calcDays.value = days
        calculateDate()
    }

    fun onCalcOperationChanged(operation: DateOperation) {
        _calcOperation.value = operation
        calculateDate()
    }

    private fun calculateDate() {
        val years = _calcYears.value.toLongOrNull() ?: 0
        val months = _calcMonths.value.toLongOrNull() ?: 0
        val days = _calcDays.value.toLongOrNull() ?: 0

        val date = _calcStartDate.value
        _calcResultDate.value = if (_calcOperation.value == DateOperation.ADD) {
            date.plusYears(years).plusMonths(months).plusDays(days)
        } else {
            date.minusYears(years).minusMonths(months).minusDays(days)
        }
    }

    // --- Time Difference Logic ---
    fun onDiffStartDateChanged(date: LocalDate) {
        _diffStartDate.value = date
        calculateDifference()
    }

    fun onDiffEndDateChanged(date: LocalDate) {
        _diffEndDate.value = date
        calculateDifference()
    }

    private fun calculateDifference() {
        val start = _diffStartDate.value
        val end = _diffEndDate.value
        
        val p = java.time.Period.between(start, end)
        val daysBetween = ChronoUnit.DAYS.between(start, end)
        
        _diffResult.value = "${p.years} Years, ${p.months} Months, ${p.days} Days\n(Total: $daysBetween Days)"
    }

    // --- Timestamp Logic ---
    fun onTimestampInputChanged(input: String) {
        _timestampInput.value = input
        convertTimestamp()
    }

    private fun convertTimestamp() {
        val input = _timestampInput.value.toLongOrNull() ?: return
        try {
            // Heuristic for Timestamp Precision:
            // < 11 digits: Seconds (up to year 2286)
            // 13 digits: Milliseconds
            // 16 digits: Microseconds
            // 19 digits: Nanoseconds
            
            val instant = when (input.toString().length) {
                in 0..11 -> Instant.ofEpochSecond(input)
                in 12..14 -> Instant.ofEpochMilli(input)
                in 15..17 -> Instant.ofEpochSecond(input / 1_000_000, (input % 1_000_000) * 1000) // Micros
                else -> Instant.ofEpochSecond(input / 1_000_000_000, input % 1_000_000_000) // Nanos
            }
            
            val zoneId = ZoneId.systemDefault()
            val dateTime = LocalDateTime.ofInstant(instant, zoneId)
            // Show fractional seconds if available
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
            _timestampResult.value = dateTime.format(formatter)
        } catch (e: Exception) {
            _timestampResult.value = "Invalid Timestamp"
        }
    }
    
    // --- Time Zone State ---
    private val _timeZoneSourceTime = MutableStateFlow(LocalDateTime.now())
    val timeZoneSourceTime: StateFlow<LocalDateTime> = _timeZoneSourceTime.asStateFlow()

    private val _timeZoneSourceId = MutableStateFlow(ZoneId.systemDefault().id)
    val timeZoneSourceId: StateFlow<String> = _timeZoneSourceId.asStateFlow()

    private val _timeZoneTargetId = MutableStateFlow("UTC")
    val timeZoneTargetId: StateFlow<String> = _timeZoneTargetId.asStateFlow()

    private val _timeZoneResult = MutableStateFlow("")
    val timeZoneResult: StateFlow<String> = _timeZoneResult.asStateFlow()

    // --- Business Day State ---
    private val _bizStartDate = MutableStateFlow(LocalDate.now())
    val bizStartDate: StateFlow<LocalDate> = _bizStartDate.asStateFlow()

    private val _bizDays = MutableStateFlow("0")
    val bizDays: StateFlow<String> = _bizDays.asStateFlow()

    private val _bizOperation = MutableStateFlow(DateOperation.ADD)
    val bizOperation: StateFlow<DateOperation> = _bizOperation.asStateFlow()

    private val _bizResultDate = MutableStateFlow(LocalDate.now())
    val bizResultDate: StateFlow<LocalDate> = _bizResultDate.asStateFlow()

    // --- Age Calculator State ---
    private val _ageBirthDate = MutableStateFlow(LocalDate.now().minusYears(20))
    val ageBirthDate: StateFlow<LocalDate> = _ageBirthDate.asStateFlow()

    private val _ageResult = MutableStateFlow("")
    val ageResult: StateFlow<String> = _ageResult.asStateFlow()

    // --- Time Zone Logic ---
    fun onTimeZoneSourceTimeChanged(time: LocalDateTime) {
        _timeZoneSourceTime.value = time
        calculateTimeZone()
    }

    fun onTimeZoneSourceIdChanged(id: String) {
        _timeZoneSourceId.value = id
        calculateTimeZone()
    }

    fun onTimeZoneTargetIdChanged(id: String) {
        _timeZoneTargetId.value = id
        calculateTimeZone()
    }

    private fun calculateTimeZone() {
        try {
            val sourceZone = ZoneId.of(_timeZoneSourceId.value)
            val targetZone = ZoneId.of(_timeZoneTargetId.value)
            val sourceZoned = _timeZoneSourceTime.value.atZone(sourceZone)
            val targetZoned = sourceZoned.withZoneSameInstant(targetZone)
            
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            _timeZoneResult.value = targetZoned.format(formatter)
        } catch (e: Exception) {
            _timeZoneResult.value = "Invalid Zone"
        }
    }

    // --- Business Day Logic ---
    fun onBizStartDateChanged(date: LocalDate) {
        _bizStartDate.value = date
        calculateBizDate()
    }

    fun onBizDaysChanged(days: String) {
        _bizDays.value = days
        calculateBizDate()
    }

    fun onBizOperationChanged(op: DateOperation) {
        _bizOperation.value = op
        calculateBizDate()
    }

    private fun calculateBizDate() {
        val daysToAdd = _bizDays.value.toLongOrNull() ?: 0L
        var date = _bizStartDate.value
        var added = 0L
        val direction = if (_bizOperation.value == DateOperation.ADD) 1 else -1
        
        while (added < daysToAdd) {
            date = date.plusDays(direction.toLong())
            val dayOfWeek = date.dayOfWeek
            if (dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                added++
            }
        }
        _bizResultDate.value = date
    }

    // --- Age Calculator Logic ---
    fun onAgeBirthDateChanged(date: LocalDate) {
        _ageBirthDate.value = date
        calculateAge()
    }

    private fun calculateAge() {
        val birth = _ageBirthDate.value
        val now = LocalDate.now()
        
        if (birth.isAfter(now)) {
            _ageResult.value = "Not born yet!"
            return
        }

        val p = java.time.Period.between(birth, now)
        val totalDays = ChronoUnit.DAYS.between(birth, now)
        val nextBirthday = birth.plusYears((p.years + 1).toLong())
        val daysToBirthday = ChronoUnit.DAYS.between(now, nextBirthday)

        _ageResult.value = """
            ${p.years} Years, ${p.months} Months, ${p.days} Days
            
            Total Days: $totalDays
            Next Birthday in: $daysToBirthday Days
        """.trimIndent()
    }

    init {
        calculateDate()
        calculateDifference()
        convertTimestamp()
        calculateTimeZone()
        calculateBizDate()
        calculateAge()
    }
}

enum class DateOperation {
    ADD, SUBTRACT
}
