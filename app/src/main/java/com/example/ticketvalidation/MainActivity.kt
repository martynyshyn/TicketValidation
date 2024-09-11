package com.example.ticketvalidation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ticketvalidation.databinding.ActivityMainBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.Validation
import com.maxkeppeler.sheets.input.type.InputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.toolbar.title = "Ticket Validator"
        binding.toolbar.subtitle = ""
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setSubtitleTextColor(Color.WHITE)
        binding.toolbar.setBackgroundColor(resources.getColor(R.color.md_theme_light_primary))
//        binding.toolbar.setLogoScaleType(ImageView.ScaleType.FIT_START)
//        binding.toolbar.logo = ContextCompat.getDrawable(this, R.drawable.ic_action_name)

        val appPrefs = Utils.getAppPreferences(this)


        if (appPrefs.token == "") {
            binding.fab.visibility = View.GONE
            InputSheet().show(this) {
                title("Please, authorize")
                with(InputEditText("user") {
                    required()
                    hint("Username")
                })
                with(InputEditText("email") {
                    style(SheetStyle.DIALOG)
                    required()
                    hint("E-mail")
                    validationListener { value ->
                        if (Utils.isValidEmail(value.toString())) Validation.success()
                        else Validation.failed("Enter valid e-mail")
                    }
                })
                with(InputEditText("pwd") {
                    required()
                    hint("Password")
                })
                onNegative {
//                    activity?.finish()
//                    exitProcess(0)
                    // Продовжуємо роботу
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MainFragment())
                        .addToBackStack(null)
                        .commit()
                    binding.fab.setImageResource(android.R.drawable.ic_menu_camera)
                    binding.fab.setOnClickListener {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ScannerFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }
                onPositive { result: Bundle ->

                    val u = result.getString("email")
                    val p = result.getString("pwd")
                    val loginUrl =
                        "https://tickets.sitegist.net/api/validator/login?email=$u&password=$p"
                    Log.d(TAG, "onCreate: $loginUrl")

                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://tickets.sitegist.net")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val apiService = retrofit.create(ApiService::class.java)
                    val call = apiService.fetchData(loginUrl)
                    call.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.isSuccessful) {
                                Log.d(TAG, "Response: ${response.body()}")
                                // TODO: Обробка json

                                // TODO: Запам'ятати користувача

                                // Продовжуємо роботу
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, MainFragment())
                                    .addToBackStack(null)
                                    .commit()
                                binding.fab.setImageResource(android.R.drawable.ic_menu_camera)
                                binding.fab.setOnClickListener {
                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, ScannerFragment())
                                        .addToBackStack(null)
                                        .commit()
                                }
                            } else {
                                Log.d(TAG, "Error: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                }
            }
        } else {
            binding.toolbar.subtitle = "User: ${appPrefs.user}"
            binding.fab.apply {
                setImageResource(android.R.drawable.ic_menu_camera)
                visibility = View.VISIBLE
                setOnClickListener {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ScannerFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .addToBackStack(null)
                .commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}