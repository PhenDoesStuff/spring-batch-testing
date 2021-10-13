package com.montague.springbatchtesting.deciders;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class ThankDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String result = 1 + Math.random() * 10 < 7 ? "THANK CUSTOMER" : "REFUND CUSTOMER";
        System.out.println("Thank Decider result is: " + result);
        return new FlowExecutionStatus(result);
    }
}
