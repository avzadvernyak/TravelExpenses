package m.kampukter.travelexpenses.data

sealed class FolderDeletionResult{
    object Success : FolderDeletionResult()
    data class Warning (val folderName: String, val countRecords: Long): FolderDeletionResult()
}
