package com.anirudhpundhir.webhooks.config;

import jakarta.validation.constraints.Min;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Polling settings for the outbox worker.
 */
@Validated
@ConfigurationProperties(prefix = "webhooks")
public record WebhookProperties(
        @DefaultValue("true") boolean pollerEnabled,
        @DefaultValue("1s") Duration pollInterval,
        @DefaultValue("100") @Min(1) int pollBatchSize) {}
