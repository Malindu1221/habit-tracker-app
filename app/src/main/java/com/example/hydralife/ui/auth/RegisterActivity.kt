package com.example.hydralife.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hydralife.MainActivity
import com.example.hydralife.R
import com.example.hydralife.data.repository.SharedPreferencesManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var prefsManager: SharedPreferencesManager
    
    // Store registered users (in SharedPreferences for this demo)
    private val KEY_REGISTERED_USERS = "registered_users"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // Initialize prefsManager
        prefsManager = SharedPreferencesManager.getInstance(this)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        tilName = findViewById(R.id.til_name)
        etName = findViewById(R.id.et_name)
        tilEmail = findViewById(R.id.til_email)
        etEmail = findViewById(R.id.et_email)
        tilPassword = findViewById(R.id.til_password)
        etPassword = findViewById(R.id.et_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        tvLogin = findViewById(R.id.tv_login)
    }
    
    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            register()
        }
        
        tvLogin.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun register() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        
        // Validate inputs
        if (!validateInputs(name, email, password, confirmPassword)) {
            return
        }
        
        // Check if user already exists
        if (isUserRegistered(email)) {
            Toast.makeText(this, "User with this email already exists", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Register the user
        registerUser(email, password, name)
    }
    
    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        
        if (name.isEmpty()) {
            tilName.error = "Name is required"
            isValid = false
        } else {
            tilName.error = null
        }
        
        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            tilEmail.error = null
        }
        
        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            tilPassword.error = null
        }
        
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }
        
        return isValid
    }
    
    private fun isUserRegistered(email: String): Boolean {
        val sharedPrefs = getSharedPreferences(KEY_REGISTERED_USERS, MODE_PRIVATE)
        return sharedPrefs.contains(email)
    }
    
    private fun registerUser(email: String, password: String, name: String) {
        val sharedPrefs = getSharedPreferences(KEY_REGISTERED_USERS, MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(email, password)
            .apply()
        
        // Save user name in SharedPreferencesManager
        prefsManager.setUserName(name)
        
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        
        // Set user as logged in
        prefsManager.setUserLoggedIn(true)
        
        // Navigate to main activity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}