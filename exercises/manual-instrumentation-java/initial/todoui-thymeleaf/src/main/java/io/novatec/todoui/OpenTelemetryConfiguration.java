package io.novatec.todoui;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

//Basic Otel API & SDK
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;

//Tracing and Spans
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

import  io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;


@SuppressWarnings("deprecation")
@Configuration
public class OpenTelemetryConfiguration {

    @Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public OpenTelemetry openTelemetry(){

		Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "tododui").put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

		OtlpGrpcSpanExporter jaegerOtlpExporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317")
            .setTimeout(30, TimeUnit.SECONDS)
            .build();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
			.addSpanProcessor(SimpleSpanProcessor.create(jaegerOtlpExporter))
    //      .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build()) // same results for now
			.setResource(resource)
			.build();	
	//		.buildAndRegisterGlobal();	


		OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
			.setTracerProvider(sdkTracerProvider)
			.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
			.build();

		return openTelemetry;
	}

}
