package io.bootique.jersey.client.instrumented.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.bootique.jersey.client.instrumented.ClientTimingFilter;
import io.bootique.jersey.client.instrumented.threshold.RangeHealthCheck;
import io.bootique.jersey.client.instrumented.threshold.ThresholdHealthCheckFactory;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.metrics.health.check.DoubleRangeFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RangeHealthCheckTest {

    private MetricRegistry registry;
    private ThresholdHealthCheckFactory healthCheckFactory;
    private HealthCheckRegistry healthCheckRegistry;

    @Before
    public void before() {
        this.registry = Mockito.mock(MetricRegistry.class);
        this.healthCheckFactory = new ThresholdHealthCheckFactory();
        DoubleRangeFactory timeRequestsThresholds = new DoubleRangeFactory();
        timeRequestsThresholds.setCritical(0.05);
        timeRequestsThresholds.setWarning(0.01);

        healthCheckFactory.setTimeRequestsThresholds(timeRequestsThresholds);
        healthCheckFactory.initMetricRegistry(registry);


        Mockito.when(registry.timer(ClientTimingFilter.TIMER_NAME)).thenReturn(Mockito.mock(Timer.class));

        RangeHealthCheck rangeHealthCheck = healthCheckFactory.createThresholdHealthCheck();
        Map healthChecks = new HashMap();
        healthChecks.put("test", rangeHealthCheck);
        this.healthCheckRegistry = new HealthCheckRegistry(healthChecks);
    }

    @Test
    public void testRange_1() {
        Timer timer = registry.timer(ClientTimingFilter.TIMER_NAME);
        
        Mockito.when(timer.getOneMinuteRate()).thenReturn(0.009);
        HealthCheckOutcome outcome = healthCheckRegistry.runHealthCheck("test");

        assertEquals(HealthCheckOutcome.ok().getStatus(), outcome.getStatus());
    }


    @Test
    public void testRange_2() {
        Timer timer = registry.timer(ClientTimingFilter.TIMER_NAME);

        Mockito.when(timer.getOneMinuteRate()).thenReturn(0.03);
        HealthCheckOutcome range = healthCheckRegistry.runHealthCheck("test");

        assertEquals(HealthCheckOutcome.warning().getStatus(), range.getStatus());
    }

    @Test
    public void testRange_3() {
        Timer timer = registry.timer(ClientTimingFilter.TIMER_NAME);

        Mockito.when(timer.getOneMinuteRate()).thenReturn(0.06);
        HealthCheckOutcome range = healthCheckRegistry.runHealthCheck("test");

        assertEquals(HealthCheckOutcome.critical().getStatus(), range.getStatus());
    }

}
