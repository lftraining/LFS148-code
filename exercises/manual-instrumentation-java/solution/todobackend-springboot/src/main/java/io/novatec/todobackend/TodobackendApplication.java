package io.novatec.todobackend;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")

public class TodobackendApplication {

	private Logger logger = LoggerFactory.getLogger(TodobackendApplication.class);

	private OpenTelemetry openTelemetry;
	private Tracer tracer;
	private Meter meter;
	private LongCounter counter;

	@Value("${HOSTNAME:not_set}")
	String hostname;

	@Value("${spring.profiles.active: none}")
	String profile;

	@Autowired
	TodoRepository todoRepository;

	public TodobackendApplication(OpenTelemetry openTelemetry) {
		this.openTelemetry = openTelemetry;
		tracer = this.openTelemetry.getTracer(TodobackendApplication.class.getName(), "0.1.0");
		meter = this.openTelemetry.getMeter(TodobackendApplication.class.getName());

		counter = meter.counterBuilder("todobackend.requests.counter")
				.setDescription("How many times the GET call has been invoked.")
				.setUnit("requests")
				.build();

	}

	private String getInstanceId() {

		if (!hostname.equals("not_set"))
			return hostname;
		return "probably localhost";

	}

	@GetMapping("/hello")
	String hello() {

		return getInstanceId() + " Hallo, Welt ! ";

	}

	@GetMapping("/fail")
	String fail() {

		System.exit(1);
		return "fixed!";
	}

	@GetMapping("/todos/")
	List<String> getTodos() {

		List<String> todos = new ArrayList<String>();

		todoRepository.findAll().forEach(todo -> todos.add(todo.getTodo()));
		logger.info("GET /todos/ " + todos.toString());
		counter.add(1,Attributes.of(stringKey("http.method"), "GET"));

		return todos;
	}

	@PostMapping("/todos/{todo}")
	String addTodo(HttpServletRequest request, HttpServletResponse response, @PathVariable String todo) {

		logger.info("POST /todos/ " + todo.toString());

		Span span = tracer.spanBuilder("addTodo").setSpanKind(SpanKind.SERVER).startSpan();

		span.setAttribute("http.method", request.getMethod());
		span.setAttribute("http.url", request.getRequestURL().toString());
		span.setAttribute("client.address", request.getRemoteAddr());
		span.setAttribute("user.agent", request.getHeader("User-Agent"));

		try (Scope scope = span.makeCurrent()) {
			this.someInternalMethod(todo);
			response.setStatus(HttpServletResponse.SC_CREATED);
			span.setAttribute("response.status", HttpServletResponse.SC_CREATED);
		} catch (Throwable t) {
			span.setStatus(StatusCode.ERROR, "Error on server side!");
			span.recordException(t);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			span.setAttribute("response.status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			span.end();
		}

		logger.info("Span.toString():" + span.toString());
		return todo;

	}

	String someInternalMethod(String todo) {

		Span childSpan = tracer.spanBuilder("someInternalMethod").setSpanKind(SpanKind.INTERNAL).startSpan();

		todoRepository.save(new Todo(todo));

		if (todo.equals("slow")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (todo.equals("fail")) {

			System.out.println("Failing ...");
			throw new RuntimeException();

		}

		logger.info("childSpan.toString():" + childSpan.toString());
		childSpan.end();
		return todo;

	}

	@DeleteMapping("/todos/{todo}")
	String removeTodo(@PathVariable String todo) {

		todoRepository.deleteById(todo);
		logger.info("DELETE /todos/ " + todo.toString());
		return "removed " + todo;

	}

	public static void main(String[] args) {
		SpringApplication.run(TodobackendApplication.class, args);
	}

}

@Entity
class Todo {

	@Id
	String todo;

	public Todo() {
	}

	public Todo(String todo) {
		this.todo = todo;
	}

	public String getTodo() {
		return todo;
	}

	public void setTodo(String todo) {
		this.todo = todo;
	}

}

interface TodoRepository extends CrudRepository<Todo, String> {

}