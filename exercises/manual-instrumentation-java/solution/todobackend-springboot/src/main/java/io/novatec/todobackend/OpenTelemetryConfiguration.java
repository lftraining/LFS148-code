package io.novatec.todobackend;

import java.time.Duration;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

//Basic Otel API & SDK
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
//Tracing and Spans
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

@SuppressWarnings("deprecation")
@Configuration
public class OpenTelemetryConfiguration {

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public OpenTelemetry openTelemetry() {

		Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "todobackend")
				.put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

		OtlpGrpcSpanExporter jaegerOtlpExporter = OtlpGrpcSpanExporter.builder()
				.setEndpoint("http://localhost:4317")
				.build();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
				.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
				.addSpanProcessor(SimpleSpanProcessor.create(jaegerOtlpExporter))
				.setResource(resource)
				.build();

		SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
				.registerMetricReader(
					PeriodicMetricReader
						.builder(LoggingMetricExporter.create())
						.setInterval(Duration.ofSeconds(10))
						.build())
				.setResource(resource)
				.build();


		OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
				.setTracerProvider(sdkTracerProvider)
				.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				.setMeterProvider(sdkMeterProvider)
				.build();

		return openTelemetry;
	}

}
