package com.pfe.stb.user.mapper;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.pfe.stb.auth.dto.request.SignUpDto;
import com.pfe.stb.auth.dto.response.SignUpResponseDto;
import com.pfe.stb.user.dto.response.UserDto;
import com.pfe.stb.user.model.Role;
import com.pfe.stb.user.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public abstract class UserMapper {

  public abstract User toAuthUserFromDto(SignUpDto signupDto);
  @Mapping(target = "roles", expression = "java(mapRolesToRoleNames(user.getRoles()))")
  public abstract UserDto toUserDto(User user);

  @Mapping(target = "roles", expression = "java(mapRolesToRoleNames(user.getRoles()))")
  public abstract SignUpResponseDto toSignUpResponseDto(User user);

  // Role Mapping Methods
  protected List<String> mapRolesToRoleNames(Set<Role> roles) {
    if (roles == null) {
      return Collections.emptyList();
    }
    return roles.stream().map(role -> role.getName().name()).toList();
  }
}
