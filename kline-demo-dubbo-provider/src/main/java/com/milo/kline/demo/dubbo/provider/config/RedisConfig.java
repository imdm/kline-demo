package com.milo.kline.demo.dubbo.provider.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {

    public GenericObjectPoolConfig getGenericObjectLettucePoolConfig(RedisProperties redisProperties){
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
        genericObjectPoolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
        genericObjectPoolConfig.setMaxWait(redisProperties.getLettuce().getPool().getMaxWait());
        //默认值为false,在获取连接之前检测是否为有效连接,tps很高的应用可以使用默认值
        genericObjectPoolConfig.setTestOnBorrow(true);
        genericObjectPoolConfig.setTestOnReturn(true);
        //使用lettuce pool的配置的,需要打开此配置,用于检测控线连接并回收
        genericObjectPoolConfig.setTestWhileIdle(true);
        return genericObjectPoolConfig;
    }

    @Bean
    public RedisConnectionFactory connectionFactory(RedisProperties redisProperties) {
        //添加额外属性
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(redisProperties.getTimeout())
                .poolConfig(getGenericObjectLettucePoolConfig(redisProperties))
                .clientName(redisProperties.getClientName())
                .build();

        //单机连接配置,单实例redis连接放开下边注释,同时注释掉RedisClusterConfiguration的配置
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisProperties.getHost(),redisProperties.getPort());
        configuration.setPassword(redisProperties.getPassword());

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(configuration, clientConfig);
        //如果要使pool参数生效,一定要关闭shareNativeConnection
        lettuceConnectionFactory.setShareNativeConnection(false);

        return lettuceConnectionFactory;
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
