package com.montague.springbatchtesting;

import com.montague.springbatchtesting.dao.Order;
import com.montague.springbatchtesting.mapper.OrderRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.List;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchTestingApplication {

    public static String[] tokens = new String[]{"order_id", "first_name", "last_name", "email", "cost", "item_id",
            "item_name", "ship_date"};

    public static String ORDER_SQL = "select order_id, first_name, last_name, "
            + "email, cost, item_id, item_name, ship_date "
            + "from SHIPPED_ORDER order by order_id";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchTestingApplication.class, args);
    }

    @Bean
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        factoryBean.setSelectClause("select order_id, first_name, last_name, "
                + "email, cost, item_id, item_name, ship_date "
                + "from SHIPPED_ORDER order by order_id");
        factoryBean.setFromClause("from SHIPPED_ORDER");
        factoryBean.setSortKey("order_id");
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean
    public ItemReader<Order> itemReader() throws Exception {
        return new JdbcPagingItemReader<Order>()
                .dataSource(dataSource)
                .name("JdbcPagingItemReader")
                .queryProvider(queryProvider())
                .rowMapper(new OrderRowMapper())
                .pageSize(10)
                .build();
    }

    @Bean
    public Step chunkBasedStep() throws Exception {
        return stepBuilderFactory.get("chunkBasedStep")
                .<Order, Order>chunk(10)
                .reader(itemReader())
                .writer(new ItemWriter<Order>() {
                    public void write(List<? extends Order> items) throws Exception {
                        System.out.printf("Received list of size: %s", items.size());
                        items.forEach(System.out::println);
                    }
                }).build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("job").start(chunkBasedStep()).build();
    }
}
