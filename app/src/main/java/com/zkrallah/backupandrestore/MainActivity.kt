package com.zkrallah.backupandrestore

import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zkrallah.backupandrestore.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: UsersDatabase
    private lateinit var list: List<User>
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = UsersDatabase.getInstance()
        val job = lifecycleScope.launch(Dispatchers.IO) {
            list = database.usersDAO().getUsers()
        }

        lifecycleScope.launch {
            job.join()
            adapter = UsersAdapter(list)
            binding.recycler.adapter = adapter
            binding.recycler.layoutManager = LinearLayoutManager(this@MainActivity)
        }

//        used to create dummy data
//        for (i in 0..10){
//            lifecycleScope.launch {
//                database.usersDAO().insert(User("User$i"))
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dbName = "users_database"
        val documentFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        when (item.itemId) {
            R.id.back_up -> {
                val db = getDatabasePath(dbName).absolutePath
                val wal = getDatabasePath("$dbName-wal").absolutePath
                val shm = getDatabasePath("$dbName-shm").absolutePath
                File(db).copyTo(File(documentFolder, dbName), true)
                File(wal).copyTo(File(documentFolder, "$dbName-wal"), true)
                File(shm).copyTo(File(documentFolder, "$dbName-shm"), true)
            }
            R.id.restore -> {
                val dbExternal = "$documentFolder/$dbName"
                val walExternal = "$documentFolder/$dbName-wal"
                val shmExternal = "$documentFolder/$dbName-shm"

                val db = getDatabasePath(dbName).absolutePath
                val wal = getDatabasePath("$dbName-wal").absolutePath
                val shm = getDatabasePath("$dbName-shm").absolutePath
                File(dbExternal).copyTo(File(db), true)
                File(walExternal).copyTo(File(wal), true)
                File(shmExternal).copyTo(File(shm), true)
            }

        }
        return super.onOptionsItemSelected(item)
    }
}