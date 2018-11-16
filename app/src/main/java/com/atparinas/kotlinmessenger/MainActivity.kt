package com.atparinas.kotlinmessenger

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        registerButton.setOnClickListener {
            registerUserHandler()
        }

        loginTextView.setOnClickListener{
            Log.d("MainActivity", "Switching to Login Activity")

            //Create an Intent
            val intent = Intent(this, LoginActivity::class.java)

            //Start the activity
            startActivity(intent)
        }


    }

    private fun registerUserHandler(){
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        Log.d("MainActivity", "Email is $email")
        Log.d("MainActivity", "password is $password")

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                //Else if Successful
                Log.d("MainActivity-Register", "Successfully Created User with UID: ${it.result!!.user.uid} ")
            }
            .addOnFailureListener {
                Log.d("MainActivity-Error", "Error Registration ${it.message} ")
                Toast.makeText(this, "Error Registration ${it.message} ", Toast.LENGTH_SHORT).show()
            }
    }
}
