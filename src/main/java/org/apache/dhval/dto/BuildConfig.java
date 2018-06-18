package org.apache.dhval.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:build.properties")
public class BuildConfig {

    @Value( "${build.name}" )
    public String buildName;

    @Value( "${build.date}" )
    public String buildDate;

    @Value( "${build.version}" )
    public String buildVersion;

    @Override
    public String toString() {
        return "BuildConfig{" +
                "buildName='" + buildName + '\'' +
                ", buildDate='" + buildDate + '\'' +
                ", buildVersion='" + buildVersion + '\'' +
                '}';
    }
}
