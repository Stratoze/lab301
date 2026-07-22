import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import routeConfig from './routes/routeConfig';

function App() {
  return (
    <Router>
      <Routes>
        {routeConfig.map(({ path, element, protected: isProtected, adminOnly }) => (
          <Route
            key={path}
            path={path}
            element={
              isProtected
                ? <ProtectedRoute adminOnly={adminOnly}>{element}</ProtectedRoute>
                : element
            }
          />
        ))}
        <Route path="/" element={<Navigate to="/lottery" />} />
      </Routes>
    </Router>
  );
}

export default App;
