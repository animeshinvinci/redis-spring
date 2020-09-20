@SuppressWarnings({ "rawtypes", "unused" })
@Configuration
@EnableCaching(proxyTargetClass = true, mode = AdviceMode.ASPECTJ, order = 1)
@PropertySource("classpath:/application.properties")
public class CacheConfigImpl extends CachingConfigurerSupport {

    private @Value("${redis.ip}") String redisHost;
    private @Value("${redis.port}") int redisPort;

     private static final Map<String, Long> cacheMap = new HashMap<String, Long>();
    static {
        cacheMap.put("method1cache", 600L);
        cacheMap.put("method2cache", 600L);
        cacheMap.put("method3cache", 800L);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
        redisConnectionFactory.setHostName(CustomPropertyLoader.getProperty("redis.ip"));
        redisConnectionFactory.setPort(Integer.parseInt(CustomPropertyLoader.getProperty("redis.port")));
        return redisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean(name = "RCacheManager")
    public CacheManager cacheManager(RedisTemplate redisTemplate) {

        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setExpires(cacheMap);
        cacheManager.setUsePrefix(true);
        final String redis_client_name = CustomPropertyLoader.getProperty("redis.client.name");
        cacheManager.setCachePrefix(new RedisCachePrefix() {
            private final RedisSerializer<String> serializer = new StringRedisSerializer();
            private final String delimiter = ":";

            public byte[] prefix(String cacheName) {
                return this.serializer
                        .serialize(redis_client_name.concat(this.delimiter).concat(cacheName).concat(this.delimiter));
            }
        });
        return cacheManager;
    }
    }
