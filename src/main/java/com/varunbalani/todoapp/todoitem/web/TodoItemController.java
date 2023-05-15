package com.varunbalani.todoapp.todoitem.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.varunbalani.todoapp.todoitem.TodoItem;
import com.varunbalani.todoapp.todoitem.TodoItemNotFoundException;
import com.varunbalani.todoapp.todoitem.TodoItemRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class TodoItemController {

	private final TodoItemRepository repository;

	public enum ListFilter {
		ALL, ACTIVE, COMPLETED
	}

	public TodoItemController(TodoItemRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public String index(Model model) {
		addAttributesForIndex(model, ListFilter.ALL);
		return "index";
	}

	@GetMapping("/active")
	public String indexActive(Model model) {
		addAttributesForIndex(model, ListFilter.ACTIVE);
		return "index";
	}

	@GetMapping("/completed")
	public String indexCompleted(Model model) {
		addAttributesForIndex(model, ListFilter.COMPLETED);
		return "index";
	}

	private void addAttributesForIndex(Model model, ListFilter listFilter) {
		model.addAttribute("item", new TodoItemFormData());
		model.addAttribute("filter", listFilter);
		model.addAttribute("todos", getTodoItems(listFilter));
		model.addAttribute("totalNumberOfItems", repository.count());
		model.addAttribute("numberOfActiveItems", getNumberOfActiveItems());
		model.addAttribute("numberOfCompletedItems", getNumberOfCompletedItems());
	}

	private List<TodoItemDTO> getTodoItems(ListFilter filter) {
		return switch (filter) {
		case ALL -> convertToDTO(repository.findAll());
		case ACTIVE -> convertToDTO(repository.findAllByCompleted(false));
		case COMPLETED -> convertToDTO(repository.findAllByCompleted(true));
		};
	}

	private int getNumberOfActiveItems() {
		return repository.countAllByCompleted(false);
	}

	private int getNumberOfCompletedItems() {
		return repository.countAllByCompleted(true);
	}

	private List<TodoItemDTO> convertToDTO(List<TodoItem> todoItems) {
		return todoItems.stream()
				.map(todoItem -> new TodoItemDTO(todoItem.getId(), todoItem.getTitle(), todoItem.isCompleted()))
				.collect(Collectors.toList());
	}

	private TodoItemDTO convertToDTO(TodoItem todoItem) {
		return new TodoItemDTO(todoItem.getId(), todoItem.getTitle(), todoItem.isCompleted());
	}

	private List<TodoItemDTO> getTodoItems() {
		return repository.findAll().stream()
				.map(todoItem -> new TodoItemDTO(todoItem.getId(), todoItem.getTitle(), todoItem.isCompleted()))
				.collect(Collectors.toList());
	}

	public static record TodoItemDTO(long id, String title, boolean completed) {

	}

	@PostMapping
	public String addNewTodoItem(@Valid @ModelAttribute("item") TodoItemFormData formData) {
		repository.save(new TodoItem(formData.getTitle(), false));
		return "redirect:/";
	}

	@PostMapping(headers = "HX-Request")
	public String htmxAddTodoItem(TodoItemFormData formData, Model model, HttpServletResponse response) {
		TodoItem item = repository.save(new TodoItem(formData.getTitle(), false));
		model.addAttribute("item", convertToDTO(item));
		response.setHeader("HX-Trigger", "itemAdded");
		return "todoitem-fragment :: todoItem";
	}

	@GetMapping(value = "/active-items-count", headers = "HX-Request")
	public String htmxGetActiveItemsCount(Model model, HttpServletResponse response) {
		model.addAttribute("numberOfActiveItems", getNumberOfActiveItems());
		return "active-item-count-fragment :: active-items-count";
	}

	@PutMapping(value = "/{id}/toggle", headers = "HX-Request")
	public String htmxToggleTodoItem(@PathVariable("id") Long id, Model model, HttpServletResponse response) {
		TodoItem todoItem = repository.findById(id).orElseThrow(() -> new TodoItemNotFoundException(id));
		todoItem.setCompleted(!todoItem.isCompleted());
		repository.save(todoItem);

		model.addAttribute("item", convertToDTO(todoItem));
		response.setHeader("HX-Trigger", "itemCompletionToggled");
		return "todoitem-fragment :: todoItem";
	}

	@DeleteMapping(value = "/{id}", headers = "HX-Request")
	@ResponseBody
	public String htmxDeleteTodoItem(@PathVariable("id") Long id, HttpServletResponse response) {
		repository.deleteById(id);
		response.setHeader("HX-Trigger", "itemDeleted");
		return "";
	}

	@PutMapping("/{id}/toggle")
	public String toggleSelection(@PathVariable("id") Long id) {
		TodoItem todoItem = repository.findById(id).orElseThrow(() -> new TodoItemNotFoundException(id));
		todoItem.setCompleted(!todoItem.isCompleted());
		repository.save(todoItem);
		return "redirect:/";
	}

	@DeleteMapping("/{id}")
	public String deleteTodoItem(@PathVariable("id") Long id) {
		repository.deleteById(id);

		return "redirect:/";
	}

	@DeleteMapping("/completed")
	public String deleteCompletedItems() {
		List<TodoItem> items = repository.findAllByCompleted(true);
		for (TodoItem item : items) {
			repository.deleteById(item.getId());
		}
		return "redirect:/";
	}

	@PutMapping("/toggle-all")
	public String toggleAll() {
		List<TodoItem> todoItems = repository.findAll();
		for (TodoItem todoItem : todoItems) {
			todoItem.setCompleted(!todoItem.isCompleted());
			repository.save(todoItem);
		}
		return "redirect:/";
	}
}
