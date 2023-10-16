package com.xupt.love.config.enums;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

//    @Value("${security.public}")
    private List<String> publicPaths = new ArrayList<>();
//    @Value("${security.authenticated}")
    private List<String> authenticatedPaths = new ArrayList<>();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public List<String> getAuthenticatedPaths() {
        return authenticatedPaths;
    }

    public void setAuthenticatedPaths(List<String> authenticatedPaths) {
        this.authenticatedPaths = authenticatedPaths;
    }
    @PostConstruct
    public void aVoid(){
        authenticatedPaths.stream().forEach(System.out::println);
        publicPaths.stream().forEach(System.out::println);
    }

}
