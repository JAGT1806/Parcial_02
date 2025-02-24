package co.edu.usco.parcial2.app.service.imp;

import co.edu.usco.parcial2.app.entity.RoleEntity;
import co.edu.usco.parcial2.app.entity.UserEntity;
import co.edu.usco.parcial2.app.repository.RoleRepository;
import co.edu.usco.parcial2.app.repository.UserRepository;
import co.edu.usco.parcial2.app.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest request;
    private final HttpServletRequest httpServletRequest;

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public UserEntity findUserByUserName(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public void saveUser(UserEntity user, Set<Integer> roleIds) {
        // Encriptar contraseña si es un usuario nuevo o si se cambió la contraseña
        if (user.getUserId() == null || !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Asignar roles
        Set<RoleEntity> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        user.setRole(roles);

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public UserEntity findUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public void updateUser(UserEntity user) {
        userRepository.save(user);  // Guarda los cambios en la base de datos
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);  // Elimina el usuario por ID
    }


    public Set<RoleEntity> getAllRoles() {
        return new HashSet<>(roleRepository.findAll());
    }


    public UserEntity getCurrentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("No hay sesion activa");
        }

        HttpSession session = request.getSession(false);

        if(session == null) {
            throw new RuntimeException("No hay sesion activa");
        }

        UserEntity user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user;
    }

    public List<UserEntity> getAllProfesores() {
        return userRepository.findByRole("DOCENTE").orElse(new ArrayList<>());
    }

}

