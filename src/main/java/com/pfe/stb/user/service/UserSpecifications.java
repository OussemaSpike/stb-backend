package com.pfe.stb.user.service;

import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for querying UserEntity. - root is the entity that we are working with - query is
 * the query that we are building - cb is the criteria builder that we use to build the query
 */
public class UserSpecifications {

  private UserSpecifications() {}

  /**
   * Creates a specification to find users by a search criteria. The criteria is matched against
   * email, first name, last name, and phone number.
   *
   * @param criteria the search criteria
   * @return a specification for finding users by criteria
   */
  public static Specification<User> hasCriteria(String criteria, RoleType role) {
    return (root, query, cb) -> {
      Predicate search =
          cb.or(
              cb.like(cb.lower(root.get("email")), "%" + criteria.toLowerCase() + "%"),
              cb.like(cb.lower(root.get("firstName")), "%" + criteria.toLowerCase() + "%"),
              cb.like(cb.lower(root.get("lastName")), "%" + criteria.toLowerCase() + "%"),
              cb.like(cb.lower(root.get("phoneNumber")), "%" + criteria.toLowerCase() + "%"));

      if (role == null) {
        return search;
      } else {
        Predicate roles = cb.equal(root.join("roles").get("name"), role.name());
        return cb.and(roles, search);
      }
    };
  }
}
