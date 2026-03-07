import FileUpload from './FileUpload';
import AttachmentList from './AttachmentList';

function TaskModal({ isOpen, onClose, onSubmit, task, setTask, categories, isEdit }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50 overflow-y-auto">
      <div className={`bg-white rounded-lg shadow-xl w-full p-6 my-8 max-h-[90vh] overflow-y-auto ${isEdit ? 'max-w-2xl' : 'max-w-md'}`}>
        <h2 className="text-2xl font-bold text-gray-900 mb-4">{isEdit ? 'Edit Task' : 'Create New Task'}</h2>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Title *</label>
            <input type="text" value={task.title} onChange={(e) => setTask({ ...task, title: e.target.value })} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" required />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
            <textarea value={task.description || ''} onChange={(e) => setTask({ ...task, description: e.target.value })} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" rows="3" />
          </div>

          <div className="grid grid-cols-2 gap-4">
            {isEdit && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Status</label>
                <select value={task.status} onChange={(e) => setTask({ ...task, status: e.target.value })} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="ARCHIVED">Archived</option>
                </select>
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Priority</label>
              <select value={task.priority} onChange={(e) => setTask({ ...task, priority: e.target.value })} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Due Date</label>
              <input type="date" value={task.dueDate || ''} onChange={(e) => setTask({ ...task, dueDate: e.target.value })} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" />
            </div>
          </div>

          {categories?.length > 0 && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Categories</label>
              <div className="flex flex-wrap gap-2">
                {categories.map((cat) => (
                  <button
                    key={cat.id}
                    type="button"
                    onClick={() => {
                      const ids = task.categoryIds || [];
                      setTask({ ...task, categoryIds: ids.includes(cat.id) ? ids.filter(id => id !== cat.id) : [...ids, cat.id] });
                    }}
                    className={`px-3 py-1 rounded-full text-xs font-semibold transition ${(task.categoryIds || []).includes(cat.id) ? 'text-white' : 'bg-gray-100 text-gray-700'}`}
                    style={{ backgroundColor: (task.categoryIds || []).includes(cat.id) ? cat.color : undefined }}
                  >
                    {cat.name}
                  </button>
                ))}
              </div>
            </div>
          )}

          {isEdit && task.id && (
            <>
              <FileUpload taskId={task.id} onUploadSuccess={() => window.location.reload()} />
              <AttachmentList attachments={task.attachments} onDelete={() => window.location.reload()} />
            </>
          )}

          <div className="flex gap-3 pt-4">
            <button type="submit" className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
              {isEdit ? 'Update' : 'Create'}
            </button>
            <button type="button" onClick={onClose} className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default TaskModal;