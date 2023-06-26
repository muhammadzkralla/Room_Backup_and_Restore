package com.zkrallah.backupandrestore

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        // First we have to ask the user for the read and write permissions.
        grantPermissions()

//        Used to create dummy data, uncomment in the FIRST RUN ONLY, comment it again after.
//
//        for (i in 0..10){
//            lifecycleScope.launch {
//                database.usersDAO().insert(User("User$i"))
//            }
//        }

        // Create the Database instance and load the dummy data in the database.
        database = UsersDatabase.getInstance()
        val job = lifecycleScope.launch(Dispatchers.IO) {
            list = database.usersDAO().getUsers()
        }

        // Show the dummy data in the UI.
        lifecycleScope.launch {
            job.join()
            adapter = UsersAdapter(list)
            binding.recycler.adapter = adapter
            binding.recycler.layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun grantPermissions() {
        // For Android versions below 11, ask for read and write external storage.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    100
                )
            }
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    102
                )
            }
        }

        // For Android versions higher than 11, ask for managing the external storage.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data =
                        Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityIfNeeded(intent, 101)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    startActivityIfNeeded(intent, 101)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // We will need this data in the process, the Database name and
        // the directory where we will store the Database files which is here
        // the "DCIM" directory in the phone external storage.
        val dbName = "users_database"
        val documentFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

        when (item.itemId) {
            R.id.back_up -> {
                // Get the paths to the original Database files.
                val db = getDatabasePath(dbName).absolutePath
                val wal = getDatabasePath("$dbName-wal").absolutePath
                val shm = getDatabasePath("$dbName-shm").absolutePath

                try {
                    // Copying these files to the DCIM directory so that they can
                    // be accessed by us later, as we can't access the original Database
                    // files we fetched in lines (108, 109, 110) and like that
                    // we can transfer these files to another device, restore them later
                    // or upload the entire database files to a cloud service.
                    File(db).copyTo(File(documentFolder, dbName), true)
                    File(wal).copyTo(File(documentFolder, "$dbName-wal"), true)
                    File(shm).copyTo(File(documentFolder, "$dbName-shm"), true)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.restore -> {
                // To restore the Database we need to copy the backup files
                // from the DCIM to the original Database files locations,
                // so we get the paths to the files in the DCIM directory :
                val dbExternal = "$documentFolder/$dbName"
                val walExternal = "$documentFolder/$dbName-wal"
                val shmExternal = "$documentFolder/$dbName-shm"

                val db = getDatabasePath(dbName).absolutePath
                val wal = getDatabasePath("$dbName-wal").absolutePath
                val shm = getDatabasePath("$dbName-shm").absolutePath

                try {
                    // Copying these file from the DCIM directory to the original
                    // Database files, so they are now replaced by these files
                    File(dbExternal).copyTo(File(db), true)
                    File(walExternal).copyTo(File(wal), true)
                    File(shmExternal).copyTo(File(shm), true)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }
}