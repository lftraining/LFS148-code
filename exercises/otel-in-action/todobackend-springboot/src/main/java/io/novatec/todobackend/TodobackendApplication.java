package io.novatec.todobackend;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")

public class TodobackendApplication {

	private Logger logger = LoggerFactory.getLogger(TodobackendApplication.class);

	@Value("${CF_INSTANCE_GUID:not_set}")
	String cfInstance;

	@Value("${HOSTNAME:not_set}")
	String hostname;

	@Value("${spring.profiles.active: none}")
	String profile;

	@Autowired
	TodoRepository todoRepository;

	private String getInstanceId() {

		if (!hostname.equals("not_set"))
			return hostname;
		if (!cfInstance.equals("not_set"))
			return cfInstance;
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
	List<String> getTodos(){

		logger.info(cfInstance);

		List<String> todos = new ArrayList<String>();

		//for(Todo todo : todoRepository.findAll()) todos.add(todo.getTodo());
		todoRepository.findAll().forEach(todo -> todos.add(todo.getTodo()));
		logger.info("GET /todos/ "+todos.toString());


		return todos;
	}

	@PostMapping("/todos/{todo}")
	String addTodo(@PathVariable String todo){

		this.someInternalMethod(todo);
		//todoRepository.save(new Todo(todo));
		logger.info("POST /todos/ "+todo.toString());

		return todo;

	}

	@WithSpan
	String someInternalMethod(@SpanAttribute String todo){

		todoRepository.save(new Todo(todo));
		if(todo.equals("slow")){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(todo.equals("fail")){

			System.out.println("Failing ...");
			throw new RuntimeException();

		}
		return todo;

	}

	@DeleteMapping("/todos/{todo}")
	String removeTodo(@PathVariable String todo) {

		todoRepository.deleteById(todo);
		logger.info("DELETE /todos/ "+todo.toString());
		return "removed "+todo;

	}

	public static void main(String[] args) {
		SpringApplication.run(TodobackendApplication.class, args);
	}
}

@Entity
class Todo{

	@Id
	String todo;

	public Todo(){}

	public Todo(String todo){
		this.todo = todo;
	}

	public String getTodo(){
		return todo;
	}

	public void setTodo(String todo) {
		this.todo = todo;
	}

}

interface TodoRepository extends CrudRepository<Todo, String> {

}