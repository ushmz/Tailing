package com.rabhareit.tailing.service;

import com.rabhareit.tailing.entity.TailingAccount;
import com.rabhareit.tailing.entity.TailingSocialAccount;
import com.rabhareit.tailing.repository.TailingAccountRepository;
import com.rabhareit.tailing.repository.TailingSocialAccountRepository;
import com.rabhareit.tailing.repository.TweetCountRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

class TailingSocialUserInfo {
  long userid;

  String imgUrl;

  String bannerUrl;

  public long getUserid() { return userid; }

  public void setUserid(long userid) { this.userid = userid; }

  public String getImgUrl() { return imgUrl; }

  public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

  public String getBannerUrl() { return bannerUrl; }

  public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

  TailingSocialUserInfo(long userid, String imgUrl, String bannerUrl) {
    this.userid = userid;
    this.imgUrl = imgUrl;
    this.bannerUrl = bannerUrl;
  }
}

@Component
@Transactional
public class ConnectionSignUpImpl implements ConnectionSignUp {
    /*
    * Socialログイン時のユーザー追加(自システムのDBに) する必要はあるか？
    * user id(@~~~)でログインしてもらう.
    * -> username = userid(@~~~)
    * */

  @Value("${app.tailing-consumer-key}")
  private String tailingConsumerKey;

  @Value("${app.tailing-consumer-secret}")
  private String tailingConsumerSecret;

  @Value("${app.tailing-access-token}")
  private String tailingAccessToken;

  @Value("${app.tailing-access-token-secret}")
  private String tailingAccessTokenSecret;

  @Autowired
  TailingAccountRepository repository;

  @Autowired
  TailingSocialAccountRepository socialRepository;

  @Autowired
  TweetCountRepository tweetcount;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Override
  public String execute(Connection<?> connection) {
    UserProfile profile = connection.fetchUserProfile();
    String username = profile.getUsername();
    try{
      System.out.println("パスワード決めてもらう");
      String pw = RandomStringUtils.randomAlphanumeric(10);
      System.out.println(pw);

      TailingSocialUserInfo userinfo = getTwitterInfo(username);
      TailingSocialAccount socialAccount = new TailingSocialAccount(userinfo.getUserid(),userinfo.getUserid(),username,profile.getName(),passwordEncoder.encode(pw),false,userinfo.getImgUrl(),userinfo.getBannerUrl());
      socialRepository.insertAccount(socialAccount);

      //tweetcountテーブルにセット
      setCountor(socialAccount);

    } catch (TwitterException e) {
      e.printStackTrace();
    }
    return username;
  }

  TailingSocialUserInfo getTwitterInfo(String username) throws TwitterException {
    Configuration configuration = new ConfigurationBuilder()
      .setOAuthConsumerKey(tailingConsumerKey)
      .setOAuthConsumerSecret(tailingConsumerSecret)
      .setOAuthAccessToken(tailingAccessToken)
      .setOAuthAccessTokenSecret(tailingAccessTokenSecret)
      .build();
    Twitter twitter = new TwitterFactory(configuration).getInstance();
    User user = twitter.showUser(username);
    long userid= user.getId();
    String imgUrl = user.getProfileImageURL();
    String bannerUrl = user.getProfileBannerURL();
    return new TailingSocialUserInfo(userid, imgUrl, bannerUrl);
  }

  void setCountor(TailingSocialAccount account) {
    tweetcount.setCountor(account.getTwitterId(), account.getUserName(),0);
  }

}
