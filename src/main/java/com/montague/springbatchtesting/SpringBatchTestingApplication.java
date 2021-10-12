package com.montague.springbatchtesting;

import com.montague.springbatchtesting.deliverydecider.DeliveryDecider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
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

	@Bean
	public JobExecutionDecider decider() {
		return new DeliveryDecider();
	}

	@Bean
	public Step leaveAtDoorStep() {
		return stepBuilderFactory.get("leaveAtDoorStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Leaving the package at the door.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step storePackageStep() {
		return stepBuilderFactory.get("storePackageStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Storing the package while the customer address is located.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step givePackageToCustomerStep() {
		return stepBuilderFactory.get("givePackageToCustomerStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Given the package to the customer");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step driveToAddressStep() {
		boolean GOT_LOST = false;
		return stepBuilderFactory.get("driveToAddressStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
				if (GOT_LOST) {
					throw new RuntimeException("Got lost driving to the address.");
				}
				System.out.println("Successfully arrived at the address.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step packageItemStep() {
		return stepBuilderFactory.get("packageItemStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
				String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
				String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();
				String count = chunkContext.getStepContext().getJobParameters().get("count").toString();
				System.out.println(String.format("The %s item has been packaged on %s - %s", item, date, count));
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Job deliverPackageJob() {
		return jobBuilderFactory.get("deliverPackageJob").start(packageItemStep()).next(driveToAddressStep()).
				on("FAILED").to(storePackageStep()).from(driveToAddressStep()).on("*")
				.to(decider()).on("PRESENT").to(givePackageToCustomerStep()).from(decider())
				.on("NOT PRESENT").to(leaveAtDoorStep()).end().build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchTestingApplication.class, args);
	}

}
