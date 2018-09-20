package com.oms;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import oracle.retail.stores.common.data.JdbcUtilities;
import oracle.retail.stores.common.data.jdbchelper.OracleHelper;

@SpringBootApplication
@ComponentScan({ "com.oms","oracle", "com.payment","payment" })
@EnableScheduling
public class Application
{
    
    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(final ApplicationContext ctx) 
    {
        return new CommandLineRunner()
        {
			@Override
            public void run(String... args) throws Exception
            {

			    System.out.println("Let's inspect the beans provided by Spring Boot:");
			    JdbcUtilities.setJdbcHelperClass(new OracleHelper());
			    String[] beanNames = ctx.getBeanDefinitionNames();
			    Arrays.sort(beanNames);
			    for (String beanName : beanNames) 
			    {
			        System.out.println(beanName);
			    }

			}
		};
    }

}