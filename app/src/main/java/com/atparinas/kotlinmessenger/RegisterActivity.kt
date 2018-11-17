package com.atparinas.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var selectPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        registerButton.setOnClickListener {
            registerUserHandler()
        }

        loginTextView.setOnClickListener{
            Log.d("RegisterActivity", "Switching to Login Activity")

            //Create an Intent
            val intent = Intent(this, LoginActivity::class.java)

            //Start the activity
            startActivity(intent)
        }


        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectPhotoUri = data.data
            Log.d("RegisterActivity", "selectPhotoUri is $selectPhotoUri")

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPhotoUri)

            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectPhotoButton.setBackgroundDrawable(bitmapDrawable)

            selectPhotoImageView.setImageBitmap(bitmap)
            selectPhotoButton.alpha = 0f
        }
    }

    private fun registerUserHandler(){
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        Log.d("RegisterActivity", "Email is $email")
        Log.d("RegisterActivity", "password is $password")

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                //Else if Successful
                Log.d("RegisterActivity", "Successfully Created User with UID: ${it.result!!.user.uid} ")

                //Upload the photo to Firebase
                uploadImageToFirebaseStorage()

            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Error Registration ${it.message} ")
                Toast.makeText(this, "Error Registration ${it.message} ", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage(){

        if (selectPhotoUri == null){
            Log.d("RegisterActivity", "Error Registration.. PhotoURI is null ")
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        Log.d("RegisterActivity", "Uploading the image...")

        ref.putFile(selectPhotoUri!!).addOnSuccessListener {
            Log.d("RegisterActivity", "Successfully uploaded the image ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded the image $it")

                saveUserToFirebase(it.toString())
            }
        }
        .addOnFailureListener {
            Log.d("RegisterActivity", "Error uploading the image ${it.message}")
        }


    }


    private fun saveUserToFirebase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, usernameEditText.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            Log.d("RegisterActivity", "Finally! User save to Firebase")
        }

    }
}

class User(val uid: String, val username: String, val profileImage: String)