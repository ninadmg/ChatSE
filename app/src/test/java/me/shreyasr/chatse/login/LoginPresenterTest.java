package me.shreyasr.chatse.login;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import me.shreyasr.chatse.R;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by ninad on 06/07/17.
 */
@RunWith (MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock
    private LoginContract.ILoginView view;

    private LoginContract.ILoginPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new LoginPresenter(view);
    }

    @Test
    public void shouldShowErrorMessageWhenEmailIsInvalid() throws Exception{
        when(view.getEmailValue()).thenReturn("Ninad");
        presenter.attemptLogin();
        verify(view).setEmailError(R.string.err_invalid_email);
    }

    @Test
    public void shouldShowErrorMessageWhenEmailIsEmpty() throws Exception {
        when(view.getEmailValue()).thenReturn("");
        presenter.attemptLogin();
        verify(view).setEmailError(R.string.err_blank_email);
    }

    @Test
    public void shouldShowErrorMessageWhenPasswordIsEmpty() throws Exception{
        when(view.getEmailValue()).thenReturn("mgn524@gmail.com");
        when(view.getPasswordValue()).thenReturn("");
        presenter.attemptLogin();
        verify(view).setPasswordError(R.string.err_blank_password);
    }

}
