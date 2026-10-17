import { createBrowserRouter, Navigate } from 'react-router-dom'
import Layout from '../components/Layout'
import RequireAuth from '../components/RequireAuth'
import HomePage from '../pages/HomePage'
import DramaDetailPage from '../pages/DramaDetailPage'
import PlayerPage from '../pages/PlayerPage'
import LoginPage from '../pages/LoginPage'
import RegisterPage from '../pages/RegisterPage'
import MinePage from '../pages/MinePage'
import FavoritesPage from '../pages/FavoritesPage'
import HistoryPage from '../pages/HistoryPage'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/drama/:id', element: <DramaDetailPage /> },
      { path: '/drama/:id/play', element: <PlayerPage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      {
        path: '/mine',
        element: (
          <RequireAuth>
            <MinePage />
          </RequireAuth>
        ),
      },
      {
        path: '/mine/history',
        element: (
          <RequireAuth>
            <HistoryPage />
          </RequireAuth>
        ),
      },
      {
        path: '/mine/favorites',
        element: (
          <RequireAuth>
            <FavoritesPage />
          </RequireAuth>
        ),
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
