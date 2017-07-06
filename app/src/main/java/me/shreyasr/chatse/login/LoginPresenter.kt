package me.shreyasr.chatse.login

import android.os.AsyncTask
import android.view.View
import com.squareup.okhttp.FormEncodingBuilder
import com.squareup.okhttp.Request
import me.shreyasr.chatse.R
import me.shreyasr.chatse.network.Client
import me.shreyasr.chatse.network.ClientManager
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException

/**
 * Created by ninad on 06/07/17.
 */
class LoginPresenter (val view :LoginContract.ILoginView) : LoginContract.ILoginPresenter {

    override fun attemptLogin() {

        view.loginButtonEnabled(false)

        // Reset errors.
        view.setEmailError(null)
        view.setPasswordError(null)

        if (!validateInputs()) {
            view.loginButtonEnabled(true)
            return
        }

       view.setProgressBarVisibility(View.VISIBLE)//TODO check shound we user Android.view Here

         LoginAsyncTask().execute(view.getEmailValue(),view.getPasswordValue())

    }

    private fun validateInputs(): Boolean {

        var isValid = true
        val email = view.getEmailValue()
        val password = view.getPasswordValue()

        if (!isEmailValid(email)) {
            view.setEmailError(R.string.err_invalid_email)
            isValid = false
        }

        if (email.isEmpty()) {
            view.setEmailError(R.string.err_blank_email)
            isValid = false
        }

        if (password.isNullOrBlank()) {
            view.setPasswordError(R.string.err_blank_password)
            isValid = false
        }

        // STOPSHIP
        //TODO store using AccountManager

        return isValid
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@") //TODO Improve email prevalidation
    }


    //TODO: We don't want this to be an inner class. We can fix it, or just replace it
    // with RxJava like #6 suggests.
    private inner class LoginAsyncTask : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String): Boolean? {
            val email = params[0]
            val password = params[1]

            try {
                val client = ClientManager.client

                seOpenIdLogin(client, email, password)
                loginToSE(client)
                loginToSite(client, "https://stackoverflow.com", email, password)
                return true
            } catch (e: IOException) {
                Timber.e(e)
                return false
            }

        }

        override fun onPostExecute(success: Boolean?) {
            if (success!!) {
                view.startChatActivity()

            } else {
                view.setProgressBarVisibility(View.GONE)
                view.loginButtonEnabled(false)
                view.showToast(R.string.err_failed_to_connect)
            }
        }

        @Throws(IOException::class)
        private fun loginToSite(client: Client, site: String,
                                email: String, password: String) {
            val soFkey = Jsoup.connect(site + "/users/login/")
                    .userAgent(Client.USER_AGENT).get()
                    .select("input[name=fkey]").attr("value")

            val soLoginRequestBody = FormEncodingBuilder()
                    .add("email", email)
                    .add("password", password)
                    .add("fkey", soFkey)
                    .build()
            val soLoginRequest = Request.Builder()
                    .url(site + "/users/login/")
                    .post(soLoginRequestBody)
                    .build()
            val soLoginResponse = client.newCall(soLoginRequest).execute()
            Timber.i("Site login: " + soLoginResponse.toString())
        }

        @Throws(IOException::class)
        private fun loginToSE(client: Client) {
            val loginPageRequest = Request.Builder()
                    .url("http://stackexchange.com/users/login/")
                    .build()
            val loginPageResponse = client.newCall(loginPageRequest).execute()

            val doc = Jsoup.parse(loginPageResponse.body().string())
            val fkeyElements = doc.select("input[name=fkey]")
            val fkey = fkeyElements.attr("value")

            if (fkey == "") throw IOException("Fatal: No fkey found.")

            val data = FormEncodingBuilder()
                    .add("oauth_version", "")
                    .add("oauth_server", "")
                    .add("openid_identifier", "https://openid.stackexchange.com/")
                    .add("fkey", fkey)

            val loginRequest = Request.Builder()
                    .url("https://stackexchange.com/users/authenticate/")
                    .post(data.build())
                    .build()
            val loginResponse = client.newCall(loginRequest).execute()
            Timber.i("So login: " + loginResponse.toString())
        }

        @Throws(IOException::class)
        private fun seOpenIdLogin(client: Client, email: String, password: String) {
            val seLoginPageRequest = Request.Builder()
                    .url("https://openid.stackexchange.com/account/login/")
                    .build()
            val seLoginPageResponse = client.newCall(seLoginPageRequest).execute()

            val seLoginDoc = Jsoup.parse(seLoginPageResponse.body().string())
            val seLoginFkeyElements = seLoginDoc.select("input[name=fkey]")
            val seFkey = seLoginFkeyElements.attr("value")

            val seLoginRequestBody = FormEncodingBuilder()
                    .add("email", email)
                    .add("password", password)
                    .add("fkey", seFkey)
                    .build()
            val seLoginRequest = Request.Builder()
                    .url("https://openid.stackexchange.com/account/login/submit/")
                    .post(seLoginRequestBody)
                    .build()
            val seLoginResponse = client.newCall(seLoginRequest).execute()
            Timber.i("Se openid login: " + seLoginResponse.toString())
        }
    }
}