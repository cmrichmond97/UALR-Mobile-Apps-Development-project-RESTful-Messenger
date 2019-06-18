package edu.ualr.cmrichmond.restfulmessenger

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {//OkHttpClient is set up here
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        client = OkHttpClient()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val sharedPreferences =//get shared preferences file
            getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

        val editor = //initialize the editor
            sharedPreferences.edit()

        editor.putString(AUTHENTICATION_KEY, savedInstanceState?.getString("authenticationKey", ""))

        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences =//get shared preferences file
            getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

        val editor = //initialize the editor
            sharedPreferences.edit()


        val loginSwitch = //pull the login switch value from shared prefs
            sharedPreferences.getInt(LOGIN_SWITCH, 0)


        val submissionFieldLayout: LinearLayout = //grab the layout
            findViewById(R.id.submissionFieldLayout)

        submissionFieldLayout.removeAllViews()//clear the layout

//------------------------------------------------------add views to the layout
        val newUsernameEditText = EditText(this)
        submissionFieldLayout.addView(newUsernameEditText)//username input view
        newUsernameEditText.hint = getString(R.string.user_field_hint)


        val newPasswordEditText = EditText(this)
        submissionFieldLayout.addView(newPasswordEditText)//password input view
        newPasswordEditText.hint = getString(R.string.pass_field_hint)
        newPasswordEditText.inputType = 81




        val newConfirmPassEditText = EditText(this)
        newConfirmPassEditText.hint = getString(R.string.confirm_pass_field_hint)
        newConfirmPassEditText.inputType = 81

        if (loginSwitch == 1) {//only add if user is trying to register
            submissionFieldLayout.addView(newConfirmPassEditText)//confirm password input view
        }


        val newFeedbackTextView = TextView(this)
        newFeedbackTextView.text = ""
        submissionFieldLayout.addView(newFeedbackTextView)//feedback text view

        val newSubmitButton = Button(this)
        submissionFieldLayout.addView(newSubmitButton)//submit button
        newSubmitButton.text = getString(R.string.submit_button)

//------------------------------------------------------finish add views to the layout

        newSubmitButton.setOnClickListener{//define the functionality of the submit button based on the loginSwitch
            val formBody: RequestBody
            if(loginSwitch ==0) {//for logging in
                formBody = FormBody.Builder()
                    .add("username", newUsernameEditText.text.toString())
                    .add("password", newPasswordEditText.text.toString())
                    .build()
            }
            else{//for registering
                formBody = FormBody.Builder()
                    .add("username", newUsernameEditText.text.toString())
                    .add("password1", newPasswordEditText.text.toString())
                    .add("password2", newConfirmPassEditText.text.toString())
                    .build()

            }
            var requestURL: String

            if (loginSwitch == 0){
                requestURL = "http://messenger.mattkennett.com/api-auth/v1/login/"
            }
            else{
                requestURL = "http://messenger.mattkennett.com/api-auth/v1/registration/"
            }

            val request: Request = Request.Builder()//create request
                .url(requestURL)
                .post(formBody)
                .build()

            doAsync {//get response
                var response: Response? = null

                try {
                    response = client.newCall(request).execute()
                }catch (e: Exception) {
                    Log.d("MPK_UTILITY", e.toString())
                }

                if (response != null) {

                    val responseBody: String = response.body()!!.string()//grab contents of response


                    val gson = Gson()

                    val myUser: UserResponse = gson.fromJson(responseBody, UserResponse::class.java)//cast to kotlin object
                    uiThread {
                        if(myUser.key != null){//if credentials are valid

                            editor.putString(AUTHENTICATION_KEY, myUser.key)//sve user's authentication key
                            editor.apply()
                            val intent = Intent(this@LoginActivity, LoggedInActivity::class.java).apply{}
                            //start LoggedInActivity
                            startActivity(intent)
                        }
                        else{//if credentials not valid
                            // write errors to screen----------------------------------------------------------
                            if (myUser.username != null){
                                newFeedbackTextView.text = myUser.username[0]
                            }

                            if (myUser.password != null){
                                newFeedbackTextView.text = myUser.password[0]
                            }

                            if (myUser.password1 != null){
                                newFeedbackTextView.text = myUser.password1[0]
                            }

                            if (myUser.non_field_errors != null){
                                newFeedbackTextView.text = myUser.non_field_errors[0]

                            }
                        }//finish write errors to screen------------------------------------------------------
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
