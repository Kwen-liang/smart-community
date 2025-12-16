package com.smartcommunity.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProjectConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Autowired
    private TokenInterceptor tokenInterceptor; // 注入拦截器

    /**
     * 1. 配置跨域请求 (CORS)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 2. 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 防止空路径报错
        String safeUploadPath = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        String path = "file:" + new java.io.File(safeUploadPath).getAbsolutePath() + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations(path);
    }

    /**
     * 3. 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**") // 拦截所有接口
                .excludePathPatterns("/auth/login", "/common/upload", "/files/**"); // 放行登录、上传和静态资源
    }

    /**
     * 4. MyBatis Plus 分页插件
     * 这个插件就是你报错找不到的类，只要 pom.xml 依赖正确，这个 Bean 就能正常创建
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型为 PostgreSQL，有助于分页方言的正确识别
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}