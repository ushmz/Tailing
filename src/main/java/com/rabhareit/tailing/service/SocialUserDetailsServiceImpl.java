package com.rabhareit.tailing.service;

import com.rabhareit.tailing.entity.TailingSocialUser;
import com.rabhareit.tailing.repository.TailingSocialUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
/**
 * Social ログイン時の userDetailService として利用
 *
 */

@Service
@Transactional
public class SocialUserDetailsServiceImpl implements SocialUserDetailsService {

  private TailingSocialUserRepository socialUserRepository;

  private PasswordEncoder passwordEncoder;

  @Override
  public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
    if(userId == null || "".equals(userId)) {
      throw new UsernameNotFoundException("UserID is empty");
    }

    TailingSocialUser account = socialUserRepository.findByUserId(userId);
    if (account == null) {
      throw new UsernameNotFoundException("User not found: " + userId);
    }

    //What Username mean!? "twitter id(long)" or "user id(@~~~)" or "screen name"
    return new TailingSocialUser(account.getUsername(),passwordEncoder.encode(account.getPassword()),getAuthorities(account));
  }

  private Collection<GrantedAuthority> getAuthorities(TailingSocialUser account) {
    if (account.isAdmin()) {
      return AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
    } else {
      return AuthorityUtils.createAuthorityList("ROLE_USER");
    }
  }
}
