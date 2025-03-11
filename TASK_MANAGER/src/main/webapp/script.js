window.onload = fetchTasks;

document.getElementById('taskForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const title = document.getElementById('taskTitle').value;
    const desc = document.getElementById('taskDesc').value;

    try {
        const response = await fetch('/TaskManager/TaskServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'add', title: title, description: desc })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        if (data.success) {
            fetchTasks();
            this.reset();
        } else {
            console.error('Error adding task:', data.error);
        }
    } catch (err) {
        console.error('Fetch error:', err);
    }
});

async function fetchTasks() {
    try {
        const response = await fetch('/TaskManager/TaskServlet?action=get');
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const tasks = await response.json();
        const tbody = document.getElementById('taskList');
        tbody.innerHTML = '';
        tasks.forEach(task => addTaskToTable(task));
    } catch (err) {
        console.error('Error fetching tasks:', err);
    }
}

function addTaskToTable(task) {
    const tbody = document.getElementById('taskList');
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${task.id}</td>
        <td>${task.title}</td>
        <td>${task.description || ''}</td>
        <td>${task.is_completed ? 'Completed' : 'Pending'}</td>
        <td><button onclick="deleteTask(${task.id})">Delete</button></td>
    `;
    tbody.appendChild(row);
}

async function deleteTask(id) {
    try {
        const response = await fetch('/TaskManager/TaskServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', id: id })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        if (data.success) {
            fetchTasks();
        } else {
            console.error('Error deleting task:', data.error);
        }
    } catch (err) {
        console.error('Fetch error:', err);
    }
}
