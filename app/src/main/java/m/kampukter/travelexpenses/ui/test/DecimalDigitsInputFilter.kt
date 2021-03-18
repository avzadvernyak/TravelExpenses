package m.kampukter.travelexpenses.ui.test


class DecimalDigitsInputFilter{}/*(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
    private val mPattern: Pattern =
        Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val matcher: Matcher = mPattern.matcher(dest)
        return if (!matcher.matches()) "" else null
    }

}*/