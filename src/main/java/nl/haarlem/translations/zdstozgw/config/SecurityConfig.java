package nl.haarlem.translations.zdstozgw.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{

    @Value("${api.username:ozb}")
    private String username;

    @Value("${api.password:ozb}")
    private String password;
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http
            .csrf().disable()
            .authorizeRequests().antMatchers("/api/**")
            .authenticated()
            .and()
            .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
        throws Exception
    {
        auth.inMemoryAuthentication()
            .withUser(username)
            .password("{noop}"+password)
            .roles("USER");
    }
}
