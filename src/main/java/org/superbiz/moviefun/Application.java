package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.hibernate.dialect.MySQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.management.MXBean;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.beans.beancontext.BeanContext;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {

        String vcapServices = "{\"p-mysql\": [{\"credentials\": {\"jdbcUrl\": \"jdbc:mysql://127.0.0.1:3306/albums?user=root\"}, \"name\": \"albums-mysql\"}, {\"credentials\": {\"jdbcUrl\": \"jdbc:mysql://127.0.0.1:3306/movies?user=root\"}, \"name\": \"movies-mysql\"}]}";
        if(System.getenv("VCAP_SERVICES") != null) {
            vcapServices = System.getenv("VCAP_SERVICES");
        }
        return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();

        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        return dataSource;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();

        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));

        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter getJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Qualifier("albumsEMF")
    @Bean
    LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBeanAlbums(HibernateJpaVendorAdapter va) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(albumsDataSource(databaseServiceCredentials()));
        factoryBean.setJpaVendorAdapter(va);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        factoryBean.setPersistenceUnitName("Albums");
        return factoryBean;
    }

    @Qualifier("moviesEMF")
    @Bean
    LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBeanMovies(HibernateJpaVendorAdapter va) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(moviesDataSource(databaseServiceCredentials()));
        factoryBean.setJpaVendorAdapter(va);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        factoryBean.setPersistenceUnitName("Movies");
        return factoryBean;
    }

    @Qualifier("albumsTM")
    @Bean
    PlatformTransactionManager getPlatformTransactionManagerForAlbums(@Qualifier("albumsEMF") LocalContainerEntityManagerFactoryBean emf ) {
        JpaTransactionManager jtm = new JpaTransactionManager();
        jtm.setEntityManagerFactory(emf.getNativeEntityManagerFactory());
        return jtm;
    }

    @Qualifier("moviesTM")
    @Bean
    PlatformTransactionManager getPlatformTransactionManagerForMovies(@Qualifier("moviesEMF") LocalContainerEntityManagerFactoryBean emf ) {
        JpaTransactionManager jtm = new JpaTransactionManager();
        jtm.setEntityManagerFactory(emf.getNativeEntityManagerFactory());
        return jtm;
    }
}





