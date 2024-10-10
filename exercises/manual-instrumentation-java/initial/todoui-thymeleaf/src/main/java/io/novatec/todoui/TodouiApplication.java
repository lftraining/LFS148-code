package io.novatec.todoui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

@SpringBootApplication
@Controller
public class TodouiApplication {

	private Logger logger = LoggerFactory.getLogger(TodouiApplication.class);

	private OpenTelemetry openTelemetry;
	private Tracer tracer;

	@Value("${backend.url}")
	String endpoint;
	RestTemplate template = new RestTemplate();

	public TodouiApplication(OpenTelemetry openTelemetry) {
		this.openTelemetry = openTelemetry;
		tracer = this.openTelemetry.getTracer(TodouiApplication.class.getName(), "0.1.0");
	}

	@PostConstruct
	public void postConstruct(){

		logger.info(" UI initialized for backend at "+endpoint);
	}

	@GetMapping("/stress")
	public String stress(){

		logger.info(java.time.LocalDateTime.now() + " : Starting stress");
		double result = 0;
		for (int i = 0; i < 100000000; i++) {
			result += System.currentTimeMillis();
		}
		logger.info(java.time.LocalDateTime.now() + " : Ending stress, result: " + result);
		return "redirect:/";

	}

	public static List<Double> list = new ArrayList<>(); // can never be GC'ed
	@GetMapping("/leak")
	public String leak(){

		logger.info(java.time.LocalDateTime.now() + " : Start leaking");
		for (int i = 0; i < 10000000; i++) {
			list.add(Math.random());
		}
		logger.info(java.time.LocalDateTime.now() + " : End leaking");
		return "redirect:/";

	}

	@GetMapping("/createcookie")
	public String createCookie(HttpServletResponse response) {

		Cookie cookie = new Cookie("featureflag", "on");
		cookie.setPath("/");
		response.addCookie(cookie);
		return "redirect:/";

	}

	@GetMapping("/deletecookie")
	public String deleteCookie(HttpServletResponse response) {

		Cookie cookie = new Cookie("featureflag", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
		return "redirect:/";

	}


	@GetMapping
	public String getItems(Model model){

		logger.info("GET "+ endpoint + "/todos/");
		ResponseEntity<String[]> response = template.getForEntity(endpoint+"/todos/", String[].class);
		if(response != null) model.addAttribute("items", response.getBody());
		return "items";

	}

	@PostMapping
	public String addItem(String toDo){

		logger.info("POST "+ endpoint + "/todos/"+toDo);

		Span span = tracer.spanBuilder("addItem").setSpanKind(SpanKind.CLIENT).startSpan();

		Context context = Context.current();

		System.out.println("Context: "+context);
		System.out.println("### Hallo");

		template.postForEntity(endpoint+"/todos/"+toDo, null, String.class);

		

		span.end();

		return "redirect:/";

	}

	@PostMapping("{toDo}")
	public String setItemDone(@PathVariable String toDo){

		logger.info("POST "+ endpoint + "/todos/"+toDo);
		template.delete(endpoint+"/todos/"+toDo);
		return "redirect:/";

	}

	public static void main(String[] args) {
		SpringApplication.run(TodouiApplication.class, args);
	}
}
