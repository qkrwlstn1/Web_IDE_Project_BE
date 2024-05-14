package com.als.webIde.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.DockerContextMetaFile.Endpoints.Docker;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 빈 등록 구성정보 제공 Config -> 의존 주입 받아서 사용
public class DockerJavaConfig {
    @Bean
    //객체 만들고 인스턴스화. -> 환경 변수 설정
    public DockerClientConfig dockerClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    }
    @Bean

//    호 스트 URL 설정 (docker Host)
//    sslConfig: Docker 데몬과의 보안 통신에 SSL이 사용되는 경우 SSL 구성.
    public DockerHttpClient dockerHttpClient() {
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig().getDockerHost())
                .sslConfig(dockerClientConfig().getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
    }

    @Bean
    public DockerClient dockerClient() {
        return DockerClientImpl.getInstance(dockerClientConfig(), dockerHttpClient());
    }
}
