package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private SignupBusinessService signupBusinessService;

    @Autowired
    private AuthenticationService authenticationService;



    /**
     * // ENDPOITN - signup - "/user/signup"
     * //
     * // This endpoint is used to register a new user in the Quora Application.
     * @param signupUserRequest
     * @return ResponseEntity
     * @throws SignUpRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signUp(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {

        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUsername(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setSalt("123abc");
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutme(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setRole("nonadmin");
        userEntity.setContactNumber(signupUserRequest.getContactNumber());


        final UserEntity createUser = signupBusinessService.signUp(userEntity);

        SignupUserResponse userResponse = new SignupUserResponse().id(userEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }



    /**
     *  //signin - "/user/signin"
     *     //
     *     //This endpoint is used for user authentication. The user authenticates in the application and after successful authentication, JWT token is given to a user.
     * @param authorization
     * @return ResponseENtity
     * @throws AuthenticationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signIn(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        UserAuthEntity userAuthToken = authenticationService.authenticate(decodedArray[0], decodedArray[1]);

        UserEntity user = userAuthToken.getUser();

        SigninResponse signinResponse = new SigninResponse().id(user.getUuid()).message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();

        headers.add("access-token", userAuthToken.getAccessToken());

        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }



    /**
     *  //signout - "/user/signout"
     *     //
     *     //This endpoint is used to sign out from the Quora Application. The user cannot access any other endpoint once he is signed out of the application.
     * @param authorization
     * @return ResponseEntity
     * @throws AuthenticationFailedException
     * @throws SignOutRestrictedException
     */
    @RequestMapping(method=RequestMethod.POST,path="/user/signout", produces=MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signOut(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException, SignOutRestrictedException {

        UserAuthEntity updateuserAuthToken=authenticationService.getUserByToken(authorization);
        ZonedDateTime now=ZonedDateTime.now();
        updateuserAuthToken.setLogoutAt(now);
      //  updateuserAuthToken.setAccessToken("");

        authenticationService.authTokenUpdate(updateuserAuthToken);

        SignoutResponse signoutResponse=new SignoutResponse().id(updateuserAuthToken.getUuid()).message("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<SignoutResponse>(signoutResponse,HttpStatus.OK);

    }
}





