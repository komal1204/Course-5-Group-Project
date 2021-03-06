package com.upgrad.quora.service.business;


import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;



    /**
     * //This method validates user
     * @param username
     * @param password
     * @return UserAuthEntity
     * @throws AuthenticationFailedException
     */
    @Transactional(propagation=Propagation.REQUIRED)
    public UserAuthEntity authenticate(final String username, final String password) throws AuthenticationFailedException{

       UserEntity userEntity= userDao.getUserByUserName(username);

       if(userEntity==null){
           throw new AuthenticationFailedException("ATH-001","Username does not exists");
       }

       final String encryptedPass=cryptographyProvider.encrypt(password,userEntity.getSalt());

       if(encryptedPass.equals(userEntity.getPassword())){
           JwtTokenProvider jwtTokenProvider=new JwtTokenProvider(encryptedPass);

           UserAuthEntity userAuthToken=new UserAuthEntity();
           userAuthToken.setUser(userEntity);
           userAuthToken.setUuid(UUID.randomUUID().toString());
           ZonedDateTime now=ZonedDateTime.now();
           ZonedDateTime expiresAt=now.plusHours(8);
           userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(),now,expiresAt));
           userAuthToken.setLoginAt(now);
           userAuthToken.setExpiresAt(expiresAt);

           userDao.createAuthToken(userAuthToken);

           return userAuthToken;

       }else{
           throw new AuthenticationFailedException("ATH-002","Password failed");
       }

    }




    /**
     * //This method validates user by access token
     * @param authorization
     * @return //This method validates user
     * @throws SignOutRestrictedException
     */
    @Transactional(propagation=Propagation.REQUIRED)
    public UserAuthEntity getUserByToken(final String authorization) throws SignOutRestrictedException {

       UserAuthEntity userTokenExists= userDao.getUserAuthToken(authorization);
       if(userTokenExists==null) {
           throw new SignOutRestrictedException("SGR-001", "User is not Signed in");

       }else{
        return userTokenExists;

       }

    }

    /**
     * updates
     * @param userAuthEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void authTokenUpdate(final UserAuthEntity userAuthEntity){
        userDao.updateAuthToken(userAuthEntity);
    }
}
