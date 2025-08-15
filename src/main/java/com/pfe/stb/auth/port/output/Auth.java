package com.pfe.stb.auth.port.output;

import com.pfe.stb.auth.model.AuthCode;
import com.pfe.stb.auth.model.AuthResponse;
import com.pfe.stb.user.model.User;
import java.util.Optional;

public interface Auth {

  User signUp(User authUser);

  AuthResponse signIn(String email, String password, boolean rememberMe);

  void activateAccount(AuthCode authCode, User user);

  void changePassword(User user, String newPassword);

  Optional<User> findByEmail(String email);

  AuthResponse refreshToken(String refreshToken);
}
