package com.pfe.stb.config;

import com.pfe.stb.notifications.model.enums.EmailType;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class EmailTypeConfig {

  @Value("${application.frontend-url}")
  private String frontendUrl;

  @PostConstruct
  public void init() {
    log.info("Setting frontend URL for email types on the url: {}", frontendUrl);
    EmailType.setFrontendUrl(frontendUrl);
  }
}
