import { createBrowserRouter, Navigate } from 'react-router-dom'
import RequireAdmin from '../components/RequireAdmin'
import LoginPage from '../pages/LoginPage'
import DramasPage from '../pages/DramasPage'
import DramaFormPage from '../pages/DramaFormPage'
import EpisodesPage from '../pages/EpisodesPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: (
      <RequireAdmin>
        <DramasPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/dramas/new',
    element: (
      <RequireAdmin>
        <DramaFormPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/dramas/:id/edit',
    element: (
      <RequireAdmin>
        <DramaFormPage />
      </RequireAdmin>
    ),
  },
  {
    path: '/dramas/:id/episodes',
    element: (
      <RequireAdmin>
        <EpisodesPage />
      </RequireAdmin>
    ),
  },
  { path: '*', element: <Navigate to="/" replace /> },
])
