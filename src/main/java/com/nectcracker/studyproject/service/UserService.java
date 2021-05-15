package com.nectcracker.studyproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.nectcracker.studyproject.domain.*;
import com.nectcracker.studyproject.json.friendsFromVK.FriendsFromVk;
import com.nectcracker.studyproject.json.friendsFromVK.Nickname;
import com.nectcracker.studyproject.repos.UserFriendsRepository;
import com.nectcracker.studyproject.repos.UserInfoRepository;
import com.nectcracker.studyproject.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private EventsService eventsService;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;
    private final ObjectMapper objectMapper;
    private final OAuth20Service vkScribejavaService;
    private final UserFriendsRepository userFriendsRepository;

    private String accessToken;


    private Random random = new Random();


    public UserService(UserRepository userRepository, MailSender mailSender, UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, OAuth20Service vkScribejavaService, UserFriendsRepository userFriendsRepository) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.vkScribejavaService = vkScribejavaService;
        this.userFriendsRepository = userFriendsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    public String addUser(UserRegistrationRequest userRegistrationRequest) throws IOException, GeneralSecurityException {

        if(!Pattern.matches(".*@.*", userRegistrationRequest.getEmail()))
            return "Wrong e-mail address";

        if (userRepository.findByUsername(userRegistrationRequest.getLogin()) != null || userRepository.findByEmail(userRegistrationRequest.getEmail()) != null)
            return "User exists!";

        User newUser = new User(userRegistrationRequest.getLogin(), passwordEncoder.encode(userRegistrationRequest.getPassword()), userRegistrationRequest.getEmail());
        newUser.setRoles(Collections.singleton(Role.USER));
        newUser.setActivationCode(UUID.randomUUID().toString());

        userRepository.save(newUser);

        if(!StringUtils.isEmpty(newUser.getEmail())) {
            String message = String.format("Hello %s \n" +
                    "Welcome to GiftsWeb, please follow to this link for confirm your account: " +
                    "http://localhost:8080/registration/activate/%s", userRegistrationRequest.getFirst_name(), newUser.getActivationCode());
            mailSender.send(newUser.getEmail(), "Activation code", message);
        }

        UserInfo userInfo = UserInfo.builder()
                .firstName(userRegistrationRequest.getFirst_name())
                .lastName(userRegistrationRequest.getLast_name())
                .birthday(userRegistrationRequest.getBirthday())
                .user(newUser).build();
        userInfoRepository.save(userInfo);
        return "";
    }

    public void addUserFromVk(Map<String, Object> userInfoMap) throws InterruptedException, ExecutionException, IOException {
        Long vkId = Long.valueOf((Integer) userInfoMap.get("id"));
        User user = userRepository.findByVkId(vkId);
        if (user == null) {
            user = new User(String.valueOf(vkId), passwordEncoder.encode(String.valueOf(random.nextInt(2147483600))), vkId);
            user.setEmail("q");
            user.setRoles(Collections.singleton(Role.USER));
            user.setConfirmed(true);
            userRepository.save(user);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.M.yyyy");
            Date bDate = null;
            try {
                if(userInfoMap.get("bdate") != null)
                    bDate = simpleDateFormat.parse((String) userInfoMap.get("bdate"));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            UserInfo userInfo = UserInfo.builder()
                    .firstName((String) userInfoMap.get("first_name"))
                    .lastName((String) userInfoMap.get("last_name"))
                    .photo50((String) userInfoMap.get("photo_50"))
                    .birthday(bDate)
                    .user(user).build();
            userInfoRepository.save(userInfo);
            user.setInfo(userInfo);
            takeFriendFromVk(user);
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        request.getSession().setAttribute("name", user.getInfo().getFirstName() + " " + user.getInfo().getLastName());
        request.getSession().setAttribute("photo", user.getInfo().getPhoto50());
    }

    public boolean activateUser(String code) {
        User user = userRepository.findByActivationCode(code);
        if( user == null)
            return false;

        user.setActivationCode(null);
        user.setConfirmed(true);

        userRepository.save(user);
        return true;

    }

    public Map<String, Set> takeFriendFromVk(User user) throws InterruptedException, ExecutionException, IOException {
        Set<User> friendsSetRegistered = new HashSet<>();
        Set<Nickname> friendsNicknamesSetNotRegistered = new HashSet<>();
        if(user.getVkId() != null) {
            final OAuthRequest friendsRequest = new OAuthRequest(Verb.GET, "https://api.vk.com/method/friends.get?user_id=" + user.getVkId() + "&fields=nickname,photo_50&v=" + VkontakteApi.VERSION);
            vkScribejavaService.signRequest(accessToken, friendsRequest);
            final Response friendsResponse = vkScribejavaService.execute(friendsRequest);

            String userFriendsFromVk = friendsResponse.getBody();
            Set<Nickname> friendsNicknames = objectMapper.readValue(userFriendsFromVk, FriendsFromVk.class).getResponse().getItems();


            User friend;
            for (Nickname friendsNickname : friendsNicknames) {
                friend = userRepository.findByVkId(friendsNickname.getId());
                if (friend != null)
                    friendsSetRegistered.add(friend);
                else
                    friendsNicknamesSetNotRegistered.add(friendsNickname);
            }
        }
        if(friendsSetRegistered.size() != 0 && (user.getFriends() == null || !user.getFriends().stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()).equals(friendsSetRegistered))) {
            Set<UserFriends> setFriends= new HashSet<>();
            Set<UserFriends> setFriendsOf = new HashSet<>();
            for (User friend : friendsSetRegistered){
                setFriends.add(new UserFriends(user, friend, true));
                setFriendsOf.add(new UserFriends(friend, user, true));
            }
            user.setFriends(setFriends);
            user.setFriendsOf(setFriendsOf);
            userRepository.save(user);
        }

        HashMap<String, Set> friendMapForForm= new HashMap<>();
        friendMapForForm.put("registered", friendsSetRegistered);
        friendMapForForm.put("notRegistered", friendsNicknamesSetNotRegistered);
        return friendMapForForm;
    }

    public Map<String, Set<User>> getFriends(User user) throws InterruptedException, ExecutionException, IOException {
        HashMap<String, Set<User>> resultFriendsMap = new HashMap<>();
        if(user.getVkId() != null){
            takeFriendFromVk(user);
        }
        resultFriendsMap.put("registered", userFriendsRepository.findAllByUser(user).stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()));
        return resultFriendsMap;
    }

    public Map<User, Boolean> findFriend(User firstUser, UserRegistrationRequest userRegistrationRequest) {
        boolean founded = false;
        boolean foundByLastName = false;
        userRegistrationRequest.setEmail(userRegistrationRequest.getEmail().replaceAll(" ", ""));
        userRegistrationRequest.setLogin(userRegistrationRequest.getLogin().replaceAll(" ", ""));
        userRegistrationRequest.setFirst_name(userRegistrationRequest.getFirst_name().replaceAll(" ", ""));
        userRegistrationRequest.setLast_name(userRegistrationRequest.getLast_name().replaceAll(" ", ""));
        Map<User, Boolean> soughtUsers = new HashMap<>();
        Set<UserInfo> usersInfoByFirstLastName = new HashSet<>();
        if(!userRegistrationRequest.getEmail().isEmpty()) {
            User user = userRepository.findByEmail(userRegistrationRequest.getEmail());
            if (user != null){
                soughtUsers.put(user, firstUser.getFriends().stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()).contains(user));
                founded = true;
            }
        }
        if(!founded && !userRegistrationRequest.getLogin().isEmpty()) {
            User user = userRepository.findByUsername(userRegistrationRequest.getLogin());
            if (user != null){
                soughtUsers.put(user, firstUser.getFriends().stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()).contains(user));
                founded = true;
            }
        }

        if(!founded && !userRegistrationRequest.getLast_name().isEmpty()){
            usersInfoByFirstLastName = userInfoRepository.findAllByLastName(userRegistrationRequest.getLast_name());
            if(!usersInfoByFirstLastName.isEmpty()){
                foundByLastName = true;
            }
        }
        if (!founded &&  !userRegistrationRequest.getFirst_name().isEmpty()){
            Set<UserInfo> usersInfoByFirstName = userInfoRepository.findAllByFirstName(userRegistrationRequest.getFirst_name());
            if(foundByLastName){
                usersInfoByFirstLastName = usersInfoByFirstLastName.stream().filter(usersInfoByFirstName::contains).collect(Collectors.toSet());
            } else {
                usersInfoByFirstLastName = usersInfoByFirstName;
            }
        }
        if(!founded && !usersInfoByFirstLastName.isEmpty()){
            for (UserInfo userInfo : usersInfoByFirstLastName){
                soughtUsers.put(userInfo.getUser(), firstUser.getFriends().stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()).contains(userInfo.getUser()));
            }
        }
        return soughtUsers;
    }

}
