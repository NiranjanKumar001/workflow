import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { taskApi } from '../api/taskApi';
import { categoryApi } from '../api/categoryApi';
import TaskCard from '../components/TaskCard';
import TaskModal from '../components/TaskModal';
import toast from 'react-hot-toast';

function Tasks() {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedTask, setSelectedTask] = useState(null);
  const [newTask, setNewTask] = useState({ title: '', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: '', categoryIds: [] });

  useEffect(() => {
    loadCategories();
    loadTasks();
  }, [filter, selectedCategories]);

  const loadCategories = async () => {
    try {
      const res = await categoryApi.getAllCategories();
      if (res.success) setCategories(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const loadTasks = async () => {
    try {
      setLoading(true);
      let res;
      if (selectedCategories.length > 0) {
        res = await taskApi.filterTasks({ categoryId: selectedCategories[0], page: 0, size: 100, sortBy: 'createdAt', sortDirection: 'DESC' });
        if (res.success) setTasks(res.data.content ?? []);
      } else if (filter === 'ALL') {
        res = await taskApi.getAllTasks();
        if (res.success) setTasks(res.data);
      } else if (filter === 'OVERDUE') {
        res = await taskApi.getOverdueTasks();
        if (res.success) setTasks(res.data);
      } else {
        res = await taskApi.getTasksByStatus(filter);
        if (res.success) setTasks(res.data);
      }
    } catch (err) {
      toast.error('Failed to load tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) return loadTasks();
    try {
      setLoading(true);
      const res = await taskApi.filterTasks({
        searchQuery: searchQuery.trim(),
        page: 0,
        size: 100,
        sortBy: 'createdAt',
        sortDirection: 'DESC'
      });
      if (res.success) {
        const taskList = res.data.content ?? [];
        setTasks(taskList);
        toast.success(`Found ${taskList.length} task(s)`);
      }
    } catch (err) {
      toast.error('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!newTask.title.trim()) return toast.error('Title required');
    try {
      const res = await taskApi.createTask(newTask);
      if (res.success) {
        toast.success('Task created!');
        setShowCreateModal(false);
        setNewTask({ title: '', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: '', categoryIds: [] });
        loadTasks();
      }
    } catch (err) {
      toast.error(err.message || 'Failed to create');
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const res = await taskApi.updateTask(selectedTask.id, selectedTask);
      if (res.success) {
        toast.success('Task updated!');
        setShowEditModal(false);
        setSelectedTask(null);
        loadTasks();
      }
    } catch (err) {
      toast.error(err.message || 'Update failed');
    }
  };

  const handleUpdateStatus = async (id, status) => {
    try {
      const res = await taskApi.updateTaskStatus(id, status);
      if (res.success) {
        toast.success('Status updated!');
        loadTasks();
      }
    } catch (err) {
      toast.error(err.message || 'Failed');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this task?')) return;
    try {
      const res = await taskApi.deleteTask(id);
      if (res.success) {
        toast.success('Deleted!');
        loadTasks();
      }
    } catch (err) {
      toast.error('Delete failed');
    }
  };

  const handleEdit = (task) => {
    setSelectedTask({ ...task, categoryIds: task.categories?.map(c => c.id) || [] });
    setShowEditModal(true);
  };

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600"></div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Tasks</h1>
            <p className="text-gray-600">Manage efficiently</p>
          </div>
          <div className="flex gap-3">
            <button onClick={() => navigate('/categories')} className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700">Categories</button>
            <button onClick={() => navigate('/dashboard')} className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700">Dashboard</button>
            <button onClick={() => setShowCreateModal(true)} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Create</button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Search */}
        <div className="mb-6 flex gap-3">
          <input type="text" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} onKeyPress={(e) => e.key === 'Enter' && handleSearch()} placeholder="Search..." className="flex-1 px-4 py-2 border rounded-lg" />
          <button onClick={handleSearch} className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">Search</button>
          {searchQuery && <button onClick={() => { setSearchQuery(''); loadTasks(); }} className="px-6 py-2 bg-gray-200 rounded-lg">Clear</button>}
        </div>

        {/* Category Filter */}
        {categories.length > 0 && (
          <div className="mb-6">
            <h3 className="text-sm font-medium mb-3">Filter by Category:</h3>
            <div className="flex flex-wrap gap-2">
              {categories.map(cat => (
                <button key={cat.id} onClick={() => setSelectedCategories(p => p.includes(cat.id) ? p.filter(i => i !== cat.id) : [...p, cat.id])} className={`px-4 py-2 rounded-full text-sm font-medium ${selectedCategories.includes(cat.id) ? 'text-white' : 'bg-white'}`} style={{ backgroundColor: selectedCategories.includes(cat.id) ? cat.color : undefined }}>
                  {cat.name}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Status Filter */}
        <div className="mb-6 flex gap-3 overflow-x-auto">
          {['ALL', 'TODO', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'].map(f => (
            <button key={f} onClick={() => { setFilter(f); setSelectedCategories([]); }} className={`px-4 py-2 rounded-lg font-medium whitespace-nowrap ${filter === f ? 'bg-blue-600 text-white' : 'bg-white'}`}>
              {f.replace('_', ' ')}
            </button>
          ))}
        </div>

        {/* Tasks */}
        {tasks.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <h3 className="text-xl font-semibold mb-2">No tasks</h3>
            <p className="text-gray-600 mb-4">{searchQuery ? 'Try different search' : 'Create your first task!'}</p>
            {!searchQuery && <button onClick={() => setShowCreateModal(true)} className="px-6 py-3 bg-blue-600 text-white rounded-lg">Create</button>}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {tasks.map(task => <TaskCard key={task.id} task={task} onEdit={handleEdit} onDelete={handleDelete} onUpdateStatus={handleUpdateStatus} />)}
          </div>
        )}
      </main>

      <TaskModal isOpen={showCreateModal} onClose={() => setShowCreateModal(false)} onSubmit={handleCreate} task={newTask} setTask={setNewTask} categories={categories} isEdit={false} />
      <TaskModal isOpen={showEditModal} onClose={() => { setShowEditModal(false); setSelectedTask(null); }} onSubmit={handleUpdate} task={selectedTask || {}} setTask={setSelectedTask} categories={categories} isEdit={true} />
    </div>
  );
}
export default Tasks;