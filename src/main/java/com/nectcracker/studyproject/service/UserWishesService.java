package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.domain.Events;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.repos.EventsRepository;
import com.nectcracker.studyproject.repos.UserRepository;
import com.nectcracker.studyproject.repos.UserRepositoryCustom;
import com.nectcracker.studyproject.repos.UserWishesRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserWishesService implements UserRepositoryCustom {
    private final UserWishesRepository userWishesRepository;
    private final UserRepository userRepository;
    private final EventsRepository eventsRepository;

    public UserWishesService(UserWishesRepository userWishesRepository, UserRepository userRepository, EventsRepository eventsRepository) {
        this.userWishesRepository = userWishesRepository;
        this.userRepository = userRepository;
        this.eventsRepository = eventsRepository;
    }

    @Override
    public User findByAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName());
    }

    public Iterable<UserWishes> getUserWishes(User user) {
        return userWishesRepository.findByUserId(user.getId());
    }

    public boolean addWish(String text, String eventId, String imgURL) {

        try {
            if(text.isEmpty())
                return false;
            Events event = null;
            if(!eventId.isEmpty())
                event= eventsRepository.getOne(Long.parseLong(eventId));
            User currentUser = findByAuthentication();
            UserWishes m = new UserWishes(currentUser, text, imgURL);
            m.setFriendCreateWish(false);
            m.setEventForWish(event);
            userWishesRepository.save(m);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean addWishFromFriend(String username, String wishName, String eventId, String imgURL){
        try {
            User user = userRepository.findByUsername(username);
            Events event = null;
            if(!eventId.isEmpty())
                event= eventsRepository.getOne(Long.parseLong(eventId));
            UserWishes m = new UserWishes(user, wishName, imgURL);
            m.setFriendCreateWish(true);
            m.setEventForWish(event);
            userWishesRepository.save(m);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserWishes getById(Long id) {
        Optional<UserWishes> wish = userWishesRepository.findById(id);
        return wish.orElseGet(UserWishes::new);
    }

    public void deleteUserWish(Long id) {
        UserWishes wish = getById(id);
        wish.setDeleted(true);
        userWishesRepository.save(wish);
    }
}
