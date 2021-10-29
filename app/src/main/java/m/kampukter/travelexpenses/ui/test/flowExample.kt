package m.kampukter.travelexpenses.ui.test
/*

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*


@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String
)

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String
)

data class SettingsProjection(
    val id: String,

    @ColumnInfo(name = "settings_title")
    val settingsTitle: String,

    @ColumnInfo(name = "folder_title")
    val folderTitle: String
)

@Dao
interface SomeDao {

    @Query("""
        select 
            *
        from folders
    """)
    fun getAllFoldersFlow(): Flow<List<Folder>>

    @Query("""
        select
            *
        from folders
        where id = :id
    """)
    fun getFolderByIdFlow(id: String?): Flow<Folder?>

    @Query("""
        select
            *
        from settings
        where id = :id
    """)
    fun getSettingsByIdFlow(id: String?): Flow<Settings?>

    @Query("""
        update settings
        set folder_id = :folderId
        where id = :settingsId
    """)
    fun updateSettingsFolder(settingsId: String, folderId: String)

    @Query("""
        select
            settings.id as id,
            settings.title as settings_title,
            folders.title as folder_title
        from settings
        join folders on settings.folder_id = folders.id
    """)
    fun getSettingsProjectionsFlow(): Flow<List<SettingsProjection>>

}

@ExperimentalCoroutinesApi
class Repo(private val someDao: SomeDao) {

    private val currentSettingsIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)

    val currentSettingsFlow: Flow<Settings?> = currentSettingsIdFlow.flatMapLatest { settingsId ->
        someDao.getSettingsByIdFlow(settingsId)
    }

    val allFoldersFlow = someDao.getAllFoldersFlow()

    val settingsProjectionsFlow = someDao.getSettingsProjectionsFlow()

    val currentFolderFlow: Flow<Folder?> = currentSettingsFlow.flatMapLatest { currentSettings ->
        someDao.getFolderByIdFlow(currentSettings?.folderId)
    }

    suspend fun setCurrentSettings(settingsId: String) {
        currentSettingsIdFlow.emit(settingsId)
    }

    fun updateSettingsFolder(settingsId: String, folderId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            someDao.updateSettingsFolder(settingsId, folderId)
        }
    }

}

class SomeViewModel(private val repo: Repo) : ViewModel() {

    val settingsLiveData: LiveData<Pair<Settings?, List<SettingsProjection>>>
            = MediatorLiveData<Pair<Settings?, List<SettingsProjection>>>().apply {

        var previousCurrentSettings: Settings? = null
        var previousAllSettings: List<SettingsProjection>? = null

        fun update() {
            val allSettings = previousAllSettings ?: return
            postValue(Pair(previousCurrentSettings, allSettings))
        }

        addSource(repo.currentSettingsFlow.asLiveData()) { currentSettings ->
            previousCurrentSettings = currentSettings
            update()
        }

        addSource(repo.settingsProjectionsFlow.asLiveData()) { settings ->
            previousAllSettings = settings
            update()
        }

    }

    val foldersLiveData: LiveData<Triple<Folder?, Settings?, List<Folder>>>
            = MediatorLiveData<Triple<Folder?, Settings?, List<Folder>>>().apply {

        var previousSettings: Settings? = null
        var previousFolder: Folder? = null
        var previousFolders: List<Folder>? = null

        fun update() {
            val folders = previousFolders ?: return
            postValue(Triple(previousFolder, previousSettings, folders))
        }

        addSource(repo.currentSettingsFlow.asLiveData()) { settings ->
            previousSettings = settings
            update()
        }

        addSource(repo.currentFolderFlow.asLiveData()) { folder ->
            previousFolder = folder
            update()
        }

        addSource(repo.allFoldersFlow.asLiveData()) { folders ->
            previousFolders = folders
            update()
        }

    }

    fun setCurrentSettings(settingsId: String) {
        viewModelScope.launch {
            repo.setCurrentSettings(settingsId)
        }
    }

    fun updateSettingsFolder(settingsId: String, folderId: String) {
        repo.updateSettingsFolder(settingsId, folderId)
    }

}

data class SettingsItem(
    val settingsId: String,
    val settingsTitle: String,
    val folderTitle: String,
    val isSelected: Boolean
)

class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: SettingsItem, onClickListener: ((String) -> Unit)?) {
        with(itemView) {
            settingsTitleView.text = item.settingsTitle
            folderTitleView.text = "Selected folder: ${item.folderTitle}"
            isSelectedView.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            setOnClickListener {
                onClickListener?.invoke(item.settingsId)
            }
        }
    }

}

class SettingsAdapter : RecyclerView.Adapter<SettingsViewHolder>() {

    private var items = emptyList<SettingsItem>()
    var onClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder =
        SettingsViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_item, parent, false))

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(items[position], onClickListener)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<SettingsItem>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                    = items[oldItemPosition].settingsId == newItems[newItemPosition].settingsId

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                    = items[oldItemPosition] == newItems[newItemPosition]

        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

}

data class FolderItem(
    val folderId: String,
    val title: String,
    val isSelected: Boolean,
    val settingsId: String?
)

class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: FolderItem, onClickListener: ((String, String) -> Unit)?) {
        with(itemView) {
            titleView.text = item.title
            isFolderSelectedView.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            setOnClickListener {
                item.settingsId?.let { settingsId ->
                    onClickListener?.invoke(settingsId, item.folderId)
                }
            }
        }
    }

}

class FoldersAdapter : RecyclerView.Adapter<FolderViewHolder>() {

    private var items = emptyList<FolderItem>()
    var onClickListener: ((String, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder
            = FolderViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.folder_item, parent, false))

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(items[position], onClickListener)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<FolderItem>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                    = items[oldItemPosition].folderId == newItems[newItemPosition].folderId

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                    = items[oldItemPosition] == newItems[newItemPosition]
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

}

class SomeActivity : AppCompatActivity() {

    private val someViewModel: SomeViewModel by inject()

    private val settingsAdapter = SettingsAdapter()
    private val foldersAdapter = FoldersAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.some_activity)

        settingsView.apply {
            layoutManager = LinearLayoutManager(this@SomeActivity)
            adapter = settingsAdapter.also {
                it.onClickListener = { id ->
                    someViewModel.setCurrentSettings(id)
                }
            }
        }

        foldersView.apply {
            layoutManager = LinearLayoutManager(this@SomeActivity)
            adapter = foldersAdapter.also {
                it.onClickListener = { settingsId, folderId ->
                    someViewModel.updateSettingsFolder(settingsId, folderId)
                }
            }
        }

        someViewModel.settingsLiveData.observe(this) { (current, projections) ->
            settingsAdapter.updateItems(projections.map {
                SettingsItem(
                    it.id,
                    it.settingsTitle,
                    it.folderTitle,
                    it.id == current?.id
                )
            })
        }

        someViewModel.foldersLiveData.observe(this) { (currentFolder, currentSettings, folders) ->
            foldersAdapter.updateItems(folders.map { FolderItem(it.id, it.title, it.id == currentFolder?.id, currentSettings?.id) })
        }

    }

}

class SomeApplication : Application() {

    private val appModule = module {
        single { Room.inMemoryDatabaseBuilder(applicationContext, SomeDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val folders = listOf(
                        Folder(title = "First folder"),
                        Folder(title = "Second folder"),
                        Folder(title = "Third folder"),
                        Folder(title = "Fourth folder"),
                        Folder(title = "Fifth folder")
                    )
                    for (folder in folders) {
                        db.execSQL("insert into folders (id, title) values ('${folder.id}', '${folder.title}')")
                    }
                    val settings = listOf(
                        Settings(title = "Settings one", folderId = folders.random().id),
                        Settings(title = "Settings two", folderId = folders.random().id),
                        Settings(title = "Settings three", folderId = folders.random().id)
                    )
                    for (setting in settings) {
                        db.execSQL("insert into settings (id, title, folder_id) values ('${setting.id}', '${setting.title}', '${setting.folderId}')")
                    }
                }
            }).build()
        }
        single { get<SomeDatabase>().getSomeDao() }
        single { Repo(get()) }
        viewModel { SomeViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SomeApplication)
            modules(appModule)
        }
    }

}

@Database(
    entities = [
        Folder::class,
        Settings::class
    ],
    version = 1
)
abstract class SomeDatabase : RoomDatabase() {

    abstract fun getSomeDao(): SomeDao

}*/
