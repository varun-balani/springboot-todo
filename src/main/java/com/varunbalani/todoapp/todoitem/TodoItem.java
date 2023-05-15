package com.varunbalani.todoapp.todoitem;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class TodoItem {

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	private String title;

	private boolean completed;

	public TodoItem(String title, boolean completed) {
		this.title = title;
		this.completed = completed;
	}

	// Default constructor for hibernate
	public TodoItem() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
