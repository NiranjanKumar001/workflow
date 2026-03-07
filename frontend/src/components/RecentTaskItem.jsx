function RecentTaskItem({ task, onClick }) {
  const getStatusStyle = (status) => {
    const styles = {
      TODO: 'bg-blue-100 text-blue-800',
      IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
      COMPLETED: 'bg-green-100 text-green-800',
      ARCHIVED: 'bg-gray-100 text-gray-800',
    };
    return styles[status] || 'bg-gray-100 text-gray-800';
  };

  const getPriorityStyle = (priority) => {
    const styles = {
      URGENT: 'bg-red-100 text-red-800',
      HIGH: 'bg-red-100 text-red-800',
      MEDIUM: 'bg-orange-100 text-orange-800',
      LOW: 'bg-gray-100 text-gray-800',
    };
    return styles[priority] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div
      className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition cursor-pointer"
      onClick={onClick}
    >
      <div className="flex justify-between items-start">
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900">{task.title}</h3>
          {task.description && (
            <p className="text-sm text-gray-600 mt-1 line-clamp-1">{task.description}</p>
          )}
          {task.dueDate && (
            <p className="text-xs text-gray-400 mt-1">
              Due: {new Date(task.dueDate).toLocaleDateString()}
            </p>
          )}
        </div>
        <div className="flex gap-2 ml-4 flex-shrink-0">
          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusStyle(task.status)}`}>
            {task.status.replace('_', ' ')}
          </span>
          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getPriorityStyle(task.priority)}`}>
            {task.priority}
          </span>
          {task.overdue && (
            <span className="px-3 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800">
              OVERDUE
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export default RecentTaskItem;