package me.shreyasr.chatse.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.squareup.okhttp.FormEncodingBuilder
import com.squareup.okhttp.Request
import me.shreyasr.chatse.App
import me.shreyasr.chatse.R
import me.shreyasr.chatse.chat.ChatActivity
import me.shreyasr.chatse.network.Client
import me.shreyasr.chatse.network.ClientManager
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException

/**
 * Activity to login the user.
 */
class LoginActivity : AppCompatActivity() , LoginContract.ILoginView{



    // Views
    lateinit var emailView: EditText
    lateinit var passwordView: EditText
    lateinit var progressBar: ProgressBar
    lateinit var loginButton: Button
    lateinit var prefs: SharedPreferences
    lateinit var presenter:LoginContract.ILoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = App.sharedPreferences
        if (prefs.getBoolean(App.PREF_HAS_CREDS, false)) {
            this.startActivity(Intent(this, ChatActivity::class.java))
            this.finish()
            return
        }

        setContentView(R.layout.activity_login)

        // Get views
        emailView = findViewById(R.id.login_email) as EditText
        passwordView = findViewById(R.id.login_password) as EditText
        progressBar = findViewById(R.id.login_progress) as ProgressBar
        loginButton = findViewById(R.id.login_submit) as Button

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        presenter = LoginPresenter(this)

        loginButton.setOnClickListener { presenter.attemptLogin() }

        emailView.setText(prefs.getString(App.PREF_EMAIL, ""))
        passwordView.setText(prefs.getString("password", "")) // STOPSHIP
        passwordView.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == R.id.login_submit || id == EditorInfo.IME_NULL) {
                presenter.attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
    }



    private fun validateInputs(): Boolean {
        var isValid = true
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

//        if (!isEmailValid(email)) {
//            emailView.error = getString(R.string.err_invalid_email)
//            isValid = false
//        }

        if (email.isEmpty()) {
            emailView.error = getString(R.string.err_blank_email)
            isValid = false
        }

        if (password.isNullOrBlank()) {
            passwordView.error = getString(R.string.err_blank_password)
            isValid = false
        }

        // STOPSHIP
        //TODO store using AccountManager
        if (isValid) {
            prefs.edit().putString(App.PREF_EMAIL, email).putString("password", password).apply()
        }

        return isValid
    }





    override fun loginButtonEnabled(enabled: Boolean) {
        loginButton.isEnabled = enabled
    }

    override fun setEmailError(error: Int?) {
        emailView.error = error?.let { getString(it) }
    }

    override fun setPasswordError(error: Int?) {
        passwordView.error = error?.let {getString(it)}
    }

    override fun getEmailValue(): String {
        return emailView.text.toString()
    }

    override fun getPasswordValue(): String {
       return passwordView.text.toString()
    }

    override fun setProgressBarVisibility(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun showToast(message: Int) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    override fun startChatActivity() {
        this@LoginActivity.startActivity(Intent(this@LoginActivity, ChatActivity::class.java))
        this@LoginActivity.finish()
    }
}
