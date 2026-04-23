package org.iips.actions.integration;

import org.iips.actions.exception.InvalidTaskException;
import org.iips.actions.exception.TaskNotFoundException;
import org.iips.actions.model.Task;
import org.iips.actions.repository.TaskRepository;
import org.iips.actions.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TaskService Integration Tests (Top-Down, Mockito)")
@ExtendWith(MockitoExtension.class)
class TaskServiceIT {
  @Mock TaskRepository repository;

  @InjectMocks TaskService service;

  @Nested
  @DisplayName("Create Task")
  class CreateTask {
    @Test
    @DisplayName("Should delegate to repository and return saved task")
    void shouldDelegateCreateTask() {
      Task mockTask = new Task(UUID.randomUUID(), "Integration", false, LocalDate.now());
      when(repository.save(any(Task.class))).thenReturn(mockTask);
      Task result = service.createTask("Integration", LocalDate.now());
      assertEquals(mockTask.description(), result.description());
      verify(repository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw InvalidTaskException for blank description")
    void shouldThrowForBlankDescription() {
      assertThrows(InvalidTaskException.class, () -> service.createTask(" ", LocalDate.now()));
      verify(repository, never()).save(any());
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
      when(repository.findById(id)).thenReturn(Optional.of(existing));
      when(repository.save(any(Task.class))).thenReturn(completed);
      Task result = service.completeTask(id);
      assertTrue(result.completed());
      verify(repository).findById(id);
      verify(repository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException if not found")
    void shouldThrowIfNotFound() {
      UUID id = UUID.randomUUID();
      when(repository.findById(id)).thenReturn(Optional.empty());
      assertThrows(TaskNotFoundException.class, () -> service.completeTask(id));
      verify(repository).findById(id);
      verify(repository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Delete Task")
  class DeleteTask {
    @Test
    @DisplayName("Should delete task if found")
    void shouldDeleteTaskIfFound() {
      UUID id = UUID.randomUUID();
      when(repository.deleteById(id)).thenReturn(true);
      assertDoesNotThrow(() -> service.deleteTask(id));
      verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException if not found")
    void shouldThrowIfNotFound() {
      UUID id = UUID.randomUUID();
      when(repository.deleteById(id)).thenReturn(false);
      assertThrows(TaskNotFoundException.class, () -> service.deleteTask(id));
      verify(repository).deleteById(id);
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
      when(repository.findById(id)).thenReturn(Optional.of(task));
      Task result = service.getTaskById(id);
      assertEquals(id, result.id());
      verify(repository).findById(id);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException if not found")
    void shouldThrowIfNotFound() {
      UUID id = UUID.randomUUID();
      when(repository.findById(id)).thenReturn(Optional.empty());
      assertThrows(TaskNotFoundException.class, () -> service.getTaskById(id));
      verify(repository).findById(id);
    }
  }
}
