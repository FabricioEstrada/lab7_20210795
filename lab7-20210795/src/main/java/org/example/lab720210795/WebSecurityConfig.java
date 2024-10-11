package org.example.lab720210795;

import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.sql.DataSource;
import javax.xml.crypto.Data;

@Configuration
public class WebSecurityConfig {
    final UsersRepository usersRepository;
    final DataSource dataSource;
    public WebSecurityConfig(DataSource dataSource, UsersRepository usersRepository) {
        this.dataSource = dataSource;
        this.usersRepository = usersRepository;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);

        String sql1 = "SELECT email, password FROM users WHERE email = ?";

        String sql2 = "SELECT u.email, r.name FROM users u "
                + "INNER JOIN roles r ON (u.roleId = r.id) "
                + "WHERE u.email = ?";
        users.setUsersByUsernameQuery(sql1);
        users.setAuthoritiesByUsernameQuery(sql2);
        return users;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.formLogin()
                .loginPage("/openLoginWindow")
                .loginProcessingUrl("/submitLoginForm")
                .successHandler((request, response, authentication) -> {
                    System.out.println("Usuario autenticado: " + authentication.getName());
                    DefaultSavedRequest defaultSavedRequest =
                            (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");

                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usersRepository.findByEmail(authentication.getName()));


                    //si vengo por url -> defaultSR existe
                    if (defaultSavedRequest != null) {
                        String targetURl = defaultSavedRequest.getRequestURL();
                        new DefaultRedirectStrategy().sendRedirect(request, response, targetURl);
                    } else { //estoy viniendo del botón de login
                        String rol = "";
                        for (GrantedAuthority role : authentication.getAuthorities()) {
                            rol = role.getAuthority();

                            break;
                        }

                        switch (rol) {
                            case "admin":
                                response.sendRedirect("/admin");
                                break;
                            case "cliente":
                                response.sendRedirect("/cliente");
                                break;
                            case "gerente":
                                response.sendRedirect("/gerente");
                                break;
                            default:
                                break;
                        }
                    }
                });

        http.authorizeHttpRequests()
                .requestMatchers("/admin", "/admin/**").hasAnyAuthority("admin")
                .requestMatchers("/gerente", "/gerente/**").hasAnyAuthority("gerente", "admin")
                .requestMatchers("/cliente", "/cliente/**").hasAnyAuthority("cliente", "gerebte","admin")
                .anyRequest().permitAll();

        http.logout()
                .logoutSuccessUrl("/product")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);

        return http.build();
    }


}
