package ua.edu.springLibrary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ua.edu.springLibrary.domain.Book;
import ua.edu.springLibrary.domain.Role;
import ua.edu.springLibrary.domain.User;
import ua.edu.springLibrary.mapper.UserMapper;
import ua.edu.springLibrary.repository.UserRepository;

import java.util.List;

@Service("UserService")
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::mapUserEntityToUser).orElse(null);

    }

    @Override
    public void saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(userMapper.mapUserToUserEntity(user));
    }
}