import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

// We are using standard relative paths now to avoid the "os error 2"
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import AuthCallback from './pages/AuthCallback';
import TasksPage from "./pages/Tasks.jsx";
import AdminDashboard from './pages/AdminDashboard';
import UserDetails from './pages/UserDetails';
import { authApi } from './api/authApi.js';
import UserProfile from './pages/UserProfile';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};
function AdminRoute({ children }) {
  const isAuthenticated = authApi.isAuthenticated();
  const isAdmin = authApi.isAdmin();

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (!isAdmin) {
    return <Navigate to="/dashboard" />;
  }

  return children;
}
function App() {
  return (
    <>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/auth/callback" element={<AuthCallback />} />
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/tasks" element={<ProtectedRoute><TasksPage /></ProtectedRoute>} />
          <Route path="/" element={<Navigate to="/dashboard" />} />
          <Route path="/profile" element={<ProtectedRoute><UserProfile /></ProtectedRoute>}/>

  {/* Admin Routes */}
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminDashboard />
            </AdminRoute>
          }
        />

        <Route
          path="/admin/users/:userId"
          element={
            <AdminRoute>
              <UserDetails />
            </AdminRoute>
          }
        />

        </Routes>
      </Router>
      <Toaster position="top-right" />
    </>
  );
}

export default App;