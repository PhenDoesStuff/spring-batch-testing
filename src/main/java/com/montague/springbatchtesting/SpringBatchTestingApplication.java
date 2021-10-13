package com.montague.springbatchtesting;

import com.montague.springbatchtesting.deciders.DeliveryDecider;
import com.montague.springbatchtesting.deciders.ThankDecider;
import com.montague.springbatchtesting.listeners.FlowersSelectionStepExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchTestingApplication {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchTestingApplication.class, args);
    }

    @Bean
    public JobExecutionDecider deliveryDecider() {
        return new DeliveryDecider();
    }

    @Bean
    public JobExecutionDecider thankDecider() {
        return new ThankDecider();
    }

    @Bean
    public Flow deliveryFlow() {
        return new FlowBuilder<SimpleFlow>("deliveryFlow").start(driveToAddressStep())
                .on("FAILED").fail()
                .from(driveToAddressStep())
                .on("*").to(deliveryDecider())
                .on("PRESENT").to(thankCustomerStep())
                .next(thankDecider()).on("THANK CUSTOMER").to(thankCustomerStep())
                .from(thankDecider()).on("REFUND CUSTOMER").to(refundCustomerStep())
                .from(deliveryDecider())
                .on("NOT PRESENT").to(leaveAtDoorStep()).build();
    }

    @Bean
    public StepExecutionListener selectFlowerListener() {
        return new FlowersSelectionStepExecutionListener();
    }

    @Bean
    public Step packageItemStep() {
        return stepBuilderFactory.get("packageItemStep").tasklet((stepContribution, chunkContext) -> {
            String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
            String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();
            String count = chunkContext.getStepContext().getJobParameters().get("count").toString();
            System.out.printf("The %s item has been packaged on %s - %s%n", item, date, count);
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step storePackageStep() {
        return stepBuilderFactory.get("storePackageStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Storing the package while the customer address is located.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step driveToAddressStep() {
        boolean GOT_LOST = false;
        return stepBuilderFactory.get("driveToAddressStep").tasklet((stepContribution, chunkContext) -> {
            if (GOT_LOST) {
                throw new RuntimeException("Got lost driving to the address.");
            }
            System.out.println("Successfully arrived at the address.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return stepBuilderFactory.get("givePackageToCustomerStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Given the package to the customer");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return stepBuilderFactory.get("leaveAtDoorStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Leaving the package at the door.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step selectFlowersStep() {
        return this.stepBuilderFactory.get("selectFlowersStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Gathering flowers for order.");
            return RepeatStatus.FINISHED;
        }).listener(selectFlowerListener()).build();
    }

    @Bean
    public Step removeThornsStep() {
        return this.stepBuilderFactory.get("removeThornsStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Remove thorns from roses.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step arrangeFlowersStep() {
        return this.stepBuilderFactory.get("arrangeFlowersStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Arranging flowers for order.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step thankCustomerStep() {
        return stepBuilderFactory.get("thankCustomerStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Thank you for your order");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step refundCustomerStep() {
        return stepBuilderFactory.get("refundCustomerStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("So sorry - I will process your refund as soon as possible!");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step sendInvoiceStep() {
        return stepBuilderFactory.get("sendInvoiceStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Invoice is sent to the customer");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Job deliverPackageJob() {
        return jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
                .on("*").to(deliveryFlow())
                .end()
                .build();
    }

    @Bean
    public Job prepareFlowers() {
        return this.jobBuilderFactory.get("prepareFlowersJob")
                .start(selectFlowersStep())
                .on("TRIM REQUIRED").to(removeThornsStep()).next(arrangeFlowersStep())
                .from(selectFlowersStep())
                .on("NO TRIM REQUIRED").to(arrangeFlowersStep())
                .from(arrangeFlowersStep()).on("*").to(deliveryFlow())
                .end()
                .build();
    }

    @Bean
    public Job billingJob() {
        return jobBuilderFactory.get("billingJob").start(sendInvoiceStep()).build();
    }
}
