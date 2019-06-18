package edu.ualr.cmrichmond.restfulmessenger

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

const val SHARED_PREFS_FILE = "edu.ualr.cmrichmond.restfulmessenger.SHARED_PREFS"//shared preferences file
const val AUTHENTICATION_KEY = "edu.ualr.cmrichmond.restfulmessenger.AUTHENTICATION_KEY"
const val LOGIN_SWITCH = "edu.ualr.cmrichmond.restfulmessenger.LOGIN_SWITCH"//Tracks whether the user is logging in or registering
const val CURRENT_CHANNEL = "-1"

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toLogin(view: View){//Sets the LoginSwitch to 0 and creates an intent to the LoginActivity
        val sharedPreferences =//get the shared preferences file
            getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()//initialize the editor

        editor.putInt(LOGIN_SWITCH, 0)
        editor.apply()//Sets the LoginSwitch to 0

        val intent = Intent(this,LoginActivity::class.java).apply{}

        //start LoginActivity
        startActivity(intent)
    }

    fun toRegister(view: View){//Sets the LoginSwitch to 1 and creates an intent to the LoginActivity
        val sharedPreferences =//get the shared preferences file
            getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()//initialize the editor

        editor.putInt(LOGIN_SWITCH, 1)
        editor.apply()//Sets the LoginSwitch to 1

        val intent = Intent(this,LoginActivity::class.java).apply{}

        //start LoginActivity
        startActivity(intent)
    }
}
