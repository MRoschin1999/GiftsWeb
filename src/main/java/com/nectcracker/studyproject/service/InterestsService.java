package com.nectcracker.studyproject.service;

import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.nectcracker.studyproject.domain.Interests;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.repos.InterestsRepository;
import com.nectcracker.studyproject.repos.UserRepository;
import com.nectcracker.studyproject.repos.UserRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class InterestsService implements UserRepositoryCustom {
    private final InterestsRepository interestsRepository;
    private final UserRepository userRepository;
    private final OAuth20Service vkScribejavaService;
    private String accessToken;

    public InterestsService(InterestsRepository interestsRepository, UserRepository userRepository, OAuth20Service vkScribejavaService) {
        this.interestsRepository = interestsRepository;
        this.userRepository = userRepository;
        this.vkScribejavaService = vkScribejavaService;
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }


    @Override
    public User findByAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName());
    }

    public void updateUserInterests(String interest) {
        if (interest.trim().equals("")) {
            return;
        }
        User currentUser = findByAuthentication();
        Interests tmpInterest = interestsRepository.findByInterestName(interest);
        if (tmpInterest != null) {
            currentUser.getInterestsSet().add(tmpInterest);
        } else {
            Interests newInterest = new Interests(interest);
            interestsRepository.save(newInterest);
            currentUser.getInterestsSet().add(newInterest);
        }
        userRepository.save(currentUser);
    }

    public Set<Interests> getUserInterests() {
        User currentUser = findByAuthentication();
        return currentUser.getInterestsSet();
    }

    public Set<Interests> getSmbInterests(User user) {
        return user.getInterestsSet();
    }

    public boolean deleteInterest(String name) {
        User currentUser = findByAuthentication();
        boolean isRemoved =  currentUser.getInterestsSet().removeIf(interest -> interest.getInterestName().equals(name));
        userRepository.save(currentUser);
        return isRemoved;
    }

    public List<Interests> getAllInterests() {
       return interestsRepository.findAll();
    }

    public void findInterestByVk(User user) throws IOException, ExecutionException, InterruptedException {
        final OAuthRequest friendsRequest = new OAuthRequest(Verb.GET, "https://api.vk.com/method/groups.get?user_id=" + user.getVkId() + "&fields=activity,age_limits&v=" + VkontakteApi.VERSION);
        vkScribejavaService.signRequest(accessToken, friendsRequest);
        final Response friendsResponse = vkScribejavaService.execute(friendsRequest);
        updateUserInterests("Кулинария");
    }
}
