package m.kampukter.travelexpenses.ui

interface ExpensesClickEventDelegate<T> {

    fun onClick(item: T)
    fun onLongClick(item: T)
    fun onLocationClick(item: T)
    fun onPhotoClick(item: T)
}