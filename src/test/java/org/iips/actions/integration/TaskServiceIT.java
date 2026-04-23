package org.iips.actions.integration;

import org.iips.actions.exception.InvalidTaskException;
import org.iips.actions.exception.TaskNotFoundException;
import org.iips.actions.model.Task;
import org.iips.actions.repository.InMemoryTaskRepository;
import org.iips.actions.repository.TaskRepository;
import org.iips.actions.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskService Integration Tests (Top-Down, Mockito)")
class TaskServiceIT {
  TaskRepository repository;

  TaskService service;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTaskRepository();
    service = new TaskService(repository);
  }

  @Nested
  @DisplayName("Create Task")
  class CreateTask {
    @Test
    @DisplayName("Should delegate to repository and return saved task")
    void shouldDelegateCreateTask() {
      LocalDate dueDate = LocalDate.now();
      Task result = service.createTask("Integration", dueDate);

      assertEquals("Integration", result.description());
      assertFalse(result.completed());
      assertEquals(dueDate, result.dueDate());
      assertEquals(result, repository.findById(result.id()).orElseThrow());
    }

    @Test
    @DisplayName("Should throw InvalidTaskException for blank description")
    void shouldThrowForBlankDescription() {
      assertThrows(InvalidTaskException.class, () -> service.createTask(" ", LocalDate.now()));
      assertTrue(repository.findAll().isEmpty());
    }
  }

  @Nested
  @DisplayName("Complete Task")
  class CompleteTask {
    @Test
    @DisplayName("Should complete task if found")
    void shouldCompleteTaskIfFound() {
      UUID id = UUID.randomUUID();
      Task existing = new Task(id, "Complete", false, null);
      Task completed = new Task(id, "Complete", true, null);

      repository.save(existing);
      Task result = service.completeTask(id);

      assertTrue(result.completed());
      assertEquals(completed, result);
      assertEquals(completed, repository.findById(id).orElseThrow());
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException if not found")
    void shouldThrowIfNotFound() {
      UUID id = UUID.randomUUID();
      assertThrows(TaskNotFoundException.class, () -> service.completeTask(id));
      assertTrue(repository.findById(id).isEmpty());
    }
  }

  @Nested
  @DisplayName("Delete Task")
  class DeleteTask {
    @Test
    @DisplayName("Should delete task if found")
    void shouldDeleteTaskIfFound() {
      UUID id = UUID.randomUUID();

      repository.save(new Task(id, "Delete", false, null));
      assertDoesNotThrow(() -> service.deleteTask(id));
      assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException if not found")
    void shouldThrowIfNotFound() {
      UUID id = UUID.randomUUID();
      assertThrows(TaskNotFoundException.class, () -> service.deleteTask(id));
      assertTrue(repository.findById(id).isEmpty());
    }
  }

  @Nested
  @DisplayName("Get Task")
  class GetTask {
    @Test
    @DisplayName("Should get task if found")
    void shouldGetTaskIfFound() {
      UUID id = UUID.randomUUID();
      Task task = new Task(id, "Get", false, null);
      repository.save(task);
      Task result = service.getTaskById(id);

      assertEquals(id, result.id());
      assertEquals(task, result);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException if not found")
    void shouldThrowIfNotFound() {
      UUID id = UUID.randomUUID();
      assertThrows(TaskNotFoundException.class, () -> service.getTaskById(id));
      assertTrue(repository.findById(id).isEmpty());
    }
  }
}
