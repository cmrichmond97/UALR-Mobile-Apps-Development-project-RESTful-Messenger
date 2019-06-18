package edu.ualr.cmrichmond.restfulmessenger

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_chat_channel.*
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception

class ChatChannelActivity : AppCompatActivity() {

    private var currentChannel :Int? = null
    private var authenticationToken :String? = null
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {//OkHttpClient is set up here
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_channel)

        client = OkHttpClient()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val sharedPreferences =//get shared preferences file
                getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

        val editor = //initialize the editor
                sharedPreferences.edit()

        editor.putString(AUTHENTICATION_KEY, savedInstanceState?.getString("authenticationKey", ""))//restore authentication key
        editor.putInt(CURRENT_CHANNEL, savedInstanceState?.getInt("currentChannel", -1)!!)//restore current channel ID
        editor.apply()
    }

    override fun onResume() {
        super.onResume()


        val sharedPreferences =//get shared preferences file
                getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)


        authenticationToken = "Token " +  sharedPreferences.getString(AUTHENTICATION_KEY, "" )//create the authentication token from sharedPrefs

        currentChannel = sharedPreferences.getInt(CURRENT_CHANNEL,-1)//get the channel ID from sharedPrefs

        displayMessages()//print the channel messages to the screen


    }

    private fun displayMessages() {


        if (currentChannel != -1) {

            val chatUrl: String = "http://messenger.mattkennett.com/api/v1/channel-messages/" + currentChannel.toString()
            val request: Request = Request.Builder()//create request for channel messages
                    .url(chatUrl)
                    .header("Authorization", authenticationToken!!)
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

//                    val messageSet: MessageSet = gson.fromJson(responseBody, MessageSet::class.java)//cast to kotlin object
                    val messageSet: Array<MessageInfo>? = gson.fromJson(responseBody, object: TypeToken<Array<MessageInfo>>() {}.type)

                    uiThread {

                        val messagesFieldLayout: LinearLayout = //grab the layout
                                findViewById(R.id.messagesFieldLayout)

                        messagesFieldLayout.removeAllViews()//clear the layout

                        if (messageSet != null) {
                            for (MessageInfo in messageSet) {//create a new TextView for each message and add them to the layout
                                var messageText = ""
                                if (MessageInfo.user != null) {

                                    messageText = MessageInfo.timestamp.toString() + " | " +
                                            MessageInfo.user.username + ":  " +
                                            MessageInfo.message

                                }

                                val newText = TextView(this@ChatChannelActivity)
                                newText.text = messageText
                                messagesFieldLayout.addView(newText)
                            }
                        }
                    }
                }
            }
        }
    }

    fun postMessage(view: View) {

        val chatUrl = "http://messenger.mattkennett.com/api/v1/messages/"

        val formBody: RequestBody
        formBody = FormBody.Builder()
                .add("channel", currentChannel.toString())
                .add("message", submitEditText.text.toString())
                .build()


        val request: Request = Request.Builder()//create request
                .url(chatUrl)
                .header("Authorization", authenticationToken!!)
                .post(formBody)
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
                val responseCode = response.code()
                var chatResponse: ChatResponse? = null

                if (responseCode == 401) {
                    val gson = Gson()
                    chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)//cast to kotlin object
                }
                uiThread {

                    val messagesFieldLayout: LinearLayout = //grab the layout
                            findViewById(R.id.messagesFieldLayout)


                    displayMessages()


                    if (responseCode == 401) {//Write errors to string
                        if (chatResponse != null) {
                            val newText = TextView(this@ChatChannelActivity)
                            newText.text = chatResponse.detail
                            messagesFieldLayout.addView(newText)
                        }

                    }
                    else {
                        val newText = TextView(this@ChatChannelActivity)
                        newText.text = getString(R.string.chat_error_message_500)
                        messagesFieldLayout.addView(newText)
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
        savedInstanceState?.putInt("currentChannel", sharedPreferences.getInt(CURRENT_CHANNEL, -1))
    }
}
