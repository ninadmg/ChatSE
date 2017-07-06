package me.shreyasr.chatse.login

/**
 * Created by ninad on 06/07/17.
 */
interface LoginContract {

    interface ILoginView {

        fun loginButtonEnabled(enabled : Boolean)
        fun setEmailError(error: Int?)
        fun setPasswordError(error: Int?)
        fun getEmailValue():String
        fun getPasswordValue():String
        fun setProgressBarVisibility(visibility:Int)
        fun showToast(message: Int)
        fun startChatActivity()

    }

    interface ILoginPresenter {

        fun attemptLogin()

    }

}