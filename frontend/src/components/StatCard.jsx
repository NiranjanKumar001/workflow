function StatCard({ title, value, icon, color = 'blue' }) {
  const colors = {
    blue: 'bg-blue-100 text-blue-600',
    purple: 'bg-purple-100 text-purple-600',
    yellow: 'bg-yellow-100 text-yellow-600',
    green: 'bg-green-100 text-green-600',
    red: 'bg-red-100 text-red-600'
  };

  const textColors = {
    blue: 'text-blue-600',
    purple: 'text-purple-600',
    yellow: 'text-yellow-600',
    green: 'text-green-600',
    red: 'text-red-600'
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-gray-500 text-sm">{title}</p>
          <p className={`text-3xl font-bold ${textColors[color]}`}>{value}</p>
        </div>
        <div className={`${colors[color]} rounded-full p-3`}>
          {icon}
        </div>
      </div>
    </div>
  );
}

export default StatCard;