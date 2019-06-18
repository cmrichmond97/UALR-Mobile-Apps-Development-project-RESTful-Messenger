package edu.ualr.cmrichmond.restfulmessenger

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception

class LoggedInActivity : AppCompatActivity() {

    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {//OkHttpClient is set up here
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_in)

        client = OkHttpClient()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val sharedPreferences =//get shared preferences file
            getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

        val editor = //initialize the editor
            sharedPreferences.edit()

        editor.putString(AUTHENTICATION_KEY, savedInstanceState?.getString("authenticationKey", ""))
        editor.apply()//put the authentication key back into sharedprefs
    }

    override fun onResume() {
        super.onResume()


        val sharedPreferences =//get shared preferences file
                getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

        val editor = //initialize the editor
                sharedPreferences.edit()

        val authenticationToken: String = "Token " + sharedPreferences.getString(AUTHENTICATION_KEY, "")//create the authentication token

        val channelFieldLayout: LinearLayout = //grab the layout
                findViewById(R.id.channelFieldLayout)

        channelFieldLayout.removeAllViews()//clear the layout


        val request: Request = Request.Builder()//create request for channel list
                .url("http://messenger.mattkennett.com/api/v1/channels/")
                .header("Authorization", authenticationToken)
                .get()
                .build()
        doAsync {
            //get response
            var response: Response? = null

            try {
                response = client.newCall(request).execute()
            } catch (e: Exception) {
                Log.d("MPK_UTILITY", e.toString())
            }

            if (response != null) {

                val responseBody: String = response.body()!!.string()//grab contents of response

                val gson = Gson()

                val channelSet: Array<ChannelInfo>? = gson.fromJson(responseBody, object : TypeToken<Array<ChannelInfo>>() {}.type)
                uiThread {
                    if (channelSet != null) {


                        for (ChannelInfo in channelSet) {//create a new button for each channel and add it to the layout
                            val newButton = Button(this@LoggedInActivity)
                            newButton.text = ChannelInfo.name

                            newButton.setOnClickListener {
                                editor.putInt(CURRENT_CHANNEL, ChannelInfo.pk!!)
                                editor.apply()
                                val intent = Intent(this@LoggedInActivity, ChatChannelActivity::class.java).apply {}
                                startActivity(intent)
                            }
                            channelFieldLayout.addView(newButton)
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)

        val sharedPreferences =//get shared preferences file
            getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

        savedInstanceState?.putString("authenticationKey", sharedPreferences.getString(AUTHENTICATION_KEY, "" ))
    }
}
