package com.example.ticketvalidation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ticketvalidation.databinding.ActivityMainBinding
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.Validation
import com.maxkeppeler.sheets.input.type.InputEditText
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var authForm: InputSheet
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
        supportActionBar?.title = "Ticket Validator"

        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setSubtitleTextColor(Color.WHITE)
        binding.toolbar.setBackgroundColor(resources.getColor(R.color.md_theme_light_primary))
        binding.toolbar.logo = ContextCompat.getDrawable(this, R.drawable.ic_action_logo)

        val appPrefs = Utils.getAppPreferences(this)

        if (appPrefs.token == "") {
            binding.fab.visibility = View.GONE
            authForm = InputSheet().build(this) {
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
                    // TODO:  Exit App?
                }

                onPositive { result: Bundle ->

                    val u = result.getString("user")
                    val e = result.getString("email")
                    val p = result.getString("pwd")
                    val loginUrl =
                        "https://tickets.sitegist.net/api/validator/login?email=$e&password=$p"
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
                                InfoSheet().show(this@MainActivity) {
                                    style(SheetStyle.DIALOG)
                                    displayButtons(false)
                                    cancelableOutside(false)
                                    title("Authorization Success")
                                    drawable(
                                        IconicsDrawable(
                                            this@MainActivity,
                                            GoogleMaterial.Icon.gmd_check
                                        ).apply {
                                            sizeDp = 32
                                        }
                                    )
                                    drawableColor(R.color.md_theme_light_primary)
                                    content("Welcome to board, $u\n\nYour token\n\n${response.body()}")
                                    onPositive { }
                                }
                                Log.d(TAG, "Response: ${response.body()}")
                                // TODO: Parse json

                                // TODO: Store user

                                // TODO: next steps
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
                                InfoSheet().show(this@MainActivity) {
                                    style(SheetStyle.DIALOG)
                                    cancelableOutside(false)
                                    title("Authorization Error")
                                    drawable(
                                        IconicsDrawable(
                                            this@MainActivity,
                                            GoogleMaterial.Icon.gmd_error_outline
                                        ).apply {
                                            sizeDp = 32
                                        }
                                    )
                                    drawableColor(R.color.md_theme_light_error)
                                    content("Unable to authorize!\n\nСheck that the e-mail and password are correct and try again.")
                                    onPositive { authForm.show() }
                                    onNegative {
                                        activity?.finish()
                                        exitProcess(0)
                                    }

                                }
                                Log.d(TAG, "Error: ${response.code()}")
                                Log.d(TAG, "Response: ${response.body()}")
                            }
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                }
            }
            authForm.show()
        } else {
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
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_settings).icon =
            IconicsDrawable(this, GoogleMaterial.Icon.gmd_more_vert).apply {
                colorInt = Color.WHITE
                sizeDp = 16
            }
        menu.findItem(R.id.mnu1).icon =
            IconicsDrawable(this, GoogleMaterial.Icon.gmd_looks_one).apply {
                colorInt = Color.parseColor("#006B5B")
                sizeDp = 16
            }
        menu.findItem(R.id.mnu2).icon =
            IconicsDrawable(this, GoogleMaterial.Icon.gmd_looks_two).apply {
                colorInt = Color.parseColor("#006B5B")
                sizeDp = 16
            }
        menu.findItem(R.id.mnu3).icon =
            IconicsDrawable(this, GoogleMaterial.Icon.gmd_looks_3).apply {
                colorInt = Color.parseColor("#006B5B")
                sizeDp = 16
            }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}